package me.fluffy.dactyl.module.impl.client;

import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;

public class Colors extends Module {
    public Setting<ColorMode> colorModeSetting = new Setting<ColorMode>("Mode", ColorMode.RAINBOW);

    // rainbow
    public Setting<Boolean> rollingRainbow = new Setting<Boolean>("RollingRainbow", true, vis->colorModeSetting.getValue() == ColorMode.RAINBOW);
    public Setting<Integer> rainbowSpeed = new Setting<Integer>("RainbowSpeed", 75, 1, 100, vis->colorModeSetting.getValue() == ColorMode.RAINBOW);
    public Setting<Integer> rainbowSaturation = new Setting<Integer>("Saturation", 175, 1, 255, vis->colorModeSetting.getValue() == ColorMode.RAINBOW);
    //public Setting<Integer> rainbowSaturation = new Setting<Integer>("Saturation", 175, 1, 255, vis->colorModeSetting.getValue() == ColorMode.RAINBOW);



    public static Colors INSTANCE;
    public Colors() {
        super("Colors", Category.CLIENT);
        INSTANCE = this;
    }



    private enum ColorMode {
        RAINBOW,
        RGB
    }
}
