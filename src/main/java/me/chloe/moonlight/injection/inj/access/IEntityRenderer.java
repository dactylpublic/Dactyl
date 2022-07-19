package me.chloe.moonlight.injection.inj.access;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface IEntityRenderer {
    @Invoker("orientCamera")
    public void orient(float partialTicks);
}
