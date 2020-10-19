package me.fluffy.dactyl.module.impl.combat;

import me.fluffy.dactyl.event.impl.action.EventKeyPress;
import me.fluffy.dactyl.event.impl.player.EventUpdateWalkingPlayer;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.Bind;
import me.fluffy.dactyl.util.CombatUtil;
import me.fluffy.dactyl.util.HoleUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Iterator;

public class Offhand extends Module {
    Setting<Boolean> strictSwap = new Setting<Boolean>("StrictSwap", false);
    Setting<Boolean> moveBypass = new Setting<Boolean>("MoveBypass", false);

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

    // other
    Setting<Integer> modeUpdates = new Setting<Integer>("Updates", 2, 1, 2);
    Setting<Boolean> deathCheck = new Setting<Boolean>("DangerSwap", true);
    Setting<Double> dangerDistance = new Setting<Double>("DangerDist", 10.0, 1.0D, 13.5D);


    public static Offhand INSTANCE;

    public Offhand() {
        super("Offhand", Category.COMBAT);
        INSTANCE = this;
    }


    private final HashMap<Integer, Integer> strictSwitchMap = new HashMap<>();

    public final TimeUtil lastSwitch = new TimeUtil();

    @Override
    public void onToggle() {
        strictSwitchMap.clear();
        lastSwitch.reset();
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
        double targetSwitchHealth = 0.0D;
        String switchTargetName = "";
        boolean needsAnGrammar = false;
        if (!(targetKey == gappleBind.getValue().getKey() || targetKey == crystalBind.getValue().getKey() || targetKey == obiBind.getValue().getKey())) {
            return;
        }

        boolean wasMoving = false;
        if(moveBypass.getValue()) {
            if(mc.player.moveForward != 0.0f || mc.player.moveStrafing != 0.0f) {
                wasMoving = true;
            }
        }

        if(deathCheck.getValue()) {
            if (CombatUtil.requiredDangerSwitch(dangerDistance.getValue())) {
                return;
            }
        }

        if(targetKey == gappleBind.getValue().getKey()) {
            targetToSwitch = Items.GOLDEN_APPLE;
            targetSwitchHealth = HoleUtil.isInHole() ? gappleHoleHealth.getValue() : gappleHealth.getValue();
            switchTargetName = "Gapple";
        } else if(targetKey == crystalBind.getValue().getKey()) {
            targetToSwitch = Items.END_CRYSTAL;
            targetSwitchHealth = HoleUtil.isInHole() ? crystalHoleHealth.getValue() : crystalHealth.getValue();
            switchTargetName = "Crystal";
        } else if(targetKey == obiBind.getValue().getKey()) {
            targetToSwitch = Item.getItemFromBlock(Blocks.OBSIDIAN);
            targetSwitchHealth = HoleUtil.isInHole() ? obiHoleHealth.getValue() : obiHealth.getValue();
            switchTargetName = "Obsidan Block";
            needsAnGrammar = true;
        }
        boolean passesCheck = true;
        if(mc.player.getHeldItemOffhand().getItem() == targetToSwitch) {
            targetToSwitch = Items.TOTEM_OF_UNDYING;
            targetSwitchHealth = 1.0D;
            switchTargetName = "Totem";
        }

        if(targetSwitchHealth >= (mc.player.getHealth()+mc.player.getAbsorptionAmount())) {
            return;
        }

        if(targetToSwitch != null) {
            boolean isCrapple = (targetToSwitch == Items.GOLDEN_APPLE && !notchApple.getValue());
            int targetSwitchSlot = -1;
            if(isCrapple) {
                if(CombatUtil.findCrapple() == -1) {
                    passesCheck = false;
                    return;
                } else {
                    targetSwitchSlot = CombatUtil.findCrapple();
                }
            } else {
                if(targetToSwitch == Items.GOLDEN_APPLE) {
                    if (CombatUtil.findItemSlotDamage1(targetToSwitch) == -1) {
                        passesCheck = false;
                        return;
                    } else {
                        targetSwitchSlot = CombatUtil.findItemSlotDamage1(targetToSwitch);
                    }
                } else {
                    if (CombatUtil.findItemSlot(targetToSwitch) == -1) {
                        passesCheck = false;
                        return;
                    } else {
                        targetSwitchSlot = CombatUtil.findItemSlot(targetToSwitch);
                    }
                }
            }
            if(targetSwitchSlot == -1) {
                passesCheck = false;
                return;
            }


            if (CombatUtil.passesOffhandCheck(targetSwitchHealth, targetToSwitch, isCrapple)) {
                if(!strictSwap.getValue()) {
                    if(mc.player.getHeldItemOffhand().getItem() != targetToSwitch) {
                        boolean doBypass = false;
                        double motionX = mc.player.motionX;
                        double motionY = mc.player.motionY;
                        double motionZ = mc.player.motionZ;
                        if(moveBypass.getValue()) {
                            if(wasMoving) {
                                doBypass = true;
                                mc.player.motionX = mc.player.motionY = mc.player.motionZ = 0.0d;
                            }
                        }
                        lastSwitch.reset();
                        CombatUtil.switchOffhandNonStrict(targetSwitchSlot);
                        if(doBypass)  {
                            mc.player.motionX = motionX;
                            mc.player.motionY = motionY;
                            mc.player.motionZ = motionZ;
                        }
                    }
                } else {
                    boolean doBypass = false;
                    double motionX = mc.player.motionX;
                    double motionY = mc.player.motionY;
                    double motionZ = mc.player.motionZ;
                    if(moveBypass.getValue()) {
                        if(wasMoving) {
                            doBypass = true;
                            mc.player.motionX = mc.player.motionY = mc.player.motionZ = 0.0d;
                        }
                    }
                    lastSwitch.reset();
                    strictSwitchMap.put(targetSwitchSlot, 0);
                    if(doBypass)  {
                        mc.player.motionX = motionX;
                        mc.player.motionY = motionY;
                        mc.player.motionZ = motionZ;
                    }
                }
                return;
            }
        }
    }

