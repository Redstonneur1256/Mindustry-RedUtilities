package fr.redstonneur1256.utils.graphics;

import arc.graphics.Pixmap;

public class PixmapHelper {

    public static Pixmap resize(Pixmap image, int width, int height) {
        Pixmap copy = new Pixmap(width, height);
        copy.drawPixmap(image, 0, 0, image.getWidth(), image.getHeight(), 0, 0, width, height);
        return copy;
    }

}
