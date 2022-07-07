package me.chloe.dactyl.util.render.font;

import me.chloe.dactyl.module.impl.client.HUD;
import me.chloe.dactyl.module.impl.client.Media;
import net.minecraft.client.Minecraft;

import java.awt.*;

import org.lwjgl.opengl.GL11;

public class FontUtil {
    private CFontRenderer font = new CFontRenderer(new Font("Verdana", Font.PLAIN, 18), true, true);

    public void drawString(String text, int x, int y, int color, boolean shadow) {
        if (HUD.INSTANCE.customFont.getValue()) {
            if(shadow) {
                font.drawStringWithShadow(Media.INSTANCE.onRenderString(text), x, y, color);
            } else {
                font.drawString(Media.INSTANCE.onRenderString(text), x, y, color, false);
            }
        } else {
            Minecraft.getMinecraft().fontRenderer.drawString(Media.INSTANCE.onRenderString(text), x, y, color, shadow);
        }
    }

    public void drawString(String text, float x, float y, int color, boolean shadow) {
        if (HUD.INSTANCE.customFont.getValue()) {
            if(shadow) {
                font.drawStringWithShadow(Media.INSTANCE.onRenderString(text), x, y, color);
            } else {
                font.drawString(Media.INSTANCE.onRenderString(text), x, y, color, false);
            }
        } else {
            Minecraft.getMinecraft().fontRenderer.drawString(Media.INSTANCE.onRenderString(text), x, y, color, shadow);
        }
    }

    public void drawString(String text, int x, int y, int color) {
        if (HUD.INSTANCE.customFont.getValue()) {
            font.drawString(Media.INSTANCE.onRenderString(text), x, y, color, false);
        } else {
            Minecraft.getMinecraft().fontRenderer.drawString(Media.INSTANCE.onRenderString(text), x, y, color);
        }
    }

    public void drawStringWithShadow(String text, int x, int y, int color) {
        Media.INSTANCE.onRenderString(text);
        if (HUD.INSTANCE.customFont.getValue()) {
            font.drawStringWithShadow(Media.INSTANCE.onRenderString(text), x, y, color);
        } else {
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(Media.INSTANCE.onRenderString(text), x, y, color);
        }
    }

    public void drawStringFloatShadow(String text, float x, float y, int color) {
        font.drawStringWithShadow(Media.INSTANCE.onRenderString(text), x, y, color);
    }

    public boolean isCustomFont() {
        return HUD.INSTANCE.customFont.getValue();
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
        return (float) (getStringWidth(Media.INSTANCE.onRenderString(text)) * scale);
    }

    public float getStringWidth(String text) {
        if (HUD.INSTANCE.customFont.getValue()) {
            return font.getStringWidth(Media.INSTANCE.onRenderString(text));
        } else {
            return Minecraft.getMinecraft().fontRenderer.getStringWidth(Media.INSTANCE.onRenderString(text));
        }
    }

    public CFontRenderer getFont() {
        return this.font;
    }
}
