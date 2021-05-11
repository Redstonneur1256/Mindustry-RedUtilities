package fr.redstonneur1256.schematic;

import arc.func.Floatc;
import arc.struct.Seq;
import arc.struct.StringMap;
import fr.redstonneur1256.redutilities.graphics.Palette;
import fr.redstonneur1256.redutilities.graphics.Palette.Container;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Schematic;
import mindustry.game.Schematic.Stile;
import mindustry.type.Item;

import java.awt.image.BufferedImage;

public class SorterSchematic {

    private Palette<Palette.Container<Item>> itemsPalette;

    public void init() {
        itemsPalette = new Palette<>();
        //itemsPalette.useCache(true);

        for(Item item : Vars.content.items()) {
            itemsPalette.addColor(new Palette.Container<>(item, item.color.rgb888()));
        }
    }

    public void buildAndOpen(BufferedImage image) {
        Schematic schematic = buildSchematic(image);

        Vars.schematics.add(schematic);
        Vars.control.input.useSchematic(schematic);
    }

    public BufferedImage createPreview(BufferedImage image) {
        return createPreview(buildImage(image), image.getWidth(), image.getHeight());
    }

    public BufferedImage createPreview(Container<Item>[] items, int width, int height) {
        return itemsPalette.toImage(items, width, height);
    }

    public Container<Item>[] buildImage(BufferedImage image) {
        return buildImage(image, f -> {
        });
    }

    public Container<Item>[] buildImage(BufferedImage image, Floatc progressCons) {
        @SuppressWarnings("unchecked")
        Container<Item>[] containers = new Container[image.getWidth() * image.getHeight()];

        try {
            int width = image.getWidth();
            int height = image.getHeight();
            float max = width * height;

            for(int y = 0; y < height; y++) {
                for(int x = 0; x < width; x++) {
                    containers[x + y * width] = itemsPalette.matchColor(image.getRGB(x, y));
                    progressCons.get((x + y * width) / max);
                }
            }
        }finally {
            itemsPalette.clearCache();
        }

        return containers;
    }

    public Schematic buildSchematic(BufferedImage image) {
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
