package fr.redstonneur1256.ui.dialogs;

import arc.Core;
import arc.files.Fi;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.scene.style.TextureRegionDrawable;
import arc.struct.IntMap;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Strings;
import arc.util.Time;
import fr.redstonneur1256.RedMod;
import fr.redstonneur1256.processing.image.AsyncImageSimplifier;
import fr.redstonneur1256.processing.image.ImageSimplifier;
import fr.redstonneur1256.redutilities.graphics.ImageHelper;
import fr.redstonneur1256.ui.RPal;
import fr.redstonneur1256.ui.base.BasicImageDialog;
import fr.redstonneur1256.utils.RUtils;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Schematic;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.ui.Bar;
import mindustry.ui.Cicon;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.logic.LogicDisplay;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Comparator;

public class LogicImageDialog extends BasicImageDialog {

    private RedMod mod;
    private Seq<LogicDisplay> displays;
    private LogicDisplay display;
    private boolean keepRatios;
    private String name;
    private float speed;
    private BufferedImage image;
    private BufferedImage used;
    private String operation;
    private float globalProgress;
    private float operationProgress;
    private boolean working;
    private boolean async;

    public LogicImageDialog(RedMod mod) {
        super("Image to [white]" + Iconc.blockMicroProcessor);
        this.mod = mod;
        this.displays = Vars.content
                .blocks()
                .select(block -> block instanceof LogicDisplay)
                //.filter(display -> !display.localizedName.contains("orderless")) // Remove those bugged "borderless" display
                .map(block -> (LogicDisplay) block);
        this.display = displays.max(Comparator.comparingInt(display -> display.displaySize));
        this.keepRatios = true;
        this.name = "!!! Name Me";
        this.speed = 1;
        this.operation = "Waiting";
        this.globalProgress = 0;
    }

    @Override
    protected void setup() {
        super.setup();

        if(used != null) {
            drawableImage.set(used);
            drawableImage.getCell().size(300);
        }

        TextureRegionDrawable drawable = new TextureRegionDrawable(Blocks.logicDisplay.icon(Cicon.full));
        cont.button(display.localizedName, drawable, () -> mod.ui.showSelection("Display", displays, display -> display.localizedName, type -> {
            display = type;
            prepareImage();
            setup();
        })).width(400).disabled(ignored -> working);

        cont.row();
        cont.table(settings -> {
            settings.field(name, name -> this.name = name).width(280).disabled(ignored -> working);
            settings.row();
            settings.check("Keep ratios", keepRatios, checked -> {
                keepRatios = checked;
                prepareImage();
            }).center().disabled(ignored -> working);
            settings.row();
            settings.check("Multithread (experimental)", async, checked -> async = checked).center().disabled(ignored -> working);
            settings.row();
            settings.table(st -> {
                st.add("Speed multiplier: ");
                st.field(String.valueOf(speed), value -> speed = Strings.parseFloat(value, speed)).disabled(ignored -> working);
            }).center();
        }).center().width(400);

        cont.row();
        Bar bar1 = new Bar(
                () -> Strings.fixed(globalProgress * 100, 2) + "%",
                () -> RPal.cyan,
                () -> globalProgress);
        Bar bar2 = new Bar(
                () -> operation + ": " + Strings.fixed(operationProgress * 100, 2) + "%",
                () -> RPal.cyan,
                () -> operationProgress);

        cont.add(bar1).width(400).height(40).visible(() -> working);
        cont.row();
        cont.add(bar2).width(400).height(40).visible(() -> working);

        buttons.button("Export", Icon.export, () -> {
            try {
                working = true;
                mod.pool.execute(this::buildSchematic);
            }catch(Throwable exception) {
                exception.printStackTrace();
                working = false;
            }
        }).disabled(button -> image == null || working);
    }

    @Override
    protected void imageSelected(BufferedImage image, Fi file) {
        this.name = file.nameWithoutExtension();
        this.image = image;
        this.prepareImage();
        this.setup();
    }

    protected void prepareImage() {
        int size = display.displaySize;

        if(image == null) {
            return;
        }

        if(!keepRatios) {
            used = ImageHelper.resize(image, size, size);
        }else if(image.getWidth() != size || image.getHeight() != size) {
            BufferedImage scaled = RUtils.resizeRatio(image, size, size);

            used = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

            Graphics2D graphics = used.createGraphics();
            graphics.drawImage(scaled, 0, 0, null);
            graphics.dispose();
        }else {
            used = image;
        }

        applyImage(used);
    }

