package me.fluffy.dactyl.injection.inj;

import me.fluffy.dactyl.module.impl.render.Chams;
import me.fluffy.dactyl.module.impl.render.ESP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RenderLivingBase.class, priority = Integer.MAX_VALUE)
public abstract class InjRenderLivingBase<T extends EntityLivingBase> extends Render<T> {
    public InjRenderLivingBase(RenderManager renderManagerIn, ModelBase modelBaseIn, float shadowSizeIn) {
        super(renderManagerIn);
    }

    @Redirect(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V"))
    private void onRenderModel(ModelBase modelBase, Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if(Chams.INSTANCE.isEnabled()) {
            if (Chams.INSTANCE.onRenderEntity(modelBase, entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale)) {
                return;
            }
        }
        if (ESP.INSTANCE.isEnabled()) {
            if (ESP.INSTANCE.onRenderEntity(modelBase, entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale)) {
                return;
            }
        }
        modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    }
}
