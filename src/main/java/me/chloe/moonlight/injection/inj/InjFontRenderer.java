package me.chloe.moonlight.injection.inj;

import me.chloe.moonlight.module.impl.client.Media;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FontRenderer.class)
public class InjFontRenderer {

    @ModifyVariable(method = { "renderString" }, at = @At("HEAD"), ordinal = 0)
    private String renderString(final String string) {
        if(Media.INSTANCE == null) {
            return string;
        }
        return Media.INSTANCE.isEnabled() ? Media.INSTANCE.onRenderString(string) : string;
    }
}
