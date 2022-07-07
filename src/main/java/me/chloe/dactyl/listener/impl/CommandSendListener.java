package me.chloe.dactyl.listener.impl;

import me.chloe.dactyl.Dactyl;
import me.chloe.dactyl.event.impl.action.CommandSendEvent;
import me.chloe.dactyl.listener.Listener;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CommandSendListener extends Listener {
    public CommandSendListener() {
        super("CommandSendListener", "Used for processing every command used in the client. (Cancelable)");
    }

    @SubscribeEvent
    public void onCommand(CommandSendEvent event) {
        Dactyl.commandManager.processCommand(event.getCmd(), event.getArgs());
    }
}
