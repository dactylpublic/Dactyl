package me.fluffy.dactyl.listener.impl;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.listener.Listener;
import me.fluffy.dactyl.module.Module;
import net.minecraft.client.Minecraft;
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
}
