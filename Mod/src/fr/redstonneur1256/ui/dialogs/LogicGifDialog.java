package fr.redstonneur1256.ui.dialogs;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Rect;
import arc.struct.IntMap;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.struct.StringMap;
import arc.util.Strings;
import arc.util.Structs;
import arc.util.Time;
import fr.redstonneur1256.RedMod;
import fr.redstonneur1256.processing.image.ImageSimplifier;
import fr.redstonneur1256.ui.base.BasicImageDialog;
import fr.redstonneur1256.utils.graphics.PixmapHelper;
import fr.redstonneur1256.utils.RUtils;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Schematic;
import mindustry.gen.Icon;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.logic.LogicBlock.LogicLink;

import static mindustry.game.Schematic.Stile;

public class LogicGifDialog extends BasicImageDialog {

    int quality = 40;
    private RedMod mod;
    private Pixmap[] originals;
    private Pixmap[] frames;
    private int delay;
    private boolean working;
    private long lastFrame;

    public LogicGifDialog(RedMod mod) {
        super("Logic to GIF", true);
        this.mod = mod;
        this.delay = 50;

        shown(() -> {
            if(frames != null) {
                drawableImage.set(frames);
                drawableImage.getCell().size(300);
            }
        });
    }

    @Override
    protected void setup() {
        super.setup();

        drawableImage.getCell().size(300);

        cont.table(settings -> {
            settings.table(table -> {
                table.add("Delay (MS): ");
                table.field(String.valueOf(delay), value -> delay = Strings.parseInt(value, delay));
            });
            settings.row();
            settings.field(String.valueOf(quality), value -> {
                quality = Strings.parseInt(value, quality);
                updateFrames();
            });
        });
        cont.row();

        buttons.button("Export", Icon.export, () -> mod.pool.execute(() -> {
            working = true;
            try {
                buildSchematic();
            }catch(Exception exception) {
                exception.printStackTrace();
                working = false;
            }
        })).disabled(button -> working || frames == null);
    }

    @Override
    protected void imagesSelected(Pixmap[] images, int delay, Fi file) {
        this.originals = images;
        this.frames = new Pixmap[images.length];
        this.updateFrames();

        setup();
    }

    private void updateFrames() {
        if(originals == null) {
            return;
        }

        for(Pixmap frame : frames) {
            if(frame != null) {
                frame.dispose();
            }
        }

        for(int i = 0; i < frames.length; i++) {
            Pixmap image = originals[i];
            if(image.getWidth() == image.getHeight()) {
                // Image is squared no need to calculate ratios
                frames[i] = PixmapHelper.resize(image, quality, quality);
                image.dispose();
                continue;
            }

            Pixmap copy = new Pixmap(quality, quality, Pixmap.Format.rgba8888);

            Pixmap resized = RUtils.resizeRatio(image, quality, quality);
            copy.drawPixmap(resized, 0, 0);
            resized.dispose();

            frames[i] = copy;
        }

        drawableImage.set(quality, quality, frames);
        drawableImage.getCell().size(300);
    }

    @Override
    public void draw() {
        super.draw();

        if(Time.timeSinceMillis(lastFrame) > delay) {
            lastFrame = Time.millis();
            drawableImage.nextImage();
        }
    }

