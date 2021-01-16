package me.fluffy.dactyl.injection.inj.access;

import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketPlayerPosLook.class)
public interface ISPacketPlayerPosLook {
    @Accessor("yaw")
    public void setYaw(float yaw);
    @Accessor("pitch")
    public void setPitch(float pitch);

    @Accessor("x")
    public void setX(double x);
    @Accessor("z")
    public void setZ(double z);
}
