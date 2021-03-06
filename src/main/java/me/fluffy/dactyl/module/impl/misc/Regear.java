package me.fluffy.dactyl.module.impl.misc;

import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.TimeUtil;

public class Regear extends Module {
    public Setting<Integer> delay = new Setting<Integer>("Delay", 100, 1, 1000);
    public Regear() {
        super("Regear", Category.MISC);
    }

    private final TimeUtil timer = new TimeUtil();
}
