package me.fluffy.dactyl.managers;

import me.fluffy.dactyl.listener.Listener;
import me.fluffy.dactyl.listener.impl.*;
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
