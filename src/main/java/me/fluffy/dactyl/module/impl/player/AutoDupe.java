package me.fluffy.dactyl.module.impl.player;

import me.fluffy.dactyl.injection.inj.access.IKeyBinding;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.module.impl.movement.EntityControl;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.ChatUtil;
import me.fluffy.dactyl.util.CombatUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiScreenHorseInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.ArrayList;

public class AutoDupe extends Module {
    public Setting<Integer> moveDelay = new Setting<Integer>("MoveDelay", 100, 1, 500, "Delay in between moving items");
    public Setting<Double> mountDelay = new Setting<Double>("MountDelay", 0.5d, 0.1d, 5d, "How long to wait for mount after dropping");
    public Setting<Double> delay = new Setting<Double>("DupeDelay", 1d, 0.1d, 5d, "Delay per dupe (in seconds)");
    public AutoDupe() {
        super("AutoDupe", Category.PLAYER, "Best AutoDupe in the wild wild west");
    }
    private final TimeUtil delayTimer = new TimeUtil();
    private final TimeUtil startDropTimer = new TimeUtil();
    private final TimeUtil onGroundTimer = new TimeUtil();
    private final TimeUtil mountTimer = new TimeUtil();
    private final TimeUtil moveTimer = new TimeUtil();
    private final TimeUtil dropTimer = new TimeUtil();
    ArrayList<Integer> donkeyInvShulkers = new ArrayList<>();
    int ridingId = -1;
    int step = 0;
    int droppedItems = -1;
    int movedItems = -1;
    int shulkerSlot = CombatUtil.findShulkerOpenInv();
    ArrayList<Integer> shulkerSlots = new ArrayList<>();

