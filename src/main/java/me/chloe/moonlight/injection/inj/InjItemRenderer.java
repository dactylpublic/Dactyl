package me.chloe.moonlight.injection.inj;

import me.chloe.moonlight.module.impl.render.NoRender;
import me.chloe.moonlight.module.impl.render.ViewModel;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class InjItemRenderer {
    @Inject(method = { "renderFireInFirstPerson" }, at = { @At("HEAD") }, cancellable = true)
    public void renderFireInFirstPersonHook(final CallbackInfo info) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.fire.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = { "renderSuffocationOverlay" }, at = { @At("HEAD") }, cancellable = true)
    public void renderSuffocationOverlay(final CallbackInfo ci) {
        if (NoRender.INSTANCE.isEnabled() && NoRender.INSTANCE.noBlocks.getValue()) {
            ci.cancel();
        }
    }

    @Inject(method = "updateEquippedItem", at = @At("HEAD"), cancellable = true)
    public void updateEquippedItem(CallbackInfo callbackInfo) {
        ViewModel.INSTANCE.updateEquipped();
    }

    @Inject(method={"renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/ItemRenderer;renderItemSide(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;Z)V")})
    private void doItemModelTransforms(AbstractClientPlayer player, float p_187457_2_, float p_187457_3_, EnumHand hand, float p_187457_5_, ItemStack stack, float p_187457_7_, CallbackInfo ci) {
        if(ViewModel.INSTANCE.isEnabled()) {
            GlStateManager.scale(ViewModel.INSTANCE.scaleX.getValue(), ViewModel.INSTANCE.scaleY.getValue(), ViewModel.INSTANCE.scaleZ.getValue());
            if (player.isHandActive() && player.getItemInUseCount() > 0 && player.getActiveHand() == hand) {
                if (stack.getItemUseAction() == EnumAction.EAT || stack.getItemUseAction() == EnumAction.DRINK) {
                    return;
                }
            }
            GlStateManager.translate(ViewModel.INSTANCE.translateX.getValue(), ViewModel.INSTANCE.translateY.getValue(), ViewModel.INSTANCE.translateZ.getValue());
        }
    }
}