    private void buildSchematic() {
        Seq<Stile> tiles = new Seq<>();

        Seq<IntMap<IntSeq>> changedFrames = new Seq<>();

        for(int i = 0; i < frames.length; i++) {
            Pixmap prev = frames[i == 0 ? frames.length - i - 1 : i - 1];
            Pixmap next = frames[i];

            IntMap<IntSeq> points = new IntMap<>();

            for(int x = 0; x < prev.getWidth(); x++) {
                for(int y = 0; y < prev.getHeight(); y++) {
                    int prevRGB = prev.getPixel(x, y);
                    int nextRGB = next.getPixel(x, y);

                    if(prevRGB != nextRGB) {
                        points.get(nextRGB, IntSeq::new).add(Point2.pack(x, y));
                    }
                }
            }

            changedFrames.add(points);
        }

        // TODO: Compress changing frames by making rectangles like the ImageSimplifier

        Seq<CodeBuilder> codeBuilders = new Seq<>();

        for(int y = 0; y < 12; y += 2) {
            for(int x = 0; x < 14; x += 2) {
                // Left corner is processor + switch
                if(y == 0 && x == 0) {
                    continue;
                }


                // Don't place processor into logic display
                if(x > 2 && x < 10 && y > 0 && y < 8) {
                    continue;
                }

                codeBuilders.add(new CodeBuilder(x, y));
            }
        }

        int scale = Mathf.ceil(176F / quality);

        IntMap<Seq<Rect>> firstFrame = new ImageSimplifier().simplify(frames[0], p -> {
        });
        for(IntMap.Entry<Seq<Rect>> colorGroup : firstFrame) {
            int color = colorGroup.key;
            Seq<Rect> areas = colorGroup.value;

            int r = color >> 16 & 0xFF;
            int g = color >> 8 & 0xFF;
            int b = color & 0xFF;

            CodeBuilder bestBuilder = null;
            boolean justFound = false;

            while(!areas.isEmpty()) {
                Rect area = areas.pop();

                if(bestBuilder == null) {
                    // Find the code builder with the less amount of lines
                    bestBuilder = codeBuilders.min(Structs.comparingInt(builder -> builder.lines));
                    justFound = true;
                }

                if(bestBuilder.frame != -1) {
                    bestBuilder.frame = -1;

                    int line = bestBuilder.lines;

                    bestBuilder.appendLine("read frame cell1 0");
                    bestBuilder.appendLine("jump " + line + " notEqual frame 0");
                }

                if(justFound) {
                    bestBuilder.appendLine("draw color " + r + " " + g + " " + b);
                    justFound = false;
                }

                bestBuilder.appendLine("draw rect " + (int) area.x * scale + " " + (176 - (int) area.y * scale) + " " + (int) area.width * scale + " " + (int) area.height * scale + " 0 0");
                bestBuilder.operations++;

                if(bestBuilder.operations >= 50) {
                    bestBuilder.appendLine("drawflush display1");

                    bestBuilder.operations = 0;
                    bestBuilder = null;
                }

            }
        }

        for(int frame = 0; frame < changedFrames.size; frame++) {
            IntMap<IntSeq> framePixels = changedFrames.get(frame);

            for(IntMap.Entry<IntSeq> color : framePixels) {
                CodeBuilder best = codeBuilders.min(Structs.comparingInt(a -> a.builder.length()));

                StringBuilder builder = best.builder;

                if(best.frame != frame) {
                    int line = best.lines;
                    //int line = builder.length() == 0 ? 0 : builder.toString().split("\n").length;

                    best.appendLine("drawflush display1");
                    best.appendLine("read frame cell1 0");
                    best.appendLine("jump " + line + " notEqual frame " + frame);
                }

                best.frame = frame;

                int rgb = color.key;
                int r = rgb >> 16 & 0xFF;
                int g = rgb >> 8 & 0xFF;
                int b = rgb & 0xFF;

                builder.append("draw color ").append(r).append(' ').append(g).append(' ').append(b);
                best.appendLine();

                for(int item : color.value.toArray()) {
                    int x = Point2.x(item);
                    int y = Point2.y(item);
                    builder.append("draw rect ").append(x * scale).append(' ').append(176 - (y * scale)).append(' ').append(scale).append(' ').append(scale);
                    best.appendLine();
                }

            }
        }

        for(CodeBuilder builder : codeBuilders) {
            int x = builder.x;
            int y = builder.y;

            builder.appendLine("drawflush display1");

            Seq<LogicLink> links = new Seq<>();
            links.add(new LogicLink(6 - x, 4 - y, "display1", true));
            links.add(new LogicLink(1 - x, 1 - y, "cell1", true));

            tiles.add(new Stile(Blocks.logicProcessor, x, y, LogicBlock.compress(builder.builder.toString(), links), (byte) 0));
        }

        String code = "" +
                "set i -1\n " +
                "op add i i 1\n" +
                "sensor enabled switch1 @enabled\n" +
                "jump 8 equal enabled 1\n" +
                "write -1 cell1 0\n" +
                "draw clear 0 0 0 _ _ _\n" +
                "drawflush display1\n" +
                "jump 2 always _ _\n" +
                "write i cell1 0\n" +
                "op add wait @time " + delay + "\n" +
                "jump 10 lessThan @time wait\n" +
                "jump 1 lessThan i " + frames.length + "\n";
        Seq<LogicLink> links = new Seq<>();
        links.add(new LogicLink(1, 0, "cell1", true));
        links.add(new LogicLink(0, -1, "switch1", true));
        links.add(new LogicLink(1, -1, "message1", true));
        links.add(new LogicLink(4, 1, "display1", true));

        tiles.add(new Stile(Blocks.switchBlock, 0, 0, false, (byte) 0));
        tiles.add(new Stile(Blocks.message, 1, 0, "", (byte) 0));
        tiles.add(new Stile(Blocks.microProcessor, 0, 1, LogicBlock.compress(code, links), (byte) 0));
        tiles.add(new Stile(Blocks.memoryCell, 1, 1, null, (byte) 0));
        tiles.add(new Stile(Blocks.largeLogicDisplay, 6, 4, null, (byte) 0));

        StringMap tags = new StringMap();
        tags.put("name", "---- Name me");

        Schematic schematic = new Schematic(tiles, tags, 7 * 2, 6 * 2);

        Core.app.post(() -> {
            hide();
            Vars.ui.schematics.hide();
            Vars.control.input.useSchematic(schematic);
            working = false;
        });
    }

    private static class CodeBuilder {

        public int operations;
        public int lines;
        private int x;
        private int y;
        private StringBuilder builder;
        private int frame;

        public CodeBuilder(int x, int y) {
            this.x = x;
            this.y = y;
            this.builder = new StringBuilder();
            this.frame = Integer.MIN_VALUE;
        }

        public void appendLine(String line) {
            builder.append(line).append('\n');
            lines++;
        }

        public void appendLine() {
            builder.append('\n');
            lines++;
        }

    }

}
