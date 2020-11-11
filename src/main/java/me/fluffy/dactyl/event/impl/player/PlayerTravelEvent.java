package me.fluffy.dactyl.event.impl.player;

import me.fluffy.dactyl.event.ForgeEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PlayerTravelEvent extends ForgeEvent {
    public float strafe;

    public float vertical;

    public float forward;

    public PlayerTravelEvent(float strafe, float vertical, float forward) {
        this.setStage(Stage.PRE);
        this.strafe = strafe;
        this.vertical = vertical;
        this.forward = forward;
    }
}
