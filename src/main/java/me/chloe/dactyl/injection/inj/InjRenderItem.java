package me.chloe.dactyl.injection.inj;

import me.chloe.dactyl.module.impl.client.Colors;
import me.chloe.dactyl.module.impl.render.EnchantColor;
import me.chloe.dactyl.module.impl.render.HandColor;
import me.chloe.dactyl.util.render.RenderUtil;
import net.minecraft.client.renderer.RenderItem;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.awt.*;

@Mixin(RenderItem.class)
public class InjRenderItem {
    @ModifyArg(method = { "renderEffect" }, at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/RenderItem.renderModel(Lnet/minecraft/client/renderer/block/model/IBakedModel;I)V"), index = 1)
    private int renderModel(final int a1) {
        if (EnchantColor.INSTANCE != null && EnchantColor.INSTANCE.isEnabled()) {
            if (HandColor.INSTANCE.rainbow.getValue()) {
                Color rainbowColor = HandColor.INSTANCE.colorSync.getValue() ? Colors.INSTANCE.getCurrentColor() : new Color(RenderUtil.getRainbow(HandColor.INSTANCE.speed.getValue() * 100, 0, HandColor.INSTANCE.saturation.getValue() / 100.0F, HandColor.INSTANCE.brightness.getValue() / 100.0F));
                return rainbowColor.getRGB();
            } else {
                Color color = HandColor.INSTANCE.colorSync.getValue() ? new Color(Colors.INSTANCE.getCurrentColor().getRed(), Colors.INSTANCE.getCurrentColor().getBlue(), Colors.INSTANCE.getCurrentColor().getGreen(), HandColor.INSTANCE.alpha.getValue()) : new Color(HandColor.INSTANCE.red.getValue(), HandColor.INSTANCE.green.getValue(), HandColor.INSTANCE.blue.getValue(), HandColor.INSTANCE.alpha.getValue());
                return color.getRGB();
            }
        }
        return a1;
    }
}
