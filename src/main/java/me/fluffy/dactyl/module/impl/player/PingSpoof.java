package me.fluffy.dactyl.module.impl.player;

import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.network.Packet;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PingSpoof extends Module {
    Setting<Integer> delay = new Setting<Integer>("Delay", 120, 10, 14000);
    public PingSpoof() {
        super("PingSpoof", Category.PLAYER);
    }

    private final HashMap<Packet<?>, Long> packetsMap = new HashMap<>();
    private TimeUtil timer = new TimeUtil();

}
