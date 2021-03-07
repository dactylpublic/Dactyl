package me.fluffy.dactyl.module.impl.combat;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.module.impl.client.HUD;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.CombatUtil;
import me.fluffy.dactyl.util.TimeUtil;
import me.fluffy.dactyl.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class AutoArmor extends Module {
    public Setting<Integer> delay = new Setting<Integer>("Delay", 100, 1, 1000);
    public Setting<Boolean> pantsBlastPrio = new Setting<Boolean>("BlastPants", true);
    public Setting<Boolean> lessPackets = new Setting<Boolean>("MinPackets", false);
    public Setting<Boolean> allowElytra = new Setting<Boolean>("AllowElytra", true);
    public Setting<Boolean> mendWhileXP = new Setting<Boolean>("XPMend", false);
    public Setting<Integer> mendPercent = new Setting<Integer>("Percent", 80, 5, 100, v->mendWhileXP.getValue());
    public Setting<Boolean> mendRotate = new Setting<Boolean>("XPRotate", true, v->mendWhileXP.getValue());
    Setting<Boolean> deathCheck = new Setting<Boolean>("DangerCheck", true, v->mendWhileXP.getValue());
    Setting<Double> enemyDistance = new Setting<Double>("EnemyRange", 5.0D, 1.0D, 13.5D, vis->deathCheck.getValue()&&mendWhileXP.getValue());
    Setting<Double> crystalDistance = new Setting<Double>("CrystalRange", 10.0D, 1.0D, 13.5D, vis->deathCheck.getValue()&&mendWhileXP.getValue());
    public AutoArmor() {
        super("AutoArmor", Category.COMBAT);
    }

    private final TimeUtil timer = new TimeUtil();

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        this.setModuleInfo(mendWhileXP.getValue() ? "Mend" : "Equip");
        boolean doMend = (mendWhileXP.getValue() && Mouse.isButtonDown(1) && (mc.player.getHeldItemMainhand().getItem() instanceof ItemExpBottle));
        if(deathCheck.getValue()) {
            // player check
            for (EntityPlayer target : mc.world.playerEntities) {
                if((target == mc.player || Dactyl.friendManager.isFriend(target.getName()))) {
                    continue;
                }
                if (((target).getHealth() <= 0) || target.isDead) {
                    continue;
                }
                if((mc.player.getDistance(target) > 13.5)) {
                    continue;
                }
                if(mc.player.getDistance(target) <= enemyDistance.getValue()) {
                    doMend = false;
                }
            }
            //crystal check
            if(CombatUtil.requiredDangerSwitch(crystalDistance.getValue())) {
                doMend = false;
            }
        }
        if(doMend) {
            for (ItemStack is : mc.player.inventory.armorInventory) {
                if (is.isEmpty()) {
                    continue;
                }
                int dmg = 0;
                final int itemDurability = is.getMaxDamage() - is.getItemDamage();
                final float green = (is.getMaxDamage() - (float)is.getItemDamage()) / is.getMaxDamage();
                final float red = 1.0f - green;
                dmg = 100 - (int)(red * 100.0f);
                if(dmg >= mendPercent.getValue()) {
                    unequipArmor(getTypeFromItem(is.getItem()));
                }
            }
        } else {
            for(int i = 3; i >= 0; i--) {
                if(mc.player.inventory.armorInventory.get(i).isEmpty()) {
                    equipArmor(i);
                    break;
                }
            }
        }
    }

    private void unequipArmor(ArmorType armorType) {
        int bestSlot = -1;
        for(int i = 9; i <= 44; i++) {
            Item item = mc.player.inventoryContainer.getSlot(i).getStack().getItem();
            if(item != null && item instanceof ItemAir) {
                bestSlot = i;
            }
        }

        if(bestSlot != -1) {
            if(armorType != null) {
                if (timer.hasPassed(delay.getValue())) {
                    if (lessPackets.getValue()) {
                        mc.playerController.windowClick(0, getSlotByType(armorType), 0, ClickType.QUICK_MOVE, mc.player);
                    } else {
                        if (getSlotByType(armorType) != -1) {
                            mc.playerController.windowClick(0, getSlotByType(armorType), 0, ClickType.PICKUP, mc.player);
                            mc.playerController.windowClick(0, bestSlot, 0, ClickType.PICKUP, mc.player);
                            mc.playerController.windowClick(0, getSlotByType(armorType), 0, ClickType.PICKUP, mc.player);
                            mc.playerController.updateController();
                        }
                    }
                    timer.reset();
                }
            }
        }
    }

    private void equipArmor(int slot) {
        ArmorType armorType = getArmorTypeFromSlot(slot);
        int bestSlot = -1;
        int bestRating = -1;

        for(int i = 9; i <= 44; i++) {
            Item item = mc.player.inventoryContainer.getSlot(i).getStack().getItem();
            if(item instanceof ItemArmor && getTypeFromItem(item) == armorType) {
                int damageReduction = ((ItemArmor) item).damageReduceAmount;
                if(getTypeFromItem(item) == ArmorType.PANTS && pantsBlastPrio.getValue()) {
                    if(EnchantmentHelper.getEnchantmentLevel(Enchantments.BLAST_PROTECTION, mc.player.inventoryContainer.getSlot(i).getStack()) > 0) {
                        bestSlot = i;
                        bestRating = damageReduction;
                    }
                } else {
                    if (damageReduction >= bestRating) {
                        bestSlot = i;
                        bestRating = damageReduction;
                    }
                }
            }
        }

        if(bestSlot != -1 && bestRating != -1) {
            if(timer.hasPassed(delay.getValue())) {
                if (lessPackets.getValue()) {
                    mc.playerController.windowClick(0, bestSlot, 0, ClickType.QUICK_MOVE, mc.player);
                } else {
                    if (getSlotByType(armorType) != -1) {
                        mc.playerController.windowClick(0, bestSlot, 0, ClickType.PICKUP, mc.player);
                        mc.playerController.windowClick(0, getSlotByType(armorType), 0, ClickType.PICKUP, mc.player);
                        mc.playerController.windowClick(0, bestSlot, 0, ClickType.PICKUP, mc.player);
                        mc.playerController.updateController();
                    }
                }
                timer.reset();
            }
        }
    }

    @Override
    public void onToggle() {
        timer.reset();
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.OUTGOING) {
            if(event.getPacket() instanceof CPacketPlayer) {
                CPacketPlayer packet = (CPacketPlayer)event.getPacket();
                if (mendWhileXP.getValue() && mendRotate.getValue() && Mouse.isButtonDown(1) && (mc.player.getHeldItemMainhand().getItem() instanceof ItemExpBottle)) {
                    packet.pitch = 90.0f;
                }
            }
        }
    }

    private ArmorType getTypeFromItem(Item item) {
        if(Items.DIAMOND_HELMET.equals(item) || Items.GOLDEN_HELMET.equals(item) || Items.IRON_HELMET.equals(item) || Items.CHAINMAIL_HELMET.equals(item) || Items.LEATHER_HELMET.equals(item)) {
            return ArmorType.HELMET;
        } else if(Items.DIAMOND_CHESTPLATE.equals(item) || Items.GOLDEN_CHESTPLATE.equals(item) || Items.IRON_CHESTPLATE.equals(item) || Items.CHAINMAIL_CHESTPLATE.equals(item) || Items.LEATHER_CHESTPLATE.equals(item)) {
            return ArmorType.CHESTPLATE;
        } else if(Items.DIAMOND_LEGGINGS.equals(item) || Items.GOLDEN_LEGGINGS.equals(item) || Items.IRON_LEGGINGS.equals(item) || Items.CHAINMAIL_LEGGINGS.equals(item) || Items.LEATHER_LEGGINGS.equals(item)) {
            return ArmorType.PANTS;
        } else if(Items.DIAMOND_BOOTS.equals(item) || Items.GOLDEN_BOOTS.equals(item) || Items.IRON_BOOTS.equals(item) || Items.CHAINMAIL_BOOTS.equals(item) || Items.LEATHER_BOOTS.equals(item)) {
            return ArmorType.BOOTS;
        }
        return null;
    }

    private ArmorType getArmorTypeFromSlot(int slot) {
        switch(slot) {
            case 3:
                return ArmorType.HELMET;
            case 2:
                return ArmorType.CHESTPLATE;
            case 1:
                return ArmorType.PANTS;
            case 0:
                return ArmorType.BOOTS;
            default:
                return null;
        }
    }

    private int getSlotByType(ArmorType type) {
        switch(type) {
            case HELMET:
                return 5;
            case CHESTPLATE:
                return 6;
            case PANTS:
                return 7;
            case BOOTS:
                return 8;
            default:
                return -1;
        }
    }

    private enum ArmorType {
        HELMET,
        CHESTPLATE,
        PANTS,
        BOOTS;
    }
}
