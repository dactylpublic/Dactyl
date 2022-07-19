package me.chloe.moonlight.listener.impl;

import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.event.impl.action.CommandSendEvent;
import me.chloe.moonlight.listener.Listener;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CommandSendListener extends Listener {
    public CommandSendListener() {
        super("CommandSendListener", "Used for processing every command used in the client. (Cancelable)");
    }

    @SubscribeEvent
    public void onCommand(CommandSendEvent event) {
        Moonlight.commandManager.processCommand(event.getCmd(), event.getArgs());
    }
}
