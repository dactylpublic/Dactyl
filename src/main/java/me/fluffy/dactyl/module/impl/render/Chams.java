package me.fluffy.dactyl.module.impl.render;

import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;

public class Chams extends Module {
    public Setting<ChamsPage> page = new Setting<ChamsPage>("Setting", ChamsPage.CRYSTALS);

    // crystals
    public Setting<CrystalMode> crystalESPMode = new Setting<CrystalMode>("CMode", CrystalMode.WIREFRAME, v->page.getValue() == ChamsPage.CRYSTALS);
    public Setting<Boolean> crystals = new Setting<Boolean>("Crystals", true, v->page.getValue() == ChamsPage.CRYSTALS);
    public Setting<Boolean> renderCrystals = new Setting<Boolean>("RenderCrystals", false, v->page.getValue() == ChamsPage.CRYSTALS);
    public Setting<Double> lineWidth = new Setting<Double>("Width", 1.5d, 0.1d, 3.0d, v->page.getValue() == ChamsPage.CRYSTALS);
    public Setting<Boolean> extraOutline = new Setting<Boolean>("ExtraOutline", true, v->page.getValue() == ChamsPage.CRYSTALS&&crystalESPMode.getValue() == CrystalMode.WIREFRAME);
    public Setting<Boolean> crystalChamFill = new Setting<Boolean>("CrystalFill", true, v->page.getValue() == ChamsPage.CRYSTALS&&crystalESPMode.getValue() == CrystalMode.WIREFRAME);
    public Setting<Integer> crystalFillOpacity = new Setting<Integer>("FillAlpha", 100, 1, 255, v->page.getValue() == ChamsPage.CRYSTALS&&crystalESPMode.getValue() == CrystalMode.WIREFRAME&&crystalChamFill.getValue());
    public Setting<Boolean> crystalColorSync = new Setting<Boolean>("CrystalCSync", true, v->page.getValue() == ChamsPage.CRYSTALS);

    public static Chams INSTANCE;
    public Chams() {
        super("Chams", Category.RENDER);
        INSTANCE = this;
    }

    public enum CrystalMode {
        OUTLINE,
        WIREFRAME
    }


    public enum ChamsPage {
        PLAYERS,
        CRYSTALS
    }
}
