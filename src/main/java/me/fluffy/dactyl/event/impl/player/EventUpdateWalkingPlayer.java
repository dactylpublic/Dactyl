package me.fluffy.dactyl.event.impl.player;

import me.fluffy.dactyl.event.ForgeEvent;

public class EventUpdateWalkingPlayer extends ForgeEvent {
    private float yaw, pitch;
    public EventUpdateWalkingPlayer(Stage stage, float yaw, float pitch) {
        this.setStage(stage);
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public EventUpdateWalkingPlayer(Stage stage) {
        this.setStage(stage);
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }
}
