package me.chloe.moonlight.event.impl.player;

import me.chloe.moonlight.event.ForgeEvent;

public class UpdatePopEvent extends ForgeEvent {
    private final String user;
    private final int popCount;
    public UpdatePopEvent(String user, int popCount) {
        this.setStage(Stage.PRE);
        this.user = user;
        this.popCount = popCount;
    }

    public String getUser() {
        return this.user;
    }
    public int getPopCount() {
        return this.popCount;
    }
}
