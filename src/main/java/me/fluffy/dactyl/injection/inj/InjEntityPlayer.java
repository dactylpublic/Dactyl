package me.fluffy.dactyl.injection.inj;

import me.fluffy.dactyl.event.impl.world.CollisionAppliedEvent;
import me.fluffy.dactyl.event.impl.world.WaterPushEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public class InjEntityPlayer {
    @Inject(method = "isPushedByWater()Z", at = @At("HEAD"), cancellable = true)
    public void isPushedByWater(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        WaterPushEvent eventPlayerPushedByWater = new WaterPushEvent();
        MinecraftForge.EVENT_BUS.post(eventPlayerPushedByWater);
        if (eventPlayerPushedByWater.isCanceled()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    @Inject(method = "applyEntityCollision", at = @At("HEAD"), cancellable = true)
    public void applyEntityCollision(Entity entity, CallbackInfo callbackInfo) {
        CollisionAppliedEvent collisionAppliedEvent = new CollisionAppliedEvent(entity);
        MinecraftForge.EVENT_BUS.post(collisionAppliedEvent);
        if (collisionAppliedEvent.isCanceled()) {
            callbackInfo.cancel();
        }
    }
}
