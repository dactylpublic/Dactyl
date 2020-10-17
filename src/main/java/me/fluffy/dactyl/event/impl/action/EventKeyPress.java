package me.fluffy.dactyl.event.impl.action;

import me.fluffy.dactyl.event.ForgeEvent;

public class EventKeyPress extends ForgeEvent {
    private final int key;
    public EventKeyPress(int key) {
        this.key = key;
    }

    public int getKey() {
        return this.key;
    }
}
