package me.fluffy.dactyl.module.impl.render;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;

public class XRay extends Module {
    //public Setting<Double> opacity = new Setting<Double>("Opacity", 175d, 25d, 255d);

    public static XRay INSTACE;
    public XRay() {
        super("XRay", Category.RENDER);
        INSTACE = this;
    }

    @Override
    public void onToggle() {
        mc.renderGlobal.loadRenderers();
    }

    public boolean shouldRender(Block block) {
        return Dactyl.xRayManager.hasBlock(block);
    }
}
