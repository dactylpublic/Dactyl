package me.fluffy.dactyl.module.impl.client;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.Bind;
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
        mc.displayGuiScreen(Dactyl.clickGUI);
    }
}
