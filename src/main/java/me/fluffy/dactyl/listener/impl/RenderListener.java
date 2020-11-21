package me.fluffy.dactyl.listener.impl;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.listener.Listener;
import me.fluffy.dactyl.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class RenderListener extends Listener {
    public RenderListener() {
        super("RenderListener", "Handles module events that render text on screen and such.");
    }

    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        RenderGameOverlayEvent.ElementType target = (!mc.player.isCreative() && mc.player.getRidingEntity() instanceof AbstractHorse) ? RenderGameOverlayEvent.ElementType.HEALTHMOUNT : RenderGameOverlayEvent.ElementType.EXPERIENCE;
        if(event.getType() == target) {
            for (Module module : Dactyl.moduleManager.getModules()) {
                if(module.isEnabled() || module.isAlwaysListening()) {
                    module.onScreen();
                    module.onScreen2D(event.getPartialTicks());
                }
            }
            GL11.glPushMatrix();
            GL11.glPopMatrix();
            GlStateManager.enableCull();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.enableDepth();
        }
    }
}
