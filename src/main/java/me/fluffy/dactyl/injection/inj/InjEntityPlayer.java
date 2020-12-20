package me.fluffy.dactyl.injection.inj;

import me.fluffy.dactyl.event.impl.player.PlayerTravelEvent;
import me.fluffy.dactyl.event.impl.world.CollisionAppliedEvent;
import me.fluffy.dactyl.event.impl.world.WaterPushEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public abstract class InjEntityPlayer extends InjEntity {
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void travel(float strafe, float vertical, float forward, CallbackInfo info) {
        PlayerTravelEvent event = new PlayerTravelEvent(strafe, vertical, forward);
        MinecraftForge.EVENT_BUS.post(event);
        if(event.isCanceled()) {
            move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            info.cancel();
        }
    }

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
