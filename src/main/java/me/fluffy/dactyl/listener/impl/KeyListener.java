package me.fluffy.dactyl.listener.impl;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.event.impl.action.EventKeyPress;
import me.fluffy.dactyl.listener.Listener;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.Bind;
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
