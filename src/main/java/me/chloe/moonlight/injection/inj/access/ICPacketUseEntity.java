package me.chloe.moonlight.injection.inj.access;

import net.minecraft.network.play.client.CPacketUseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CPacketUseEntity.class)
public interface ICPacketUseEntity {
    @Accessor("entityId")
    public void setEntityId(int entityId);

    @Accessor("action")
    public void setAction(CPacketUseEntity.Action action);

    @Accessor("entityId")
    public int getEntityID();
}
