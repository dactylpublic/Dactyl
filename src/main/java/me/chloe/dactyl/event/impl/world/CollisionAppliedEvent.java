package me.chloe.dactyl.event.impl.world;

import me.chloe.dactyl.event.ForgeEvent;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class CollisionAppliedEvent extends ForgeEvent {
    public Entity entity;
    public CollisionAppliedEvent(Entity entity) {
        this.setStage(Stage.PRE);
        this.entity = entity;
    }
}
