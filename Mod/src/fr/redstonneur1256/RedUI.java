package fr.redstonneur1256;

import arc.func.Cons;
import arc.func.Func;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import fr.redstonneur1256.processing.MapToSchem;
import fr.redstonneur1256.ui.dialogs.LogicGifDialog;
import fr.redstonneur1256.ui.dialogs.LogicImageDialog;
import fr.redstonneur1256.ui.dialogs.SorterGifDialog;
import fr.redstonneur1256.ui.dialogs.SorterImageDialog;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.BaseDialog;

public class RedUI {

    public RedMod mod;
    public LogicImageDialog logicImage;
    public LogicGifDialog logicGif;
    public SorterImageDialog sorterImage;
    public SorterGifDialog sorterGif;

    public RedUI(RedMod mod) {
        this.mod = mod;
    }

    /**
     * Init the UI by:
     * - Initializing different dialogs
     * - Adding buttons to schematics dialog
     */
    public void init() {
        logicImage = new LogicImageDialog(mod);
        logicGif = new LogicGifDialog(mod);
        sorterImage = new SorterImageDialog(mod);
        sorterGif = new SorterGifDialog(mod);

        Table table = Vars.ui.schematics.buttons;
        table.button("Processor image", Icon.paste, logicImage::show);
        //table.button("Processor GIF", Icon.paste, logicGif::show); GIFs are disabled (broken by pixmaps)
        table.button("Sorter image", Icon.paste, sorterImage::show);
        //table.button("Sorter GIF", Icon.paste, sorterGif::show);
        table.button("Map to Schem", Icon.export, MapToSchem::export);
    }

    public <T> void showSelection(String title, Seq<T> elements, Func<T, String> nameProv, Cons<T> selected) {
        BaseDialog dialog = new BaseDialog(title);
        dialog.cont.pane(table -> {
            for(T element : elements) {
                table.button(nameProv.get(element), () -> {
                    dialog.hide();
                    selected.get(element);
                }).growX().pad(8);
                table.row();
            }
        }).size(400, 350);
        dialog.addCloseButton();
        dialog.show();
    }

}
