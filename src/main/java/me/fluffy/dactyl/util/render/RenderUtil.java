package me.fluffy.dactyl.util.render;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.injection.inj.access.IRenderManager;
import me.fluffy.dactyl.module.impl.client.Colors;
import me.fluffy.dactyl.module.impl.combat.AutoCrystal;
import me.fluffy.dactyl.util.EntityUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.Timer;

public class RenderUtil {
    public static ICamera camera = new Frustum();
    private static final FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
    private static final IntBuffer viewport = BufferUtils.createIntBuffer(16);
    private static final FloatBuffer modelView = BufferUtils.createFloatBuffer(16);;
    private static final FloatBuffer projection = BufferUtils.createFloatBuffer(16);

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

    public static boolean isInView(Entity entity) {
        camera.setPosition(Minecraft.getMinecraft().getRenderViewEntity().posX, Minecraft.getMinecraft().getRenderViewEntity().posY, Minecraft.getMinecraft().getRenderViewEntity().posZ);

        AxisAlignedBB bb = entity.getEntityBoundingBox();

        bb = bb.expand(0.15f, 0.1f, 0.15f);

        if (!camera.isBoundingBoxInFrustum(bb)) {
            return false;
        }
        return true;
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

    public static void renderWireFrame(Color espColor, float width, ModelBase modelBase, Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, float offset) {
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef(offset, offset, offset);
        GlStateManager.color(espColor.getRed() / 255f, espColor.getGreen() / 255f, espColor.getBlue() / 255f, espColor.getAlpha() / 255f);
        GlStateManager.glLineWidth(width);
        modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.resetColor();
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    public static void drawEntityESP(Entity entity, Color color, float lineWidth, boolean outline, boolean box, int boxAlpha) {
        AxisAlignedBB bb = new AxisAlignedBB(entity.posX - (Minecraft.getMinecraft().getRenderManager()).viewerPosX, entity.posY - (Minecraft.getMinecraft().getRenderManager()).viewerPosY, entity.posZ - (Minecraft.getMinecraft().getRenderManager()).viewerPosZ, (entity.posX + entity.getEntityBoundingBox().maxX) - (Minecraft.getMinecraft().getRenderManager()).viewerPosX, (entity.posY + entity.getEntityBoundingBox().maxY) - (Minecraft.getMinecraft().getRenderManager()).viewerPosY, (entity.posZ + entity.getEntityBoundingBox().maxZ) - (Minecraft.getMinecraft().getRenderManager()).viewerPosZ);
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

    public static void drawTracerPointer(final float x, final float y, final float size, final float widthDiv, final float heightDiv, final boolean outline, final float outlineWidth, final int color, float alpha) {
        final boolean blend = GL11.glIsEnabled(3042);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glPushMatrix();
        hexColor(color);
        GL11.glBegin(7);
        GL11.glVertex2d((double)x, (double)y);
        GL11.glVertex2d((double)(x - size / widthDiv), (double)(y + size));
        GL11.glVertex2d((double)x, (double)(y + size / heightDiv));
        GL11.glVertex2d((double)(x + size / widthDiv), (double)(y + size));
        GL11.glVertex2d((double)x, (double)y);
        GL11.glEnd();
        if (outline) {
            GL11.glLineWidth(outlineWidth);
            GL11.glColor4f(0.0f, 0.0f, 0.0f, alpha);
            GL11.glBegin(2);
            GL11.glVertex2d((double)x, (double)y);
            GL11.glVertex2d((double)(x - size / widthDiv), (double)(y + size));
            GL11.glVertex2d((double)x, (double)(y + size / heightDiv));
            GL11.glVertex2d((double)(x + size / widthDiv), (double)(y + size));
            GL11.glVertex2d((double)x, (double)y);
            GL11.glEnd();
        }
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        if (!blend) {
            GL11.glDisable(3042);
        }
        GL11.glDisable(2848);
    }

    public static void hexColor(final int hexColor) {
        final float red = (hexColor >> 16 & 0xFF) / 255.0f;
        final float green = (hexColor >> 8 & 0xFF) / 255.0f;
        final float blue = (hexColor & 0xFF) / 255.0f;
        final float alpha = (hexColor >> 24 & 0xFF) / 255.0f;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public static void drawBlockOutline(final BlockPos pos, final Color color, final float linewidth, final boolean air) {
        final IBlockState iblockstate = Minecraft.getMinecraft().world.getBlockState(pos);
        if ((air || iblockstate.getMaterial() != Material.AIR) && Minecraft.getMinecraft().world.getWorldBorder().contains(pos)) {
            final Vec3d interp = EntityUtil.interpolateEntity(Minecraft.getMinecraft().player, Minecraft.getMinecraft().getRenderPartialTicks());
            drawBlockOutline(iblockstate.getSelectedBoundingBox(Minecraft.getMinecraft().world, pos).grow(0.0020000000949949026).offset(-interp.x, -interp.y, -interp.z), color, linewidth);
        }
    }

    public static void drawBlockOutline(final BlockPos pos, final Color color, final float linewidth, final boolean air, final double height) {
        final IBlockState iblockstate = Minecraft.getMinecraft().world.getBlockState(pos);
        if ((air || iblockstate.getMaterial() != Material.AIR) && Minecraft.getMinecraft().world.getWorldBorder().contains(pos)) {
            final AxisAlignedBB blockAxis = new AxisAlignedBB(pos.getX() - Minecraft.getMinecraft().getRenderManager().viewerPosX, pos.getY() - Minecraft.getMinecraft().getRenderManager().viewerPosY, pos.getZ() - Minecraft.getMinecraft().getRenderManager().viewerPosZ, pos.getX() + 1 - Minecraft.getMinecraft().getRenderManager().viewerPosX, pos.getY() + 1 - Minecraft.getMinecraft().getRenderManager().viewerPosY + height, pos.getZ() + 1 - Minecraft.getMinecraft().getRenderManager().viewerPosZ);
            drawBlockOutline(blockAxis.grow(0.0020000000949949026), color, linewidth);
        }
    }

    public static void drawBBBox(AxisAlignedBB box)
    {
        if (box == null)
        {
            return;
        }

        // back
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glEnd();
        // left
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glEnd();
        // right
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glEnd();
        // front
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glEnd();
        // top
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
        GL11.glVertex3d(box.minX, box.maxY, box.minZ);
        GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
        GL11.glEnd();
        // bottom
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(box.maxX, box.minY, box.minZ);
        GL11.glVertex3d(box.minX, box.minY, box.minZ);
        GL11.glVertex3d(box.minX, box.minY, box.maxZ);
        GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
        GL11.glEnd();
    }

    public static void renderOne(float width)
    {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(width * 2F);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glClearStencil(0xF);
        GL11.glStencilFunc(GL11.GL_NEVER, 1, 0xF);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
        GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE);
    }

    public static void renderTwo()
    {
        GL11.glStencilFunc(GL11.GL_NEVER, 0, 0xF);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
        GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
    }

    public static void renderThree()
    {
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xF);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glPolygonMode(GL11.GL_FRONT, GL11.GL_LINE);
    }

    public static void renderFour(Entity renderEntity)
    {
        float[] color;

        if (renderEntity instanceof EntityLivingBase)
        {
            EntityLivingBase entity = (EntityLivingBase) renderEntity;
            float distance = Minecraft.getMinecraft().player.getDistance(entity);

            if (distance <= 32)
            {
                color = new float[] {1F, distance / 32F, 0F};
            }
            else
            {
                color = new float[] {0F, 0.9F, 0F};
            }
        }
        else
        {
            float distance = Minecraft.getMinecraft().player.getDistance(renderEntity);

            if (distance <= 32)
            {
                color = new float[] {1F, distance / 32F, 0F};
            }
            else
            {
                color = new float[] {0F, 0.9F, 0F};
            }
        }

        GlStateManager.color(color[0], color[1], color[2], 0.85F);
        renderFour();
    }

    public static void renderFour()
    {
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_LINE);
        GL11.glPolygonOffset(1F, -2000000F);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
    }

    public static void renderFive()
    {
        GL11.glPolygonOffset(1.0F, 2000000F);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_LINE);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glPopAttrib();
    }

