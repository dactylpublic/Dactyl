package me.fluffy.dactyl.module.impl.render;

import me.fluffy.dactyl.event.impl.world.Render3DEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.CombatUtil;
import me.fluffy.dactyl.util.render.RenderUtil;

import java.awt.*;

public class HoleESP extends Module {
    Setting<OutlineStyle> outlineStyleSetting = new Setting<OutlineStyle>("LineStyle", OutlineStyle.OUTLINE);
    Setting<Double> renderRange = new Setting<Double>("RenderRange", 8.0d, 1.0d, 20.0d);
    Setting<Double> yTranslate = new Setting<Double>("TranslateY", 1.0d, 0.1d, 3.0d);
    Setting<Double> yExtrude = new Setting<Double>("ExtrudeY", 1.0d, 0.1d, 3.0d);
    public HoleESP() {
        super("HoleESP", Category.RENDER);
    }


    @Override
    public void onRender3D(Render3DEvent event) {
        //RenderUtil.drawBoxESP(crystalRender, new Color(0, 13, 255, 255), 1.5f, true, true, 40);
    }


    private enum OutlineStyle {
        FILL,
        OUTLINE,
        NONE
    }
}
