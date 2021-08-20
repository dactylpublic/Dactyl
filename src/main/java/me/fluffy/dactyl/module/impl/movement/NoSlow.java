package me.fluffy.dactyl.module.impl.movement;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.event.impl.action.KeyboardMoveEvent;
import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.event.impl.player.MoveEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.module.impl.player.Freecam;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.CombatUtil;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class NoSlow extends Module {
    Setting<SettingPage> page = new Setting<SettingPage>("Setting", SettingPage.COMMON);

    // common
    Setting<Boolean> strictSlowDown = new Setting<Boolean>("Strict", false, v->page.getValue() == SettingPage.COMMON);
    Setting<Boolean> strictInventory = new Setting<Boolean>("StrictInventory", false, v->page.getValue() == SettingPage.COMMON);
    Setting<Boolean> inventoryMove = new Setting<Boolean>("InvMove", true, v->page.getValue() == SettingPage.COMMON);
    Setting<Boolean> noSlowItem = new Setting<Boolean>("Items", true, v->page.getValue() == SettingPage.COMMON);
    Setting<Boolean> noSoulSand = new Setting<Boolean>("SoulSand", true, v->page.getValue() == SettingPage.COMMON);
    //Setting<Boolean> sneakBypass = new Setting<Boolean>("2bBypass", false, v->page.getValue() == SettingPage.COMMON);
    Setting<Double> soulSandSpeed = new Setting<Double>("SoulSpeed", 1.5D, 0.1D, 6.0D);

    // webs
    Setting<Boolean> webs = new Setting<Boolean>("Webs", true, v->page.getValue() == SettingPage.WEB);

    Setting<Boolean> webXZ = new Setting<Boolean>("WebXZ", true, v->page.getValue() == SettingPage.WEB);
    Setting<Double> webXZSpeed = new Setting<Double>("WebXZSpeed", 3.5D, 0.1D, 8.0D, v->webXZ.getValue() && page.getValue() == SettingPage.WEB);

    Setting<Boolean> webY = new Setting<Boolean>("WebY", true, v->page.getValue() == SettingPage.WEB);
    Setting<Double> webYSpeed = new Setting<Double>("WebYSpeed", 6.0D, 0.1D, 8.0D, v->webXZ.getValue() && page.getValue() == SettingPage.WEB);

    public NoSlow() {
        super("NoSlow", Category.MOVEMENT);
    }

    private boolean sneaking = false;

    @Override
    public void onToggle() {
        sneaking = false;
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null || mc.gameSettings == null) {
            return;
        }
        this.setModuleInfo(strictSlowDown.getValue() ? "Strict" : "Loose");
        handleWebYMovement();
        handleShieldOffhand();
        //handleSneakBypass();
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        handleWebXZMovement(event);
        handleSoulSandSlowdown(event);
    }

    @SubscribeEvent
    public void onKeyboardMove(KeyboardMoveEvent event) {
        if(inventoryMove.getValue()) {
            if(isInMoveableScreen()) {
                handleInventoryRotate();
                handleInventoryMovement();
            }
        }
        handleItemMovement();
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.OUTGOING) {
            if (event.getPacket() instanceof CPacketPlayer) {
                handleStrictPacket();
            }
            if(event.getPacket() instanceof CPacketClickWindow) {
                handleStrictInventory();
            }
        }
    }

    /*@SubscribeEvent
    public void onItem(LivingEntityUseItemEvent event) {
        if(!sneakBypass.getValue()) {
            return;
        }
        if(!sneaking) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            sneaking = true;
        }
    }*/

    private void handleStrictInventory() {
        if(strictInventory.getValue()) {
            if (mc.player.isActiveItemStackBlocking()) {
                mc.playerController.onStoppedUsingItem(mc.player);
            }
            if (mc.player.isSneaking()) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }
            if (mc.player.isSprinting()) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }
        }
    }

    /*private void handleSneakBypass() {
        if(!sneakBypass.getValue()) {
            return;
        }
        Item item = mc.player.getActiveItemStack().getItem();
        if (sneaking && ((!mc.player.isHandActive() && item instanceof ItemFood || item instanceof ItemBow || item instanceof ItemPotion))) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            sneaking = false;
        }
    }*/

    private void handleSoulSandSlowdown(MoveEvent event) {
        if(mc.currentScreen == Dactyl.clickGUI || Freecam.INSTANCE.isEnabled()) {
            return;
        }
        if(noSoulSand.getValue()) {
            if (isAboveSoulSand()) {
                if (mc.player.onGround) {
                    event.setX(event.getX() * (soulSandSpeed.getValue()));
                    event.setZ(event.getZ() * (soulSandSpeed.getValue()));
                }
            }
        }
    }

    private void handleStrictPacket() {
        if(noSlowItem.getValue() && strictSlowDown.getValue() && mc.player.isHandActive() && !mc.player.isRiding()) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ)), EnumFacing.DOWN));
        }
    }

    private void handleWebXZMovement(MoveEvent event) {
        if(!webs.getValue() || Freecam.INSTANCE.isEnabled()) {
            return;
        }
        if(mc.currentScreen == Dactyl.clickGUI) {
            return;
        }
        if(mc.player.isInWeb) {
            if(webXZ.getValue()) {
                if(mc.player.onGround) {
                    event.setX(event.getX()*(webXZSpeed.getValue()));
                    event.setZ(event.getZ()*(webXZSpeed.getValue()));
                }
            }
        }
    }

    private void handleShieldOffhand() {
        if (mc.player.isHandActive()) {
            if (mc.player.getHeldItem(mc.player.getActiveHand()).getItem() instanceof ItemShield) {
                if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0 && mc.player.getItemInUseMaxCount() >= 8) {
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
                }
            }
        }
    }

    private void handleWebYMovement() {
        if(!webs.getValue() || Freecam.INSTANCE.isEnabled()) {
            return;
        }
        if(mc.currentScreen == Dactyl.clickGUI) {
            return;
        }
        if(mc.player.isInWeb) {
            if(webY.getValue()) {
                if(mc.player.motionY < 0) {
                    mc.player.motionY *= webYSpeed.getValue();
                }
            }
        }
    }

    private void handleItemMovement() {
        if (noSlowItem.getValue() && mc.player.isHandActive() && !mc.player.isRiding()) {
            mc.player.movementInput.moveForward /= 0.2F;
            mc.player.movementInput.moveStrafe /= 0.2F;
        }
    }

    private void handleInventoryMovement() {
        mc.player.movementInput.moveStrafe = 0.0F;
        mc.player.movementInput.moveForward = 0.0F;
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()));
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()));
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()));
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()));
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())) {
            ++mc.player.movementInput.moveForward;
            mc.player.movementInput.forwardKeyDown = true;
        } else {
            mc.player.movementInput.forwardKeyDown = false;
        }
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode())) {
            --mc.player.movementInput.moveForward;
            mc.player.movementInput.backKeyDown = true;
        } else {
            mc.player.movementInput.backKeyDown = false;
        }
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode())) {
            ++mc.player.movementInput.moveStrafe;
            mc.player.movementInput.leftKeyDown = true;
        } else {
            mc.player.movementInput.leftKeyDown = false;
        }
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode())) {
            --mc.player.movementInput.moveStrafe;
            mc.player.movementInput.rightKeyDown = true;
        } else {
            mc.player.movementInput.rightKeyDown = false;
        }
        mc.player.movementInput.jump = Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
    }

    private void handleInventoryRotate() {
        if (Keyboard.isKeyDown(200))
            mc.player.rotationPitch -= 4.0F;
        if (Keyboard.isKeyDown(208))
            mc.player.rotationPitch += 4.0F;
        if (Keyboard.isKeyDown(203))
            mc.player.rotationYaw -= 5.0F;
        if (Keyboard.isKeyDown(205))
            mc.player.rotationYaw += 5.0F;
        if (mc.player.rotationPitch > 90.0F)
            mc.player.rotationPitch = 90.0F;
        if (mc.player.rotationPitch < -90.0F)
            mc.player.rotationPitch = -90.0F;
    }

    private boolean isInMoveableScreen() {
        return (mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat));
    }

    private boolean isAboveSoulSand() {
        Vec3d playerPos = CombatUtil.interpolateEntity(mc.player);
        BlockPos blockpos = new BlockPos(playerPos.x, playerPos.y, playerPos.z);
        return (mc.world.getBlockState(blockpos).getBlock() == Blocks.SOUL_SAND);
    }



    private enum SettingPage {
        COMMON,
        WEB
    }
}
