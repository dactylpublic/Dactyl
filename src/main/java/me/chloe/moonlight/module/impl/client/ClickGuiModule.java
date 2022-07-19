package me.chloe.moonlight.module.impl.client;

import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.setting.Setting;
import me.chloe.moonlight.util.Bind;
import me.chloe.moonlight.module.Module;
import org.lwjgl.input.Keyboard;

public class ClickGuiModule extends Module {
    public Setting<Boolean> gradientGui = new Setting<Boolean>("GradientGUI", true);
    public static ClickGuiModule INSTANCE;
    public ClickGuiModule() {
        super("ClickGui", Category.CLIENT, true);
        this.getSettingsList().get(0).setValue(new Bind(Keyboard.KEY_RSHIFT));
        INSTANCE = this;
    }

    @Override
    public void onToggle() {
        mc.displayGuiScreen(Moonlight.clickGUI);
    }
}
