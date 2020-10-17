package me.fluffy.dactyl.event.impl.network;

import me.fluffy.dactyl.event.ForgeEvent;
import net.minecraft.network.Packet;

public class PacketEvent extends ForgeEvent {
    private final Packet packet;
    private final PacketType packetType;
    public PacketEvent(Packet packet, PacketType packetType) {
        this.setStage(Stage.PRE);
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
