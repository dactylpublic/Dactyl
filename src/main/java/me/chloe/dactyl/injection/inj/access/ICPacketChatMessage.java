package me.chloe.dactyl.injection.inj.access;

import net.minecraft.network.play.client.CPacketChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CPacketChatMessage.class)
public interface ICPacketChatMessage {
    @Accessor("message")
    public void setMessage(String message);
}
