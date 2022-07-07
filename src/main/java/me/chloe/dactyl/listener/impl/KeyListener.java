package me.chloe.dactyl.listener.impl;

import me.chloe.dactyl.Dactyl;
import me.chloe.dactyl.util.Bind;
import me.chloe.dactyl.event.impl.action.EventKeyPress;
import me.chloe.dactyl.listener.Listener;
import me.chloe.dactyl.module.Module;
import me.chloe.dactyl.setting.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class KeyListener extends Listener {
    public KeyListener() {
        super("KeyListener", "Listens for every key press and sends actions accordingly.");
    }

    @SubscribeEvent
    public void onKey(EventKeyPress event) {
        for(Module module : Dactyl.moduleManager.getModules()) {
            Setting<Bind> bindSetting = module.getSettingsList().get(0);
            if(bindSetting.getValue().getKey() == event.getKey()) {
                module.toggle();
            }
        }
    }
}
