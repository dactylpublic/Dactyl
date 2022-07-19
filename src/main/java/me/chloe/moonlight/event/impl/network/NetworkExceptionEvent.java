package me.chloe.moonlight.event.impl.network;

import me.chloe.moonlight.event.ForgeEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class NetworkExceptionEvent extends ForgeEvent {
    public NetworkExceptionEvent() {
        this.setStage(Stage.PRE);
    }
}
