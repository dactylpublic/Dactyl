package me.chloe.dactyl.event.impl.player;

import me.chloe.dactyl.event.ForgeEvent;

public class PlayerMotionEvent extends ForgeEvent {
    public PlayerMotionEvent() {
        this.setStage(Stage.PRE);
    }
}
