package me.chloe.moonlight.event.impl.player;

import me.chloe.moonlight.event.ForgeEvent;
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
