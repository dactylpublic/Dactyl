package me.fluffy.dactyl.module.impl.client;

import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;

public class HUD extends Module {

    public Setting<Boolean> customFont = new Setting<Boolean>("CustomFont", true);


    public static HUD INSTANCE;
    public HUD() {
        super("HUD", Category.CLIENT, true);
        INSTANCE = this;
    }



    private enum SettingPage {
        GENERAL,
        PLAYER,
        PVP,

    }
}
