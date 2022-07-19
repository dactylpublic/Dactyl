package me.chloe.moonlight.event.impl.player;

import me.chloe.moonlight.event.ForgeEvent;

public class PlayerMotionEvent extends ForgeEvent {
    public PlayerMotionEvent() {
        this.setStage(Stage.PRE);
    }
}
