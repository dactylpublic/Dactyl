package me.chloe.dactyl.injection.inj;

import me.chloe.dactyl.module.impl.render.EnchantColor;
import me.chloe.dactyl.util.render.RenderUtil;
import me.chloe.dactyl.module.impl.client.Colors;
import me.chloe.dactyl.module.impl.render.HandColor;
import me.chloe.dactyl.module.impl.render.NoRender;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(LayerArmorBase.class)
public class InjLayerArmorBase {
    @Inject(method = "renderArmorLayer", at = @At("HEAD"), cancellable = true)
    public void renderArmorLayer(EntityLivingBase p_Entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slotIn, CallbackInfo callbackInfo) {
        if(NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.noArmor.getValue()) {
            callbackInfo.cancel();
        }
    }

    @Redirect(method = { "renderEnchantedGlint" }, at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/GlStateManager.color(FFFF)V"))
    private static void renderEnchantedGlint(float a2, float a3, float a4, float v1) {
        if (EnchantColor.INSTANCE != null && EnchantColor.INSTANCE.isEnabled() && EnchantColor.INSTANCE.armor.getValue()) {
            if (HandColor.INSTANCE.rainbow.getValue()) {
                Color rainbowColor = HandColor.INSTANCE.colorSync.getValue() ? Colors.INSTANCE.getCurrentColor() : new Color(RenderUtil.getRainbow(HandColor.INSTANCE.speed.getValue() * 100, 0, HandColor.INSTANCE.saturation.getValue() / 100.0F, HandColor.INSTANCE.brightness.getValue() / 100.0F));
                a2 = rainbowColor.getRed();
                a4 = rainbowColor.getBlue();
                a3 = rainbowColor.getGreen();
                v1 = rainbowColor.getAlpha();
            } else {
                Color color = HandColor.INSTANCE.colorSync.getValue() ? new Color(Colors.INSTANCE.getCurrentColor().getRed(), Colors.INSTANCE.getCurrentColor().getBlue(), Colors.INSTANCE.getCurrentColor().getGreen(), HandColor.INSTANCE.alpha.getValue()) : new Color(HandColor.INSTANCE.red.getValue(), HandColor.INSTANCE.green.getValue(), HandColor.INSTANCE.blue.getValue(), HandColor.INSTANCE.alpha.getValue());
                a2 = color.getRed();
                a4 = color.getBlue();
                a3 = color.getGreen();
                v1 = color.getAlpha();
            }
        }
        GlStateManager.color(a2, a3, a4, v1);
    }
}
