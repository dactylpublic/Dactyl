package me.fluffy.dactyl.module.impl.player;

import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.injection.inj.access.ICPacketUseEntity;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.ChatUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.*;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class BypassCrystal extends Module {
    public Setting<Boolean> antiKick = new Setting<Boolean>("IllegalCancel", true);
    public Setting<Integer> attacks = new Setting<Integer>("Iteration", 2, 1, 20);
    public Setting<Boolean> swing = new Setting<Boolean>("SwingPacket", true);
    public Setting<Boolean> debug = new Setting<Boolean>("Debug", false);
    public Setting<Integer> delay = new Setting<Integer>("Delay", 0, 0, 200);
    public static BypassCrystal INSTANCE;
    public BypassCrystal() {
        super("BypassCrystal", Category.PLAYER);
        INSTANCE = this;
    }
    private Item[] illegalItems = new Item[] {Items.ENDER_EYE, Items.POTIONITEM, Items.LINGERING_POTION, Items.SPLASH_POTION, Items.EXPERIENCE_BOTTLE, Items.STRING, Items.BOW, Items.ENDER_PEARL, Items.BOAT, Items.ACACIA_BOAT, Items.BIRCH_BOAT, Items.DARK_OAK_BOAT, Items.JUNGLE_BOAT, Items.SPRUCE_BOAT, Items.EGG};
    public static int biggestEntityID = -1337;

    @Override
    public void onEnable() {
        if(mc.world != null) {
            setHighestID();
        }
    }

    @Override
    public void onLogout() {
        this.biggestEntityID = -1337;
    }

    @Override
    public void onDisable() {
        this.biggestEntityID = -1337;
    }

    @Override
    public void onClientUpdate() {
        if (debug.getValue()) {
            for (Entity entity : mc.world.loadedEntityList) {
                if (!(entity instanceof EntityEnderCrystal)) continue;
                entity.setCustomNameTag(String.valueOf(entity.entityId));
                entity.setAlwaysRenderNameTag(true);
            }
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.OUTGOING) {
            if(event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
                CPacketPlayerTryUseItemOnBlock packet = (CPacketPlayerTryUseItemOnBlock)event.getPacket();
                if(mc.player.getHeldItem(packet.getHand()).getItem() instanceof ItemEndCrystal) {
                    // do prediction FUCK YOU
                    if(antiKick.getValue() && playerHoldingIllegal()) {
                        return;
                    }
                    setHighestID();
                    for (int x = 1; x < attacks.getValue(); ++x) {
                        attack(biggestEntityID + x);
                    }
                }
            }
         }
    }

    private void attack(int entityID) {
        Entity entity = mc.world.getEntityByID(entityID);
        if(entity == null || entity instanceof EntityEnderCrystal) {
            UseThread useThread = new UseThread(entityID, this.delay.getValue());
            if (delay.getValue() == 0) {
                useThread.run();
            } else {
                useThread.start();
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.INCOMING) {
            if (event.getPacket() instanceof SPacketSpawnObject) {
                this.setCheckID(((SPacketSpawnObject) event.getPacket()).getEntityID());
            } else if (event.getPacket() instanceof SPacketSpawnExperienceOrb) {
                this.setCheckID(((SPacketSpawnExperienceOrb) event.getPacket()).getEntityID());
            } else if (event.getPacket() instanceof SPacketSpawnPlayer) {
                this.setCheckID(((SPacketSpawnPlayer) event.getPacket()).getEntityID());
            } else if (event.getPacket() instanceof SPacketSpawnGlobalEntity) {
                this.setCheckID(((SPacketSpawnGlobalEntity) event.getPacket()).getEntityId());
            } else if (event.getPacket() instanceof SPacketSpawnPainting) {
                this.setCheckID(((SPacketSpawnPainting) event.getPacket()).getEntityID());
            } else if (event.getPacket() instanceof SPacketSpawnMob) {
                this.setCheckID(((SPacketSpawnMob) event.getPacket()).getEntityID());
            }
        }
    }

    public static class UseThread extends Thread {
        private final int id;
        private final int delay;

        public UseThread(int idIn, int delayIn) {
            this.id = idIn;
            this.delay = delayIn;
        }

        @Override
        public void run() {
            try {
                if (this.delay != 0) {
                    TimeUnit.MILLISECONDS.sleep(this.delay);
                }
                mc.addScheduledTask(() -> {
                    CPacketUseEntity attackPacket = new CPacketUseEntity();
                    ((ICPacketUseEntity) attackPacket).setEntityId(id);
                    ((ICPacketUseEntity) attackPacket).setAction(CPacketUseEntity.Action.ATTACK);
                    if (BypassCrystal.INSTANCE.debug.getValue()) {
                        ChatUtil.printMsg("BiggestID is " + biggestEntityID, true, false);
                        ChatUtil.printMsg("Attacked entity with id " + ((ICPacketUseEntity) attackPacket).getEntityID(), true, false);
                    }
                    mc.player.connection.sendPacket(attackPacket);
                    if (BypassCrystal.INSTANCE.swing.getValue()) {
                        mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                    }
                });
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void setCheckID(int id) {
        if (id > biggestEntityID) {
            biggestEntityID = id;
        }
    }

    private void setHighestID() {
        for (Entity entity : mc.world.loadedEntityList) {
            if (entity.getEntityId() <= biggestEntityID) continue;
            biggestEntityID = entity.getEntityId();
        }
    }

    private boolean playerHoldingIllegal() {
        for(Entity player : mc.world.loadedEntityList) {
            if(!(player instanceof EntityPlayer)) {
                continue;
            }
            if(((EntityPlayer)player).getHealth() <= 0.0f) {
                continue;
            }
            if(player.isDead) {
                continue;
            }
            if(isHoldingIllegal((EntityPlayer)player)) {
                return true;
            }
        }
        return false;
    }


    private boolean isHoldingIllegal(EntityPlayer player) {
        return (Arrays.asList(illegalItems).contains(player.getHeldItemMainhand().getItem()) || Arrays.asList(illegalItems).contains(player.getHeldItemOffhand().getItem()));
    }

}
