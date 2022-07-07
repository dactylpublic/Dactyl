package me.chloe.dactyl.event.impl.world;

import me.chloe.dactyl.event.ForgeEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class WaterPushEvent extends ForgeEvent {
    public WaterPushEvent() {
        super();
        this.setStage(Stage.PRE);
    }
}
