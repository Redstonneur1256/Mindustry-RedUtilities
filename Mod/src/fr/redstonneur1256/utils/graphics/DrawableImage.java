package fr.redstonneur1256.utils.graphics;

import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import fr.redstonneur1256.utils.RUtils;

import static arc.graphics.Pixmap.Format;

public class DrawableImage {

    private int width;
    private int height;
    private Pixmap[] images;
    private Texture[] textures;
    private TextureRegionDrawable[] drawables;
    private Table table;
    private Cell<Table> cell;
    private int index;

    public DrawableImage() {
        width = 1;
        height = 1;
    }

    public DrawableImage setMaxSize(int maxWidth, int maxHeight) {
        Pixmap image = images[0];
        if(image.getWidth() > maxWidth || image.getHeight() > maxHeight) {
            double widthRatio = (double) maxWidth / image.getWidth();
            double heightRatio = (double) maxHeight / image.getHeight();
            double ratio = Math.min(widthRatio, heightRatio);
            int width = (int) (image.getWidth() * ratio);
            int height = (int) (image.getHeight() * ratio);
            return setSize(width, height);
        }
        return setSize(image.getWidth(), image.getHeight());
    }

    public DrawableImage setSize(int width, int height) {
        this.width = Math.max(width, 1);
        this.height = Math.max(height, 1);
        if(cell != null) {
            cell.size(this.width, this.height);
        }
        return updateImages();
    }

    public DrawableImage set(Pixmap image, Pixmap... images) {
        Pixmap[] copy = new Pixmap[images.length + 1];
        copy[0] = image;
        System.arraycopy(images, 0, copy, 1, images.length);
        return set(image.getWidth(), image.getHeight(), copy);
    }

    public DrawableImage set(Pixmap[] images) {
        this.images = images;
        return updateImages();
    }

    public DrawableImage set(int maxWidth, int maxHeight, Pixmap... images) {
        this.images = images;
        return setMaxSize(maxWidth, maxHeight);
    }

    public DrawableImage dispose() {
        if(textures != null) {
            for(Texture texture : textures) {
                texture.dispose();
            }
            textures = null;
        }
        drawables = null;

        return this;
    }

    protected DrawableImage updateImages() {
        dispose();
        int count = images.length;

        this.textures = new Texture[count];
        this.drawables = new TextureRegionDrawable[count];

        for(int i = 0; i < count; i++) {
            Pixmap image = images[i];

            if(image.getWidth() != width || image.getHeight() != height) {
                Pixmap prevImage = image;
                image = images[i] = RUtils.resizeRatio(image, width, height);
                prevImage.dispose();
            }

            Texture texture = new Texture(image);
            TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(texture));

            textures[i] = texture;
            drawables[i] = drawable;
        }

        if(table != null) {
            index = 0;
            table.background(drawables[0]);
        }

        return this;
    }

    public void nextImage() {
        if(table != null && drawables != null) {
            index = (index + 1) % drawables.length;
            table.background(drawables[index]);
        }
    }

    public void set(Cell<Table> table) {
        if(this.table != null) {
            this.table.remove();
        }
        this.table = table.get();
        this.cell = table.size(width, height);
    }

    public void add(Table table) {
        if(this.table != null) {
            this.table.remove();
            this.table = null;
        }
        this.cell = table.table().size(width, height);
        this.table = cell.get();

        if(drawables != null) {
            this.table.background(drawables[index % drawables.length]);
        }
    }

    public Pixmap[] getPixmaps() {
        return images;
    }

    public void reload() {
        for(Texture texture : textures) {
            texture.load(texture.getTextureData());
        }
    }

    public Table getTable() {
        return table;
    }

    public Cell<Table> getCell() {
        return cell;
    }

}
