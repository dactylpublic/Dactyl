package me.fluffy.dactyl.module.impl.render;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.event.impl.world.Render3DEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.module.impl.client.Colors;
import me.fluffy.dactyl.module.impl.client.HUD;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.EntityUtil;
import me.fluffy.dactyl.util.StringUtil;
import me.fluffy.dactyl.util.render.OutlineUtils;
import me.fluffy.dactyl.util.render.RenderUtil;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ESP extends Module {
    public Setting<ESPMode> espModeSetting = new Setting<ESPMode>("Mode", ESPMode.OUTLINE);
    public Setting<Boolean> players = new Setting<Boolean>("Players", true);
    public Setting<Boolean> monsters = new Setting<Boolean>("Monsters", true);
    public Setting<Boolean> animals = new Setting<Boolean>("Animals", false);
    public Setting<Boolean> invisibles = new Setting<Boolean>("Invisibles", true);
    public Setting<Boolean> selfESP = new Setting<Boolean>("Self", false);
    public Setting<Boolean> mobOwner = new Setting<Boolean>("MobOwner", true);
    public Setting<Boolean> renderEntity = new Setting<Boolean>("RenderEntity", true);
    public Setting<Boolean> itemESP = new Setting<Boolean>("Items", true);
    public Setting<Boolean> itemNameTags = new Setting<Boolean>("ItemNameTags", true);
    public Setting<Boolean> itemESPFill = new Setting<Boolean>("ItemFill", false, v->itemESP.getValue());
    public Setting<Boolean> pearls = new Setting<Boolean>("Pearls", true);
    public Setting<Boolean> pearlFill = new Setting<Boolean>("PearlFill", false, v->pearls.getValue());
    public Setting<Boolean> xpBottles = new Setting<Boolean>("XPBottles", true);
    public Setting<Boolean> xpFill = new Setting<Boolean>("XPFill", false, v->xpBottles.getValue());
    public Setting<Double> lineWidth = new Setting<Double>("Width", 1.0d, 0.1d, 3.0d);

    public static ESP INSTANCE;
    public ESP() {
        super("ESP", Category.RENDER);
        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        if(mc.world == null) {
            return;
        }
        for(Entity entity: mc.world.loadedEntityList) {
            if(entity instanceof EntityTameable || entity instanceof AbstractHorse || entity instanceof EntityItem) {
                try {
                    entity.setAlwaysRenderNameTag(false);
                } catch(Exception ignored) {
                }
            }
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if(mc.world == null || mc.player == null) {
            return;
        }
        if(itemESP.getValue()) {
            int i = 0;
            for(Entity entity : mc.world.loadedEntityList) {
                if (entity instanceof EntityItem && ESP.mc.player.getDistanceSq(entity) < 2500.0) {
                    doRenderEntityOutlineFill(entity, itemESPFill.getValue());
                    if (++i >= 50) {
                        break;
                    }
                    continue;
                }
            }
        }

        if(itemNameTags.getValue()) {
            for(Entity entity : mc.world.loadedEntityList) {
                if (entity instanceof EntityItem && ESP.mc.player.getDistanceSq(entity) < 2500.0) {
                    this.drawText(entity);
                    continue;
                }
            }
        }

        if(mobOwner.getValue()) {
            int i = 0;
            for(Entity entity : mc.world.loadedEntityList) {
                if (entity instanceof EntityTameable && ESP.mc.player.getDistanceSq(entity) < 2500.0) {
                    EntityTameable tameable = (EntityTameable) entity;
                    if(tameable.isTamed() && tameable.getOwner() != null) {
                        tameable.setAlwaysRenderNameTag(true);
                        tameable.setCustomNameTag("Owner: " + tameable.getOwner().getDisplayName().getFormattedText());
                    }
                    if (++i >= 50) {
                        break;
                    }
                    continue;
                }
            }
        }
        if(pearls.getValue()) {
            int i = 0;
            for(Entity entity : mc.world.loadedEntityList) {
                if (entity instanceof EntityEnderPearl && ESP.mc.player.getDistanceSq(entity) < 2500.0) {
                    doRenderEntityOutlineFill(entity, pearlFill.getValue());
                    if (++i >= 50) {
                        break;
                    }
                    continue;
                }
            }
        }
        if(xpBottles.getValue()) {
            int i = 0;
            for(Entity entity : mc.world.loadedEntityList) {
                if (entity instanceof EntityExpBottle && ESP.mc.player.getDistanceSq(entity) < 2500.0) {
                    doRenderEntityOutlineFill(entity, xpFill.getValue());
                    if (++i >= 50) {
                        break;
                    }
                    continue;
                }
            }
        }
    }

    private void drawText(final Entity entityIn) {
        if(entityIn == null || entityIn.getDisplayName() == null) {
            return;
        }
        GlStateManager.pushMatrix();
        final double scale = 1.0;
        final String name = (entityIn instanceof EntityItem) ? ((EntityItem)entityIn).getItem().getDisplayName() : ((entityIn instanceof EntityEnderPearl) ? "Thrown Ender Pearl" : ((entityIn instanceof EntityExpBottle) ? "Thrown Exp Bottle" : "null"));
        final Vec3d interp = EntityUtil.getInterpolatedRenderPos(entityIn, mc.getRenderPartialTicks());
        final float yAdd = entityIn.height / 2.0f + 0.5f;
        final double x = interp.x;
        final double y = interp.y + yAdd;
        final double z = interp.z;
        final float viewerYaw = mc.getRenderManager().playerViewY;
        final float viewerPitch = mc.getRenderManager().playerViewX;
        final boolean isThirdPersonFrontal = mc.getRenderManager().options.thirdPersonView == 2;
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-viewerYaw, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate((isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0f, 0.0f, 0.0f);
        final float f = mc.player.getDistance(entityIn);
        final float m = f / 8.0f * (float)Math.pow(1.258925437927246, scale);
        GlStateManager.scale(m, m, m);
        GlStateManager.scale(-0.025f, -0.025f, 0.025f);
        final String str = name + ((entityIn instanceof EntityItem) ? (" (" + ((EntityItem)entityIn).getItem().getCount()) + ")" : "");
        final int i = mc.fontRenderer.getStringWidth(str) / 2;
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f);
        if (HUD.INSTANCE.customFont.getValue()) {
            Dactyl.fontUtil.drawStringWithShadow(str, -i, 9, Color.WHITE.getRGB());
        } else {
            GlStateManager.enableTexture2D();
            mc.fontRenderer.drawStringWithShadow(str, (float)(-i), 9.0f, Color.WHITE.getRGB());
            GlStateManager.disableTexture2D();
        }
        GlStateManager.glNormal3f(0.0f, 0.0f, 0.0f);
        GlStateManager.popMatrix();
    }

    private void doRenderEntityOutlineFill(Entity entity, boolean doFill) {
        final Vec3d interp = EntityUtil.getInterpolatedRenderPos(entity, ESP.mc.getRenderPartialTicks());
        final AxisAlignedBB bb = new AxisAlignedBB(entity.getEntityBoundingBox().minX - 0.05 - entity.posX + interp.x, entity.getEntityBoundingBox().minY - 0.0 - entity.posY + interp.y, entity.getEntityBoundingBox().minZ - 0.05 - entity.posZ + interp.z, entity.getEntityBoundingBox().maxX + 0.05 - entity.posX + interp.x, entity.getEntityBoundingBox().maxY + 0.1 - entity.posY + interp.y, entity.getEntityBoundingBox().maxZ + 0.05 - entity.posZ + interp.z);
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glLineWidth(1.0f);
        Color itemESPColor = (Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false)));
        if(doFill) {
            RenderGlobal.renderFilledBox(bb, (itemESPColor.getRed() / 255f), (itemESPColor.getGreen() / 255f), (itemESPColor.getBlue() / 255f), 66f / 255f);
        }
        GL11.glDisable(2848);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        RenderUtil.drawBlockOutline(bb, itemESPColor, 1.0f);
    }


    public boolean onRenderEntity(ModelBase modelBase, Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if(!passesSettingsCheck(entityIn)) {
            return false;
        }
        if(!RenderUtil.isInView(entityIn)) {
            return false;
        }
        boolean fancyGraphics = mc.gameSettings.fancyGraphics;
        mc.gameSettings.fancyGraphics = false;
        if(renderEntity.getValue()) {
            modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        }
        float gamma = mc.gameSettings.gammaSetting;
        mc.gameSettings.gammaSetting = 10000.0F;
        Color entityESPColor = getColorOfEntity(entityIn);
        if(espModeSetting.getValue() == ESPMode.WIREFRAME) {
            RenderUtil.renderWireFrame(entityESPColor, lineWidth.getValue().floatValue(), modelBase, entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, 0.0f);
        } else if(espModeSetting.getValue() == ESPMode.OUTLINE) {
            GlStateManager.resetColor();
            OutlineUtils.setColor(entityESPColor);
            OutlineUtils.renderOne(lineWidth.getValue().floatValue());
            modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            OutlineUtils.setColor(entityESPColor);
            OutlineUtils.renderTwo();
            modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            OutlineUtils.setColor(entityESPColor);
            OutlineUtils.renderThree();
            modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            OutlineUtils.setColor(entityESPColor);
            OutlineUtils.renderFour(entityESPColor);
            modelBase.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            OutlineUtils.setColor(entityESPColor);
            OutlineUtils.renderFive();
            OutlineUtils.setColor(Color.WHITE);
        }
        try {
            mc.gameSettings.fancyGraphics = fancyGraphics;
            mc.gameSettings.gammaSetting = gamma;
        } catch (Exception exception) {}
        return true;
    }

    private Color getColorOfEntity(Entity entity) {
        if(entity instanceof EntityPlayer) {
            if(Dactyl.friendManager.isFriend(((EntityPlayer)entity).getName())) {
                return new Color(85, 255, 255);
            } else {
                return new Color(255, 85, 85);
            }
        }
        if(EntityUtil.isPassiveEntity(entity)) {
            return new Color(85, 255, 85, 255);
        }
        if(!EntityUtil.isPassiveEntity(entity) && !(entity instanceof EntityPlayer)) {
            return new Color(255, 85, 85);
        }
        return new Color(85, 255, 85, 255);
    }


    private boolean passesSettingsCheck(Entity entity) {
        boolean doesPass = true;
        if(entity == null) {
            doesPass = false;
        }
        if(entity.isInvisible() && !invisibles.getValue()) {
            doesPass = false;
        }
        if(entity.equals(mc.player) && !selfESP.getValue()) {
            doesPass = false;
        }
        if(entity instanceof EntityPlayer && !players.getValue()) {
            doesPass = false;
        }
        if(EntityUtil.isPassiveEntity(entity) && !animals.getValue()) {
            doesPass = false;
        }
        if(!EntityUtil.isPassiveEntity(entity) && !(entity instanceof EntityPlayer) && !monsters.getValue()) {
            doesPass = false;
        }
        return doesPass;
    }



    public enum ESPMode {
        OUTLINE,
        WIREFRAME
    }
}
