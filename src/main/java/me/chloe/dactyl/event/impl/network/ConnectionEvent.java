package me.chloe.dactyl.event.impl.network;

import me.chloe.dactyl.event.ForgeEvent;

public class ConnectionEvent extends ForgeEvent {
    private final String name;
    private final ConnectionType connectionType;
    public ConnectionEvent(String name, ConnectionType connectionType) {
        this.setStage(Stage.PRE);
        this.name = name;
        this.connectionType = connectionType;
    }

    public String getName() {
        if(this.name == null) {
            return "";
        }
        return this.name;
    }


    public ConnectionType getConnectionType() {
        return this.connectionType;
    }

    public enum ConnectionType {
        LOGIN,
        LOGOUT
    }



}
