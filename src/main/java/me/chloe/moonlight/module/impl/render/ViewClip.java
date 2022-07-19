package me.chloe.moonlight.module.impl.render;

import me.chloe.moonlight.setting.Setting;
import me.chloe.moonlight.module.Module;

public class ViewClip extends Module {
    public Setting<Double> distance = new Setting<Double>("Distance", 4.0d, 1.0d, 10.0d);
    public static ViewClip INSTANCE;
    public ViewClip() {
        super("ViewClip", Category.RENDER);
        INSTANCE = this;
    }
}
