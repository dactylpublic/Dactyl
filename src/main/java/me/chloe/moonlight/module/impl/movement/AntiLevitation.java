package me.chloe.moonlight.module.impl.movement;

import me.chloe.moonlight.module.Module;

public class AntiLevitation extends Module {
    public static AntiLevitation INSTANCE;
    public AntiLevitation() {
        super("AntiLevitation", Category.MOVEMENT);
        INSTANCE = this;
    }
}
