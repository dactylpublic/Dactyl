package me.chloe.moonlight.module.impl.player;

import me.chloe.moonlight.module.Module;

public class MultiTask extends Module {

    public static MultiTask INSTANCE;
    public MultiTask() {
        super("MultiTask", Category.PLAYER);
        INSTANCE = this;
    }
}
