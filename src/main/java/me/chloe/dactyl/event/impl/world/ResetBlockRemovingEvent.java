package me.chloe.dactyl.event.impl.world;

import me.chloe.dactyl.event.ForgeEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class ResetBlockRemovingEvent extends ForgeEvent {
    public ResetBlockRemovingEvent() {
        this.setStage(Stage.PRE);
    }
}
