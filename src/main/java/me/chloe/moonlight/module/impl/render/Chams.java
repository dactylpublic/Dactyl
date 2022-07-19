package me.chloe.moonlight.module.impl.render;

import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.util.EntityUtil;
import me.chloe.moonlight.module.Module;
import me.chloe.moonlight.module.impl.client.Colors;
import me.chloe.moonlight.setting.Setting;
import me.chloe.moonlight.util.render.RenderUtil;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;

public class Chams extends Module {
    public Setting<ChamsPage> page = new Setting<ChamsPage>("Setting", ChamsPage.CRYSTALS);

    // crystals
    public Setting<CrystalMode> crystalESPMode = new Setting<CrystalMode>("CMode", CrystalMode.WIREFRAME, v->page.getValue() == ChamsPage.CRYSTALS);
    public Setting<Boolean> crystals = new Setting<Boolean>("Crystals", true, v->page.getValue() == ChamsPage.CRYSTALS);
    public Setting<Boolean> renderCrystals = new Setting<Boolean>("RenderCrystals", false, v->page.getValue() == ChamsPage.CRYSTALS);
    public Setting<Double> lineWidth = new Setting<Double>("Width", 1.5d, 0.1d, 3.0d, v->page.getValue() == ChamsPage.CRYSTALS);
    public Setting<Boolean> extraOutline = new Setting<Boolean>("ExtraOutline", true, v->page.getValue() == ChamsPage.CRYSTALS&&crystalESPMode.getValue() == CrystalMode.WIREFRAME);
    public Setting<Boolean> betterWireframe = new Setting<Boolean>("TrippyFrame", false, v->page.getValue() == ChamsPage.CRYSTALS&&crystalESPMode.getValue() == CrystalMode.WIREFRAME);
    public Setting<Boolean> crystalChamFill = new Setting<Boolean>("CrystalFill", true, v->page.getValue() == ChamsPage.CRYSTALS&&crystalESPMode.getValue() == CrystalMode.WIREFRAME);
    public Setting<Integer> crystalFillOpacity = new Setting<Integer>("FillAlpha", 100, 1, 255, v->page.getValue() == ChamsPage.CRYSTALS&&crystalESPMode.getValue() == CrystalMode.WIREFRAME&&crystalChamFill.getValue());
    public Setting<Boolean> crystalColorSync = new Setting<Boolean>("CrystalCSync", true, v->page.getValue() == ChamsPage.CRYSTALS);

    // players
    public Setting<Boolean> renderEntity = new Setting<Boolean>("RenderEntity", false, v->page.getValue() == ChamsPage.OTHER);
    public Setting<Boolean> entityExtraOutline = new Setting<Boolean>("EExtraOutline", false, v->page.getValue() == ChamsPage.OTHER);
    public Setting<Boolean> players = new Setting<Boolean>("Players", true, v->page.getValue() == ChamsPage.OTHER);
    public Setting<Boolean> monsters = new Setting<Boolean>("Monsters", true, v->page.getValue() == ChamsPage.OTHER);
    public Setting<Boolean> animals = new Setting<Boolean>("Animals", false, v->page.getValue() == ChamsPage.OTHER);
    public Setting<Boolean> invisibles = new Setting<Boolean>("Invisibles", true, v->page.getValue() == ChamsPage.OTHER);
    public Setting<Boolean> selfESP = new Setting<Boolean>("Self", false, v->page.getValue() == ChamsPage.OTHER);
    public Setting<Integer> entityFillOutline = new Setting<Integer>("EntityAlpha", 100, 1, 255, v->page.getValue() == ChamsPage.OTHER);
    public Setting<Boolean> colorSyncPlayer = new Setting<Boolean>("SyncAll", true, v->page.getValue() == ChamsPage.OTHER);

    public static Chams INSTANCE;
    public Chams() {
        super("Chams", Category.RENDER);
        INSTANCE = this;
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
        if(entityExtraOutline.getValue()) {
            RenderUtil.renderWireFrame(entityESPColor, lineWidth.getValue().floatValue(), modelBase, entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, 0.0f);
        }
        RenderUtil.renderWireFrameFill(entityESPColor, entityFillOutline.getValue(), lineWidth.getValue().floatValue(), modelBase, entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, 0.0f);
        try {
            mc.gameSettings.fancyGraphics = fancyGraphics;
            mc.gameSettings.gammaSetting = gamma;
        } catch (Exception exception) {}
        return true;
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

    private Color getColorOfEntity(Entity entity) {
        if(colorSyncPlayer.getValue()) {
            return Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false));
        }
        if(entity instanceof EntityPlayer) {
            if(Moonlight.friendManager.isFriend(((EntityPlayer)entity).getName())) {
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

    public enum CrystalMode {
        OUTLINE,
        WIREFRAME
    }


    public enum ChamsPage {
        OTHER,
        CRYSTALS
    }
}
