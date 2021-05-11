package fr.redstonneur1256;

import arc.Core;
import arc.func.Cons;
import arc.func.Prov;
import arc.scene.ui.SettingsDialog.SettingsTable;
import fr.redstonneur1256.sound.EggSound;
import mindustry.Vars;

@SuppressWarnings("unchecked")
public enum Settings {

    EGG_SOUNDS("redMod.eggSounds", false, () -> Vars.ui.settings.sound, EggSound::setEnabled),
    SEAMLESS_DISPLAYS("redMod.displays", false, () -> Vars.ui.settings.game, value -> {});

    private String name;
    private Object defaultValue;
    private Prov<SettingsTable> categoryProv;
    private Cons<Object> change;

    <T> Settings(String name, T defaultValue, Prov<SettingsTable> categoryProv, Cons<T> change) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.categoryProv = categoryProv;
        this.change = value -> change.get((T) value);
    }

    public static void defaults() {
        for(Settings value : values()) {
            Core.settings.defaults(value.name, value.defaultValue);

            value.change.get(Core.settings.get(value.name, value.defaultValue));
        }
    }

    public static void register() {
        for(Settings value : values()) {
            Class<?> type = value.defaultValue.getClass();
            SettingsTable category = value.categoryProv.get();

            if(type == Boolean.class) {
                category.checkPref(value.key(), value.bool(), newValue -> value.change.get(newValue));
            }

        }
    }

    public String key() {
        return name;
    }

    public void set(Object value) {
        Core.settings.put(name, value);
    }

    public int num() {
        return Core.settings.getInt(name, (Integer) defaultValue);
    }

    public boolean bool() {
        return Core.settings.getBool(name, (Boolean) defaultValue);
    }

}