    public static void drawPlane(double x, double y, double z, AxisAlignedBB bb, float width, int color) {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        drawPlane(bb, width, color);
        GL11.glPopMatrix();
    }

    public static void drawPlane(AxisAlignedBB axisalignedbb, float width, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.glLineWidth(width);
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        drawPlane(axisalignedbb, color);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawPlane(AxisAlignedBB boundingBox, int color) {
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        double minX = boundingBox.minX;
        double minY = boundingBox.minY;
        double minZ = boundingBox.minZ;

        double maxX = boundingBox.maxX;
        double maxY = boundingBox.maxY;
        double maxZ = boundingBox.maxZ;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(minX, minY, maxZ).color(red, green, blue, 0).endVertex();
        bufferbuilder.pos(maxZ, minY, minZ).color(red, green, blue, alpha).endVertex();

        tessellator.draw();
    }


    public static void drawLine(double startX, double startY, double startZ, double endX, double endY, double endZ, int weight, Color color) {
        GlStateManager.pushMatrix();
        GlStateManager.glLineWidth(weight);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.pos(startX, startY, startZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        bufferBuilder.pos(endX, endY, endZ)    .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableAlpha();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableTexture2D();
        Tessellator.getInstance().draw();
        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.popMatrix();
    }

    private static final boolean crystalCheck = false;

    public static float[] distanceToColor(Entity entity) {
        if (entity instanceof EntityPlayer && Dactyl.friendManager.isFriend(entity.getName())) {
            return new float[] { 0.0f, 0.5f, 1.0f, 1.0f };
        }
        final Color col = new Color(Color.HSBtoRGB((float) (Math.max(0.0F, Math.min(Minecraft.getMinecraft().player.getDistanceSq(entity), crystalCheck ? AutoCrystal.INSTANCE.placeRange.getValue() * AutoCrystal.INSTANCE.placeRange.getValue() : 2500) / (crystalCheck ? AutoCrystal.INSTANCE.placeRange.getValue() * AutoCrystal.INSTANCE.placeRange.getValue() : 2500)) / 3.0F), 1.0F, 0.8f) | 0xFF000000);
        return new float[] { col.getRed() / 255.0f, col.getGreen() / 255.0f, col.getBlue() / 255.0f, 1.0f };
    }

    public static Color distToColor(Entity entity) {
        if (entity instanceof EntityPlayer && Dactyl.friendManager.isFriend(entity.getName())) {
            return new Color(85, 255, 255, 255);
        }
        final Color col = new Color(Color.HSBtoRGB((float) (Math.max(0.0F, Math.min(Minecraft.getMinecraft().player.getDistanceSq(entity), crystalCheck ? AutoCrystal.INSTANCE.placeRange.getValue() * AutoCrystal.INSTANCE.placeRange.getValue() : 2500) / (crystalCheck ? AutoCrystal.INSTANCE.placeRange.getValue() * AutoCrystal.INSTANCE.placeRange.getValue() : 2500)) / 3.0F), 1.0F, 0.8f) | 0xFF000000);
        return col;
    }

    public static Vec3d to2D(final double x, final double y, final double z) {
        GL11.glGetFloat(2982, RenderUtil.modelView);
        GL11.glGetFloat(2983, RenderUtil.projection);
        GL11.glGetInteger(2978, RenderUtil.viewport);
        final boolean result = GLU.gluProject((float)x, (float)y, (float)z, RenderUtil.modelView, RenderUtil.projection, RenderUtil.viewport, RenderUtil.screenCoords);
        if (result) {
            return new Vec3d((double)RenderUtil.screenCoords.get(0), (double)(Display.getHeight() - RenderUtil.screenCoords.get(1)), (double)RenderUtil.screenCoords.get(2));
        }
        return null;
    }

    public static void drawBlockOutline(final AxisAlignedBB bb, final Color color, final float linewidth) {
        final float red = color.getRed() / 255.0f;
        final float green = color.getGreen() / 255.0f;
        final float blue = color.getBlue() / 255.0f;
        final float alpha = color.getAlpha() / 255.0f;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glLineWidth(linewidth);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GL11.glDisable(2848);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
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

    public static void drawBetterColoredRect(double left, double top, double right, double bottom, float width, int color) {
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
        GlStateManager.glLineWidth(width);
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
