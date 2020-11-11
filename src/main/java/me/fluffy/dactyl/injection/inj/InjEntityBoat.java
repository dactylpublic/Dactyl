package me.fluffy.dactyl.injection.inj;

import me.fluffy.dactyl.module.impl.movement.BoatFly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityBoat.class)
public abstract class InjEntityBoat extends InjEntity {

    @Shadow
    public abstract double getMountedYOffset();

    @Inject(method = {"updateMotion"}, at = {@At("RETURN")})
    private void updateMotion(CallbackInfo info) {
        if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.isRiding() && this.getRidingEntity() == Minecraft.getMinecraft().player && BoatFly.INSTANCE.isEnabled()) {
            info.cancel();
        }
    }

    @Inject(method = "applyOrientationToEntity", at = @At("HEAD"), cancellable = true)
    private void applyOrientationToEntity(Entity passenger, CallbackInfo info) {
        if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.isRiding() && Minecraft.getMinecraft().player == passenger && BoatFly.INSTANCE.isEnabled()) {
            info.cancel();
        }
    }

    @Inject(method = "controlBoat", at = @At("HEAD"), cancellable = true)
    private void controlBoat(CallbackInfo info) {
        if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.isRiding() && this.getRidingEntity() == Minecraft.getMinecraft().player && BoatFly.INSTANCE.isEnabled()) {
            info.cancel();
        }
    }

    @Inject(method = "updatePassenger", at = @At("HEAD"), cancellable = true)
    private void updatePassenger(Entity passenger, CallbackInfo info) {
        if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.isRiding() && Minecraft.getMinecraft().player == passenger && BoatFly.INSTANCE.isEnabled()) {
            info.cancel();
            float f = 0.0F;
            float f1 = (float) ((this.isDead ? 0.009999999776482582D : getMountedYOffset()) + passenger.getYOffset());
            Vec3d vec3d = (new Vec3d(f, 0.0D, 0.0D)).rotateYaw(-this.rotationYaw * 0.017453292F - 1.5707964F);
            passenger.setPosition(this.posX + vec3d.x, this.posY + f1, this.posZ + vec3d.z);
        }
    }
}
