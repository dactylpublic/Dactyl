package me.fluffy.dactyl.util.render.font;

import me.fluffy.dactyl.module.impl.client.HUD;
import net.minecraft.client.Minecraft;

import java.awt.*;

import org.lwjgl.opengl.GL11;

public class FontUtil {
    private CFontRenderer font = new CFontRenderer(new Font("Verdana", Font.PLAIN, 18), true, true);

    public void drawString(String text, int x, int y, int color, boolean shadow) {
        if (HUD.INSTANCE.customFont.getValue()) {
            if(shadow) {
                font.drawStringWithShadow(text, x, y, color);
            } else {
                font.drawString(text, x, y, color, false);
            }
        } else {
            Minecraft.getMinecraft().fontRenderer.drawString(text, x, y, color, shadow);
        }
    }

    public void drawString(String text, float x, float y, int color, boolean shadow) {
        if (HUD.INSTANCE.customFont.getValue()) {
            if(shadow) {
                font.drawStringWithShadow(text, x, y, color);
            } else {
                font.drawString(text, x, y, color, false);
            }
        } else {
            Minecraft.getMinecraft().fontRenderer.drawString(text, x, y, color, shadow);
        }
    }

    public void drawString(String text, int x, int y, int color) {
        if (HUD.INSTANCE.customFont.getValue()) {
            font.drawString(text, x, y, color, false);
        } else {
            Minecraft.getMinecraft().fontRenderer.drawString(text, x, y, color);
        }
    }

    public void drawStringWithShadow(String text, int x, int y, int color) {
        if (HUD.INSTANCE.customFont.getValue()) {
            font.drawStringWithShadow(text, x, y, color);
        } else {
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x, y, color);
        }
    }

    public boolean isCustomFont() {
        return HUD.INSTANCE.customFont.getValue();
    }

    public void drawString(String text, float textScale, int x, int y, int color, boolean shadow) {
        GL11.glScaled(textScale, textScale,0);
        drawString(text, x, y, color, shadow);
        GL11.glScaled(1 / textScale, 1 / textScale,0);
    }

    public void drawString(String text, float textScale, int x, int y, int color) {
        GL11.glScaled(textScale, textScale,0);
        drawString(text, x, y, color, false);
        GL11.glScaled(1 / textScale, 1 / textScale,0);
    }

    public float getFontHeight() {
        if (HUD.INSTANCE.customFont.getValue()) {
            return font.getHeight();
        } else {
            return Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
        }
    }

    public float getFontHeight(double scale) {
        return (float) (getFontHeight() * scale);
    }

    public float getStringWidth(String text, double scale) {
        return (float) (getStringWidth(text) * scale);
    }

    public float getStringWidth(String text) {
        if (HUD.INSTANCE.customFont.getValue()) {
            return font.getStringWidth(text);
        } else {
            return Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
        }
    }

    public CFontRenderer getFont() {
        return this.font;
    }
}
