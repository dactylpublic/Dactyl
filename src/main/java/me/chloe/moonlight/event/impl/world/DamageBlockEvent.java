package me.chloe.moonlight.event.impl.world;

import me.chloe.moonlight.event.ForgeEvent;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class DamageBlockEvent extends ForgeEvent {
    private final BlockPos pos;

    private final EnumFacing facing;

    public DamageBlockEvent(BlockPos pos, EnumFacing facing) {
        this.setStage(Stage.PRE);
        this.pos = pos;
        this.facing = facing;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public EnumFacing getFacing() {
        return this.facing;
    }
}
