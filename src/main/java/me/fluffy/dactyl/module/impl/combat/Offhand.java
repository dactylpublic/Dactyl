package me.fluffy.dactyl.module.impl.combat;

import me.fluffy.dactyl.event.impl.action.EventKeyPress;
import me.fluffy.dactyl.event.impl.player.EventUpdateWalkingPlayer;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.*;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Iterator;

public class Offhand extends Module {
    Setting<Boolean> strictSwap = new Setting<Boolean>("StrictSwap", false);

    // gapple
    Setting<Bind> gappleBind = new Setting<Bind>("Gapple", new Bind(Keyboard.KEY_NONE));
    Setting<Double> gappleHealth = new Setting<Double>("GappleHealth", 15.0D, 1.0D, 36.0D);
    Setting<Double> gappleHoleHealth = new Setting<Double>("G-H-Health", 10.0, 1.0D, 36.0D);
    Setting<Boolean> notchApple = new Setting<Boolean>("NotchApple", true);

    // crystal
    Setting<Bind> crystalBind = new Setting<Bind>("Crystal", new Bind(Keyboard.KEY_NONE));
    Setting<Double> crystalHealth = new Setting<Double>("CrystalHealth", 15.0D, 1.0D, 36.0D);
    Setting<Double> crystalHoleHealth = new Setting<Double>("C-H-Health", 10.0, 1.0D, 36.0D);

    // obi
    Setting<Bind> obiBind = new Setting<Bind>("Obi", new Bind(Keyboard.KEY_NONE));
    Setting<Double> obiHealth = new Setting<Double>("ObiHealth", 15.0D, 1.0D, 36.0D);
    Setting<Double> obiHoleHealth = new Setting<Double>("O-H-Health", 10.0, 1.0D, 36.0D);

    // enderchest
    Setting<Bind> echestBind = new Setting<Bind>("EChest", new Bind(Keyboard.KEY_NONE));
    Setting<Double> echestHealth = new Setting<Double>("EChestHealth", 15.0D, 1.0D, 36.0D);
    Setting<Double> echestHoleHealth = new Setting<Double>("E-H-Health", 10.0, 1.0D, 36.0D);

    // other
    Setting<Integer> modeUpdates = new Setting<Integer>("Updates", 2, 1, 2);
    Setting<Integer> switchesPerSec = new Setting<Integer>("SwitchesPerS", 4, 1, 50, v->strictSwap.getValue());
    Setting<Integer> swapDelay = new Setting<Integer>("SwapDelay", 2, 1, 10, v->strictSwap.getValue());
    Setting<EmergencyMode> emergencyMode = new Setting<EmergencyMode>("ZeroKey", EmergencyMode.EGAP);
    Setting<Boolean> deathCheck = new Setting<Boolean>("DangerCheck", true);
    Setting<Double> dangerDistance = new Setting<Double>("DangerDist", 10.0, 1.0D, 13.5D, vis->deathCheck.getValue());
    Setting<Boolean> soft = new Setting<Boolean>("Soft", false);
    Setting<Double> softHealth = new Setting<Double>("SoftHealth", 15.0, 1.0D, 36.0D, vis->soft.getValue());


    public static Offhand INSTANCE;

    public Offhand() {
        super("Offhand", Category.COMBAT);
        INSTANCE = this;
    }


    private final HashMap<Integer, Integer> strictSwitchMap = new HashMap<>();

    public final TimeUtil sps = new TimeUtil();
    public final TimeUtil lastSwitch = new TimeUtil();

    public int ticksNotTotemed = 0;
    public int switches = 0;

    private OffhandMode offhandMode = OffhandMode.TOTEM;

    @Override
    public void onToggle() {
        strictSwitchMap.clear();
        lastSwitch.reset();
        switches = 0;
        ticksNotTotemed = 0;
        offhandMode = OffhandMode.TOTEM;
    }

