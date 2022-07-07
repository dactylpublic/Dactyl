package me.chloe.dactyl.event.impl.action;

import me.chloe.dactyl.event.ForgeEvent;

public class EventKeyPress extends ForgeEvent {
    private final int key;
    public EventKeyPress(int key) {
        this.key = key;
    }

    public int getKey() {
        return this.key;
    }
}
