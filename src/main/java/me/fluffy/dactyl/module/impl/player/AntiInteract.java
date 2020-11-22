package me.fluffy.dactyl.module.impl.player;

import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiInteract extends Module {
    Setting<Boolean> foodCheck = new Setting<Boolean>("FoodCheck", true, "Only AntiInteract if holding food");
    Setting<HandMode> handCheck = new Setting<HandMode>("FoodHand", HandMode.BOTH, v->foodCheck.getValue());
    Setting<Boolean> antiPlace = new Setting<Boolean>("CrystalPlace", true, "Don't place crystal if one in mainhand and food in offhand");
    Setting<Boolean> echests = new Setting<Boolean>("EChests", true);
    Setting<Boolean> hoppers = new Setting<Boolean>("Hoppers", true);
    Setting<Boolean> anvils = new Setting<Boolean>("Anvils", true);

    public AntiInteract() {
        super("AntiInteract", Category.PLAYER);
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        doAntiInteract(event);
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        doAntiInteract(event);
    }


    private void doAntiInteract(Object fin) {
        if(doHandCheck()) {
            return;
        }
        if(fin instanceof PacketEvent) {
            PacketEvent event = (PacketEvent)fin;
            if(event.getType() == PacketEvent.PacketType.OUTGOING) {
                if(event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
                    CPacketPlayerTryUseItemOnBlock packet = (CPacketPlayerTryUseItemOnBlock)event.getPacket();
                    Block block = mc.world.getBlockState(packet.getPos()).getBlock();
                    if(block != null) {
                        if(echests.getValue() && block.equals(Blocks.ENDER_CHEST)) {
                            event.setCanceled(true);
                        }
                        if(anvils.getValue() && block.equals(Blocks.ANVIL)) {
                            event.setCanceled(true);
                        }
                        if(hoppers.getValue() && block.equals(Blocks.HOPPER)) {
                            event.setCanceled(true);
                        }
                        if(antiPlace.getValue() && (block.equals(Blocks.OBSIDIAN) || block.equals(Blocks.BEDROCK))) {
                            if(mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) {
                                event.setCanceled(true);
                            }
                        }
                    }
                }
            }
        } else if(fin instanceof PlayerInteractEvent.RightClickBlock) {
            PlayerInteractEvent.RightClickBlock event = (PlayerInteractEvent.RightClickBlock)fin;
            Block block = mc.world.getBlockState(event.getPos()).getBlock();
            if(block != null) {
                if(echests.getValue() && block.equals(Blocks.ENDER_CHEST)) {
                    event.setCanceled(true);
                }
                if(anvils.getValue() && block.equals(Blocks.ANVIL)) {
                    event.setCanceled(true);
                }
                if(hoppers.getValue() && block.equals(Blocks.HOPPER)) {
                    event.setCanceled(true);
                }
                if(antiPlace.getValue() && (block.equals(Blocks.OBSIDIAN) || block.equals(Blocks.BEDROCK))) {
                    if(mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }


    private boolean doHandCheck() {
        if(mc.player == null || mc.world == null) {
            return true;
        }
        if(foodCheck.getValue()) {
            switch ((HandMode)handCheck.getValue()) {
                case BOTH:
                    if(mc.player.getHeldItemMainhand().getItem() instanceof ItemFood || mc.player.getHeldItemOffhand().getItem() instanceof ItemFood) {
                        return false;
                    }
                    break;
                case MAINHAND:
                    if(mc.player.getHeldItemMainhand().getItem() instanceof ItemFood) {
                        return false;
                    }
                    break;
                case OFFHAND:
                    if(mc.player.getHeldItemOffhand().getItem() instanceof ItemFood) {
                        return false;
                    }
                    break;
            }
        } else {
            return false;
        }
        return true;
    }



    private enum HandMode {
        BOTH,
        MAINHAND,
        OFFHAND
    }
}
