package fr.redstonneur1256.ui.dialogs;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Pixmap;
import arc.scene.ui.TextField;
import arc.util.Strings;
import arc.util.Time;
import fr.redstonneur1256.RedMod;
import fr.redstonneur1256.utils.graphics.PixmapHelper;
import fr.redstonneur1256.utils.graphics.Palette;
import fr.redstonneur1256.ui.RPal;
import fr.redstonneur1256.ui.base.BasicImageDialog;
import mindustry.game.Schematic;
import mindustry.gen.Icon;
import mindustry.type.Item;
import mindustry.ui.Bar;

import static mindustry.Vars.*;

public class SorterImageDialog extends BasicImageDialog {

    private RedMod mod;
    private Pixmap image;
    private Pixmap preview;
    private Palette.Container<Item>[] items;
    private float progress;
    private int width;
    private int height;
    private float ratio;
    private boolean lockRatios;
    private TextField widthField;
    private TextField heightField;
    private long lastModified;
    private boolean working;

    public SorterImageDialog(RedMod mod) {
        super("Sorter to image");
        this.mod = mod;
    }

    @Override
    protected void setup() {
        super.setup();

        if(image != null) {
            applyImage(image);
            lastModified = 0;
        }

        progress = 0;
        Bar bar = new Bar(
                () -> image == null ? "Select image" : ("Progress : " + Strings.fixed(progress * 100, 2) + "%"),
                () -> RPal.cyan,
                () -> progress);

        cont.add(bar).width(400).height(40);
        cont.row();

        cont.table(settings -> {
            settings.check("Lock ratios", lockRatios, checked -> lockRatios = checked);
            settings.row();
            settings.add("Width: ");
            widthField = settings.field(String.valueOf(width), value -> buildWidth(Strings.parseInt(value, width))).get();
            settings.row();
            settings.add("Height: ");
            heightField = settings.field(String.valueOf(height), value -> buildHeight(Strings.parseInt(value, height))).get();
        }).grow();

        buttons.button("Save", Icon.export, () -> {
            if(items.length != width * height) {
                // OOF
                return;
            }
            Schematic schematic = mod.sorterSchematic.buildSchematic(items, width, height);
            hide();
            ui.schematics.hide();
            schematics.add(schematic);
            control.input.useSchematic(schematic);
        }).disabled(button -> items == null || items.length != width * height);
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

    private void createPreview() {
        items = mod.sorterSchematic.buildImage(image, p -> progress = p);
        preview = mod.sorterSchematic.createPreview(items, image.getWidth(), image.getHeight());
        progress = 1;

        working = false;

        Core.app.post(() -> applyImage(preview));
    }

    @Override
    protected void imageSelected(Pixmap image, Fi file) {
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.ratio = (float) width / height;
        setup();
    }

    @Override
    public void draw() {
        super.draw();

        if(image != null && Time.timeSinceMillis(lastModified) > 1000 && !working) {
            lastModified = Long.MAX_VALUE;
            working = true;

            mod.pool.execute(this::resize);
        }
    }

    private void resize() {
        progress = 0;
        Pixmap prevImage = image;
        image = PixmapHelper.resize(image, width, height);
        prevImage.dispose();

        Core.app.post(() -> applyImage(image));
        mod.pool.execute(this::createPreview);
    }

    @Override
    protected void dispose() {
        super.dispose();
        image = null;
        items = null;
        preview = null;
    }

}
