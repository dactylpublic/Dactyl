package me.fluffy.dactyl.event.impl.player;

import me.fluffy.dactyl.event.ForgeEvent;

public class MoveEvent extends ForgeEvent {
    private double motionX, motionY, motionZ;

    public MoveEvent(double motionX, double motionY, double motionZ, Stage stage) {
        this.setStage(stage);
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
    }

    public double getX() {
        return this.motionX;
    }
    public double getY() {
        return this.motionY;
    }
    public double getZ() {
        return this.motionZ;
    }

    public void setX(double x) {
        this.motionX = x;
    }
    public void setY(double y) {
        this.motionY = y;
    }
    public void setZ(double z) {
        this.motionZ = z;
    }

    public void setMotion(double x, double y, double z) {
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
    }
}
