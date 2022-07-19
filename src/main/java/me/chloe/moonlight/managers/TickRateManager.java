package me.chloe.moonlight.managers;

import me.chloe.moonlight.event.impl.network.PacketEvent;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TickRateManager {
    private long prevTime;
    private float[] ticks = new float[20];
    private int currentTick;

    public TickRateManager() {
        this.prevTime = -1;
        for (int i = 0, len = this.ticks.length; i < len; i++) {
            this.ticks[i] = 0.0f;
        }
        MinecraftForge.EVENT_BUS.register(this);
    }

    public float getTickRate() {
        int tickCount = 0;
        float tickRate = 0.0f;

        for (int i = 0; i < this.ticks.length; i++) {
            final float tick = this.ticks[i];

            if (tick > 0.0f) {
                tickRate += tick;
                tickCount++;
            }
        }

        return MathHelper.clamp((tickRate / tickCount), 0.0f, 20.0f);
    }

    public void unload() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof SPacketTimeUpdate) {
            if (this.prevTime != -1) {
                this.ticks[this.currentTick % this.ticks.length] = MathHelper.clamp((20.0f / ((float) (System.currentTimeMillis() - this.prevTime) / 1000.0f)), 0.0f, 20.0f);
                this.currentTick++;
            }

            this.prevTime = System.currentTimeMillis();
        }
    }
}
