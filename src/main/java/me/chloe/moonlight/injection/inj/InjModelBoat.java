package me.chloe.moonlight.injection.inj;

import me.chloe.moonlight.module.impl.movement.BoatFly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBoat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBoat.class)
public class InjModelBoat {
    @Inject(method = "render", at = @At("HEAD"))
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo info) {
        if(BoatFly.INSTANCE == null) {
            return;
        }
        if(Minecraft.getMinecraft().player.getRidingEntity() == entityIn && BoatFly.INSTANCE.isEnabled()) {
            GlStateManager.color(1.0f, 1.0f, 1.0f, BoatFly.INSTANCE.boatOpacity.getValue().floatValue());
            GlStateManager.enableBlend();
        }
    }
}
