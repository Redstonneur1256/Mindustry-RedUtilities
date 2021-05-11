package fr.redstonneur1256.sound;

import arc.Core;
import arc.audio.Sound;
import arc.struct.ObjectMap;
import mindustry.Vars;
import mindustry.entities.bullet.BulletType;
import mindustry.type.Weapon;
import mindustry.world.blocks.defense.turrets.Turret;

import static mindustry.Vars.content;

public class EggSound {

    private static final ObjectMap<Weapon, Sound> weapons;
    private static final ObjectMap<Turret, Sound> turrets;
    private static final ObjectMap<BulletType, Sound> bullets;
    private static Sound sound;
    private static boolean active;

    static {
        weapons = new ObjectMap<>();
        turrets = new ObjectMap<>();
        bullets = new ObjectMap<>();
    }

    public static void enable() {
        if(active) {
            return;
        }
        active = true;

        if(sound == null) {
            sound = Core.audio.newSound(Vars.tree.resolve("assets/sounds/egg.ogg"));
        }

        for(Turret turret : content
                .blocks()
                .copy()
                .filter(block -> block instanceof Turret)
                .map(Turret.class::cast)) {
            turrets.put(turret, turret.shootSound);
            turret.shootSound = sound;
        }

        for(Weapon weapon : content
                .units()
                .flatMap(unit -> unit.weapons)) {
            weapons.put(weapon, weapon.shootSound);
            weapon.shootSound = sound;
        }

        for(BulletType bullet : content.bullets()) {
            bullets.put(bullet, bullet.hitSound);
            bullet.hitSound = sound;
        }
    }

    public static void disable() {
        if(!active) {
            return;
        }
        active = false;

        turrets.each((turret, sound) -> turret.shootSound = sound);
        weapons.each((weapon, sound) -> weapon.shootSound = sound);
        bullets.each((bullet, sound) -> bullet.hitSound = sound);

        turrets.clear();
        weapons.clear();
        bullets.clear();
    }

    public static void setEnabled(boolean enabled) {
        if(enabled) {
            enable();
        }else {
            disable();
        }
    }
}
