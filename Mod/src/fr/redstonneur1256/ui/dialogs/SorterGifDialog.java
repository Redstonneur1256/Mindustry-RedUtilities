package fr.redstonneur1256.ui.dialogs;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.scene.ui.TextField;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Time;
import fr.redstonneur1256.RedMod;
import fr.redstonneur1256.utils.graphics.Palette.Container;
import fr.redstonneur1256.ui.RPal;
import fr.redstonneur1256.ui.base.BasicImageDialog;
import fr.redstonneur1256.utils.graphics.PixmapHelper;
import mindustry.gen.Icon;
import mindustry.type.Item;
import mindustry.ui.Bar;

public class SorterGifDialog extends BasicImageDialog {

    private RedMod mod;
    private Pixmap[] frames;
    private Pixmap[] preview;
    private Seq<Container<Item>[]> items;
    private int delay;
    private float progress;
    private int width;
    private int height;
    private float ratio;
    private boolean lockRatios;
    private TextField widthField;
    private TextField heightField;
    private long lastModified;
    private boolean workingPreview;
    private long lastFrame;
    private boolean working;

    public SorterGifDialog(RedMod mod) {
        super("Sorter GIF", true);
        this.mod = mod;
        this.items = new Seq<>();
        this.delay = 100;
        this.lastModified = Long.MAX_VALUE;
    }

    @Override
    protected void setup() {
        super.setup();

        drawableImage.getCell().size(300);

        progress = 0;
        Bar bar = new Bar(
                () -> frames == null ? "Select GIF" : ("Progress : " + Strings.fixed(progress * 100, 2) + "%"),
                () -> RPal.cyan,
                () -> progress);

        cont.add(bar).width(400).height(40);
        cont.row();

        cont.table(settings -> {
            settings.check("Lock ratios", lockRatios, checked -> lockRatios = checked);
            settings.row();
            settings.table(table -> {
                table.add("Width: ");
                widthField = table.
                        field(String.valueOf(width), value -> buildWidth(Strings.parseInt(value, width)))
                        .disabled(field -> working)
                        .get();
                table.row();
                table.add("Height: ");
                heightField = table
                        .field(String.valueOf(height), value -> buildHeight(Strings.parseInt(value, height)))
                        .disabled(field -> working)
                        .get();
                table.row();
                table.add("Delay (MS): ");
                table.field(String.valueOf(delay), value -> delay = Strings.parseInt(value, delay))
                        .disabled(field -> working);
            });
        }).grow();

        buttons.button("Export", Icon.export, () -> {

        }).disabled(button -> frames == null);
    }

    private void buildWidth(int width) {
        this.lastModified = Time.millis();
        this.width = width;

        if(lockRatios) {
            this.height = (int) (width / ratio);
            heightField.setText(String.valueOf(this.height));
        }
    }

    private void buildHeight(int height) {
        this.lastModified = Time.millis();
        this.height = height;

        if(lockRatios) {
            this.width = (int) (height / ratio);
            widthField.setText(String.valueOf(this.width));
        }
    }

    @Override
    protected void imagesSelected(Pixmap[] images, int gifDelay, Fi file) {
        delay = gifDelay;
        frames = images;
        preview = new Pixmap[images.length];
        System.arraycopy(images, 0, preview, 0, frames.length);
        width = frames[0].getWidth();
        height = frames[0].getHeight();
        ratio = (float) width / height;
        delay = gifDelay;
        lastModified = Long.MAX_VALUE;

        applyImage(images);
        setup();
        if(!workingPreview) {
            mod.pool.execute(this::transformFrames);
        }
    }

    private void transformFrames() {
        workingPreview = true;

        try {
            items.clear();


            for(int i = 0; i < preview.length; i++) {
                Pixmap frame = preview[i];
                int count = preview.length;
                int step = i;
                items.add(mod.sorterSchematic.buildImage(frame, p -> progress = (p + step) / count));
            }
            progress = 0.99F;

            for(int i = 0; i < preview.length; i++) {
                preview[i] = mod.sorterSchematic.createPreview(items.get(i), preview[i].getWidth(), preview[i].getHeight());
            }

            progress = 1F;

            Core.app.post(() -> {
                workingPreview = false;
                applyImage(preview);
            });
        }catch(Exception exception) {
            exception.printStackTrace();
            workingPreview = false;
        }
    }

    @Override
    public void draw() {
        super.draw();

        if(frames != null && Time.timeSinceMillis(lastModified) > 1000 && !workingPreview) {
            lastModified = Long.MAX_VALUE;
            workingPreview = true;

            mod.pool.execute(this::resize);
        }

        if(Time.timeSinceMillis(lastFrame) > delay) {
            lastFrame = Time.millis();
            drawableImage.nextImage();
        }
    }

    private void resize() {
        progress = 0;
        for(int i = 0; i < preview.length; i++) {
            preview[i] = PixmapHelper.resize(frames[i], width, height);
        }
        Core.app.post(() -> {
            applyImage(preview);
            mod.pool.execute(this::transformFrames);
        });
    }

}
