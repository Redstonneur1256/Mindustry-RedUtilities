package fr.redstonneur1256;

import arc.Core;
import arc.Events;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Log;
import arc.util.Time;
import arc.util.async.Threads;
import fr.redstonneur1256.logic.RedLogic;
import fr.redstonneur1256.redutilities.async.ThreadPool;
import fr.redstonneur1256.redutilities.async.pools.ReusableThreadPool;
import fr.redstonneur1256.redutilities.reflection.Reflect;
import fr.redstonneur1256.schematic.SorterSchematic;
import fr.redstonneur1256.utils.DebugLogger;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mindustry.type.Category;
import mindustry.type.UnitType;
import mindustry.ui.Cicon;
import mindustry.ui.Fonts;
import mindustry.world.Block;

import java.util.concurrent.atomic.AtomicInteger;

public class RedMod extends Mod {

    /**
     * ThreadPool used for all async operations
     */
    public ThreadPool pool;
    /**
     * UI Management like {@link mindustry.core.UI}
     */
    public RedUI ui;
    /**
     * Utility class instance to build sorter schematics
     */
    public SorterSchematic sorterSchematic;

    public RedMod() {
        Log.level = Log.LogLevel.debug;
    }

    @Override
    public void init() {
        Events.run(EventType.ClientLoadEvent.class, this::lateInit);

        Settings.defaults();

        pool = new ReusableThreadPool(Threads::daemon, 4);

        ui = new RedUI(this);
        ui.init();

        sorterSchematic = new SorterSchematic();
        sorterSchematic.init();

        new RedLogic();

        Vars.enableConsole = true;
    }

    private void lateInit() {
        // We need to replace the logger after the ClientLoadEvent is
        // called so the Vars logger buffer is flushed to the script frag
        DebugLogger logger = new DebugLogger();
        Log.logger = logger;
        logger.init();

        Log.info("Mapping extended content...");
        long start = Time.millis();

        // Processors can't draw images like ores, floors, map walls... so I map them so more logic
        // is available to everyone
        ContentType[] types = new ContentType[] { ContentType.item, ContentType.unit, ContentType.block };

        int count = 0;
        for(ContentType type : types) {
            count += Vars.content.getBy(type).size;
        }

        TextureRegion[] icons = Reflect.get(Fonts.class, "iconTable");
        if(icons.length < count) {
            Log.info("Replacing current icon table array...");
            Log.info("Incrementing size to " + Mathf.nextPowerOfTwo(count));

            icons = new TextureRegion[Mathf.nextPowerOfTwo(count)];
            Reflect.set(Fonts.class, "iconTable", icons);

            icons[0] = Core.atlas.find("error");
        }

        AtomicInteger position = new AtomicInteger(0);
        for(ContentType type : types) {
            map(position, icons, type);
        }

        // Handle constants that are not saved by default
        // Use block- to not override some items such as sand
        for(Block block : Vars.content.blocks()) {
            if(!block.synthetic()) {
                Vars.constants.put("@block-" + block.name, block);
            }
        }
        for(UnitType unit : Vars.content.units()) {
            Vars.constants.put("@" + unit.name, unit);
        }

        for(Category category : Category.all) {
            Vars.constants.put("@category-" + category.name(), category);
        }

        Log.info("Mapped @ content objects in @ms", position.get() == count, Time.timeSinceMillis(start));

        Settings.register();
    }

    private void map(AtomicInteger position, TextureRegion[] icons, ContentType type) {
        for(Content cont : Vars.content.getBy(type)) {
            if(cont instanceof UnlockableContent) {
                UnlockableContent content = (UnlockableContent) cont;
                int newPosition = position.incrementAndGet();
                if(newPosition >= icons.length) {
                    return;
                }
                int prevId = content.iconId;
                content.iconId = newPosition;
                icons[newPosition] = content.icon(Cicon.full);

                Log.debug("[@] Mapping content @ @ -> @", type, content.name, prevId, content.iconId);
            }
        }
    }

}
