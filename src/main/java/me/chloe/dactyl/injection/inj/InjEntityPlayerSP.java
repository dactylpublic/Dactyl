package me.chloe.dactyl.injection.inj;

import com.mojang.authlib.GameProfile;
import me.chloe.dactyl.event.ForgeEvent;
import me.chloe.dactyl.event.impl.player.EventUpdateWalkingPlayer;
import me.chloe.dactyl.event.impl.player.MoveEvent;
import me.chloe.dactyl.event.impl.player.PlayerMotionEvent;
import me.chloe.dactyl.event.impl.world.BlockPushEvent;
import me.chloe.dactyl.module.impl.render.NoRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.MoverType;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayerSP.class)
public class InjEntityPlayerSP extends AbstractClientPlayer {
    private EventUpdateWalkingPlayer eventUpdateWalkingPlayer;

    public InjEntityPlayerSP(World p_i45074_1_, GameProfile p_i45074_2_) {
        super(p_i45074_1_, p_i45074_2_);
    }

    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;closeScreen()V"))
    public void closeScreen(EntityPlayerSP entityPlayerSP) {
        if(NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.portalAnim.getValue()) {
            return;
        }
    }

    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V"))
    public void closeScreen(Minecraft minecraft, GuiScreen screen) {
        if(NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.portalAnim.getValue()) {
            return;
        }
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"))
    private void onUpdateWalkingPlayerPre(CallbackInfo callbackInfo) {
        eventUpdateWalkingPlayer = new EventUpdateWalkingPlayer(ForgeEvent.Stage.PRE, Minecraft.getMinecraft().player.rotationYaw, Minecraft.getMinecraft().player.rotationPitch);
        MinecraftForge.EVENT_BUS.post(eventUpdateWalkingPlayer);
    }

    @Redirect(method = "onUpdateWalkingPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/entity/EntityPlayerSP;rotationYaw:F"))
    private float onUpdateWalkingPlayerRotationYaw(EntityPlayerSP player) {
        return eventUpdateWalkingPlayer.getYaw();
    }

    @Redirect(method = "onUpdateWalkingPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/entity/EntityPlayerSP;rotationPitch:F"))
    private float onUpdateWalkingPlayerRotationPitch(EntityPlayerSP player) {
        return eventUpdateWalkingPlayer.getPitch();
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("RETURN"))
    private void onUpdateWalkingPlayerPost(CallbackInfo callbackInfo) {
        EventUpdateWalkingPlayer event = new EventUpdateWalkingPlayer(ForgeEvent.Stage.POST);
        MinecraftForge.EVENT_BUS.post(event);
    }

    @Inject(method = "move", at = @At(value = "HEAD"))
    public void onMotion(CallbackInfo callbackInfo) {
        MinecraftForge.EVENT_BUS.post(new PlayerMotionEvent());
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;move(Lnet/minecraft/entity/MoverType;DDD)V"))
    public void move(AbstractClientPlayer player, MoverType type, double x, double y, double z) {
        MoveEvent moveEvent = new MoveEvent(x, y, z, ForgeEvent.Stage.PRE);
        MinecraftForge.EVENT_BUS.post(moveEvent);
        super.move(type, moveEvent.getX(), moveEvent.getY(), moveEvent.getZ());
    }

    @Inject(method = "pushOutOfBlocks",at = @At("HEAD"),cancellable = true)
    private void onPushOutOfBlocks(double x, double y, double z, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        BlockPushEvent eventPushOutOfBlocks = new BlockPushEvent();
        MinecraftForge.EVENT_BUS.post(eventPushOutOfBlocks);

        if(eventPushOutOfBlocks.isCanceled()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }
}
