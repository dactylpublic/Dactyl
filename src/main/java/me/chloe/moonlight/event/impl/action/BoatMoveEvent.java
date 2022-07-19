package me.chloe.moonlight.event.impl.action;

import me.chloe.moonlight.event.ForgeEvent;
import net.minecraft.entity.item.EntityBoat;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class BoatMoveEvent extends ForgeEvent {
    private final EntityBoat boat;
    public double x;
    public double y;
    public double z;
    public BoatMoveEvent(EntityBoat boat, double x, double y, double z) {
        this.setStage(Stage.PRE);
        this.boat = boat;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public EntityBoat getBoat() {
        return boat;
    }
}
