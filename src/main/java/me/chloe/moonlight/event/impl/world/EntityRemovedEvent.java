package me.chloe.moonlight.event.impl.world;

import me.chloe.moonlight.event.ForgeEvent;
import net.minecraft.entity.Entity;

public class EntityRemovedEvent extends ForgeEvent {
    private final Entity entity;
    public EntityRemovedEvent(Entity entity) {
        this.setStage(Stage.PRE);
        this.entity = entity;
    }

    public Entity getEntity() {
        return this.entity;
    }
}
