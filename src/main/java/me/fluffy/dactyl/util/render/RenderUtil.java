package me.fluffy.dactyl.util.render;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.module.impl.client.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Objects;

public class RenderUtil {
    public static ICamera camera = new Frustum();

    public static void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        float f = (float)(startColor >> 24 & 255) / 255.0F;
        float f1 = (float)(startColor >> 16 & 255) / 255.0F;
        float f2 = (float)(startColor >> 8 & 255) / 255.0F;
        float f3 = (float)(startColor & 255) / 255.0F;
        float f4 = (float)(endColor >> 24 & 255) / 255.0F;
        float f5 = (float)(endColor >> 16 & 255) / 255.0F;
        float f6 = (float)(endColor >> 8 & 255) / 255.0F;
        float f7 = (float)(endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(right, top, 0.0D).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(left, top, 0.0D).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(left, bottom, 0.0D).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(right, bottom, 0.0D).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor, double zLevel) {
        float f = (float)(startColor >> 24 & 255) / 255.0F;
        float f1 = (float)(startColor >> 16 & 255) / 255.0F;
        float f2 = (float)(startColor >> 8 & 255) / 255.0F;
        float f3 = (float)(startColor & 255) / 255.0F;
        float f4 = (float)(endColor >> 24 & 255) / 255.0F;
        float f5 = (float)(endColor >> 16 & 255) / 255.0F;
        float f6 = (float)(endColor >> 8 & 255) / 255.0F;
        float f7 = (float)(endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(right, top, zLevel).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(left, top, zLevel).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(left, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(right, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawGradientRect(float left, float top, float right, float bottom, int startColor, int endColor) {
        float f = (float)(startColor >> 24 & 255) / 255.0F;
        float f1 = (float)(startColor >> 16 & 255) / 255.0F;
        float f2 = (float)(startColor >> 8 & 255) / 255.0F;
        float f3 = (float)(startColor & 255) / 255.0F;
        float f4 = (float)(endColor >> 24 & 255) / 255.0F;
        float f5 = (float)(endColor >> 16 & 255) / 255.0F;
        float f6 = (float)(endColor >> 8 & 255) / 255.0F;
        float f7 = (float)(endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(right, top, 0.0D).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(left, top, 0.0D).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(left, bottom, 0.0D).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(right, bottom, 0.0D).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawText(final BlockPos pos, final String text) {
        if (pos == null || text == null) {
            return;
        }
        GlStateManager.pushMatrix();
        glBillboardDistanceScaled(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, Minecraft.getMinecraft().player, 1.0f);
        GlStateManager.disableDepth();
        GlStateManager.translate(-(Dactyl.fontUtil.getStringWidth(text) / 2.0), 0.0, 0.0);
        Dactyl.fontUtil.drawStringWithShadow(text, 0, 0, -5592406);
        GlStateManager.popMatrix();
    }

    public static void glBillboardDistanceScaled(final float x, final float y, final float z, final EntityPlayer player, final float scale) {
        glBillboard(x, y, z);
        final int distance = (int)player.getDistance((double)x, (double)y, (double)z);
        float scaleDistance = distance / 2.0f / (2.0f + (2.0f - scale));
        if (scaleDistance < 1.0f) {
            scaleDistance = 1.0f;
        }
        GlStateManager.scale(scaleDistance, scaleDistance, scaleDistance);
    }

    public static void glBillboard(final float x, final float y, final float z) {
        final float scale = 0.02666667f;
        GlStateManager.translate(x - Minecraft.getMinecraft().getRenderManager().viewerPosX, y - Minecraft.getMinecraft().getRenderManager().viewerPosY, z - Minecraft.getMinecraft().getRenderManager().viewerPosZ);
        GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(-Minecraft.getMinecraft().player.rotationYaw, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(Minecraft.getMinecraft().player.rotationPitch, (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2) ? -1.0f : 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-scale, -scale, scale);
    }

    public static void drawBoxESP(BlockPos pos, Color color, float lineWidth, boolean outline, boolean box, int boxAlpha) {
        AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - (Minecraft.getMinecraft().getRenderManager()).viewerPosX, pos.getY() - (Minecraft.getMinecraft().getRenderManager()).viewerPosY, pos.getZ() - (Minecraft.getMinecraft().getRenderManager()).viewerPosZ, (pos.getX() + 1) - (Minecraft.getMinecraft().getRenderManager()).viewerPosX, (pos.getY() + 1) - (Minecraft.getMinecraft().getRenderManager()).viewerPosY, (pos.getZ() + 1) - (Minecraft.getMinecraft().getRenderManager()).viewerPosZ);
        camera.setPosition(((Entity) Objects.requireNonNull(Minecraft.getMinecraft().getRenderViewEntity())).posX, (Minecraft.getMinecraft().getRenderViewEntity()).posY, (Minecraft.getMinecraft().getRenderViewEntity()).posZ);
        if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + (Minecraft.getMinecraft().getRenderManager()).viewerPosX, bb.minY +
                (Minecraft.getMinecraft().getRenderManager()).viewerPosY, bb.minZ +
                (Minecraft.getMinecraft().getRenderManager()).viewerPosZ, bb.maxX +
                (Minecraft.getMinecraft().getRenderManager()).viewerPosX, bb.maxY +
                (Minecraft.getMinecraft().getRenderManager()).viewerPosY, bb.maxZ +
                (Minecraft.getMinecraft().getRenderManager()).viewerPosZ))) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GL11.glEnable(2848);
            GL11.glHint(3154, 4354);
            GL11.glLineWidth(lineWidth);
            if (box) {
                RenderGlobal.renderFilledBox(bb, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, boxAlpha / 255.0F);
            }
            if (outline) {
                RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
            }
            GL11.glDisable(2848);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    public static void drawOffsetBox(BlockPos pos, double translateY, double extrudeY, Color color, float lineWidth, boolean outline, boolean box, int boxAlpha) {
        AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - (Minecraft.getMinecraft().getRenderManager()).viewerPosX, pos.getY() + translateY - (Minecraft.getMinecraft().getRenderManager()).viewerPosY, pos.getZ() - (Minecraft.getMinecraft().getRenderManager()).viewerPosZ, (pos.getX() + 1) - (Minecraft.getMinecraft().getRenderManager()).viewerPosX, (pos.getY() + extrudeY + 1) - (Minecraft.getMinecraft().getRenderManager()).viewerPosY, (pos.getZ() + 1) - (Minecraft.getMinecraft().getRenderManager()).viewerPosZ);
        camera.setPosition(((Entity) Objects.requireNonNull(Minecraft.getMinecraft().getRenderViewEntity())).posX, (Minecraft.getMinecraft().getRenderViewEntity()).posY, (Minecraft.getMinecraft().getRenderViewEntity()).posZ);
        if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + (Minecraft.getMinecraft().getRenderManager()).viewerPosX, bb.minY +
                (Minecraft.getMinecraft().getRenderManager()).viewerPosY, bb.minZ +
                (Minecraft.getMinecraft().getRenderManager()).viewerPosZ, bb.maxX +
                (Minecraft.getMinecraft().getRenderManager()).viewerPosX, bb.maxY +
                (Minecraft.getMinecraft().getRenderManager()).viewerPosY, bb.maxZ +
                (Minecraft.getMinecraft().getRenderManager()).viewerPosZ))) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GL11.glEnable(2848);
            GL11.glHint(3154, 4354);
            GL11.glLineWidth(lineWidth);
            if (box) {
                RenderGlobal.renderFilledBox(bb, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, boxAlpha / 255.0F);
            }
            if (outline) {
                RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
            }
            GL11.glDisable(2848);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    public static int toRGBA(final int r, final int g, final int b) {
        return toRGBA(r, g, b, 255);
    }

    public static int toRGBA(final int r, final int g, final int b, final int a) {
        return (r << 16) + (g << 8) + b + (a << 24);
    }

    public static int toARGB(final int r, final int g, final int b, final int a) {
        return new Color(r, g, b, a).getRGB();
    }

    public static int toRGBA(final float r, final float g, final float b, final float a) {
        return toRGBA((int)(r * 255.0f), (int)(g * 255.0f), (int)(b * 255.0f), (int)(a * 255.0f));
    }

    public static int toRGBA(final float[] colors) {
        if (colors.length != 4) {
            throw new IllegalArgumentException("colors[] must have a length of 4!");
        }
        return toRGBA(colors[0], colors[1], colors[2], colors[3]);
    }

    public static int toRGBA(final double[] colors) {
        if (colors.length != 4) {
            throw new IllegalArgumentException("colors[] must have a length of 4!");
        }
        return toRGBA((float)colors[0], (float)colors[1], (float)colors[2], (float)colors[3]);
    }

    public static int toRGBA(final Color color) {
        return toRGBA(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static void drawOutlinedRectangle(int left, int top, int right, int bottom, int color) {
        drawHorizontalLine(left, right, top, color);
        drawHorizontalLine(left, right, bottom, color);
        drawVerticalLine(left, bottom, top, color);
        drawVerticalLine(right, bottom, top, color);
    }

    public static void drawOutlinedGradientRectangle(int left, int top, int right, int bottom) {
        // bottom line
        drawGradientRect(left, bottom, right, bottom, Colors.INSTANCE.getColor(bottom, false), Colors.INSTANCE.getColor(bottom, false));
        // top line
        drawGradientRect(left, top, right, top, Colors.INSTANCE.getColor(top, false), Colors.INSTANCE.getColor(top, false));
        // left line
        drawGradientRect(left, top, left+1, bottom, Colors.INSTANCE.getColor(top, false), Colors.INSTANCE.getColor(bottom, false));
        // right line
        drawGradientRect(right, top, right-1, bottom, Colors.INSTANCE.getColor(top, false), Colors.INSTANCE.getColor(bottom, false));
    }

    public static void drawOutlinedGradientRectangleZ(int left, int top, int right, int bottom, double zLevel) {
        // bottom line
        drawGradientRect(left, bottom-1, right, bottom, Colors.INSTANCE.getColor(bottom, false), Colors.INSTANCE.getColor(bottom, false), zLevel);
        // top line
        drawGradientRect(left, top, right, top+1, Colors.INSTANCE.getColor(top, false), Colors.INSTANCE.getColor(top, false), zLevel);
        // left line
        drawGradientRect(left, top, left+1, bottom, Colors.INSTANCE.getColor(top, false), Colors.INSTANCE.getColor(bottom, false), zLevel);
        // right line
        drawGradientRect(right-1, top, right, bottom, Colors.INSTANCE.getColor(top, false), Colors.INSTANCE.getColor(bottom, false), zLevel);
    }

    public static void drawOutlinedRectangle(int left, int top, int right, int bottom, int color, double zlevel) {
        drawHorizontalLine(left, right, top, color, zlevel);
        drawHorizontalLine(left, right, bottom, color, zlevel);
        drawVerticalLine(left, bottom, top, color, zlevel);
        drawVerticalLine(right, bottom, top, color, zlevel);
    }

    public static void drawRect(int left, int top, int right, int bottom, int color) {
        if (left < right)
        {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            int j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(f, f1, f2, f3);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos((double)left, (double)bottom, 0.0D).endVertex();
        bufferbuilder.pos((double)right, (double)bottom, 0.0D).endVertex();
        bufferbuilder.pos((double)right, (double)top, 0.0D).endVertex();
        bufferbuilder.pos((double)left, (double)top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawRect(double left, double top, double right, double bottom, int color) {
        if (left < right)
        {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(f, f1, f2, f3);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos((double)left, (double)bottom, 0.0D).endVertex();
        bufferbuilder.pos((double)right, (double)bottom, 0.0D).endVertex();
        bufferbuilder.pos((double)right, (double)top, 0.0D).endVertex();
        bufferbuilder.pos((double)left, (double)top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawBetterColoredRect(double left, double top, double right, double bottom, int color) {
        if (left < right)
        {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(left, bottom, 0.0).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, bottom, 0.0).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, top, 0.0).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(left, top, 0.0).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(left, bottom, 0.0).color(f, f1, f2, f3).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawColoredWorldRect(double left, double top, double right, double bottom, int color) {
        if (left < right)
        {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 0xFF) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos((double)left, (double)bottom, 300.0D).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos((double)right, (double)bottom, 300.0D).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos((double)right, (double)top, 300.0D).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos((double)left, (double)top, 300.0D).color(f, f1, f2, f3).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }


    public static int getRainbow(int speed, int offset, float s, float b) {
        float hue = (float)((System.currentTimeMillis() + offset) % speed);
        hue /= speed;
        return Color.getHSBColor(hue, s, b).getRGB();
    }

    public static int getScreenHeight() {
        return (Minecraft.getMinecraft().displayHeight / 2);
    }

    public static int getScreenWidth() {
        return (Minecraft.getMinecraft().displayWidth / 2);
    }

    public static void drawHorizontalLine(int startX, int endX, int y, int color) {
        if (endX < startX)
        {
            int i = startX;
            startX = endX;
            endX = i;
        }

        drawRect(startX, y, endX + 1, y + 1, color);
    }

    public static void drawVerticalLine(int x, int startY, int endY, int color) {
        if (endY < startY)
        {
            int i = startY;
            startY = endY;
            endY = i;
        }

        drawRect(x, startY + 1, x + 1, endY, color);
    }

    public static void drawRect(int left, int top, int right, int bottom, int color, double zLevel) {
        if (left < right)
        {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            int j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(f, f1, f2, f3);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos((double)left, (double)bottom, zLevel).endVertex();
        bufferbuilder.pos((double)right, (double)bottom, zLevel).endVertex();
        bufferbuilder.pos((double)right, (double)top, zLevel).endVertex();
        bufferbuilder.pos((double)left, (double)top, zLevel).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawHorizontalLine(int startX, int endX, int y, int color, double zLevel) {
        if (endX < startX)
        {
            int i = startX;
            startX = endX;
            endX = i;
        }

        drawRect(startX, y, endX + 1, y + 1, color, zLevel);
    }

    public static void drawVerticalLine(int x, int startY, int endY, int color, double zLevel) {
        if (endY < startY)
        {
            int i = startY;
            startY = endY;
            endY = i;
        }

        drawRect(x, startY + 1, x + 1, endY, color, zLevel);
    }
}
