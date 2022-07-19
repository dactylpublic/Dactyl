package me.chloe.moonlight.event.impl.action;

import me.chloe.moonlight.event.ForgeEvent;

public class KeyboardMoveEvent extends ForgeEvent {
    public KeyboardMoveEvent() {
        super();
        this.setStage(Stage.PRE);
    }
}