    @Override
    public void onEnable() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(!passesEnableChecks()) {
            return;
        }
        mc.player.sendHorseInventory();
        step++;
        moveTimer.reset();
    }

    @SubscribeEvent
    public void onConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if(this.isEnabled()) {
            this.toggle();
        }
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        if(this.isEnabled()) {
            this.toggle();
        }
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(!delayTimer.hasPassed(delay.getValue().longValue()*1000L)) {
            return;
        } else {
            if(isRiding() && step == 0) {
                step = 1;
                movedItems = -1;
                moveTimer.reset();
            }
        }
        if(step == 1) {
            // put items inside donkey
            /*if(!isRiding()) {
                donkeyNull();
                return;
            }*/

            if(!(mc.currentScreen instanceof GuiScreenHorseInventory)) {
                mc.player.sendHorseInventory();
                return;
            }
            int windowId = mc.player.openContainer.windowId;
            /*if(movedItems == -1) {
                shulkerSlots = CombatUtil.findShulkersOpenInv();
                movedItems = ((shulkerSlots == null || shulkerSlots.size() == 0) ? -1 : 0);
                return;
            }
            if((shulkerSlots == null || shulkerSlots.size() == 0 || movedItems == -1)) {
                noShulkers();
                return;
            }
            if(movedItems > shulkerSlots.size()) {
                step++;
                shulkerSlots = new ArrayList<>();
                movedItems = -1;
                ChatUtil.printMsg("stepped", true, false);
                return;
            }
            if(moveTimer.hasPassed(moveDelay.getValue().longValue())) {
                ChatUtil.printMsg("moved item" + movedItems, true, false);
                mc.playerController.windowClick(windowId, shulkerSlots.get(movedItems), 0, ClickType.QUICK_MOVE, mc.player);
                movedItems++;
                moveTimer.reset();
            }*/
            if(movedItems == -1) {
                shulkerSlots = CombatUtil.findShulkersOpenInv();
                movedItems = ((shulkerSlots == null || shulkerSlots.size() == 0) ? -1 : 0);
                return;
            }
            if((shulkerSlots == null || shulkerSlots.size() == 0 || movedItems == -1)) {
                noShulkers();
                return;
            }
            if(movedItems >= shulkerSlots.size()) {
                step++;
                movedItems = -1;
                shulkerSlots = new ArrayList<>();
                moveTimer.reset();
                return;
            }
            if(moveTimer.hasPassed(moveDelay.getValue().longValue())) {
                mc.playerController.windowClick(windowId, shulkerSlots.get(movedItems), 0, ClickType.QUICK_MOVE, mc.player);
                movedItems++;
                moveTimer.reset();
            }
            ridingId = mc.player.getRidingEntity().entityId;
            return;
        }
        if(step == 2) {
            // dupe button
            if(!isRiding()) {
                donkeyNull();
                return;
            }
            if(!(mc.currentScreen instanceof GuiScreenHorseInventory)) return;
            int chestSlot = CombatUtil.findBlockInHotbar(Blocks.CHEST);
            CombatUtil.switchToSlot(false, chestSlot);
            EntityControl.INSTANCE.ignoring = true;
            mc.player.connection.sendPacket(new CPacketUseEntity(mc.player.getRidingEntity(), EnumHand.MAIN_HAND, mc.player.getRidingEntity().getPositionVector()));
            EntityControl.INSTANCE.ignoring = false;
            droppedItems = -1;
            startDropTimer.reset();
            step++;
            return;
        }
        if(step == 3) {
            if(!startDropTimer.hasPassed(700)) {
                return;
            }
            // drop the items
            if(!(mc.currentScreen instanceof GuiScreenHorseInventory)) return;
            int windowId = mc.player.openContainer.windowId;
            if(droppedItems == -1) {
                donkeyInvShulkers = CombatUtil.findShulkersDonkeyInv();
                droppedItems = ((donkeyInvShulkers == null || donkeyInvShulkers.size() == 0) ? -1 : 0);
                return;
            }
            if((donkeyInvShulkers == null || donkeyInvShulkers.size() == 0 || droppedItems == -1)) {
                noShulkersDonkey();
                return;
            }
            if(mc.player.getRidingEntity() != null) {
                ridingId = mc.player.getRidingEntity().entityId;
            }
            if(droppedItems == donkeyInvShulkers.size()) {
                step++;
                dropTimer.reset();
                return;
            }
            if(dropTimer.hasPassed(100)) {
                mc.playerController.windowClick(windowId, donkeyInvShulkers.get(droppedItems), 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(windowId, -999, 0, ClickType.PICKUP, mc.player);
                droppedItems++;
                dropTimer.reset();
            }
        }
        if(step == 4) {
            // dismount
            mc.player.closeScreen();
            mc.player.dismountRidingEntity();
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            step++;
            mountTimer.reset();
            onGroundTimer.reset();
            return;
        }
        if(step == 5) {
            if(!mc.player.onGround) {
                onGroundTimer.reset();
                return;
            }
            if(!onGroundTimer.hasPassed(75)) {
                return;
            }
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            // chest donkey
            int chestSlot = CombatUtil.findBlockInHotbar(Blocks.CHEST);
            CombatUtil.switchToSlot(false, chestSlot);
            if(chestSlot == -1) {
                noChestInHotbar();
                return;
            }
            if(mc.world.getEntityByID(ridingId) == null) {
                donkeyNull();
                return;
            }
            mc.playerController.interactWithEntity(mc.player, mc.world.getEntityByID(ridingId), EnumHand.MAIN_HAND);
            step++;
            return;
        }
        if(step == 6) {
            // get on donkey
            if(!mountTimer.hasPassed(mountDelay.getValue().longValue()*1000L)) {
                return;
            }
            mc.playerController.interactWithEntity(mc.player, mc.world.getEntityByID(ridingId), EnumHand.MAIN_HAND);
            // done
            step = 0;
            delayTimer.reset();
            donkeyInvShulkers = new ArrayList<>();
            return;
        }
    }

    @Override
    public void onDisable() {
        step = 0;
        droppedItems = -1;
        movedItems = -1;
        ridingId = -1;
        mountTimer.reset();
        delayTimer.reset();
        dropTimer.reset();
        moveTimer.reset();
        onGroundTimer.reset();
        startDropTimer.reset();
        donkeyInvShulkers = new ArrayList<>();
    }

    private void noChestInHotbar() {
        this.toggleWithReason("&a[AutoDupe] &cNo chests in hotbar.");
    }

    private void noShulkersDonkey() {
        this.toggleWithReason("&a[AutoDupe] &cNo shulkers found in donkey.");
    }

    private void noShulkers() {
        this.toggleWithReason("&a[AutoDupe] &cNo shulkers found.");
    }

    private void donkeyNull() {
        this.toggleWithReason("&a[AutoDupe] &cDonkey entity is null.");
    }

    private boolean isRiding() {
        return (mc.player.getRidingEntity() != null && mc.player.isRiding());
    }

    private boolean passesEnableChecks() {
        if(mc.player.getRidingEntity() == null || !mc.player.isRiding()) {
            this.toggleWithReason("&a[AutoDupe] &cYou are not riding an entity!");
            return false;
        }
        if(!(mc.player.getRidingEntity() instanceof AbstractChestHorse)) {
            this.toggleWithReason("&a[AutoDupe] &cDonkeys and Llamas only.");
            return false;
        } else {
            AbstractChestHorse riding = (AbstractChestHorse)mc.player.getRidingEntity();
            if(!riding.hasChest()) {
                this.toggleWithReason("&a[AutoDupe] &cEntity must be chested.");
                return false;
            }
        }
        return true;
    }
}
