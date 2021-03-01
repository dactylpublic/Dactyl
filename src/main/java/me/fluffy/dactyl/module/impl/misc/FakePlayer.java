package me.fluffy.dactyl.module.impl.misc;

import com.mojang.authlib.GameProfile;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FakePlayer extends Module {
    Setting<String> username = new Setting<String>("Name", "Catgirl");
    Setting<Boolean> copyPotions = new Setting<Boolean>("CopyPotions", true);
    public FakePlayer() {
        super("FakePlayer", Category.MISC);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            this.toggle();
            return;
        }
        EntityOtherPlayerMP fake = new EntityOtherPlayerMP((World)mc.world, new GameProfile(mc.session.getProfile().getId(), username.getValue()));
        fake.copyLocationAndAnglesFrom(mc.player);
        fake.inventory.copyInventory(mc.player.inventory);
        if(copyPotions.getValue()) {
            for (Map.Entry<Potion,PotionEffect> entry : mc.player.activePotionsMap.entrySet()) {
                fake.addPotionEffect(entry.getValue());
            }
        }
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
