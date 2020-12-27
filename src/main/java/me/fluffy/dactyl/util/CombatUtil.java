package me.fluffy.dactyl.util;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.injection.inj.access.IBlock;
import me.fluffy.dactyl.injection.inj.access.IVec3i;
import me.fluffy.dactyl.module.impl.combat.AutoCrystal;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockObsidian;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class CombatUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final List<Integer> invalidSlots = Arrays.asList(0, 5, 6, 7, 8);

    private static final List<Integer> hotbarSlots = Arrays.asList(36, 37, 38, 39, 40, 41, 42, 43, 44);

    public static final List<Block> blackList = Arrays.asList(Blocks.TALLGRASS, Blocks.ENDER_CHEST, (Block)Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE, Blocks.ANVIL, Blocks.BREWING_STAND, (Block)Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER, Blocks.TRAPDOOR);
    public static final List<Block> shulkerList = Arrays.asList(Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.SILVER_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX);

    public static final Vec3d[] protectionoffsets = new Vec3d[] {new Vec3d(0.0D, 0.0D, 0.0D), new Vec3d(1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 1.0D, 1.0D), new Vec3d(-1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 1.0D, -1.0D), new Vec3d(1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 0.0D, 1.0D), new Vec3d(-1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 0.0D, -1.0D), new Vec3d(1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 1.0D, 1.0D), new Vec3d(-1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 1.0D, -1.0D)};

    public static final BlockPos[] surroundOffset = new BlockPos[] {new BlockPos(0, -1, 0), new BlockPos(0, 0, -1), new BlockPos(1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(-1, 0, 0)};

    public static ArrayList<Vec3d> getProtectionOffsets() {
        ArrayList<Vec3d> vec3dArrayList = new ArrayList<>();
        BlockPos basePos = (new BlockPos(mc.player.getPositionVector())).down();

        for(int i = 0; i < protectionoffsets.length; i++) {
            Vec3d offset = CombatUtil.protectionoffsets[i];
            BlockPos placePosition = new BlockPos(basePos.add(offset.x, offset.y, offset.z));
            if(checkCanPlace(placePosition)) {
                vec3dArrayList.add(offset);
            }
        }

        return vec3dArrayList;
    }

    public static BlockPos flooredPos() {
        return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
    }

    public static int findHotbarBlock(final Class clazz) {
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY) {
                if (clazz.isInstance(stack.getItem())) {
                    return i;
                }
                if (stack.getItem() instanceof ItemBlock) {
                    final Block block = ((ItemBlock)stack.getItem()).getBlock();
                    if (clazz.isInstance(block)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public static int findCrapple() {
        if (mc.player == null) {
            return -1;
        }
        for (int x = 0; x < mc.player.inventoryContainer.getInventory().size(); x++) {
            if(invalidSlots.contains(x)) {
                continue;
            }
            ItemStack stack = mc.player.inventoryContainer.getInventory().get(x);
            if(stack.isEmpty()) {
                continue;
            }
            if(stack.getItem().equals(Items.GOLDEN_APPLE) && !(stack.getItemDamage() == 1)) {
                return x;
            }
        }
        return -1;
    }

    public static int findItemSlotDamage1(Item i) {
        if (mc.player == null) {
            return -1;
        }
        for (int x = 0; x < mc.player.inventoryContainer.getInventory().size(); x++) {
            if(invalidSlots.contains(x)) {
                continue;
            }
            ItemStack stack = mc.player.inventoryContainer.getInventory().get(x);
            if(stack.isEmpty()) {
                continue;
            }
            if(stack.getItem().equals(i) && (stack.getItemDamage() == 1)) {
                return x;
            }
        }
        return -1;
    }

    public static int findItemSlotNotHotbar(Item i) {
        if (mc.player == null) {
            return -1;
        }
        for (int x = 0; x < mc.player.inventoryContainer.getInventory().size(); x++) {
            if(invalidSlots.contains(x)) {
                continue;
            }
            if(hotbarSlots.contains(x)) {
                continue;
            }
            ItemStack stack = mc.player.inventoryContainer.getInventory().get(x);
            if(stack.isEmpty()) {
                continue;
            }
            if(stack.getItem().equals(i)) {
                return x;
            }
        }
        return -1;
    }

    public static Pair<Integer, Integer> findHotbarRefillSlot(int limit) {
        Pair<Integer, Integer> returnPair = null;
        for (final Map.Entry<Integer, ItemStack> hotbarSlot : getHotbar().entrySet()) {
            final ItemStack stack = hotbarSlot.getValue();
            if (!stack.isEmpty()) {
                if (stack.getItem() == Items.AIR) {
                    continue;
                }
                if (!stack.isStackable()) {
                    continue;
                }
                if (stack.getCount() >= stack.getMaxStackSize()) {
                    continue;
                }
                if (stack.getCount() > limit) {
                    continue;
                }
                final int inventorySlot = findCompatibleInventorySlot(stack);
                if (inventorySlot == -1) {
                    continue;
                }
                returnPair = new Pair<Integer, Integer>(inventorySlot, hotbarSlot.getKey());
            }
        }
        return returnPair;
    }

    private static int findCompatibleInventorySlot(final ItemStack hotbarStack) {
        int inventorySlot = -1;
        int smallestStackSize = 999;
        for (final Map.Entry<Integer, ItemStack> entry : getInventory().entrySet()) {
            final ItemStack inventoryStack = entry.getValue();
            if (!inventoryStack.isEmpty()) {
                if (inventoryStack.getItem() == Items.AIR) {
                    continue;
                }
                if (!isCompatibleStacks(hotbarStack, inventoryStack)) {
                    continue;
                }
                final int currentStackSize = ((ItemStack)mc.player.inventoryContainer.getInventory().get((int)entry.getKey())).getCount();
                if (smallestStackSize <= currentStackSize) {
                    continue;
                }
                smallestStackSize = currentStackSize;
                inventorySlot = entry.getKey();
            }
        }
        return inventorySlot;
    }

    private static boolean isCompatibleStacks(final ItemStack stack1, final ItemStack stack2) {
        if (!stack1.getItem().equals(stack2.getItem())) {
            return false;
        }
        if (stack1.getItem() instanceof ItemBlock && stack2.getItem() instanceof ItemBlock) {
            final Block block1 = ((ItemBlock)stack1.getItem()).getBlock();
            final Block block2 = ((ItemBlock)stack2.getItem()).getBlock();
            if (!((IBlock)block1).getMaterial().equals(((IBlock)block2).getMaterial())) {
                return false;
            }
        }
        return stack1.getDisplayName().equals(stack2.getDisplayName()) && stack1.getItemDamage() == stack2.getItemDamage();
    }

    public static int findItemSlot(Item i) {
        if (mc.player == null) {
            return -1;
        }
        for (int x = 0; x < mc.player.inventoryContainer.getInventory().size(); x++) {
            if(invalidSlots.contains(x)) {
                continue;
            }
            ItemStack stack = mc.player.inventoryContainer.getInventory().get(x);
            if(stack.isEmpty()) {
                continue;
            }
            if(stack.getItem().equals(i)) {
                return x;
            }
        }
        return -1;
    }

    public static int amountInInventory(Item item) {
        int quantity = 0;

        for(int i = 44; i > -1; i--) {
            ItemStack stackInSlot = mc.player.inventoryContainer.getSlot(i).getStack();
            if(stackInSlot.getItem() == item) quantity += stackInSlot.getCount();
        }
        if(mc.player.getHeldItemOffhand().getItem() == item) quantity += mc.player.getHeldItemOffhand().getCount();

        return quantity;
    }

    public static int getBlank() {
        int index = -1;
        for(int i = 44; i > -1; i--) {
            if(mc.player.inventory.getStackInSlot(i).isEmpty()) {
                index = i;
                break;
            }
        }
        return index;
    }

    private static Map<Integer, ItemStack> getInventory() {
        return getInventorySlots(9, 35);
    }

    private static Map<Integer, ItemStack> getHotbar() {
        return getInventorySlots(36, 44);
    }

    public static Map<Integer, ItemStack> getInventorySlots(int current, final int last) {
        final Map<Integer, ItemStack> fullInventorySlots = new HashMap<Integer, ItemStack>();
        while (current <= last) {
            fullInventorySlots.put(current, (ItemStack)mc.player.inventoryContainer.getInventory().get(current));
            ++current;
        }
        return fullInventorySlots;
    }

    public static int getSlotIndex(int index) {
        return index < 9 ? index + 36 : index;
    }

    public static boolean requiredDangerSwitch(double dangerRange) {
        int dangerousCrystals = (int) mc.world.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityEnderCrystal)
                .filter(entity -> mc.player.getDistance(entity) <= dangerRange)
                .filter(entity -> calculateDamage(entity.posX, entity.posY, entity.posZ, mc.player) >= (mc.player.getHealth() + mc.player.getAbsorptionAmount()))
                .count();
        return dangerousCrystals > 0;
    }

    public static boolean passesOffhandCheck(double requiredHealth, Item item, boolean isCrapple) {
        double totalPlayerHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if(!isCrapple) {
            if (findItemSlot(item) == -1) {
                return false;
            }
        } else {
            if(findCrapple() == -1) {
                return false;
            }
        }
        if(totalPlayerHealth < requiredHealth) {
            return false;
        }
        return true;
    }

    public static boolean isHard(Block block) {
        return block == Blocks.OBSIDIAN || block == Blocks.BEDROCK || block == Blocks.ANVIL || block == Blocks.ENDER_CHEST;
    }

    public static Vec3d interpolateEntity(Entity entity) {
        return new Vec3d(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.getRenderPartialTicks(), entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.getRenderPartialTicks(), entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.getRenderPartialTicks());
    }

    public static int findSurroundBlock(boolean ehcestPriority) {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock)stack.getItem()).getBlock();
                if(ehcestPriority) {
                    if (block == Blocks.ENDER_CHEST) {
                        slot = i;
                        break;
                    }
                    if(block == Blocks.OBSIDIAN) {
                        slot = i;
                        break;
                    }
                } else {
                    if(block == Blocks.OBSIDIAN) {
                        slot = i;
                        break;
                    }
                    if (block == Blocks.ENDER_CHEST) {
                        slot = i;
                        break;
                    }
                }
            }
        }
        return slot;
    }

    public static int findBlockInHotbar(Block bc) {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock)stack.getItem()).getBlock();
                if (block == bc) {
                    slot = i;
                    break;
                }
            }
        }
        return slot;
    }

    public static int findItemInHotbar(Item it) {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && !(stack.getItem() instanceof ItemBlock)) {
                if(stack.getItem() == it) {
                    slot = i;
                    break;
                }
            }
        }
        return slot;
    }

    public static void switchOffhandStrict(int targetSlot, int step) {
        switch(step) {
            case 0:
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, targetSlot, 0, ClickType.PICKUP, mc.player);
                break;
            case 1:
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP, mc.player);
                break;
            case 2:
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, targetSlot, 0,ClickType.PICKUP, mc.player);
                mc.playerController.updateController();
                break;
        }
    }

    public static void switchOffhandTotemNotStrict() {
        int targetSlot = findItemSlot(Items.TOTEM_OF_UNDYING);
        if(targetSlot != -1) {
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, targetSlot, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, targetSlot, 0, ClickType.PICKUP, mc.player);
            mc.playerController.updateController();
        }
    }

    public static void switchOffhandNonStrict(int targetSlot) {
        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, targetSlot, 0, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 45, 0, ClickType.PICKUP, mc.player);
        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, targetSlot, 0,ClickType.PICKUP, mc.player);
        mc.playerController.updateController();
    }

    // im sneaking and unsneaking on place as people really underestimate the amount of packets you can send per tick
    public static boolean placeBlock(BlockPos blockPos, boolean offhand, boolean rotate, boolean packetRotate, boolean doSwitch, boolean silentSwitch, int toSwitch) {
        if(!checkCanPlace(blockPos)) {
            return false;
        }

        EnumFacing placeSide = getPlaceSide(blockPos);
        BlockPos adjacentBlock = blockPos.offset(placeSide);
        EnumFacing opposingSide = placeSide.getOpposite();
        if(!mc.world.getBlockState(adjacentBlock).getBlock().canCollideCheck(mc.world.getBlockState(adjacentBlock), false)) {
            return false;
        }
        if(doSwitch) {
            if(!offhand) {
                if (silentSwitch) {
                    mc.player.connection.sendPacket(new CPacketHeldItemChange(toSwitch));
                } else {
                    if (mc.player.inventory.currentItem != toSwitch) {
                        mc.player.inventory.currentItem = toSwitch;
                    }
                }
            }
        }
        boolean isSneak = false;
        if(blackList.contains(mc.world.getBlockState(adjacentBlock).getBlock()) || shulkerList.contains(mc.world.getBlockState(adjacentBlock).getBlock())) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            isSneak = true;
        }
        Vec3d hitVector = getHitVector(adjacentBlock, opposingSide);
        if(rotate) {
            final float[] angle = getLegitRotations(hitVector);
            mc.player.connection.sendPacket(new CPacketPlayer.Rotation(angle[0], angle[1], mc.player.onGround));
        }

        EnumHand actionHand = offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
        mc.playerController.processRightClickBlock(mc.player, mc.world, adjacentBlock, opposingSide, hitVector, actionHand);
        mc.player.connection.sendPacket(new CPacketAnimation(actionHand));
        if(isSneak) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
        return true;
    }

    public static Vec3d getCenterDiff() {
        return new Vec3d(roundToCenter(mc.player.posX), mc.player.posY, roundToCenter(mc.player.posZ)).subtract(mc.player.getPositionVector());
    }

    public static double roundToCenter(double doubleIn) {
        return Math.round(doubleIn + 0.5) - 0.5;
    }


    public static void centerToNearestblock() {
        BlockPos pos = new BlockPos(roundVec(mc.player.getPositionVector(), 0));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(pos.getX() + 0.5, mc.player.getPosition().getY(), pos.getZ() + 0.5, true));
        mc.player.setPositionAndUpdate(pos.getX() + 0.5, mc.player.getPosition().getY(), pos.getZ() + 0.5);
    }



    public static Vec3d roundVec(final Vec3d vec3d, final int places) {
        return new Vec3d(round(vec3d.x, places), round(vec3d.y, places), round(vec3d.z, places));
    }

    public static Vec3d roundVec(final Vec3d vec3d, double offset, final int places) {
        return new Vec3d(round(vec3d.x, places), round(vec3d.y+offset, places), round(vec3d.z, places));
    }

    public static double round(final double value, final int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.FLOOR);
        return bd.doubleValue();
    }

    private static Vec3d getHitVector(BlockPos pos, EnumFacing opposingSide) {
        return new Vec3d(pos).add(0.5, 0.5, 0.5).add(new Vec3d(opposingSide.getDirectionVec()).scale(0.5));
    }

    private static EnumFacing getPlaceSide(BlockPos blockPos) {
        EnumFacing placeableSide = null;
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos adjacent = blockPos.offset(side);
            if (mc.world.getBlockState(adjacent).getBlock().canCollideCheck(mc.world.getBlockState(adjacent), false) && !mc.world.getBlockState(adjacent).getMaterial().isReplaceable()) {
                placeableSide = side;
            }
        }
        return placeableSide;
    }

    public static boolean checkCanPlace(BlockPos pos) {
        if (!(mc.world.getBlockState(pos).getBlock() instanceof BlockAir) && !(mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid)) {
            return false;
        }
        for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos))) {
            if (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb) && !(entity instanceof EntityArrow)) {
                return false;
            }
        }
        return getPlaceSide(pos) != null;
    }


    public static boolean isBreakableCrystal(EntityEnderCrystal crystal, boolean doBreakTrace, double breakTraceRange, boolean antiSuicide, double maxSelfDmg, AutoCrystal.BreakLogic breakLogic, double doesDMGMin, double enemyRange) {
        boolean doesDmgSettingBased = true;
        if(breakLogic == AutoCrystal.BreakLogic.DOESDMG || breakLogic == AutoCrystal.BreakLogic.BOTH) {
            doesDmgSettingBased = false;
            List<Entity> entities = new ArrayList<Entity>((Collection<? extends Entity>) mc.world.playerEntities.stream()
                    .filter(entityPlayer -> mc.player.getDistanceSq(entityPlayer) <= (enemyRange * enemyRange))
                    .filter(entityPlayer -> !Dactyl.friendManager.isFriend(entityPlayer.getName()))
                    .collect(Collectors.toList()));
            for (final Entity e : entities) {
                if (e != mc.player) {
                    if(!(e instanceof EntityLivingBase)) {
                        continue;
                    }
                    if (((EntityLivingBase) e).getHealth() <= 0.0f || ((EntityLivingBase)e).isDead) {
                        continue;
                    }
                    if(((double)calculateDamage(crystal.posX, crystal.posY, crystal.posZ, e) >= doesDMGMin)) {
                        doesDmgSettingBased = true;
                        break;
                    }
                }
            }
        }

        if(!doesDmgSettingBased) {
            return false;
        }

        if(breakLogic == AutoCrystal.BreakLogic.PLACED || breakLogic == AutoCrystal.BreakLogic.BOTH) {
            BlockPos crystalPos = new BlockPos(crystal.getPosition().getX(), crystal.getPosition().getY(), crystal.getPosition().getZ());
            if(!AutoCrystal.INSTANCE.getPlacedCrystals().contains(crystalPos.down())) {
                return false;
            }
        }

        if(antiSuicide) {
            if(((double)calculateDamage(crystal.posX, crystal.posY, crystal.posZ, mc.player) >= maxSelfDmg)) {
                return false;
            }
            if(((double)calculateDamage(crystal.posX, crystal.posY, crystal.posZ, mc.player) >= (mc.player.getHealth()+mc.player.getAbsorptionAmount()))) {
                return false;
            }
        }

        if(doBreakTrace) {
            if(!canSeeEntity(crystal)) {
                if (mc.player.getDistance(crystal) >= breakTraceRange) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean getMapCrystalPlace(EntityEnderCrystal crystal, double breakRange, boolean doBreakTrace, double breakTraceRange, boolean antiSuicide, double maxSelfDmg, AutoCrystal.BreakLogic breakLogic, double doesDMGMin, double enemyRange) {
        boolean doesDmgSettingBased = true;
        if(breakLogic == AutoCrystal.BreakLogic.DOESDMG || breakLogic == AutoCrystal.BreakLogic.BOTH) {
            doesDmgSettingBased = false;
            List<Entity> entities = new ArrayList<Entity>((Collection<? extends Entity>) mc.world.playerEntities.stream()
                    .filter(entityPlayer -> mc.player.getDistanceSq(entityPlayer) <= (enemyRange * enemyRange))
                    .filter(entityPlayer -> !Dactyl.friendManager.isFriend(entityPlayer.getName()))
                    .collect(Collectors.toList()));
            for (final Entity e : entities) {
                if (e != mc.player) {
                    if(!(e instanceof EntityLivingBase)) {
                        continue;
                    }
                    if (((EntityLivingBase) e).getHealth() <= 0.0f || ((EntityLivingBase)e).isDead) {
                        continue;
                    }
                    if(((double)calculateDamage(crystal.posX, crystal.posY, crystal.posZ, e) >= doesDMGMin)) {
                        doesDmgSettingBased = true;
                        break;
                    }
                }
            }
        }

        if(!doesDmgSettingBased) {
            return false;
        }

        if(breakLogic == AutoCrystal.BreakLogic.PLACED || breakLogic == AutoCrystal.BreakLogic.BOTH) {
            BlockPos crystalPos = new BlockPos(crystal.getPosition().getX(), crystal.getPosition().getY(), crystal.getPosition().getZ());
            if(!AutoCrystal.INSTANCE.getPlacedCrystals().contains(crystalPos.down())) {
                return false;
            }
        }

        if(mc.player.getDistance(crystal) >= breakRange) {
            return false;
        }

        if(antiSuicide) {
            if(((double)calculateDamage(crystal.posX, crystal.posY, crystal.posZ, mc.player) >= maxSelfDmg)) {
                return false;
            }
            if(((double)calculateDamage(crystal.posX, crystal.posY, crystal.posZ, mc.player) >= (mc.player.getHealth()+mc.player.getAbsorptionAmount()))) {
                return false;
            }
        }

        if(doBreakTrace) {
            if(!canSeeEntity(crystal)) {
                if (mc.player.getDistance(crystal) >= breakTraceRange) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isHoldingCrystal() {
        return (mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL || mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL);
    }

    public static int getItemCount(Item item) {
        return (mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == item).mapToInt(ItemStack::getCount).sum());
    }

    public static boolean placePosStillSatisfies(BlockPos blockPos, boolean antiSuicide, double maxSelfDmg, double minDamage, double startFacePlaceHealth, boolean doPlaceTrace, double placeTraceRange, double enemyRange, boolean onepointthirteen, double placeRange) {
        final List<Entity> playerEntities = new ArrayList<Entity>((Collection<? extends Entity>) mc.world.playerEntities.stream().filter(entityPlayer -> !Dactyl.friendManager.isFriend(entityPlayer.getName())).collect(Collectors.toList()));
        EntityPlayer satisfiedTarget = null;
        for (Entity entity : playerEntities) {
            if(mc.player.getDistance(entity) > enemyRange) {
                continue;
            }
            if(entity == mc.player) {
                continue;
            }
            if(((EntityLivingBase)entity).getHealth() <= 0.0f ||  ((EntityLivingBase)entity).isDead) {
                continue;
            }
            if(entity.getDistanceSq(blockPos) > 56.2) {
                continue;
            }
            double targetDamage = calculateDamage(((IVec3i)blockPos).getX() + 0.5, ((IVec3i)blockPos).getY() + 1, ((IVec3i)blockPos).getZ() + 0.5, entity);
            float targetHealth = ((EntityLivingBase)entity).getHealth() + ((EntityLivingBase)entity).getAbsorptionAmount();
            if(targetDamage < minDamage && !(targetHealth < startFacePlaceHealth)) {
                continue;
            }
            if(targetDamage <= 2.0) {
                continue;
            }
            satisfiedTarget = (EntityPlayer) entity;
        }

        if(!isValidPlacePos(onepointthirteen, blockPos)) {
            return false;
        }

        if(doPlaceTrace) {
            if(!canSeeBlock(blockPos)) {
                if (mc.player.getDistanceSq(blockPos) > (placeTraceRange * placeTraceRange)) {
                    return false;
                }
            }
        }

        if(mc.player.getDistanceSq(blockPos) > (placeRange*placeRange)) {
            return false;
        }

        double selfDamage = calculateDamage(((IVec3i)blockPos).getX() + 0.5, ((IVec3i)blockPos).getY() + 1, ((IVec3i)blockPos).getZ() + 0.5, (Entity) mc.player);
        float playerHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if(antiSuicide) {
            if(selfDamage > maxSelfDmg) {
                return false;
            }
            if((selfDamage >= playerHealth)) {
                return false;
            }
        }

        if(satisfiedTarget == null) {
            return false;
        }

        return true;
    }

    public static boolean renderPosStillSatisfies(BlockPos blockPos, boolean antiSuicide, double maxSelfDmg, double minDamage, double startFacePlaceHealth, boolean doPlaceTrace, double placeTraceRange, double enemyRange, boolean onepointthirteen, double placeRange) {
        final List<Entity> playerEntities = new ArrayList<Entity>((Collection<? extends Entity>) mc.world.playerEntities.stream().filter(entityPlayer -> !Dactyl.friendManager.isFriend(entityPlayer.getName())).collect(Collectors.toList()));
        EntityPlayer satisfiedTarget = null;
        for (Entity entity : playerEntities) {
            if(mc.player.getDistance(entity) > enemyRange) {
                continue;
            }
            if(entity == mc.player) {
                continue;
            }
            if(((EntityLivingBase)entity).getHealth() <= 0.0f ||  ((EntityLivingBase)entity).isDead) {
                continue;
            }
            if(entity.getDistanceSq(blockPos) > 56.2) {
                continue;
            }
            double targetDamage = calculateDamage(((IVec3i)blockPos).getX() + 0.5, ((IVec3i)blockPos).getY() + 1, ((IVec3i)blockPos).getZ() + 0.5, entity);
            float targetHealth = ((EntityLivingBase)entity).getHealth() + ((EntityLivingBase)entity).getAbsorptionAmount();
            if(targetDamage < minDamage && !(targetHealth < startFacePlaceHealth)) {
                continue;
            }
            if(targetDamage <= 2.0) {
                continue;
            }
            satisfiedTarget = (EntityPlayer) entity;
        }

        if(!isValidRenderPos(onepointthirteen, blockPos)) {
            return false;
        }

        if(doPlaceTrace) {
            if(!canSeeBlock(blockPos)) {
                if (mc.player.getDistanceSq(blockPos) > (placeTraceRange * placeTraceRange)) {
                    return false;
                }
            }
        }

        if(mc.player.getDistanceSq(blockPos) > (placeRange*placeRange)) {
            return false;
        }

        double selfDamage = calculateDamage(((IVec3i)blockPos).getX() + 0.5, ((IVec3i)blockPos).getY() + 1, ((IVec3i)blockPos).getZ() + 0.5, (Entity) mc.player);
        float playerHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if(antiSuicide) {
            if(selfDamage > maxSelfDmg) {
                return false;
            }
            if((selfDamage >= playerHealth)) {
                return false;
            }
        }

        if(satisfiedTarget == null) {
            return false;
        }

        return true;
    }


    public static BlockPos getBestPlacePosition(boolean antiSuicide, double maxSelfDmg, double minDamage, double startFacePlaceHealth, boolean doPlaceTrace, double placeTraceRange, double enemyRange, boolean oneBlockCA, double placeRange) {
        BlockPos placePosition = null;
        final List<BlockPos> placePositions = findPossiblePlacePoses(oneBlockCA, placeRange);
        final List<Entity> playerEnts = new ArrayList<Entity>((Collection<? extends Entity>) mc.world.playerEntities.stream().filter(entityPlayer -> !Dactyl.friendManager.isFriend(entityPlayer.getName())).collect(Collectors.toList()));
        double damage = 2.0;
        for (Entity entity : playerEnts) {
            if(mc.player.getDistance(entity) > enemyRange) {
                continue;
            }
            if(entity == mc.player) {
                continue;
            }
            if(((EntityLivingBase)entity).getHealth() <= 0.0f ||  ((EntityLivingBase)entity).isDead) {
                continue;
            }
            for(BlockPos blockPos : placePositions) {
                if(doPlaceTrace) {
                    if(!canSeeBlock(blockPos)) {
                        if (mc.player.getDistanceSq(blockPos) > (placeTraceRange * placeTraceRange)) {
                            continue;
                        }
                    }
                }
                if(mc.player.getDistanceSq(blockPos) > (placeRange*placeRange)) {
                    continue;
                }
                if(entity.getDistanceSq(blockPos) > 56.2) {
                    continue;
                }
                double targetDamage = calculateDamage(((IVec3i)blockPos).getX() + 0.5, ((IVec3i)blockPos).getY() + 1, ((IVec3i)blockPos).getZ() + 0.5, entity);
                float targetHealth = ((EntityLivingBase)entity).getHealth() + ((EntityLivingBase)entity).getAbsorptionAmount();
                if(targetDamage < minDamage && !(targetHealth < startFacePlaceHealth)) {
                    continue;
                }
                if(targetDamage <= 2.0) {
                    continue;
                }
                if (targetDamage <= damage) {
                    continue;
                }
                double selfDamage = calculateDamage(((IVec3i)blockPos).getX() + 0.5, ((IVec3i)blockPos).getY() + 1, ((IVec3i)blockPos).getZ() + 0.5, (Entity) mc.player);
                float playerHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();
                if(antiSuicide) {
                    if(selfDamage > maxSelfDmg) {
                        continue;
                    }
                    if((selfDamage >= playerHealth)) {
                        continue;
                    }
                }
                if(selfDamage > targetDamage) {
                    continue;
                }
                placePosition = blockPos;
                damage = targetDamage;
            }
        }
        if (damage == 2.0) {
            return null;
        }
        return placePosition;
    }

    public static double getDamageBestPos(boolean antiSuicide, double maxSelfDmg, double minDamage, double startFacePlaceHealth, boolean doPlaceTrace, double placeTraceRange, double enemyRange, boolean oneBlockCA, double placeRange) {
        BlockPos placePosition = null;
        final List<BlockPos> placePositions = findPossiblePlacePoses(oneBlockCA, placeRange);
        final List<Entity> playerEnts = new ArrayList<Entity>((Collection<? extends Entity>) mc.world.playerEntities.stream().filter(entityPlayer -> !Dactyl.friendManager.isFriend(entityPlayer.getName())).collect(Collectors.toList()));
        double damage = 2.0;
        for (Entity entity : playerEnts) {
            if(mc.player.getDistance(entity) > enemyRange) {
                continue;
            }
            if(entity == mc.player) {
                continue;
            }
            if(((EntityLivingBase)entity).getHealth() <= 0.0f ||  ((EntityLivingBase)entity).isDead) {
                continue;
            }
            for(BlockPos blockPos : placePositions) {
                if(doPlaceTrace) {
                    if(!canSeeBlock(blockPos)) {
                        if (mc.player.getDistanceSq(blockPos) > (placeTraceRange * placeTraceRange)) {
                            continue;
                        }
                    }
                }
                if(mc.player.getDistanceSq(blockPos) > (placeRange*placeRange)) {
                    continue;
                }
                if(entity.getDistanceSq(blockPos) > 56.2) {
                    continue;
                }
                double targetDamage = calculateDamage(((IVec3i)blockPos).getX() + 0.5, ((IVec3i)blockPos).getY() + 1, ((IVec3i)blockPos).getZ() + 0.5, entity);
                float targetHealth = ((EntityLivingBase)entity).getHealth() + ((EntityLivingBase)entity).getAbsorptionAmount();
                if(targetDamage < minDamage && !(targetHealth < startFacePlaceHealth)) {
                    continue;
                }
                if(targetDamage <= 2.0) {
                    continue;
                }
                if (targetDamage <= damage) {
                    continue;
                }
                double selfDamage = calculateDamage(((IVec3i)blockPos).getX() + 0.5, ((IVec3i)blockPos).getY() + 1, ((IVec3i)blockPos).getZ() + 0.5, (Entity) mc.player);
                float playerHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();
                if(antiSuicide) {
                    if(selfDamage > maxSelfDmg) {
                        continue;
                    }
                    if((selfDamage >= playerHealth)) {
                        continue;
                    }
                }
                if(selfDamage > targetDamage) {
                    continue;
                }
                placePosition = blockPos;
                damage = targetDamage;
            }
        }
        if (damage == 2.0) {
            return 0.0d;
        }
        return damage;
    }

    public static boolean isFacePlaceCrystal(EntityEnderCrystal crystal, double minFacePlaceHP, boolean placeTrace, double wallsPlaceRange, double placeRange, double enemyRange) {
        final List<Entity> playerEnts = new ArrayList<Entity>((Collection<? extends Entity>) mc.world.playerEntities.stream().filter(entityPlayer -> !Dactyl.friendManager.isFriend(entityPlayer.getName())).collect(Collectors.toList()));
        if(placeTrace) {
            if(!canSeeEntity(crystal) && mc.player.getDistance(crystal) > wallsPlaceRange) {
                return false;
            }
        }
        if(mc.player.getDistance(crystal) > placeRange) {
            return false;
        }
        for (Entity entity : playerEnts) {
            if(mc.player.getDistance(entity) > enemyRange) {
                continue;
            }
            if(entity == mc.player) {
                continue;
            }
            if(((EntityLivingBase)entity).getHealth() <= 0.0f ||  ((EntityLivingBase)entity).isDead) {
                continue;
            }
            float entHP = ((EntityLivingBase)entity).getHealth()+((EntityLivingBase)entity).getAbsorptionAmount();
            if(entHP > minFacePlaceHP) {
                continue;
            }
            double targetDMG = calculateDamage(crystal, entity);
            if(targetDMG >= 1.4d) {
                return true;
            }
        }
        return false;
    }

    public static BlockPos getBestPlacePosIgnoreAlreadyPlaced(boolean antiSuicide, double maxSelfDmg, double minDamage, double startFacePlaceHealth, boolean doPlaceTrace, double placeTraceRange, double enemyRange, boolean oneBlockCA, double placeRange) {
        BlockPos placePosition = null;
        final List<BlockPos> placePositions = findImpossiblePlacePoses(oneBlockCA, placeRange);
        final List<Entity> playerEnts = new ArrayList<Entity>((Collection<? extends Entity>) mc.world.playerEntities.stream().filter(entityPlayer -> !Dactyl.friendManager.isFriend(entityPlayer.getName())).collect(Collectors.toList()));
        double damage = 2.0;
        for (Entity entity : playerEnts) {
            if(mc.player.getDistance(entity) > enemyRange) {
                continue;
            }
            if(entity == mc.player) {
                continue;
            }
            if(((EntityLivingBase)entity).getHealth() <= 0.0f ||  ((EntityLivingBase)entity).isDead) {
                continue;
            }
            for(BlockPos blockPos : placePositions) {
                if(doPlaceTrace) {
                    if(!canSeeBlock(blockPos)) {
                        if (mc.player.getDistanceSq(blockPos) > (placeTraceRange * placeTraceRange)) {
                            continue;
                        }
                    }
                }
                if(mc.player.getDistanceSq(blockPos) > (placeRange*placeRange)) {
                    continue;
                }
                if(entity.getDistanceSq(blockPos) > 56.2) {
                    continue;
                }
                double targetDamage = calculateDamage(((IVec3i)blockPos).getX() + 0.5, ((IVec3i)blockPos).getY() + 1, ((IVec3i)blockPos).getZ() + 0.5, entity);
                float targetHealth = ((EntityLivingBase)entity).getHealth() + ((EntityLivingBase)entity).getAbsorptionAmount();
                if(targetDamage < minDamage && !(targetHealth < startFacePlaceHealth)) {
                    continue;
                }
                if(targetDamage <= 2.0) {
                    continue;
                }
                if (targetDamage <= damage) {
                    continue;
                }
                double selfDamage = calculateDamage(((IVec3i)blockPos).getX() + 0.5, ((IVec3i)blockPos).getY() + 1, ((IVec3i)blockPos).getZ() + 0.5, (Entity) mc.player);
                float playerHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();
                if(antiSuicide) {
                    if(selfDamage > maxSelfDmg) {
                        continue;
                    }
                    if((selfDamage >= playerHealth)) {
                        continue;
                    }
                }
                if(selfDamage > targetDamage) {
                    continue;
                }
                placePosition = blockPos;
                damage = targetDamage;
            }
        }
        if (damage == 2.0) {
            return null;
        }
        return placePosition;
    }

    public static double getHighestDMGWorld(double enemyRange, BlockPos blockPos) {
        final List<Entity> playerEnts = new ArrayList<Entity>((Collection<? extends Entity>) mc.world.playerEntities.stream().filter(entityPlayer -> !Dactyl.friendManager.isFriend(entityPlayer.getName())).collect(Collectors.toList()));
        EntityPlayer player = null;
        double bestDMG = 1.3d;
        for (Entity entity : playerEnts) {
            if(mc.player.getDistance(entity) > enemyRange) {
                continue;
            }
            if(entity == mc.player) {
                continue;
            }
            if(((EntityLivingBase)entity).getHealth() <= 0.0f ||  ((EntityLivingBase)entity).isDead) {
                continue;
            }
            double targetDamage = calculateDamage(((IVec3i)blockPos).getX() + 0.5, ((IVec3i)blockPos).getY() + 1, ((IVec3i)blockPos).getZ() + 0.5, entity);
            if(targetDamage > bestDMG) {
                bestDMG = targetDamage;
                player = (EntityPlayer) entity;
            }
        }
        return bestDMG;
    }


    public static EntityPlayer getGreatestDamageOnPlayer(double enemyRange, BlockPos blockPos) {
        final List<Entity> playerEnts = new ArrayList<Entity>((Collection<? extends Entity>) mc.world.playerEntities.stream().filter(entityPlayer -> !Dactyl.friendManager.isFriend(entityPlayer.getName())).collect(Collectors.toList()));
        EntityPlayer player = null;
        double bestDMG = 1.3d;
        for (Entity entity : playerEnts) {
            if(mc.player.getDistance(entity) > enemyRange) {
                continue;
            }
            if(entity == mc.player) {
                continue;
            }
            if(((EntityLivingBase)entity).getHealth() <= 0.0f ||  ((EntityLivingBase)entity).isDead) {
                continue;
            }
            double targetDamage = calculateDamage(((IVec3i)blockPos).getX() + 0.5, ((IVec3i)blockPos).getY() + 1, ((IVec3i)blockPos).getZ() + 0.5, entity);
            if(targetDamage > bestDMG) {
                bestDMG = targetDamage;
                player = (EntityPlayer) entity;
            }
        }
        return player;
    }


    public static List<BlockPos> getSphere(BlockPos pos, float r, int h, boolean hollow, boolean sphere, int plus_y) {
        List<BlockPos> circleblocks = new ArrayList<>();
        int cx = pos.getX();
        int cy = pos.getY();
        int cz = pos.getZ();
        for (int x = cx - (int) r; x <= cx + r; x++) {
            for (int z = cz - (int) r; z <= cz + r; z++) {
                for (int y = (sphere ? cy - (int) r : cy); y < (sphere ? cy + r : cy + h); y++) {
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
                    if (dist < r * r && !(hollow && dist < (r - 1) * (r - 1))) {
                        BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                }
            }
        }
        return circleblocks;
    }

    private static List<BlockPos> findPossiblePlacePoses(boolean isOnePointThirteen, double placeRange) {
        NonNullList positions = NonNullList.create();
        positions.addAll(getSphere(new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ)), (float)placeRange, (int)placeRange, false, true, 0).stream().filter(pos->isValidPlacePos(isOnePointThirteen, pos)).collect(Collectors.toList()));
        return (List<BlockPos>)positions;
    }

    private static List<BlockPos> findImpossiblePlacePoses(boolean isOnePointThirteen, double placeRange) {
        NonNullList positions = NonNullList.create();
        positions.addAll(getSphere(new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ)), (float)placeRange, (int)placeRange, false, true, 0).stream().filter(pos->isImpossiblePlacePos(isOnePointThirteen, pos)).collect(Collectors.toList()));
        return (List<BlockPos>)positions;
    }

    public static boolean wontSelfPop(EntityEnderCrystal crystal, boolean antiSui, double maxSelfDMG) {
        if(antiSui) {
            double dmg = calculateDamage(crystal, mc.player);
            if (dmg >= (mc.player.getHealth()+mc.player.getAbsorptionAmount())) {
                return false;
            }
            if(dmg >= maxSelfDMG) {
                return false;
            }
        }
        return true;
    }


    public static class Pair<T, S> {
        T key;
        S value;

        public Pair(final T key, final S value) {
            this.key = key;
            this.value = value;
        }

        public T getKey() {
            return this.key;
        }

        public S getValue() {
            return this.value;
        }

        public void setKey(final T key) {
            this.key = key;
        }

        public void setValue(final S value) {
            this.value = value;
        }
    }

    private static boolean isValidRenderPos(boolean isOnePointThirteen, BlockPos blockPos) {
        if(!isOnePointThirteen) {
            final BlockPos boost = blockPos.add(0, 1, 0);
            final BlockPos boost2 = blockPos.add(0, 2, 0);
            return (mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && mc.world.getBlockState(boost).getBlock() == Blocks.AIR && mc.world.getBlockState(boost2).getBlock() == Blocks.AIR && containsNotCrystals(boost) && containsNotCrystals(boost2);
        } else {
            final BlockPos boost = blockPos.add(0, 1, 0);
            return (mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && mc.world.getBlockState(boost).getBlock() == Blocks.AIR && containsNotCrystals(boost);
        }
    }

    private static boolean containsNotCrystals(BlockPos pos) {
        if(mc.world.getEntitiesWithinAABB((Class)Entity.class, new AxisAlignedBB(pos)) != null) {
            List<Entity> entityList = mc.world.getEntitiesWithinAABB((Class) Entity.class, new AxisAlignedBB(pos));
            for(Entity entity : entityList) {
                if(!(entity instanceof EntityEnderCrystal)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isValidPlacePos(boolean isOnePointThirteen, BlockPos blockPos) {
        if(!isOnePointThirteen) {
            final BlockPos boost = blockPos.add(0, 1, 0);
            final BlockPos boost2 = blockPos.add(0, 2, 0);
            return (mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && mc.world.getBlockState(boost).getBlock() == Blocks.AIR && mc.world.getBlockState(boost2).getBlock() == Blocks.AIR && mc.world.getEntitiesWithinAABB((Class)Entity.class, new AxisAlignedBB(boost)).isEmpty() && mc.world.getEntitiesWithinAABB((Class)Entity.class, new AxisAlignedBB(boost2)).isEmpty();
        } else {
            final BlockPos boost = blockPos.add(0, 1, 0);
            return (mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && mc.world.getBlockState(boost).getBlock() == Blocks.AIR && mc.world.getEntitiesWithinAABB((Class)Entity.class, new AxisAlignedBB(boost)).isEmpty();
        }
    }

    private static boolean isImpossiblePlacePos(boolean isOnePointThirteen, BlockPos blockPos) {
        if(!isOnePointThirteen) {
            final BlockPos boost = blockPos.add(0, 1, 0);
            final BlockPos boost2 = blockPos.add(0, 2, 0);
            return (mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && mc.world.getBlockState(boost).getBlock() == Blocks.AIR && mc.world.getBlockState(boost2).getBlock() == Blocks.AIR;
        } else {
            final BlockPos boost = blockPos.add(0, 1, 0);
            return (mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && mc.world.getBlockState(boost).getBlock() == Blocks.AIR;
        }
    }

    public static float calculateDamage(final double posX, final double posY, final double posZ, final Entity entity) {
        final float doubleExplosionSize = 12.0f;
        final double distancedsize = entity.getDistance(posX, posY, posZ) / doubleExplosionSize;
        final Vec3d vec3d = new Vec3d(posX, posY, posZ);
        final double blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        final double v = (1.0 - distancedsize) * blockDensity;
        final float damage = (float)(int)((v * v + v) / 2.0 * 7.0 * doubleExplosionSize + 1.0);
        double finald = 1.0;
        if (entity instanceof EntityLivingBase) {
            finald = getBlastReduction((EntityLivingBase)entity, getDamageMultiplied(damage), new Explosion((World)mc.world, (Entity)null, posX, posY, posZ, 6.0f, false, true));
        }
        return (float)finald;
    }

    public static float getBlastReduction(final EntityLivingBase entity, float damage, final Explosion explosion) {
        if (entity instanceof EntityPlayer) {
            final EntityPlayer ep = (EntityPlayer)entity;
            final DamageSource ds = DamageSource.causeExplosionDamage(explosion);
            damage = CombatRules.getDamageAfterAbsorb(damage, (float)ep.getTotalArmorValue(), (float)ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
            final int k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
            final float f = MathHelper.clamp((float)k, 0.0f, 20.0f);
            damage *= 1.0f - f / 25.0f;
            if (entity.isPotionActive(Potion.getPotionById(11))) {
                damage -= damage / 4.0f;
            }
            return damage;
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, (float)entity.getTotalArmorValue(), (float)entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        return damage;
    }

    private static float getDamageMultiplied(final float damage) {
        final int diff = mc.world.getDifficulty().getId();
        return damage * ((diff == 0) ? 0.0f : ((diff == 2) ? 1.0f : ((diff == 1) ? 0.5f : 1.5f)));
    }

    public static float calculateDamage(final EntityEnderCrystal crystal, final Entity entity) {
        return calculateDamage(crystal.posX, crystal.posY, crystal.posZ, entity);
    }

    public static float calculateDamage(BlockPos pos, final Entity entity) {
        return calculateDamage(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, entity);
    }

    public static boolean canSeeBlock(BlockPos pos) {
        return (mc.world.rayTraceBlocks(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(pos.getX(), pos.getY()+1.0f, pos.getZ()), false, true, false) == null);
    }


    public static boolean canSeeEntity(Entity entity) {
        return (mc.world.rayTraceBlocks(mc.player.getPositionEyes(mc.getRenderPartialTicks()), entity.getPositionVector(), false, true, false) == null);
    }

    public static double[] calculateLookAt(double px, double py, double pz) {
        double dirx = mc.player.posX - px;
        double diry = mc.player.posY - py;
        double dirz = mc.player.posZ - pz;
        double len = Math.sqrt(dirx*dirx + diry*diry + dirz*dirz);
        dirx /= len;
        diry /= len;
        dirz /= len;
        double pitch = Math.asin(diry);
        double yaw = Math.atan2(dirz, dirx);
        pitch = pitch * 180.0d / Math.PI;
        yaw = yaw * 180.0d / Math.PI;
        yaw += 90f;
        return new double[]{yaw,pitch};
    }

    public static float[] calcAngle(final Vec3d from, final Vec3d to) {
        final double difX = to.x - from.x;
        final double difY = (to.y - from.y) * -1.0;
        final double difZ = to.z - from.z;
        final double dist = MathHelper.sqrt(difX * difX + difZ * difZ);
        return new float[] { (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0), (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist))) };
    }

    public static float[] getLegitRotations(Vec3d vec) {
        Vec3d eyesPos = new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        float pitch = (float)-Math.toDegrees(Math.atan2(diffY, diffXZ));

        return new float[] { mc.player.rotationYaw +
                MathHelper.wrapDegrees(yaw - mc.player.rotationYaw), mc.player.rotationPitch +
                MathHelper.wrapDegrees(pitch - mc.player.rotationPitch) };
    }



}
