package me.fluffy.dactyl.event.impl.network;

import me.fluffy.dactyl.event.ForgeEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class NetworkExceptionEvent extends ForgeEvent {
    public NetworkExceptionEvent() {
        this.setStage(Stage.PRE);
    }
}
