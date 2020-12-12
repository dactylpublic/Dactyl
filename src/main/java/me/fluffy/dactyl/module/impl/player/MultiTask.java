package me.fluffy.dactyl.module.impl.player;

import me.fluffy.dactyl.module.Module;

public class MultiTask extends Module {

    public static MultiTask INSTANCE;
    public MultiTask() {
        super("MultiTask", Category.PLAYER);
        INSTANCE = this;
    }
}
