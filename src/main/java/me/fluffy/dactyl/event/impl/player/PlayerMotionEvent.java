package me.fluffy.dactyl.event.impl.player;

import me.fluffy.dactyl.event.ForgeEvent;

public class PlayerMotionEvent extends ForgeEvent {
    public PlayerMotionEvent() {
        this.setStage(Stage.PRE);
    }
}
