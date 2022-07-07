package me.chloe.dactyl.event.impl.world;

import me.chloe.dactyl.event.ForgeEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class BlockPushEvent extends ForgeEvent {
    public BlockPushEvent() {
        super();
        this.setStage(Stage.PRE);
    }
}
