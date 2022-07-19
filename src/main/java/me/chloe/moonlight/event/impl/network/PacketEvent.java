package me.chloe.moonlight.event.impl.network;

import me.chloe.moonlight.event.ForgeEvent;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PacketEvent extends ForgeEvent {
    private final Packet packet;
    private final PacketType packetType;
    public PacketEvent(Stage stage, Packet packet, PacketType packetType) {
        this.setStage(stage);
        this.packet = packet;
        this.packetType = packetType;
    }

    public enum PacketType {
        INCOMING,
        OUTGOING
    }

    public Packet getPacket() {
        return this.packet;
    }

    public PacketType getType() {
        return this.packetType;
    }
}
