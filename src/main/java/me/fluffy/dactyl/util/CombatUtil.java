package me.fluffy.dactyl.util;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.module.impl.combat.AutoCrystal;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CombatUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();


    public static boolean isBreakableCrystal(EntityEnderCrystal crystal, AutoCrystal.BreakLogic breakLogic, double doesDMGMin, double enemyRange) {
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

        return true;
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
