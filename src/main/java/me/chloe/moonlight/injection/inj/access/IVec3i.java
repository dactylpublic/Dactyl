package me.chloe.moonlight.injection.inj.access;

import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Vec3i.class)
public interface IVec3i {
    @Accessor("x")
    public int getX();

    @Accessor("y")
    public int getY();

    @Accessor("z")
    public int getZ();
}
