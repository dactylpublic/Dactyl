package me.chloe.dactyl.injection.inj;

import io.netty.channel.ChannelHandlerContext;
import me.chloe.dactyl.event.ForgeEvent;
import me.chloe.dactyl.event.impl.network.NetworkExceptionEvent;
import me.chloe.dactyl.event.impl.network.PacketEvent;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class InjNetworkManager {
    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        PacketEvent e = new PacketEvent(ForgeEvent.Stage.PRE, packet, PacketEvent.PacketType.OUTGOING);
        MinecraftForge.EVENT_BUS.post(e);
        if(e.isCanceled())
            ci.cancel();
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void onChannelRead(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        PacketEvent e = new PacketEvent(ForgeEvent.Stage.PRE, packet, PacketEvent.PacketType.INCOMING);
        MinecraftForge.EVENT_BUS.post(e);
        if(e.isCanceled())
            ci.cancel();
    }

    @Inject(method = "exceptionCaught", at = @At("HEAD"), cancellable = true)
    public void exception(ChannelHandlerContext p_exceptionCaught_1_, Throwable p_exceptionCaught_2_, CallbackInfo ci) {
        NetworkExceptionEvent exceptionEvent = new NetworkExceptionEvent();
        if(exceptionEvent.isCanceled()) {
            ci.cancel();
        }
    }
}
