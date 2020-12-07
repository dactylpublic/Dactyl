package me.fluffy.dactyl.module.impl.movement;

import me.fluffy.dactyl.module.Module;
import net.minecraft.init.MobEffects;

public class AntiLevitation extends Module {
    public static AntiLevitation INSTANCE;
    public AntiLevitation() {
        super("AntiLevitation", Category.MOVEMENT);
        INSTANCE = this;
    }
}
