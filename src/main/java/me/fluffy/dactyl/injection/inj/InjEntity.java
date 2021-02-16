package me.fluffy.dactyl.injection.inj;

import me.fluffy.dactyl.module.impl.player.Scaffold;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class InjEntity{

    @Shadow public double posZ;
    @Shadow public double posY;
    @Shadow public double posX;
    @Shadow public float rotationYaw;


    @Shadow
    public boolean isDead;

    @Shadow
    public abstract Entity getRidingEntity();

    @Shadow
    protected abstract boolean isPassenger(Entity entity);

    @Shadow
    public void move(MoverType type, double x, double y, double z) {};

    @Shadow
    public double motionX;

    @Shadow
    public double motionY;

    @Shadow
    public double motionZ;

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z"))
    public boolean isSneaking(Entity entity) {
        return Scaffold.INSTANCE.isEnabled() || entity.isSneaking();
    }
}
