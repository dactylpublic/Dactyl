package me.fluffy.dactyl.event.impl.world;

import me.fluffy.dactyl.event.ForgeEvent;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.Vec3d;

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
