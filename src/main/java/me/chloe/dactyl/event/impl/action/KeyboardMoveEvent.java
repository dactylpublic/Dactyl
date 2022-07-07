package me.chloe.dactyl.event.impl.action;

import me.chloe.dactyl.event.ForgeEvent;

public class KeyboardMoveEvent extends ForgeEvent {
    public KeyboardMoveEvent() {
        super();
        this.setStage(Stage.PRE);
    }
}
