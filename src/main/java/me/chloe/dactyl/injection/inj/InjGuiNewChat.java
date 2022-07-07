package me.chloe.dactyl.injection.inj;

import me.chloe.dactyl.Dactyl;
import me.chloe.dactyl.module.impl.client.Colors;
import me.chloe.dactyl.module.impl.client.Media;
import me.chloe.dactyl.module.impl.misc.Chat;
import me.chloe.dactyl.module.impl.misc.ChatTimestamps;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = GuiNewChat.class, priority = 200000)
public class InjGuiNewChat {
    @Redirect(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;drawRect(IIIII)V", ordinal = 0))
    private void drawChat(int left, int top, int right, int bottom, int color) {
        if(!Chat.INSTANCE.noBorder.getValue() || !Chat.INSTANCE.isEnabled()) {
            Gui.drawRect(left, top, right, bottom, color);
        }
    }

    @Redirect(method = "getChatComponent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;getStringWidth(Ljava/lang/String;)I", ordinal = 0))
    public int getChatComponent(FontRenderer fontRenderer, String text) {
        if(Chat.INSTANCE.isEnabled() && Chat.INSTANCE.customFont.getValue()) {
            return (int) Dactyl.fontUtil.getStringWidth(Media.INSTANCE.onRenderString(text));
        }
        return fontRenderer.getStringWidth(Media.INSTANCE.onRenderString(text));
    }

    @Redirect(method = "drawChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I", ordinal = 0))
    public int drawStringWithShadow(FontRenderer fontRenderer, String text, float x, float y, int color) {
        if(Chat.INSTANCE.isEnabled()) {
            if (Chat.INSTANCE.customFont.getValue()) {
                if (Chat.INSTANCE.gradientWatermark.getValue() && StringUtils.stripControlCodes(text).startsWith("<Dactyl>")) {
                    String watermark = "<Dactyl> ";
                    String restOfText = text.replace(TextFormatting.DARK_BLUE + "<" + TextFormatting.AQUA + "Dactyl" + TextFormatting.DARK_BLUE + ">" + TextFormatting.RESET + " ", "");
                    char[] characters = watermark.toCharArray();
                    float currentX = x;
                    for (char ch : characters) {
                        Dactyl.fontUtil.drawStringFloatShadow(String.valueOf(ch), currentX, y, Colors.INSTANCE.getColor((int) currentX, true));
                        currentX += Dactyl.fontUtil.getStringWidth(String.valueOf(ch));
                    }
                    Dactyl.fontUtil.drawStringFloatShadow(restOfText, currentX, y, color);
                    return 1;
                }
                if(ChatTimestamps.INSTANCE.isEnabled() && text.startsWith(TextFormatting.BLUE + "" + TextFormatting.RED + "" + TextFormatting.GREEN)) {
                    String leftAppend = "<";
                    String rightAppend = ">";
                    String extraSpace = (ChatTimestamps.INSTANCE.space.getValue() ? " " : "");
                    String normalTime = leftAppend + text.substring(text.indexOf(leftAppend) + 1, text.indexOf(rightAppend)) + rightAppend + extraSpace;
                    String hacker = TextFormatting.BLUE + "" + TextFormatting.RED + "" + TextFormatting.GREEN;
                    String restOfNormalText = text.replace(hacker + normalTime, "");
                    char[] characters = normalTime.toCharArray();
                    float currentX = x;
                    for (char ch : characters) {
                        Dactyl.fontUtil.drawStringFloatShadow(String.valueOf(ch), currentX, y, Colors.INSTANCE.getColor((int) currentX, true));
                        currentX += Dactyl.fontUtil.getStringWidth(String.valueOf(ch));
                    }
                    Dactyl.fontUtil.drawStringFloatShadow(restOfNormalText, currentX, y, color);
                    return 1;
                } else {
                    Dactyl.fontUtil.drawStringFloatShadow(text, x - 0.5f, y - 0.5f, color);
                    return 1;
                }
            } else {
                if (Chat.INSTANCE.gradientWatermark.getValue() && StringUtils.stripControlCodes(text).startsWith("<Dactyl>")) {
                    String watermark = "<Dactyl> ";
                    String restOfText = text.replace(TextFormatting.DARK_BLUE + "<" + TextFormatting.AQUA + "Dactyl" + TextFormatting.DARK_BLUE + ">" + TextFormatting.RESET + " ", "");
                    char[] characters = watermark.toCharArray();
                    float currentX = x;
                    for (char ch : characters) {
                        fontRenderer.drawStringWithShadow(String.valueOf(ch), currentX, y, Colors.INSTANCE.getColor((int) currentX, true));
                        currentX += fontRenderer.getStringWidth(String.valueOf(ch));
                    }
                    fontRenderer.drawStringWithShadow(restOfText, currentX, y, color);
                    return 1;
                }
                if(ChatTimestamps.INSTANCE.isEnabled() && text.startsWith(TextFormatting.BLUE + "" + TextFormatting.RED + "" + TextFormatting.GREEN)) {
                    String leftAppend = "<";
                    String rightAppend = ">";
                    String extraSpace = (ChatTimestamps.INSTANCE.space.getValue() ? " " : "");
                    String normalTime = leftAppend + text.substring(text.indexOf(leftAppend) + 1, text.indexOf(rightAppend)) + rightAppend + extraSpace;
                    String hacker = TextFormatting.BLUE + "" + TextFormatting.RED + "" + TextFormatting.GREEN;
                    String restOfNormalText = text.replace(hacker + normalTime, "");
                    char[] characters = normalTime.toCharArray();
                    float currentX = x;
                    for (char ch : characters) {
                        fontRenderer.drawStringWithShadow(String.valueOf(ch), currentX, y, Colors.INSTANCE.getColor((int) currentX, true));
                        currentX += fontRenderer.getStringWidth(String.valueOf(ch));
                    }
                    fontRenderer.drawStringWithShadow(restOfNormalText, currentX, y, color);
                    return 1;
                } else {
                    fontRenderer.drawStringWithShadow(text, x - 0.5f, y - 0.5f, color);
                    return 1;
                }
            }
        }
        return fontRenderer.drawStringWithShadow(Media.INSTANCE.onRenderString(text), x, y, color);
    }
}
