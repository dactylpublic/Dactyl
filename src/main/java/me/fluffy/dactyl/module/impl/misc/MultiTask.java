package me.fluffy.dactyl.module.impl.misc;

import me.fluffy.dactyl.module.Module;

public class MultiTask extends Module {

    public static MultiTask INSTANCE;
    public MultiTask() {
        super("MultiTask", Category.MISC);
        INSTANCE = this;
    }
}