    private boolean hasTotemsAvailable() {
        return (CombatUtil.findItemSlot(Items.TOTEM_OF_UNDYING) != -1);
    }

    private boolean holdingTotem() {
        return (mc.player.getHeldItemOffhand().getItem().equals(Items.TOTEM_OF_UNDYING));
    }

    private void doOffhand() {
        if(screenNotAllowed()) {
            return;
        }

        if(strictSwitchMap.size() > 0) {
            Iterator<Integer> iterator = strictSwitchMap.keySet().iterator();
            while (iterator.hasNext()) {
                Integer key = iterator.next();
                CombatUtil.switchOffhandStrict(key, strictSwitchMap.get(key));
                int newVal = strictSwitchMap.get(key)+1;
                strictSwitchMap.put(key, newVal);
                if(strictSwitchMap.get(key) > 2) {
                    iterator.remove();
                }
            }
            return;
        }
        String itemCount = String.valueOf(mc.player.getHeldItemOffhand().getCount()+(mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == mc.player.getHeldItemOffhand().getItem()).mapToInt(ItemStack::getCount).sum()));
        this.setDisplayName(getModuleNameFromItem(mc.player.getHeldItemOffhand().getItem()));
        this.setModuleInfo(itemCount);
        boolean needsSwitch = false;

        if(deathCheck.getValue()) {
            if (CombatUtil.requiredDangerSwitch(dangerDistance.getValue())) {
                if(!holdingTotem()) {
                    needsSwitch = true;
                }
            }
        }

        if(getRelativeSwitchHealth() >= (mc.player.getHealth()+mc.player.getAbsorptionAmount())) {
            if(!holdingTotem()) {
                needsSwitch = true;
            }
        }

        if(!holdingOffhandItem()) {
            needsSwitch = true;
        }

        if((!hasTotemsAvailable() || holdingTotem()) && needsSwitch) {
            needsSwitch = false;
        }

        if(needsSwitch) {
            if(!strictSwap.getValue()) {
                CombatUtil.switchOffhandTotemNotStrict();
            } else {
                if(hasTotemsAvailable()) {
                    strictSwitchMap.put(CombatUtil.findItemSlot(Items.TOTEM_OF_UNDYING), 0);
                }
            }
        }
    }

    private String switchTargetNameOffhand() {
        Item heldItemOffhand = mc.player.getHeldItemOffhand().getItem();
        if(heldItemOffhand == Items.GOLDEN_APPLE) {
            return "Gapple";
        } else if(heldItemOffhand == Items.END_CRYSTAL) {
            return "Crystal";
        } else if(heldItemOffhand == Item.getItemFromBlock(Blocks.OBSIDIAN)) {
            return "Obsidian Block";
        }
        return "Totem";
    }

    // i hate british
    private boolean needsAnGrammarOffhand() {
        Item heldItemOffhand = mc.player.getHeldItemOffhand().getItem();
        if(heldItemOffhand == Item.getItemFromBlock(Blocks.OBSIDIAN)) {
            return true;
        }
        return false;
    }

    private double getRelativeSwitchHealth() {
        Item heldItemOffhand = mc.player.getHeldItemOffhand().getItem();
        double relativeHealth = 36.0D;
        if(heldItemOffhand == Items.GOLDEN_APPLE) {
            relativeHealth = HoleUtil.isInHole() ? gappleHoleHealth.getValue() : gappleHealth.getValue();
        } else if(heldItemOffhand == Items.END_CRYSTAL) {
            relativeHealth = HoleUtil.isInHole() ? crystalHoleHealth.getValue() : crystalHealth.getValue();
        } else if(heldItemOffhand == Item.getItemFromBlock(Blocks.OBSIDIAN)) {
            relativeHealth = HoleUtil.isInHole() ? obiHoleHealth.getValue() : obiHealth.getValue();
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

}
