package me.chloe.dactyl.listener.impl;

import me.chloe.dactyl.Dactyl;
import me.chloe.dactyl.event.impl.world.Render3DEvent;
import me.chloe.dactyl.listener.Listener;
import me.chloe.dactyl.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class UpdateListener extends Listener {
    public UpdateListener() {
        super("UpdateListener", "Handles update events (for modules)");
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if(Minecraft.getMinecraft().player == null) {
            return;
        }
        for(Module mod : Dactyl.moduleManager.getModules()) {
            if(mod.isEnabled() || mod.isAlwaysListening()) {
                mod.onClientUpdate();
            }
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (event.isCanceled()) {
            return;
        }
        Minecraft.getMinecraft().profiler.startSection("dactyl");
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        GlStateManager.disableDepth();
        GlStateManager.glLineWidth(1.0F);
        Render3DEvent render3DEvent = new Render3DEvent(event.getPartialTicks());
        Dactyl.moduleManager.onRender(render3DEvent);
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
        Minecraft.getMinecraft().profiler.endSection();
    }
}
