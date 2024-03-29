package me.chloe.moonlight.module.impl.misc;

import me.chloe.moonlight.setting.Setting;
import me.chloe.moonlight.module.Module;

public class ExtraTab extends Module {
    public Setting<Boolean> friends = new Setting<Boolean>("Friends", true);
    public Setting<Boolean> colorSync = new Setting<Boolean>("ColorSync", false, v->friends.getValue());
    public Setting<Integer> red = new Setting<Integer>("Red", 5, 1, 255, v->!colorSync.getValue()&&friends.getValue());
    public Setting<Integer> green = new Setting<Integer>("Green", 5, 1, 255, v->!colorSync.getValue()&&friends.getValue());
    public Setting<Integer> blue = new Setting<Integer>("Blue", 5, 1, 255, v->!colorSync.getValue()&&friends.getValue());
    public static ExtraTab INSTANCE;
    public ExtraTab() {
        super("ExtraTab", Category.MISC);
        INSTANCE = this;
    }
}
