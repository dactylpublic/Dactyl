package me.fluffy.dactyl.module.impl.render;

import com.google.common.collect.Maps;
import me.fluffy.dactyl.event.impl.world.Render3DEvent;
import me.fluffy.dactyl.injection.inj.access.IEntityRenderer;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.module.impl.client.Colors;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.EntityUtil;
import me.fluffy.dactyl.util.MathUtil;
import me.fluffy.dactyl.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Map;

public class Tracers extends Module {
    public Setting<TracerMode> tracerModeSetting = new Setting<TracerMode>("Mode", TracerMode.LINE);
    public Setting<Boolean> players = new Setting<Boolean>("Players", true);
    public Setting<Boolean> mobs = new Setting<Boolean>("Monsters", false);
    public Setting<Boolean> animals = new Setting<Boolean>("Animals", false);
    public Setting<Boolean> invisibles = new Setting<Boolean>("Invisibles", true);
    public Setting<Double> distance = new Setting<Double>("Distance", 120d, 1d, 300d);
    public Setting<Boolean> offscreenOnly = new Setting<Boolean>("OffscreenOnly", true);
    public Setting<Double> radius = new Setting<Double>("Radius", 120d, 1d, 300d, v->tracerModeSetting.getValue() == TracerMode.ARROW);
    public Setting<Boolean> outline = new Setting<Boolean>("Outline", true, v->tracerModeSetting.getValue() == TracerMode.ARROW);
    public Setting<Double> outlineWidth = new Setting<Double>("OutlineWidth", 1.0d, 0.1d, 3.0d, v->tracerModeSetting.getValue() == TracerMode.ARROW);
    public Setting<Double> size = new Setting<Double>("Size", 0.3d, 0.1d, 5.0d);
    public Setting<Boolean> autoColor = new Setting<Boolean>("AutoColor", true);
    public Setting<Boolean> colorSync = new Setting<Boolean>("ColorSync", false, v->!autoColor.getValue());
    public Setting<Integer> red = new Setting<Integer>("Red", 255, 1, 255, v->!colorSync.getValue()&&!autoColor.getValue());
    public Setting<Integer> green = new Setting<Integer>("Green", 255, 1, 255, v->!colorSync.getValue()&&!autoColor.getValue());
    public Setting<Integer> blue = new Setting<Integer>("Blue", 255, 1, 255, v->!colorSync.getValue()&&!autoColor.getValue());
    public Setting<Integer> alpha = new Setting<Integer>("Alpha", 255, 1, 255, v->!autoColor.getValue());


    private static float partialTicks = 0.0F;
    private final EntityListener entityListener = new EntityListener();

    public Tracers() {
        super("Tracers", Category.RENDER);
    }

    @Override
    public void onClientUpdate() {
        this.setModuleInfo(tracerModeSetting.getValue() == TracerMode.ARROW ? "Arrow" : "Line");
    }

