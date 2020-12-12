package me.fluffy.dactyl.injection.inj;

import me.fluffy.dactyl.event.impl.network.PlayerDisconnectEvent;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class InjNetHandlerPlayClient {

    @Inject(method="onDisconnect", at=@At(value="HEAD"))
    private void onDisconnect(ITextComponent iTextComponent, CallbackInfo callbackInfo) {
        MinecraftForge.EVENT_BUS.post(new PlayerDisconnectEvent(iTextComponent.getUnformattedText()));
    }
}
