package me.fluffy.dactyl.module.impl.misc;

import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;

public class HClip extends Module {
    public Setting<String> clipDistance = new Setting<String>("Distance", "0.5");
    public HClip() {
        super("HClip", Category.MISC);
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
        double x = Math.cos(Math.toRadians(mc.player.rotationYaw + 90F));
        double z = Math.sin(Math.toRadians(mc.player.rotationYaw + 90F));
        mc.player.setPosition(mc.player.posX + (1F * blocks * x + 0F * blocks * z), mc.player.posY, mc.player.posZ + (1F * blocks * z - 0F * blocks * x));
        this.toggle();
    }
}
