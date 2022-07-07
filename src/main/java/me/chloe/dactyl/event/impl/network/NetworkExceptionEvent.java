package me.chloe.dactyl.event.impl.network;

import me.chloe.dactyl.event.ForgeEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class NetworkExceptionEvent extends ForgeEvent {
    public NetworkExceptionEvent() {
        this.setStage(Stage.PRE);
    }
}
