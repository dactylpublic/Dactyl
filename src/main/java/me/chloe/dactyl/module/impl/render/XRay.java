package me.chloe.dactyl.module.impl.render;

import me.chloe.dactyl.Dactyl;
import me.chloe.dactyl.module.Module;
import net.minecraft.block.Block;

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
