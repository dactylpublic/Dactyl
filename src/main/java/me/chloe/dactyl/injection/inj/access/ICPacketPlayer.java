package me.chloe.dactyl.injection.inj.access;

import net.minecraft.network.play.client.CPacketPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CPacketPlayer.class)
public interface ICPacketPlayer {
    @Accessor("yaw")
    public void setYaw(float yaw);

    @Accessor("pitch")
    public void setPitch(float pitch);
}
