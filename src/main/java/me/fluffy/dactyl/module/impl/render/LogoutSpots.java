package me.fluffy.dactyl.module.impl.render;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.event.impl.network.ConnectionEvent;
import me.fluffy.dactyl.event.impl.world.Render3DEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.module.impl.client.Colors;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.StringUtil;
import me.fluffy.dactyl.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class LogoutSpots extends Module {

    public Setting<Double> scaling = new Setting<Double>("Scaling", 4d, 1d, 5d);
    public Setting<Boolean> colorSync = new Setting<Boolean>("ColorSync", false);
    public Setting<Integer> red = new Setting<Integer>("Red", 255, 1, 255, v->!colorSync.getValue());
    public Setting<Integer> green = new Setting<Integer>("Green", 255, 1, 255, v->!colorSync.getValue());
    public Setting<Integer> blue = new Setting<Integer>("Blue", 255, 1, 255, v->!colorSync.getValue());
    public Setting<Boolean> border = new Setting<Boolean>("Border", true);
    public Setting<Boolean> box = new Setting<Boolean>("Box", true);
    public Setting<Boolean> smartScaling = new Setting<Boolean>("SmartScaling", false);
    public Setting<LocDisplay> locationDisplay = new Setting<LocDisplay>("Display", LocDisplay.COORDS);


    public LogoutSpots() {
        super("LogoutSpots", Category.RENDER);
    }

    private final List<LogoutPos> spots = new CopyOnWriteArrayList<LogoutPos>();;

    @Override
    public void onRender3D(Render3DEvent event) {
        if (!this.spots.isEmpty()) {
            synchronized (this.spots) {
                this.spots.forEach(spot -> {
                    if (spot.getEntity() != null) {
                        Color clr = new Color(red.getValue(), green.getValue(), blue.getValue());
                        if(colorSync.getValue()) {
                            clr = Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false));
                        }
                        if(box.getValue()) {
                            AxisAlignedBB interpolateAxis = RenderUtil.interpolateAxis(spot.getEntity().getEntityBoundingBox());
                            RenderUtil.drawBlockOutline(interpolateAxis, clr, 1.0f);
                        }
                        double x = this.interpolate(spot.getEntity().lastTickPosX, spot.getEntity().posX, event.getPartialTicks()) - LogoutSpots.mc.getRenderManager().viewerPosX;
                        double y = this.interpolate(spot.getEntity().lastTickPosY, spot.getEntity().posY, event.getPartialTicks()) - LogoutSpots.mc.getRenderManager().viewerPosY;
                        double z = this.interpolate(spot.getEntity().lastTickPosZ, spot.getEntity().posZ, event.getPartialTicks()) - LogoutSpots.mc.getRenderManager().viewerPosZ;
                        this.renderNameTag(spot.getName(), x, y, z, event.getPartialTicks(), spot.getX(), spot.getY(), spot.getZ());
                    }
                });
            }
        }
    }

    private void renderNameTag(final String n, final double x, final double yi, final double z, final float delta, final double xPos, final double yPos, final double zPos) {
        String name = StringUtils.stripControlCodes(n);
        double tempY = yi + 0.7D;
        Entity camera = mc.getRenderViewEntity();
        double originalPositionX = camera.posX;
        double originalPositionY = camera.posY;
        double originalPositionZ = camera.posZ;
        camera.posX = interpolate(camera.prevPosX, camera.posX, delta);
        camera.posY = interpolate(camera.prevPosY, camera.posY, delta);
        camera.posZ = interpolate(camera.prevPosZ, camera.posZ, delta);
        double distance = camera.getDistance(x + (mc.getRenderManager()).viewerPosX, yi + (mc.getRenderManager()).viewerPosY, z +
                (mc.getRenderManager()).viewerPosZ);
        int width = (int) (Dactyl.fontUtil.getStringWidth(getDisplayName(name, xPos, yPos, zPos)) / 2);
        //double scale = 0.0018D + ((scaling.getValue()*0.001)) * distance;
        double scale = 0.0018D + ((scaling.getValue()*0.001)) * distance;
        if (distance <= 8.0D && smartScaling.getValue()) {
            scale = 0.0245D;
        }
        if(!smartScaling.getValue()) {
            scale = 0.03D;
        }
        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0F, -1500000.0F);
        GlStateManager.disableLighting();
        GlStateManager.translate((float)x, (float)tempY + 1.4F, (float)z);
        GlStateManager.rotate(-(mc.getRenderManager()).playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((mc.getRenderManager()).playerViewX, (mc.gameSettings.thirdPersonView == 2) ? -1.0F : 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GL11.glDisable(2929);
        GlStateManager.enableBlend();
        int clr = new Color(red.getValue(), green.getValue(), blue.getValue()).getRGB();
        if(colorSync.getValue()) {
            clr = Colors.INSTANCE.getColor(1, false);
        }
        if(!Dactyl.fontUtil.isCustomFont()) {
            drawBorderedRect((-width - 2), -(Dactyl.fontUtil.getFontHeight() + 1), width + 2.0F, 1.5F, 0.75F, 1.0F, 1996488704, clr);
        } else {
            drawBorderedRect((-width - 2), -(Dactyl.fontUtil.getFontHeight() + 2), width + 2.0F, 1.5F, 0.75F, 1.0F, 1996488704, clr);
        }
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glColor4f(1.0f, 10.0f, 1.0f, 1.0f);
        if(!Dactyl.fontUtil.isCustomFont()) {
            Dactyl.fontUtil.drawStringWithShadow(getDisplayName(name, xPos, yPos, zPos), -width, (int) -(Dactyl.fontUtil.getFontHeight() - 1), clr);
        } else {
            Dactyl.fontUtil.drawStringWithShadow(getDisplayName(name, xPos, yPos, zPos), -width, (int) -(Dactyl.fontUtil.getFontHeight() + 2), clr);
        }
        GlStateManager.glNormal3f(0.0F, 0.0F, 0.0F);
        camera.posX = originalPositionX;
        camera.posY = originalPositionY;
        camera.posZ = originalPositionZ;
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0F, 1500000.0F);
        GlStateManager.resetColor();
        GlStateManager.popMatrix();
    }


    @SubscribeEvent
    public void onConnection(ConnectionEvent event) {
        if(mc.world == null || mc.player == null || event == null || event.getName() == null || event.getConnectionType() == null || mc.world.getPlayerEntityByName(event.getName()) == null) {
            return;
        }
        if(event.getConnectionType() == ConnectionEvent.ConnectionType.LOGIN) {
            this.spots.removeIf(pos -> pos.getName().equalsIgnoreCase(event.getName()));
        } else {
            EntityPlayer entity2 = mc.world.getPlayerEntityByName(event.getName());
            if (event.getName() != null && entity2 != null) {
                if(!event.getName().equalsIgnoreCase(mc.session.getUsername())) {
                    this.spots.add(new LogoutPos(event.getName(), entity2));
                }
            }
        }
    }

    @Override
    public void onLogout() {
        this.spots.clear();
    }

    @Override
    public void onDisable() {
        this.spots.clear();
    }


    public String getDisplayName(String name, double x, double y, double z) {
        return name + ((locationDisplay.getValue() == LocDisplay.COORDS) ? " XYZ: " + String.format("%.0f", x) + " " + String.format("%.0f", y) + " " + String.format("%.0f", z) : " "+String.format("%.0f", mc.player.getDistance(x, y, z))+"m");
    }

    public void drawBorderedRect(double x, double y, double x1, double y1, double width, float borderWidth, int internalColor, int borderColor) {
        GL11.glPushMatrix();
        enableGL2D();
        RenderUtil.drawRect(x + width, y + width, x1 - width, y1 - width, internalColor);
        if(border.getValue()) {
            RenderUtil.drawBetterColoredRect(x + borderWidth, y + borderWidth, x1 - borderWidth, y1 - borderWidth, borderWidth, borderColor);
        }
        disableGL2D();
        GL11.glPopMatrix();
    }

    public static void enableGL2D() {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
    }

    public static void disableGL2D() {
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

    private double interpolate(final double previous, final double current, final float delta) {
        return previous + (current - previous) * delta;
    }

    private static class LogoutPos
    {
        private final String name;
        private final EntityPlayer entity;
        private final double x;
        private final double y;
        private final double z;

        public LogoutPos(final String name, final EntityPlayer entity) {
            this.name = name;
            this.entity = entity;
            this.x = entity.posX;
            this.y = entity.posY;
            this.z = entity.posZ;
        }

        public String getName() {
            return this.name;
        }

        public EntityPlayer getEntity() {
            return this.entity;
        }

        public double getX() {
            return this.x;
        }

        public double getY() {
            return this.y;
        }

        public double getZ() {
            return this.z;
        }
    }

    private enum LocDisplay {
        COORDS,
        DISTANCE
    }
}
