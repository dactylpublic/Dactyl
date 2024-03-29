package me.chloe.moonlight.injection.inj.access;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Timer.class)
public interface ITimer {
    @Accessor("tickLength")
    public void setTickLength(float tickLength);

    @Accessor("tickLength")
    public float getTickLength();
}
