package me.chloe.dactyl.event.impl.network;

import me.chloe.dactyl.event.ForgeEvent;

public class PlayerDisconnectEvent extends ForgeEvent {
    private String message;
    public PlayerDisconnectEvent(String string) {
        message = string.replaceAll("§.", "");
    }

    public String getMessage() {
        return message;
    }
}
