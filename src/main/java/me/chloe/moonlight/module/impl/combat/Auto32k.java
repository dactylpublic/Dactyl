package me.chloe.moonlight.module.impl.combat;

import me.chloe.moonlight.module.Module;
import me.chloe.moonlight.setting.Setting;

public class Auto32k extends Module {
    public Setting<ThirtyTwoKMode> mode = new Setting<ThirtyTwoKMode>("Mode", ThirtyTwoKMode.HOPPER);
    public Setting<Double> placeRange = new Setting<Double>("PlaceRange", 4.0d, 1.0d, 6.0d);
    public Auto32k() {
        super("Auto32k", Category.COMBAT);
    }


    private enum ThirtyTwoKMode {
        HOPPER,
        DISPENSER
    }
}
