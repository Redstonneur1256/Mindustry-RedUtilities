package fr.redstonneur1256.ui.base;

import arc.Core;
import arc.files.Fi;
import arc.func.Cons;
import com.fmsware.GifDecoder;
import fr.redstonneur1256.utils.DrawableImage;
import fr.redstonneur1256.utils.RUtils;
import mindustry.Vars;
import mindustry.ui.dialogs.BaseDialog;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class BasicImageDialog extends BaseDialog {

    protected final boolean gif;
    protected DrawableImage drawableImage;

    public BasicImageDialog(String title) {
        this(title, false);
    }

    public BasicImageDialog(String title, boolean gif) {
        super(title);
        this.gif = gif;

        drawableImage = new DrawableImage();

        shown(this::setup);
        hidden(this::dispose);
    }

    protected void setup() {
        cont.clear();
        buttons.clear();

        cont.center();

        drawableImage.add(cont);
        cont.row();

        addButtons();
    }

    protected void addButtons() {
        addCloseButton();

        Cons<Fi> listener = file -> {
            try(InputStream input = file.read()) {
                if(gif) {
                    GifDecoder decoder = new GifDecoder();
                    int status = decoder.read(input);
                    if(status != GifDecoder.STATUS_OK) {
                        Vars.ui.showErrorMessage("Failed to open gif");
                        return;
                    }
                    BufferedImage[] frames = new BufferedImage[decoder.getFrameCount()];
                    for(int i = 0; i < frames.length; i++) {
                        frames[i] = decoder.getFrame(i);
                    }
                    imagesSelected(frames, decoder.getDelay(0), file);
                }else {
                    imageSelected(ImageIO.read(input), file);
                }
            }catch(IOException exception) {
                Vars.ui.showErrorMessage("Failed to read image:\n" + exception.getMessage());
            }
        };

        if(gif) {
            buttons.button("Select image", () -> Vars.platform.showFileChooser(true, "Select image", "gif", listener));
        }else {
            buttons.button("Select image", () -> RUtils.showImageFileChooser("Select image", listener));
        }
    }

    protected void dispose() {
        drawableImage.dispose();
    }

    protected void imageSelected(BufferedImage image, Fi file) {

    }

    protected void imagesSelected(BufferedImage[] images, int delay, Fi file) {

    }

    protected void applyImage(BufferedImage... images) {
        int maxSize = Math.min(Core.graphics.getWidth() - 200, Core.graphics.getHeight() - 200);
        drawableImage.set(maxSize, maxSize, images);
    }

}
