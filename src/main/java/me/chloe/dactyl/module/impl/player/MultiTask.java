package me.chloe.dactyl.module.impl.player;

import me.chloe.dactyl.module.Module;

public class MultiTask extends Module {

    public static MultiTask INSTANCE;
    public MultiTask() {
        super("MultiTask", Category.PLAYER);
        INSTANCE = this;
    }
}
