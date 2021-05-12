package fr.redstonneur1256.schematic;

import arc.func.Floatc;
import arc.graphics.Pixmap;
import arc.struct.Seq;
import arc.struct.StringMap;
import fr.redstonneur1256.utils.graphics.Palette;
import fr.redstonneur1256.utils.graphics.Palette.Container;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Schematic;
import mindustry.game.Schematic.Stile;
import mindustry.type.Item;

public class SorterSchematic {

    private Palette<Container<Item>> itemsPalette;

    public void init() {
        itemsPalette = new Palette<>();
        //itemsPalette.useCache(true);

        for(Item item : Vars.content.items()) {
            itemsPalette.addColor(new Container<>(item, item.color.rgb888()));
        }
    }

    public void buildAndOpen(Pixmap image) {
        Schematic schematic = buildSchematic(image);

        Vars.schematics.add(schematic);
        Vars.control.input.useSchematic(schematic);
    }

    public Pixmap createPreview(Pixmap image) {
        return createPreview(buildImage(image), image.getWidth(), image.getHeight());
    }

    public Pixmap createPreview(Container<Item>[] items, int width, int height) {
        return itemsPalette.toImage(items, width, height);
    }

    public Container<Item>[] buildImage(Pixmap image) {
        return buildImage(image, f -> {
        });
    }

    public Container<Item>[] buildImage(Pixmap image, Floatc progressCons) {
        @SuppressWarnings("unchecked")
        Container<Item>[] containers = new Container[image.getWidth() * image.getHeight()];

        try {
            int width = image.getWidth();
            int height = image.getHeight();
            float max = width * height;

            for(int y = 0; y < height; y++) {
                for(int x = 0; x < width; x++) {
                    containers[x + y * width] = itemsPalette.matchColor(image.getPixel(x, y) >> 8);
                    progressCons.get((x + y * width) / max);
                }
            }
        }finally {
            itemsPalette.clearCache();
        }

        return containers;
    }

    public Schematic buildSchematic(Pixmap image) {
        return buildSchematic(buildImage(image), image.getWidth(), image.getHeight());
    }

    public Schematic buildSchematic(Container<Item>[] items, int width, int height) {
        Seq<Stile> tiles = new Seq<>();

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                tiles.add(new Stile(Blocks.sorter, x, height - y - 1, items[x + y * width].getValue(), (byte) 0));
            }
        }

        StringMap tags = new StringMap();
        tags.put("name", "---- Name Me");
        return new Schematic(tiles, tags, width, height);
    }

}
