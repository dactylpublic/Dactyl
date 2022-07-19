package me.chloe.moonlight.event.impl.render;

import me.chloe.moonlight.event.ForgeEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PotionHUDEvent extends ForgeEvent {
    public PotionHUDEvent() {
        super();
        this.setStage(Stage.PRE);
    }
}
