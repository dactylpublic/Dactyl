package me.chloe.moonlight.module.impl.misc;

import me.chloe.moonlight.module.Module;
import me.chloe.moonlight.setting.Setting;

public class VClip extends Module {
    public Setting<String> clipDistance = new Setting<String>("Distance", "2");
    public VClip() {
        super("VClip", Category.MISC);
    }

    @Override
    public void onEnable() {
        if(mc.player == null || mc.world == null) {
            this.toggle();
            return;
        }
        double blocks = 0;
        try {
            blocks = Double.parseDouble(clipDistance.getValue());
        } catch(Exception exception) {
            this.toggleWithReason("Amount is invalid");
            return;
        }
        mc.player.setEntityBoundingBox(mc.player.getEntityBoundingBox().offset(0, blocks, 0));
        this.toggle();
    }
}
