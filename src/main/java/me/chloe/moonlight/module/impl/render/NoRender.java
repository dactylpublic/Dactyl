package me.chloe.moonlight.module.impl.render;

import me.chloe.moonlight.setting.Setting;
import me.chloe.moonlight.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.MobEffects;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoRender extends Module {
    public Setting<Boolean> fire = new Setting<Boolean>("NoFire", true);
    public Setting<Boolean> portalAnim = new Setting<Boolean>("Portals", true);
    public Setting<Boolean> pumpkin = new Setting<Boolean>("Pumpkin", true);
    public Setting<Boolean> totemPops = new Setting<Boolean>("TotemAnims", true);
    public Setting<Boolean> noItems = new Setting<Boolean>("NoItems", false);
    public Setting<Boolean> nausea = new Setting<Boolean>("Nausea", true);
    public Setting<Boolean> hurtCam = new Setting<Boolean>("NoHurtcam", true);
    public Setting<Boolean> noBoss = new Setting<Boolean>("NoBoss", false);
    public Setting<Boolean> noSkylight = new Setting<Boolean>("NoSkylight", true);
    public Setting<Boolean> noArmor = new Setting<Boolean>("NoArmor", false);
    public Setting<Boolean> noWeather = new Setting<Boolean>("NoWeather", true);
    public Setting<Boolean> liquidVision = new Setting<Boolean>("LiquidVision", true);
    public Setting<Boolean> noBlocks = new Setting<Boolean>("BlockOverlay", true);

    public static NoRender INSTANCE;
    public NoRender() {
        super("NoRender", Category.RENDER);
        INSTANCE = this;
    }

    @Override
    public void onClientUpdate() {
        if(mc.world == null) {
            return;
        }
        if (noItems.getValue()) {
            mc.world.loadedEntityList.stream().filter(EntityItem.class::isInstance).map(EntityItem.class::cast).forEach(Entity::setDead);
        }
        if (this.noWeather.getValue() && NoRender.mc.world.isRaining()) {
            NoRender.mc.world.setRainStrength(0.0f);
        }

        if(mc.player == null) {
            return;
        }
        if(nausea.getValue()) {
            mc.player.removeActivePotionEffect(MobEffects.BLINDNESS);
            mc.player.removeActivePotionEffect(MobEffects.NAUSEA);
        }
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.BOSSINFO && noBoss.getValue()) {
            event.setCanceled(true);
        }
        if(event.getType() == RenderGameOverlayEvent.ElementType.PORTAL && portalAnim.getValue()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void noFogInLiquid(EntityViewRenderEvent.FogDensity event) {
        if((mc.player.isInLava() || mc.player.isInWater()) && liquidVision.getValue()) {
            event.setDensity(0.0f);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onOverlay(RenderBlockOverlayEvent event) {
        boolean cancel = false;
        switch((RenderBlockOverlayEvent.OverlayType)event.getOverlayType()) {
            case FIRE:
                if(fire.getValue()) {
                    cancel = true;
                }
                break;
            case BLOCK:
                if(noBlocks.getValue()) {
                    cancel = true;
                }
                break;
            case WATER:
                if(liquidVision.getValue()) {
                    cancel = true;
                }
                break;
        }
        event.setCanceled(cancel);
    }

    @SubscribeEvent
    public void onGameOverlay(RenderGameOverlayEvent event) {

    }
}
