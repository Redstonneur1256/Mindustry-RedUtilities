package fr.redstonneur1256.utils;

import arc.files.Fi;
import arc.func.Boolf;
import arc.func.Cons;
import arc.graphics.Pixmap;
import arc.struct.Seq;
import fr.redstonneur1256.redutilities.reflection.Reflect;
import fr.redstonneur1256.utils.graphics.PixmapHelper;
import mindustry.gen.Player;
import mindustry.ui.dialogs.FileChooser;

public class RUtils {

    private static final Seq<String> IMAGE_EXTENSIONS;

    static {
        IMAGE_EXTENSIONS = Seq.with("png", "jpg", "jpeg", "gif", "webm");
    }

    public static Pixmap resizeRatio(Pixmap image, int maxWidth, int maxHeight) {
        double widthRatio = (double) maxWidth / image.getWidth();
        double heightRatio = (double) maxHeight / image.getHeight();
        double ratio = Math.min(widthRatio, heightRatio);

        int width = (int) (image.getWidth() * ratio);
        int height = (int) (image.getHeight() * ratio);
        return width == image.getWidth() && height == image.getHeight() ? image : PixmapHelper.resize(image, width, height);
    }

    public static void showImageFileChooser(String title, Cons<Fi> consumer) {
        showFileChooser(title, consumer, IMAGE_EXTENSIONS);
    }

    public static void showFileChooser(String title, Cons<Fi> consumer, Seq<String> allowedExtensions) {
        showFileChooser(title, file -> allowedExtensions.contains(file.extension().toLowerCase()), consumer);
    }

    public static void showFileChooser(String title, Boolf<Fi> filter, Cons<Fi> consumer) {
        new FileChooser(title, filter, true, consumer).show();
    }

    public static double factorial(double value) {
        double result = 1;
        for(int i = 1; i <= value; i++) {
            result *= i;
        }
        return result;
    }

    public static boolean isFooClient() {
        try {
            Reflect.getField(Player.class, "fooUser");
            return true;
        }catch(RuntimeException exception) {
            return false;
        }
    }

}
