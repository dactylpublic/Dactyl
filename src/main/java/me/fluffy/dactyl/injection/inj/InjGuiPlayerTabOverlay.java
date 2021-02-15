package me.fluffy.dactyl.injection.inj;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.module.impl.client.Colors;
import me.fluffy.dactyl.module.impl.misc.ExtraTab;
import me.fluffy.dactyl.util.ChatUtil;
import me.fluffy.dactyl.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.awt.*;
import java.util.List;

@Mixin(GuiPlayerTabOverlay.class)
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
        if(ExtraTab.INSTANCE.isEnabled() && Dactyl.friendManager.isFriend(username) && ExtraTab.INSTANCE.friends.getValue()) {
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