    private void doOffhand() {
        if(screenNotAllowed()) {
            return;
        }
        // set module info and display name
        String itemCount = String.valueOf(mc.player.getHeldItemOffhand().getCount()+(mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == mc.player.getHeldItemOffhand().getItem()).mapToInt(ItemStack::getCount).sum()));
        this.setDisplayName(getModuleNameFromItem(mc.player.getHeldItemOffhand().getItem()));
        this.setModuleInfo(itemCount);
        if(strictSwap.getValue() && lastSwitch.hasPassed(1250) && mc.player.onGround) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.onGround));
            lastSwitch.reset();
        }
        if(sps.hasPassed(1000)) {
            switches = 0;
            sps.reset();
        }
        if(strictSwap.getValue() && !mc.player.onGround) {
            return;
        }
        if(strictSwap.getValue()) {
            if (strictSwitchMap.size() > 0) {
                Iterator<Integer> iterator = strictSwitchMap.keySet().iterator();
                if(iterator.hasNext()) {
                    if(switches++ >= switchesPerSec.getValue()) {
                        return;
                    }
                    Integer key = iterator.next();
                    CombatUtil.switchOffhandStrict(key, strictSwitchMap.get(key));
                    int newVal = strictSwitchMap.get(key) + 1;
                    strictSwitchMap.put(key, newVal);
                    if (strictSwitchMap.get(key) > 2) {
                        iterator.remove();
                    }
                }
                return;
            }
        }

        boolean needsSwitching = false;
        boolean switchToTotem = false;

        float playerHealth = mc.player.getHealth()+mc.player.getAbsorptionAmount();

        if((getRelativeSwitchHealth() >= playerHealth) || (deathCheck.getValue() && CombatUtil.requiredDangerSwitch(dangerDistance.getValue()))) {
            if(!holdingTotem()) {
                switchToTotem = true;
            }
        }

        if(mc.player.getHeldItemOffhand().getItem() != getRelativeItem(offhandMode) && !switchToTotem) {
            needsSwitching = true;
        }

        if((!hasTotemsAvailable() || holdingTotem()) && switchToTotem) {
            switchToTotem = false;
        }

        if(!holdingOffhandItem() && !holdingTotem()) {
            int offhandModeItemCount = mc.player.getHeldItemOffhand().getCount()+(mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == getRelativeItem(offhandMode)).mapToInt(ItemStack::getCount).sum());
            if(soft.getValue()) {
                if((softHealth.getValue() >= playerHealth) || (deathCheck.getValue() && CombatUtil.requiredDangerSwitch(dangerDistance.getValue()))) {
                    if(hasTotemsAvailable() && offhandModeItemCount == 0) {
                        needsSwitching = true;
                        offhandMode = OffhandMode.TOTEM;
                    }
                }
            } else {
                if(hasTotemsAvailable() && offhandModeItemCount == 0) {
                    needsSwitching = true;
                    offhandMode = OffhandMode.TOTEM;
                }
            }
        }

        if(needsSwitching) {
            switchToItem(getRelativeItem(offhandMode));
        } else if(switchToTotem) {
            if(strictSwap.getValue() && ticksNotTotemed++ < swapDelay.getValue()) {
                return;
            }
            offhandMode = OffhandMode.TOTEM;
            switchToTotem();
        }
    }

    private void switchToTotem() {
        if(!strictSwap.getValue()) {
            CombatUtil.switchOffhandTotemNotStrict();
        } else {
            if(hasTotemsAvailable()) {
                strictSwitchMap.put(CombatUtil.findItemSlot(Items.TOTEM_OF_UNDYING), 0);
            }
        }
    }

    private void switchToItem(Item item) {
        if(getRelativeSwitching(offhandMode) == -1) {
            return;
        }
        if(!strictSwap.getValue()) {
            CombatUtil.switchOffhandNonStrict(getRelativeSwitching(offhandMode));
        } else {
            if(hasTotemsAvailable()) {
                strictSwitchMap.put(getRelativeSwitching(offhandMode), 0);
            }
        }
    }

    @SubscribeEvent
    public void onKeyPress(EventKeyPress event) {
        onPressKey(event.getKey());
    }

    private void onPressKey(int targetKey) {
        if(screenNotAllowed()) {
            return;
        }
        Item targetToSwitch = null;
        double requiredSwitchHealth = 0.0D;
        if (!(targetKey == echestBind.getValue().getKey() || targetKey == gappleBind.getValue().getKey() || targetKey == crystalBind.getValue().getKey() || targetKey == obiBind.getValue().getKey())) {
            return;
        }

        if(deathCheck.getValue()) {
            if (CombatUtil.requiredDangerSwitch(dangerDistance.getValue())) {
                return;
            }
        }
        OffhandMode nextOffhand = null;
        if(targetKey == gappleBind.getValue().getKey()) {
            targetToSwitch = Items.GOLDEN_APPLE;
            requiredSwitchHealth = HoleUtil.isInHole() ? gappleHoleHealth.getValue() : gappleHealth.getValue();
            nextOffhand = (notchApple.getValue() ? OffhandMode.GAPPLE : OffhandMode.CRAPPLE);
        } else if(targetKey == crystalBind.getValue().getKey()) {
            targetToSwitch = Items.END_CRYSTAL;
            requiredSwitchHealth = HoleUtil.isInHole() ? crystalHoleHealth.getValue() : crystalHealth.getValue();
            nextOffhand = OffhandMode.CRYSTAL;
        } else if(targetKey == obiBind.getValue().getKey()) {
            targetToSwitch = Item.getItemFromBlock(Blocks.OBSIDIAN);
            requiredSwitchHealth = HoleUtil.isInHole() ? obiHoleHealth.getValue() : obiHealth.getValue();
            nextOffhand = OffhandMode.OBI;
        } else if(targetKey == echestBind.getValue().getKey()) {
            targetToSwitch = Item.getItemFromBlock(Blocks.ENDER_CHEST);
            requiredSwitchHealth = HoleUtil.isInHole() ? echestHoleHealth.getValue() : echestHealth.getValue();
            nextOffhand = OffhandMode.ECHEST;
        }
        if(mc.player.getHeldItemOffhand().getItem() == targetToSwitch) {
            nextOffhand = (CombatUtil.getItemCount(Items.TOTEM_OF_UNDYING) == 0) ? emergencyToOffhand(emergencyMode.getValue()) : OffhandMode.TOTEM;
        }
        float playerHealth = mc.player.getHealth()+mc.player.getAbsorptionAmount();
        if(playerHealth < requiredSwitchHealth) {
            return;
        }

        if(nextOffhand != null) {
            offhandMode = nextOffhand;
        }
    }

    private OffhandMode emergencyToOffhand(EmergencyMode mode) {
        if(mode == EmergencyMode.CRAPPLE) {
            return OffhandMode.CRAPPLE;
        } else if(mode == EmergencyMode.EGAP) {
            return OffhandMode.GAPPLE;
        } else if(mode == EmergencyMode.CRYSTAL) {
            return OffhandMode.CRYSTAL;
        } else if(mode == EmergencyMode.OBI) {
            return OffhandMode.OBI;
        } else if(mode == EmergencyMode.ECHEST) {
            return OffhandMode.ECHEST;
        }
        return OffhandMode.GAPPLE;
    }

    private boolean hasTotemsAvailable() {
        return (CombatUtil.findItemSlot(Items.TOTEM_OF_UNDYING) != -1);
    }

    private boolean holdingTotem() {
        return (mc.player.getHeldItemOffhand().getItem().equals(Items.TOTEM_OF_UNDYING));
    }

    private Item getRelativeItem(OffhandMode mode) {
        switch(mode) {
            case TOTEM:
                return Items.TOTEM_OF_UNDYING;
            case GAPPLE:
            case CRAPPLE:
                return Items.GOLDEN_APPLE;
            case OBI:
                return Item.getItemFromBlock(Blocks.OBSIDIAN);
            case ECHEST:
                return Item.getItemFromBlock(Blocks.ENDER_CHEST);
            case CRYSTAL:
                return Items.END_CRYSTAL;
        }
        return null;
    }

    private int getRelativeSwitching(OffhandMode mode) {
        switch(mode) {
            case TOTEM:
                return CombatUtil.findItemSlot(Items.TOTEM_OF_UNDYING);
            case GAPPLE:
                return CombatUtil.findItemSlotDamage1(Items.GOLDEN_APPLE);
            case CRAPPLE:
                return CombatUtil.findCrapple();
            case OBI:
                return CombatUtil.findItemSlot(Item.getItemFromBlock(Blocks.OBSIDIAN));
            case ECHEST:
                return CombatUtil.findItemSlot(Item.getItemFromBlock(Blocks.ENDER_CHEST));
            case CRYSTAL:
                return CombatUtil.findItemSlot(Items.END_CRYSTAL);
        }
        return -1;
    }

    private String switchTargetNameOffhand() {
        Item heldItemOffhand = mc.player.getHeldItemOffhand().getItem();
        if(heldItemOffhand == Items.GOLDEN_APPLE) {
            return "Gapple";
        } else if(heldItemOffhand == Items.END_CRYSTAL) {
            return "Crystal";
        } else if(heldItemOffhand == Item.getItemFromBlock(Blocks.OBSIDIAN)) {
            return "Obsidian Block";
        } else if(heldItemOffhand == Item.getItemFromBlock(Blocks.ENDER_CHEST)) {
            return "Ender Chest";
        }
        return "Totem";
    }

    private double getRelativeSwitchHealth() {
        Item heldItemOffhand = mc.player.getHeldItemOffhand().getItem();
        double relativeHealth = 0.0D;
        if(heldItemOffhand == Items.GOLDEN_APPLE) {
            relativeHealth = HoleUtil.isInHole() ? gappleHoleHealth.getValue() : gappleHealth.getValue();
        } else if(heldItemOffhand == Items.END_CRYSTAL) {
            relativeHealth = HoleUtil.isInHole() ? crystalHoleHealth.getValue() : crystalHealth.getValue();
        } else if(heldItemOffhand == Item.getItemFromBlock(Blocks.OBSIDIAN)) {
            relativeHealth = HoleUtil.isInHole() ? obiHoleHealth.getValue() : obiHealth.getValue();
        } else if(heldItemOffhand == Item.getItemFromBlock(Blocks.ENDER_CHEST)) {
            relativeHealth = HoleUtil.isInHole() ? echestHoleHealth.getValue() : echestHealth.getValue();
        }
        return relativeHealth;
    }

    private boolean holdingOffhandItem() {
        Item heldOffhand = mc.player.getHeldItemOffhand().getItem();
        return (heldOffhand == Items.GOLDEN_APPLE || heldOffhand == Items.END_CRYSTAL || heldOffhand == Item.getItemFromBlock(Blocks.OBSIDIAN));
    }

    private String getModuleNameFromItem(Item item) {
        if(item != null) {
            if (Items.TOTEM_OF_UNDYING.equals(item)) {
                return "AutoTotem";
            } else if (Items.GOLDEN_APPLE.equals(item)) {
                return "OffhandGap";
            } else if (Items.END_CRYSTAL.equals(item)) {
                return "OffhandCrystal";
            } else if (Item.getItemFromBlock(Blocks.OBSIDIAN).equals(item)) {
                return "OffhandObi";
            } else if (Item.getItemFromBlock(Blocks.ENDER_CHEST).equals(item)) {
                return "OffhandObi";
            }
        }
        return "AutoTotem";
    }

    private boolean screenNotAllowed() {
        if(mc.currentScreen != null) {
            if(!(mc.currentScreen instanceof GuiInventory) && !(mc.currentScreen instanceof GuiChat) && !(mc.currentScreen instanceof GuiIngameMenu)) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onUpdate(EventUpdateWalkingPlayer event) {
        if(modeUpdates.getValue() == 1) {
            doOffhand();
        }
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(modeUpdates.getValue() == 2) {
            doOffhand();
        }
    }

    public enum EmergencyMode {
        EGAP("EGap"),
        ECHEST("EChest"),
        CRAPPLE("Crapple"),
        CRYSTAL("Crystal"),
        OBI("Obi");

        private final String name;

        private EmergencyMode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    private enum OffhandMode {
        GAPPLE,
        CRAPPLE,
        CRYSTAL,
        ECHEST,
        OBI,
        TOTEM
    }

}
