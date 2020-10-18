package me.fluffy.dactyl.module.impl.misc;

import com.mojang.authlib.GameProfile;
import me.fluffy.dactyl.module.Module;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import java.util.UUID;

public class FakePlayer extends Module {
    public FakePlayer() {
        super("FakePlayer", Category.MISC);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            this.toggle();
            return;
        }
        EntityOtherPlayerMP fake = new EntityOtherPlayerMP((World)mc.world, new GameProfile(UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"), "DactylClient"));
        fake.copyLocationAndAnglesFrom((Entity)mc.player);
        fake.inventory = mc.player.inventory;
        mc.world.addEntityToWorld(-4201337, (Entity)fake);
    }

    @Override
    public void onDisable() {
        if (mc.player == null || mc.world == null) {
            return;
        }
        mc.world.removeEntityFromWorld(-4201337);
    }
}
