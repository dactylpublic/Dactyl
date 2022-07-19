package me.chloe.moonlight.event.impl.world;

import me.chloe.moonlight.event.ForgeEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class BlockPushEvent extends ForgeEvent {
    public BlockPushEvent() {
        super();
        this.setStage(Stage.PRE);
    }
}
