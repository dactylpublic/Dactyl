package me.fluffy.dactyl.event.impl.world;

import me.fluffy.dactyl.event.ForgeEvent;
import net.minecraft.entity.Entity;

public class EntityAddedEvent extends ForgeEvent {
    private final Entity entity;
    public EntityAddedEvent(Entity entity) {
        this.setStage(Stage.PRE);
        this.entity = entity;
    }

    public Entity getEntity() {
        return this.entity;
    }
}
