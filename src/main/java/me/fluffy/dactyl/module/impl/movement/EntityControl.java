package me.fluffy.dactyl.module.impl.movement;

import me.fluffy.dactyl.module.Module;

public class EntityControl extends Module {
    public static EntityControl INSTANCE;
    public EntityControl() {
        super("EntityControl", Category.MOVEMENT);
        INSTANCE = this;
    }
}
