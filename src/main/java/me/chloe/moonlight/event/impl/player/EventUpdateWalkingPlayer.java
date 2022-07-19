package me.chloe.moonlight.event.impl.player;

import me.chloe.moonlight.event.ForgeEvent;

public class EventUpdateWalkingPlayer extends ForgeEvent {
    private float yaw, pitch;
    public boolean rotationUsed;
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
        this.rotationUsed = true;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
        this.rotationUsed = true;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }
}
