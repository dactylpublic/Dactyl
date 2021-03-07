package me.fluffy.dactyl.module.impl.player;

import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class PingSpoof extends Module {
    Setting<Integer> delay = new Setting<Integer>("Delay", 120, 10, 1400);
    public PingSpoof() {
        super("PingSpoof", Category.PLAYER);
    }

    private final HashMap<Packet<?>, Long> packetsMap = new HashMap<>();
    private TimeUtil timer = new TimeUtil();




    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(mc.world == null) {
            return;
        }
        if ((event.getPacket() instanceof CPacketKeepAlive) && !this.packetsMap.keySet().contains(event.getPacket())) {
            event.setCanceled(true);
            synchronized (this.packetsMap) {
                this.packetsMap.put((Packet<?>) event.getPacket(), System.currentTimeMillis() + delay.getValue().longValue());
            }
        }
    }

    @Override
    public void onClientUpdate() {
        if(mc.player.connection == null || mc.player == null) {
            return;
        }
        try {
            synchronized (this.packetsMap) {
                final Iterator<Map.Entry<Packet<?>, Long>> iterator = this.packetsMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    final Map.Entry<Packet<?>, Long> entry = iterator.next();
                    if(entry.getValue() <= System.currentTimeMillis()) {
                        mc.player.connection.sendPacket(entry.getKey());
                        iterator.remove();
                    }
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        this.packetsMap.clear();
    }

}
