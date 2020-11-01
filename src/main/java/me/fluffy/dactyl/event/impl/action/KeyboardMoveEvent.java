package me.fluffy.dactyl.event.impl.action;

import me.fluffy.dactyl.event.ForgeEvent;

public class KeyboardMoveEvent extends ForgeEvent {
    public KeyboardMoveEvent() {
        super();
        this.setStage(Stage.PRE);
    }
}
