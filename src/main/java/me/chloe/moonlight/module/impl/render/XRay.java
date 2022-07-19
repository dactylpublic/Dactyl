package me.chloe.moonlight.module.impl.render;

import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.module.Module;
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
        return Moonlight.xRayManager.hasBlock(block);
    }
}
