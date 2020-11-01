package me.fluffy.dactyl.event.impl.render;

import me.fluffy.dactyl.event.ForgeEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PotionHUDEvent extends ForgeEvent {
    public PotionHUDEvent() {
        super();
        this.setStage(Stage.PRE);
    }
}
