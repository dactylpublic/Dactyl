package me.fluffy.dactyl.event.impl.network;

import me.fluffy.dactyl.event.ForgeEvent;

import java.util.UUID;

public class ConnectionEvent extends ForgeEvent {
    private final String name;
    private final UUID uuid;
    private final ConnectionType connectionType;
    public ConnectionEvent(String name, UUID uuid, ConnectionType connectionType) {
        this.setStage(Stage.PRE);
        this.name = name;
        this.uuid = uuid;
        this.connectionType = connectionType;
    }

    public String getName() {
        if(this.name == null) {
            return "";
        }
        return this.name;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public ConnectionType getConnectionType() {
        return this.connectionType;
    }

    public enum ConnectionType {
        LOGIN,
        LOGOUT
    }



}
