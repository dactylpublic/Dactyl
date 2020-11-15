package me.fluffy.dactyl.event.impl.player;

import me.fluffy.dactyl.event.ForgeEvent;

public class EventUpdateWalkingPlayer extends ForgeEvent {
    public EventUpdateWalkingPlayer(Stage stage) {
        this.setStage(stage);
    }
}
