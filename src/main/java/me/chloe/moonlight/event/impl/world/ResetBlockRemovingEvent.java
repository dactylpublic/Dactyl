package me.chloe.moonlight.event.impl.world;

import me.chloe.moonlight.event.ForgeEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class ResetBlockRemovingEvent extends ForgeEvent {
    public ResetBlockRemovingEvent() {
        this.setStage(Stage.PRE);
    }
}