    @Override
    public void onScreen2D(float ticks) {
        if(mc.world == null || mc.player == null) {
            return;
        }
        if(tracerModeSetting.getValue() == TracerMode.ARROW) {
            partialTicks = ticks;
            this.entityListener.render();
            mc.world.loadedEntityList.forEach(o -> {
                Vec3d pos;
                Color color2 = null;
                Color color;
                int x;
                int y;
                float yaw;
                if (o instanceof EntityPlayer && o != mc.player && (!o.isInvisible() || this.invisibles.getValue()) && o.isEntityAlive()) {
                    if (mc.player.getDistance(o) <= distance.getValue()) {
                        EntityPlayer entity = (EntityPlayer) o;
                        pos = this.entityListener.getEntityLowerBounds().get(entity);
                        if (pos != null && !this.isOnScreen(pos) && (!RenderUtil.isInView((Entity) entity) || !this.offscreenOnly.getValue())) {
                            if (autoColor.getValue()) {
                                color2 = RenderUtil.distToColor(entity);
                            } else if (this.colorSync.getValue()) {
                                color2 = Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false));
                            } else {
                                color2 = new Color(red.getValue() / 255, green.getValue() / 255, blue.getValue() / 255, 1.0f);
                            }
                            color = color2;
                            x = Display.getWidth() / 2 / ((mc.gameSettings.guiScale == 0) ? 1 : mc.gameSettings.guiScale);
                            y = Display.getHeight() / 2 / ((mc.gameSettings.guiScale == 0) ? 1 : mc.gameSettings.guiScale);
                            yaw = this.getRotations((EntityLivingBase) entity) - mc.player.rotationYaw;
                            GL11.glTranslatef((float) x, (float) y, 0.0f);
                            GL11.glRotatef(yaw, 0.0f, 0.0f, 1.0f);
                            GL11.glTranslatef((float) (-x), (float) (-y), 0.0f);
                            RenderUtil.drawTracerPointer((float) x, (float) (y - this.radius.getValue()), this.size.getValue().floatValue() * 10, 2.0f, 1.0f, this.outline.getValue(), this.outlineWidth.getValue().floatValue(), color.getRGB(), alpha.getValue() / 255f);
                            GL11.glTranslatef((float) x, (float) y, 0.0f);
                            GL11.glRotatef(-yaw, 0.0f, 0.0f, 1.0f);
                            GL11.glTranslatef((float) (-x), (float) (-y), 0.0f);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if(mc.world == null || mc.player == null) {
            return;
        }
        if(tracerModeSetting.getValue() == TracerMode.LINE) {
            GlStateManager.pushMatrix();
            mc.world.loadedEntityList.stream().filter(e -> (e instanceof EntityLivingBase)).filter(entity -> (entity instanceof EntityPlayer) ? (this.players.getValue() && mc.player != entity) : (EntityUtil.isPassiveEntity(entity) ? this.animals.getValue() : ((boolean) this.mobs.getValue()))).filter(entity -> mc.player.getDistanceSq(entity) < MathUtil.square(this.distance.getValue())).filter(entity -> this.invisibles.getValue() || !entity.isInvisible()).forEach(entity -> {
                float[] entColor = RenderUtil.distanceToColor(entity);
                if(autoColor.getValue()) {
                    this.drawLineToEntity(entity, entColor[0], entColor[1], entColor[2], entColor[3]);
                } else if(colorSync.getValue()) {
                    Color currentColor = Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false));
                    this.drawLineToEntity(entity, currentColor.getRed() / 255f, currentColor.getGreen() / 255f, currentColor.getBlue() / 255f, alpha.getValue() / 255f);
                } else {
                    this.drawLineToEntity(entity, red.getValue() / 255f, green.getValue() / 255f, blue.getValue() / 255f, alpha.getValue() / 255f);
                }
                return;
            });
            GlStateManager.popMatrix();
        }
    }

    private float getRotations(final EntityLivingBase ent) {
        final double x = ent.posX - mc.player.posX;
        final double z = ent.posZ - mc.player.posZ;
        return (float)(-(Math.atan2(x, z) * 57.29577951308232));
    }

    private boolean isOnScreen(final Vec3d pos) {
        return pos.x > -1.0 && pos.y < 1.0 && pos.x / ((mc.gameSettings.guiScale == 0) ? 1 : mc.gameSettings.guiScale) >= 0.0 && pos.x / ((mc.gameSettings.guiScale == 0) ? 1 : mc.gameSettings.guiScale) <= Display.getWidth() && pos.y / ((mc.gameSettings.guiScale == 0) ? 1 : mc.gameSettings.guiScale) >= 0.0 && pos.y / ((mc.gameSettings.guiScale == 0) ? 1 : mc.gameSettings.guiScale) <= Display.getHeight();
    }

    public double interpolate(final double now, final double then) {
        return then + (now - then) * mc.getRenderPartialTicks();
    }

    public double[] interpolate(Entity entity) {
        final double posX = this.interpolate(entity.posX, entity.lastTickPosX) - mc.getRenderManager().viewerPosX;
        final double posY = this.interpolate(entity.posY, entity.lastTickPosY) - mc.getRenderManager().viewerPosY;
        final double posZ = this.interpolate(entity.posZ, entity.lastTickPosZ) - mc.getRenderManager().viewerPosZ;
        return new double[] { posX, posY, posZ };
    }

    public void drawLineToEntity(final Entity e, final float red, final float green, final float blue, final float opacity) {
        if(RenderUtil.isInView(e) && offscreenOnly.getValue()) {
            return;
        }
        final double[] xyz = this.interpolate(e);
        this.drawLine(xyz[0], xyz[1], xyz[2], e.height, red, green, blue, opacity);
    }

    public void drawLine(final double posx, final double posy, final double posz, final double up, final float red, final float green, final float blue, final float opacity) {
        final Vec3d eyes = new Vec3d(0.0, 0.0, 1.0).rotatePitch(-(float)Math.toRadians(mc.player.rotationPitch)).rotateYaw(-(float)Math.toRadians(mc.player.rotationYaw));
        this.drawLineFromPosToPos(eyes.x, eyes.y + mc.player.getEyeHeight(), eyes.z, posx, posy, posz, up, red, green, blue, opacity);
    }

    public void drawLineFromPosToPos(final double posx, final double posy, final double posz, final double posx2, final double posy2, final double posz2, final double up, final float red, final float green, final float blue, final float opacity) {
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glLineWidth(this.size.getValue().floatValue());
        GL11.glDisable(3553);
        GL11.glDisable(2929);
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
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glColor3d(1.0, 1.0, 1.0);
        GlStateManager.enableLighting();
    }


    private static class EntityListener
    {
        private final Map<Entity, Vec3d> entityUpperBounds;
        private final Map<Entity, Vec3d> entityLowerBounds;

        private EntityListener() {
            this.entityUpperBounds = Maps.newHashMap();
            this.entityLowerBounds = Maps.newHashMap();
        }

        private void render() {
            if (!this.entityUpperBounds.isEmpty()) {
                this.entityUpperBounds.clear();
            }
            if (!this.entityLowerBounds.isEmpty()) {
                this.entityLowerBounds.clear();
            }
            for (final Entity e : mc.world.loadedEntityList) {
                final Vec3d bound = this.getEntityRenderPosition(e);
                bound.add(new Vec3d(0.0, e.height + 0.2, 0.0));
                final Vec3d upperBounds = RenderUtil.to2D(bound.x, bound.y, bound.z);
                final Vec3d lowerBounds = RenderUtil.to2D(bound.x, bound.y - 2.0, bound.z);
                if (upperBounds != null && lowerBounds != null) {
                    this.entityUpperBounds.put(e, upperBounds);
                    this.entityLowerBounds.put(e, lowerBounds);
                }
            }
        }

        private Vec3d getEntityRenderPosition(final Entity entity) {
            final double partial = partialTicks;
            final double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partial - mc.getRenderManager().viewerPosX;
            final double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partial - mc.getRenderManager().viewerPosY;
            final double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partial - mc.getRenderManager().viewerPosZ;
            return new Vec3d(x, y, z);
        }

        public Map<Entity, Vec3d> getEntityLowerBounds() {
            return this.entityLowerBounds;
        }
    }

    private enum TracerMode {
        LINE,
        ARROW
    }
}
