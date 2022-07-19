package me.chloe.moonlight.module.impl.movement;

import me.chloe.moonlight.setting.Setting;
import me.chloe.moonlight.module.Module;
import net.minecraft.init.Blocks;

public class IceSpeed extends Module {
    public Setting<Double> slipperiness = new Setting<Double>("Slippy", 0.4d, 0.2d, 1.5d);
    public IceSpeed() {
        super("IceSpeed", Category.MOVEMENT);
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null || Blocks.ICE == null || Blocks.PACKED_ICE == null || Blocks.FROSTED_ICE == null) {
            return;
        }
        Blocks.ICE.slipperiness = this.slipperiness.getValue().floatValue();
        Blocks.PACKED_ICE.slipperiness = this.slipperiness.getValue().floatValue();
        Blocks.FROSTED_ICE.slipperiness = this.slipperiness.getValue().floatValue();
    }

    @Override
    public void onDisable() {
        if(mc.player == null || mc.world == null || Blocks.ICE == null || Blocks.PACKED_ICE == null || Blocks.FROSTED_ICE == null) {
            return;
        }
        Blocks.ICE.slipperiness = 0.98f;
        Blocks.PACKED_ICE.slipperiness = 0.98f;
        Blocks.FROSTED_ICE.slipperiness = 0.98f;
    }
}
