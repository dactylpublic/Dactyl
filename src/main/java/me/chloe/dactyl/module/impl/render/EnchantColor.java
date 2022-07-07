package me.chloe.dactyl.module.impl.render;

import me.chloe.dactyl.module.Module;
import me.chloe.dactyl.setting.Setting;

public class EnchantColor extends Module {
    public Setting<Boolean> armor = new Setting<Boolean>("Armor", false);
    public Setting<Boolean> colorSync = new Setting<Boolean>("Sync", false);
    public Setting<Boolean> rainbow = new Setting<Boolean>("Rainbow", false);
    public Setting<Integer> saturation = new Setting<Integer>("Saturation", 50, 0, 100, v -> rainbow.getValue());
    public Setting<Integer> brightness = new Setting<Integer>("Brightness", 100, 0, 100, v -> rainbow.getValue());
    public Setting<Integer> speed = new Setting<Integer>("Delay", 40, 1, 100, v -> rainbow.getValue());
    public Setting<Integer> red = new Setting<Integer>("Red", 0, 0, 255, v -> !rainbow.getValue());
    public Setting<Integer> green = new Setting<Integer>("Green", 255, 0, 255, v -> !rainbow.getValue());
    public Setting<Integer> blue = new Setting<Integer>("Blue", 0, 0, 255, v -> !rainbow.getValue());
    public Setting<Integer> alpha = new Setting<Integer>("Alpha", 255, 0, 255);

    public static EnchantColor INSTANCE;
    public EnchantColor() {
        super("EnchantColor", Category.RENDER);
        INSTANCE = this;
    }
}
