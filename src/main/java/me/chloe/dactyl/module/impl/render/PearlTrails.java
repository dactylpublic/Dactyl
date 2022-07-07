package me.chloe.dactyl.module.impl.render;

import me.chloe.dactyl.event.impl.world.Render3DEvent;
import me.chloe.dactyl.injection.inj.access.IEntityRenderer;
import me.chloe.dactyl.injection.inj.access.IRenderManager;
import me.chloe.dactyl.module.Module;
import me.chloe.dactyl.module.impl.client.Colors;
import me.chloe.dactyl.setting.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3d;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PearlTrails extends Module {
    public Setting<Boolean> colorSync = new Setting<Boolean>("Sync", true);
    public Setting<Boolean> gradient = new Setting<Boolean>("Gradient", true);
    public Setting<Integer> red = new Setting<Integer>("Red", 0, 0, 255, v -> !colorSync.getValue());
    public Setting<Integer> green = new Setting<Integer>("Green", 255, 0, 255, v -> !colorSync.getValue());
    public Setting<Integer> blue = new Setting<Integer>("Blue", 0, 0, 255, v -> !colorSync.getValue());
    public Setting<Double> width = new Setting<Double>("Width", 3d, 1d, 5d);

    public PearlTrails() {
        super("PearlTrails", Category.RENDER, "csgo");
    }
    // entityid, EntityRenderVec
    private HashMap<Integer, EntityRenderVec> locs = new HashMap<Integer, EntityRenderVec>();
    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        attemptClear();
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if(mc.world == null || mc.player == null) {
            return;
        }
        /*for (Map.Entry<Integer, EntityRenderVec> entry : locs.entrySet()) {
            GL11.glDisable(2929);
            GL11.glDisable(3553);
            GL11.glColor3d(1.0D, 1.0D, 1.0D);
            GL11.glEnable(2848);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glPushMatrix();
            GL11.glLineWidth(width.getValue().floatValue());
            GL11.glBegin(3);
            if(!gradient.getValue()) {
                Color currentColor = (colorSync.getValue() ? Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false)) : new Color(red.getValue(), green.getValue(), blue.getValue()));
                GL11.glColor4f(currentColor.getRed()/255f, currentColor.getGreen()/255f, currentColor.getBlue()/255f, 1.0f);
            }
            for (ColorVector3d entityLineLoc : ((EntityRenderVec)entry.getValue()).vectors) {
                if(gradient.getValue()) {
                    GL11.glColor4f(entityLineLoc.color.getRed()/255f, entityLineLoc.color.getGreen()/255f, entityLineLoc.color.getBlue()/255f, 1.0f);
                }
                GL11.glVertex3d(entityLineLoc.vector3d.x - ((IRenderManager) mc.getRenderManager()).getRenderPosX(), entityLineLoc.vector3d.y - ((IRenderManager) mc.getRenderManager()).getRenderPosY(), entityLineLoc.vector3d.z - ((IRenderManager) mc.getRenderManager()).getRenderPosZ());
            }
            GL11.glEnd();
            GL11.glPopMatrix();
            GL11.glDisable(3042);
            GL11.glDisable(2848);
            GL11.glEnable(3553);
            GL11.glEnable(2929);
        }*/

        for (Map.Entry<Integer, EntityRenderVec> entry : locs.entrySet()) {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glLineWidth(width.getValue().floatValue());
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
            GlStateManager.disableLighting();
            GL11.glLoadIdentity();
            ((IEntityRenderer)mc.entityRenderer).orient(mc.getRenderPartialTicks());
            GL11.glBegin(GL11.GL_LINE_STRIP);
            for (ColorVector3d entityLineLoc : ((EntityRenderVec)entry.getValue()).vectors) {
                if(gradient.getValue()) {
                    GL11.glColor4f(entityLineLoc.color.getRed()/255f, entityLineLoc.color.getGreen()/255f, entityLineLoc.color.getBlue()/255f, 1.0f);
                } else {
                    Color currentColor = (colorSync.getValue() ? Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false)) : new Color(red.getValue(), green.getValue(), blue.getValue()));
                    GL11.glColor4f(currentColor.getRed()/255f, currentColor.getGreen()/255f, currentColor.getBlue()/255f, 1.0f);
                }
                GL11.glVertex3d(entityLineLoc.vector3d.x - ((IRenderManager) mc.getRenderManager()).getRenderPosX(), entityLineLoc.vector3d.y - ((IRenderManager) mc.getRenderManager()).getRenderPosY(), entityLineLoc.vector3d.z - ((IRenderManager) mc.getRenderManager()).getRenderPosZ());
            }
            GL11.glEnd();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glColor3d(1.0, 1.0, 1.0);
            GlStateManager.enableLighting();
        }

        // add pearls from loaded entity list
        for(Entity entity : mc.world.loadedEntityList) {
            if (entity != null) {
                if(entity instanceof EntityEnderPearl) {
                    EntityEnderPearl pearl = (EntityEnderPearl) entity;
                    Color color = (colorSync.getValue() ? Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor((int) pearl.posY, false)) : new Color(red.getValue(), green.getValue(), blue.getValue()));
                    if(locs.containsKey(pearl.entityId)) {
                        EntityRenderVec renderVec = locs.get(pearl.entityId);
                        renderVec.vectors.add(new ColorVector3d(new Vector3d(pearl.posX, pearl.posY, pearl.posZ), color));
                    } else {
                        locs.put(pearl.entityId, new EntityRenderVec(new ColorVector3d(new Vector3d(pearl.posX, pearl.posY, pearl.posZ), color), pearl.entityId));
                    }
                }
            }
        }
    }

    public void drawLineFromPosToPos(final double posx, final double posy, final double posz, final double posx2, final double posy2, final double posz2, final double up, final float red, final float green, final float blue, final float opacity) {
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glLineWidth(width.getValue().floatValue());
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glColor4f(red, green, blue, opacity);
        GlStateManager.disableLighting();
        GL11.glLoadIdentity();
        ((IEntityRenderer)mc.entityRenderer).orient(mc.getRenderPartialTicks());
        GL11.glBegin(1);
        GL11.glVertex3d(posx, posy, posz);
        GL11.glVertex3d(posx2, posy2, posz2);
        GL11.glVertex3d(posx2, posy2, posz2);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor3d(1.0, 1.0, 1.0);
        GlStateManager.enableLighting();
    }

    private void attemptClear() {
        try {
            Iterator<Integer> entityRenderVecIterator = locs.keySet().iterator();
            while (entityRenderVecIterator.hasNext()) {
                Integer next = entityRenderVecIterator.next();
                if(mc.world != null) {
                    if(mc.world.getEntityByID(locs.get(next).entityId) == null) {
                        entityRenderVecIterator.remove();
                    }
                }
            }
        } catch (Exception ignored) {}
    }


    public class EntityRenderVec {
        public ArrayList<ColorVector3d> vectors;
        public int entityId;
        public EntityRenderVec(ColorVector3d firstPosition, int entityId) {
            this.vectors = new ArrayList<>();
            this.vectors.add(firstPosition);
            this.entityId = entityId;
        }
    }

    public class ColorVector3d {
        public Vector3d vector3d;
        public Color color;
        public ColorVector3d(Vector3d vector3d, Color color) {
            this.vector3d = vector3d;
            this.color = color;
        }
    }
}
