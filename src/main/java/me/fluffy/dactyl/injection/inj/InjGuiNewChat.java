package me.fluffy.dactyl.injection.inj;

import me.fluffy.dactyl.module.impl.misc.Chat;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiNewChat.class)
public class InjGuiNewChat {
    @Redirect(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;drawRect(IIIII)V", ordinal = 0))
    private void drawChat(int left, int top, int right, int bottom, int color) {
        if(!Chat.INSTANCE.noBorder.getValue() || !Chat.INSTANCE.isEnabled()) {
            Gui.drawRect(left, top, right, bottom, color);
        }
    }
}
