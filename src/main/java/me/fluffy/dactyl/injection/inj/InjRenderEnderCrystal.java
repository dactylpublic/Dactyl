package me.fluffy.dactyl.injection.inj;

import me.fluffy.dactyl.module.impl.client.Colors;
import me.fluffy.dactyl.module.impl.render.ESP;
import me.fluffy.dactyl.util.render.OutlineUtils;
import me.fluffy.dactyl.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEnderCrystal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(RenderEnderCrystal.class)
public abstract class InjRenderEnderCrystal {
    @Shadow
    public ModelBase modelEnderCrystal;
    @Shadow
    public ModelBase modelEnderCrystalNoBase;
    @Final
    @Shadow
    private static ResourceLocation ENDER_CRYSTAL_TEXTURES;

    @Shadow
    public abstract void doRender(final EntityEnderCrystal p0, final double p1, final double p2, final double p3, final float p4, final float p5);

    @Redirect(method = "doRender(Lnet/minecraft/entity/item/EntityEnderCrystal;DDDFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V"))
    private void doRenderOne(final ModelBase modelBase, final Entity entityIn, final float limbSwing, final float limbSwingAmount, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale) {
        if (!ESP.INSTANCE.isEnabled() || ESP.INSTANCE.renderCrystals.getValue()) {
            modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    @Redirect(method = { "doRender(Lnet/minecraft/entity/item/EntityEnderCrystal;DDDFF)V" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V", ordinal = 1))
    private void doRenderTwo(final ModelBase modelBase, final Entity entityIn, final float limbSwing, final float limbSwingAmount, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale) {
        if (!ESP.INSTANCE.isEnabled() || ESP.INSTANCE.renderCrystals.getValue()) {
            modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
    }

    @Inject(method = { "doRender(Lnet/minecraft/entity/item/EntityEnderCrystal;DDDFF)V" }, at = { @At("RETURN") }, cancellable = true)
    public void doRenderFinal(final EntityEnderCrystal entity, final double x, final double y, final double z, final float entityYaw, final float partialTicks, final CallbackInfo callbackInfo) {
        if (ESP.INSTANCE == null || (!RenderUtil.isInView(entity))) {
            return;
        }
        if(ESP.INSTANCE.isEnabled() && ESP.INSTANCE.crystals.getValue()) {
            if(ESP.INSTANCE.crystalESPMode.getValue() == ESP.ESPMode.OUTLINE) {
                final float f3 = entity.innerRotation + partialTicks;
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);
                Minecraft.getMinecraft().renderManager.renderEngine.bindTexture(InjRenderEnderCrystal.ENDER_CRYSTAL_TEXTURES);
                float f4 = MathHelper.sin(f3 * 0.2f) / 2.0f + 0.5f;
                f4 += f4 * f4;
                GL11.glLineWidth(5.0f);
                if (entity.shouldShowBottom()) {
                    this.modelEnderCrystal.render((Entity)entity, 0.0f, f3 * 3.0f, f4 * 0.2f, 0.0f, 0.0f, 0.0625f);
                }
                else {
                    this.modelEnderCrystalNoBase.render((Entity)entity, 0.0f, f3 * 3.0f, f4 * 0.2f, 0.0f, 0.0f, 0.0625f);
                }
                OutlineUtils.renderOne(ESP.INSTANCE.lineWidth.getValue().floatValue());
                if (entity.shouldShowBottom()) {
                    this.modelEnderCrystal.render((Entity)entity, 0.0f, f3 * 3.0f, f4 * 0.2f, 0.0f, 0.0f, 0.0625f);
                }
                else {
                    this.modelEnderCrystalNoBase.render((Entity)entity, 0.0f, f3 * 3.0f, f4 * 0.2f, 0.0f, 0.0f, 0.0625f);
                }
                OutlineUtils.renderTwo();
                if (entity.shouldShowBottom()) {
                    this.modelEnderCrystal.render((Entity)entity, 0.0f, f3 * 3.0f, f4 * 0.2f, 0.0f, 0.0f, 0.0625f);
                }
                else {
                    this.modelEnderCrystalNoBase.render((Entity)entity, 0.0f, f3 * 3.0f, f4 * 0.2f, 0.0f, 0.0f, 0.0625f);
                }
                OutlineUtils.renderThree();
                OutlineUtils.renderFour(ESP.INSTANCE.crystalColorSync.getValue() ? Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false)) : new Color(255, 71, 234, 255));
                if (entity.shouldShowBottom()) {
                    this.modelEnderCrystal.render((Entity)entity, 0.0f, f3 * 3.0f, f4 * 0.2f, 0.0f, 0.0f, 0.0625f);
                }
                else {
                    this.modelEnderCrystalNoBase.render((Entity)entity, 0.0f, f3 * 3.0f, f4 * 0.2f, 0.0f, 0.0f, 0.0625f);
                }
                OutlineUtils.renderFive();
                GlStateManager.popMatrix();
            } else if(ESP.INSTANCE.crystalESPMode.getValue() == ESP.ESPMode.WIREFRAME) {
                final float f3 = entity.innerRotation + partialTicks;
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);
                Minecraft.getMinecraft().renderManager.renderEngine.bindTexture(InjRenderEnderCrystal.ENDER_CRYSTAL_TEXTURES);
                float f4 = MathHelper.sin(f3 * 0.2f) / 2.0f + 0.5f;
                f4 += f4 * f4;
                GL11.glPushAttrib(1048575);
                GL11.glPolygonMode(1032, 6913);
                GL11.glDisable(3553);
                GL11.glDisable(2896);
                GL11.glDisable(2929);
                GL11.glEnable(2848);
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
                OutlineUtils.setColor(ESP.INSTANCE.crystalColorSync.getValue() ? Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false)) : new Color(255, 71, 234, 255));
                GL11.glLineWidth(ESP.INSTANCE.lineWidth.getValue().floatValue());
                if (entity.shouldShowBottom()) {
                    this.modelEnderCrystal.render((Entity)entity, 0.0f, f3 * 3.0f, f4 * 0.2f, 0.0f, 0.0f, 0.0625f);
                }
                else {
                    this.modelEnderCrystalNoBase.render((Entity)entity, 0.0f, f3 * 3.0f, f4 * 0.2f, 0.0f, 0.0f, 0.0625f);
                }
                GL11.glPopAttrib();
                GL11.glPopMatrix();
            }
        }
    }
}
