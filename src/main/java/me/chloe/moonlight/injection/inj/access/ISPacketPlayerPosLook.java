package me.chloe.moonlight.injection.inj.access;

import net.minecraft.network.play.server.SPacketPlayerPosLook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketPlayerPosLook.class)
public interface ISPacketPlayerPosLook {
    @Accessor("yaw")
    public void setYaw(float yaw);
    @Accessor("pitch")
    public void setPitch(float pitch);
}
