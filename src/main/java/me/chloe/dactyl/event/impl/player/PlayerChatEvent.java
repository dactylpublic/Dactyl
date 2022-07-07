package me.chloe.dactyl.event.impl.player;

import me.chloe.dactyl.event.ForgeEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PlayerChatEvent extends ForgeEvent {
    private String message;
    public PlayerChatEvent(String message, Stage stage) {
        this.setStage(stage);
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String s) {
        this.message = s;
    }
}
