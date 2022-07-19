package me.chloe.moonlight.event.impl.world;

import me.chloe.moonlight.event.ForgeEvent;

public class Render3DEvent extends ForgeEvent {
    private final float partialTicks;
    public Render3DEvent(float partialTicks) {
        this.setStage(Stage.PRE);
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }
}
