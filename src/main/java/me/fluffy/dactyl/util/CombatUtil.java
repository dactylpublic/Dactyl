package me.fluffy.dactyl.util;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.injection.inj.access.IVec3i;
import me.fluffy.dactyl.module.impl.combat.AutoCrystal;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.potion.Potion;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.util.*;
import java.util.stream.Collectors;

public class CombatUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();


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
            if(!AutoCrystal.INSTANCE.getPlacedCrystals().contains(crystal.getPositionVector())) {
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
            if(!AutoCrystal.INSTANCE.getPlacedCrystals().contains(crystal.getPositionVector())) {
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


}
