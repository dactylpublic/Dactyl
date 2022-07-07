package me.chloe.dactyl.event;

import net.minecraftforge.fml.common.eventhandler.Event;

public class ForgeEvent extends Event {
    private Stage stage;

    public ForgeEvent() {}

    public Stage getStage() {
        return this.stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public enum Stage {
        PRE,
        POST
    }
}
