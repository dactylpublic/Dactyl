package me.chloe.moonlight.listener.impl;

import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.util.Bind;
import me.chloe.moonlight.event.impl.action.EventKeyPress;
import me.chloe.moonlight.listener.Listener;
import me.chloe.moonlight.module.Module;
import me.chloe.moonlight.setting.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class KeyListener extends Listener {
    public KeyListener() {
        super("KeyListener", "Listens for every key press and sends actions accordingly.");
    }

    @SubscribeEvent
    public void onKey(EventKeyPress event) {
        for(Module module : Moonlight.moduleManager.getModules()) {
            Setting<Bind> bindSetting = module.getSettingsList().get(0);
            if(bindSetting.getValue().getKey() == event.getKey()) {
                module.toggle();
            }
        }
    }
}
