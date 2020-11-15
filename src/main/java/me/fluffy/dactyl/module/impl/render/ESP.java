package me.fluffy.dactyl.module.impl.render;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.EntityUtil;
import me.fluffy.dactyl.util.render.OutlineUtils;
import me.fluffy.dactyl.util.render.RenderUtil;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;

public class ESP extends Module {
    public Setting<ESPMode> espModeSetting = new Setting<ESPMode>("Mode", ESPMode.OUTLINE);
    public Setting<Boolean> players = new Setting<Boolean>("Players", true);
    public Setting<Boolean> monsters = new Setting<Boolean>("Monsters", true);
    public Setting<Boolean> animals = new Setting<Boolean>("Animals", true);
    public Setting<Boolean> crystals = new Setting<Boolean>("Crystals", true);
    public Setting<Boolean> invisibles = new Setting<Boolean>("Invisibles", true);
    public Setting<Boolean> selfESP = new Setting<Boolean>("Self", false);
    public Setting<Boolean> renderEntity = new Setting<Boolean>("RenderEntity", true);
    public Setting<Boolean> renderCrystals = new Setting<Boolean>("RenderCrystals", true);
    public Setting<Boolean> crystalColorSync = new Setting<Boolean>("CrystalCSync", true);
    public Setting<Double> lineWidth = new Setting<Double>("Width", 1.0d, 0.1d, 3.0d);

    public static ESP INSTANCE;
    public ESP() {
        super("ESP", Category.RENDER);
        INSTANCE = this;
    }


    public boolean onRenderEntity(ModelBase modelBase, Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if(!passesSettingsCheck(entityIn)) {
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
