package me.chloe.dactyl.module.impl.movement;

import me.chloe.dactyl.module.Module;

public class AntiLevitation extends Module {
    public static AntiLevitation INSTANCE;
    public AntiLevitation() {
        super("AntiLevitation", Category.MOVEMENT);
        INSTANCE = this;
    }
}
