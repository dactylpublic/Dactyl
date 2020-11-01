package me.fluffy.dactyl.event.impl.world;

import me.fluffy.dactyl.event.ForgeEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class BlockPushEvent extends ForgeEvent {
    public BlockPushEvent() {
        super();
        this.setStage(Stage.PRE);
    }
}
