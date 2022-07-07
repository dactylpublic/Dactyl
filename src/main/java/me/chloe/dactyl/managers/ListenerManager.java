package me.chloe.dactyl.managers;

import me.chloe.dactyl.listener.impl.*;
import me.chloe.dactyl.listener.Listener;
import me.chloe.dactyl.listener.impl.*;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;

public class ListenerManager {
    private final ArrayList<Listener> listeners;
    public ListenerManager() {
        listeners = new ArrayList<>();
        listeners.add(new KeyListener());
        listeners.add(new UpdateListener());
        listeners.add(new RenderListener());
        listeners.add(new SpeedListener());
        listeners.add(new TotemListener());
        listeners.add(new PlayerChatListener());
        listeners.add(new CommandSendListener());
        listeners.add(new ConnectionListener());
        registerListeners();
    }

    public void registerListeners() {
        for(Listener listener : listeners) {
            MinecraftForge.EVENT_BUS.register(listener);
        }
    }

    public ArrayList<Listener> getListeners() {
        return this.listeners;
    }
}
