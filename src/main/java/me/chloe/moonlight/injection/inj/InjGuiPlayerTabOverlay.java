package me.chloe.moonlight.injection.inj;

import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.module.impl.client.Colors;
import me.chloe.moonlight.module.impl.misc.ExtraTab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.util.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.awt.*;
import java.util.List;

@Mixin(value = GuiPlayerTabOverlay.class, priority = 200000)
public class InjGuiPlayerTabOverlay {
    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Ljava/util/List;subList(II)Ljava/util/List;"))
    public List subList(List list, int fromIndex, int toIndex) {
        return list.subList(fromIndex, ExtraTab.INSTANCE.isEnabled() ? Math.min(240, list.size()) : toIndex);
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    public int renderPlayerName(FontRenderer renderer, String text, float x, float y, int color) {
        int drawColor = -1;
        String username = StringUtils.stripControlCodes(text);
        if(username.startsWith("§")) {
            username = username.substring(2);
        }
        if(ExtraTab.INSTANCE.isEnabled() && Moonlight.friendManager.isFriend(username) && ExtraTab.INSTANCE.friends.getValue()) {
            if(ExtraTab.INSTANCE.colorSync.getValue()) {
                drawColor = Colors.INSTANCE.getColor(1, false);
            } else {
                drawColor = new Color(ExtraTab.INSTANCE.red.getValue(), ExtraTab.INSTANCE.green.getValue(), ExtraTab.INSTANCE.blue.getValue(), 255).getRGB();
            }
        }
        //ChatUtil.printMsg(text, true, false);
        return Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x, y, drawColor);
    }

    /*@Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
    public void getPlayerName(NetworkPlayerInfo networkPlayerInfoIn, CallbackInfoReturnable returnable) {
        if (ExtraTab.INSTANCE.isEnabled()) {
            returnable.cancel();
            returnable.setReturnValue(ExtraTab.getPlayerName(networkPlayerInfoIn));
        }
    }*/
}
