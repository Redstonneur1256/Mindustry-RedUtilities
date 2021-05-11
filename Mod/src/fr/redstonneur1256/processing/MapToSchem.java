package fr.redstonneur1256.processing;

import arc.struct.IntSet;
import arc.struct.Seq;
import arc.struct.StringMap;
import mindustry.game.Schematic;
import mindustry.game.Schematic.Stile;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.blocks.ConstructBlock.ConstructBuild;

import static mindustry.Vars.*;

public class MapToSchem {

    public static void export() {
        Seq<Stile> tiles = new Seq<>(world.width() * world.height() * 2);

        world.tiles.eachTile(tile -> {
            tiles.add(new Stile(tile.floor(), tile.x, tile.y, null, (byte) 0));
            if(tile.build == null) {
                tiles.add(new Stile(tile.block(), tile.x, tile.y, null, (byte) 0));
            }
        });

        IntSet counted = new IntSet();
        for(int x = 0; x < world.width(); x++) {
            for(int y = 0; y < world.height(); y++) {
                Building build = world.build(x, y);
                if(build == null) {
                    continue;
                }
                int pos = build.tile.pos();
                if(counted.contains(pos)) {
                    continue;
                }
                counted.add(pos);

                ConstructBuild constructBuild = build instanceof ConstructBuild ? (ConstructBuild) build : null;
                Block block = constructBuild == null ? build.block : constructBuild.cblock;
                Object config = constructBuild == null ? build.config() : constructBuild.lastConfig;

                tiles.add(new Stile(block, build.tileX(), build.tileY(), config, (byte) build.rotation));
            }
        }

        StringMap tags = new StringMap();
        tags.put("name", "---- Name Me (MAP)");
        Schematic schematic = new Schematic(tiles, tags, world.width(), world.height());

        schematics.add(schematic);
        ui.schematics.hide();
        ui.schematics.show();
    }

}
