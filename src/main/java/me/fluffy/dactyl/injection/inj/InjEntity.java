package me.fluffy.dactyl.injection.inj;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

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
}
