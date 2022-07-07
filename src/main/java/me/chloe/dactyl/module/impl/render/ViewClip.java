package me.chloe.dactyl.module.impl.render;

import me.chloe.dactyl.setting.Setting;
import me.chloe.dactyl.module.Module;

public class ViewClip extends Module {
    public Setting<Double> distance = new Setting<Double>("Distance", 4.0d, 1.0d, 10.0d);
    public static ViewClip INSTANCE;
    public ViewClip() {
        super("ViewClip", Category.RENDER);
        INSTANCE = this;
    }
}