    protected void buildSchematic() {
        operation = "Preparing";
        globalProgress = 0;

        BufferedImage image = used;

        operation = "Simplifying";
        globalProgress = 0.2F;

        ImageSimplifier simplifier = async ? new AsyncImageSimplifier() : new ImageSimplifier();

        long start = Time.nanos();
        IntMap<Seq<Rectangle>> simplified = simplifier.simplify(image, p -> {
            globalProgress = 0.2F + p * 0.2F;
            operationProgress = p;
        });
        long time = Time.timeSinceNanos(start);

        System.out.printf("Time taken to render: %.5f ms | Threads: %s%n", time / 1_000_000.0, simplifier.getThreads());

        operationProgress = 0;

        Seq<String> code = new Seq<>();
        Seq<String> current = new Seq<>();
        int drawCount = 0;
        int speed = Math.max(1, (int) (998 / this.speed));

        operation = "Building code";
        globalProgress = 0.4F;

        for(IntMap.Entry<Seq<Rectangle>> entry : simplified) {
            int color = entry.key;
            Seq<Rectangle> rectangles = entry.value;

            int r = color >> 16 & 0xFF;
            int g = color >> 8 & 0xFF;
            int b = color & 0xFF;

            while(!rectangles.isEmpty()) {
                current.add("draw color " + r + " " + g + " " + b + " 255 _ _");
                drawCount++;

                while(!rectangles.isEmpty() && current.size <= speed) {
                    Rectangle rect = rectangles.pop();

                    current.add("draw rect " + rect.x + " " + (image.getHeight() - rect.y - rect.height) + " " + rect.width + " " + rect.height + " _ _");
                    drawCount++;

                    if(drawCount >= 128) {
                        current.add("drawflush display1");
                        current.add("draw color " + r + " " + g + " " + b + " 255 _ _");
                        drawCount = 1;
                    }

                    if(current.size >= speed) {
                        current.add("drawflush display1");
                        drawCount = 0;

                        code.add(Strings.join("\n", current));
                        current.clear();
                        current.add("draw color " + r + " " + g + " " + b + " 255 _ _");
                    }
                }
            }
        }
        if(current.size > 0) {
            current.add("drawflush display1");
            code.add(Strings.join("\n", current));
        }


        operation = "Placing";
        globalProgress = 0.6F;

        int min = Mathf.ceil(Mathf.sqrt(display.size * display.size + code.size));
        int offset = min - display.size;
        int displayMin = Mathf.floor(offset / 2F);
        int displayMax = display.size + displayMin - 1;
        IntSeq positions = new IntSeq(code.size);

        out:
        for(int x = 0; x < min; x++) {
            for(int y = 0; y < min; y++) {
                if(positions.size == code.size) {
                    break out;
                }

                if(x >= displayMin && x <= displayMax && y >= displayMin && y <= displayMax) {
                    continue;
                }

                positions.add(Point2.pack(x, y));
            }
        }

        operation = "Building schematic";
        globalProgress = 0.8F;

        int displayPos = displayMax - Mathf.floor(display.size / 2F);

        Seq<Schematic.Stile> tiles = new Seq<>();

        tiles.add(new Schematic.Stile(display, displayPos, displayPos, null, (byte) 0));

        int width = 0;
        int height = 0;

        LogicBlock.LogicLink link = new LogicBlock.LogicLink(displayPos, displayPos, "display1", true);
        Seq<LogicBlock.LogicLink> links = Seq.with(link);
        for(int i = 0; i < code.size; i++) {
            String part = code.get(i);
            int position = positions.get(i);

            short x = Point2.x(position);
            short y = Point2.y(position);

            link.x = displayPos - x;
            link.y = displayPos - y;

            width = Math.max(width, x);
            height = Math.max(height, y);

            byte[] config = LogicBlock.compress(part, links);
            tiles.add(new Schematic.Stile(Blocks.microProcessor, x, y, config, (byte) 0));
        }


        operation = "Saving";
        globalProgress = 1.0F;

        StringMap tags = new StringMap();
        tags.put("name", name);

        Schematic schematic = new Schematic(tiles, tags, width, height);

        Core.app.post(() -> {
            hide();
            Vars.schematics.add(schematic);
            Vars.ui.schematics.hide();
            Vars.control.input.useSchematic(schematic);
            //((LogicBlock) Blocks.microProcessor).instructionsPerTick = 15;
            working = false;
            globalProgress = 0;
            operationProgress = 0;
        });
    }

}
