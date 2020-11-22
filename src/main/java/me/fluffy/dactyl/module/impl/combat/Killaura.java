package me.fluffy.dactyl.module.impl.combat;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.event.ForgeEvent;
import me.fluffy.dactyl.event.impl.player.EventUpdateWalkingPlayer;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.RotationUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Killaura extends Module {
    Setting<AttackMode> attackLogic = new Setting<AttackMode>("HoldLogic", AttackMode.SWORD);
    Setting<Boolean> hitDelay = new Setting<Boolean>("HitDelay", true);
    Setting<Boolean> rotate = new Setting<Boolean>("Rotate", true);
    Setting<Boolean> rotateInWebs = new Setting<Boolean>("WebRotate", false, v->rotate.getValue());
    Setting<Boolean> players = new Setting<Boolean>("Players", true);
    Setting<Boolean> mobs = new Setting<Boolean>("Mobs", false);
    Setting<Boolean> animals = new Setting<Boolean>("Animals", false);
    Setting<Boolean> vehicles = new Setting<Boolean>("Vehicles", true);
    Setting<Boolean> autoSwitch = new Setting<Boolean>("AutoSwitch", false);
    Setting<Integer> attackSpeed = new Setting<Integer>("AttackSpeed", 12, 1, 20, v->!hitDelay.getValue());
    Setting<RotationMode> rotationModeSetting = new Setting<RotationMode>("Rots", RotationMode.UNIVERSAL, v->rotate.getValue());
    Setting<Double> range = new Setting<Double>("Range", 4.5D, 1.0D, 6.0D);

    public static Killaura INSTANCE;
    public Killaura() {
        super("Killaura", Category.COMBAT);
        INSTANCE = this;
    }

    boolean isLooking = false;
    float yaw;
    float pitch;
    TimeUtil stopwatch = new TimeUtil();
    public static Entity target;


    private float[] getRotationsToward(final Entity closestEntity) {
        double xDist = closestEntity.posX - mc.player.posX;
        double yDist = closestEntity.posY + closestEntity.getEyeHeight() - (mc.player.posY + mc.player.getEyeHeight());
        double zDist = closestEntity.posZ - mc.player.posZ;
        double fDist = MathHelper.sqrt(xDist * xDist + zDist * zDist);
        float yaw = this.fixRotation(mc.player.rotationYaw, (float) (MathHelper.atan2(zDist, xDist) * 180.0D / Math.PI) - 90.0F, 360F);
        float pitch = this.fixRotation(mc.player.rotationPitch, (float) (-(MathHelper.atan2(yDist, fDist) * 180.0D / Math.PI)), 360F);
        return new float[]{yaw, pitch};
    }

    private float fixRotation(final float p_70663_1_, final float p_70663_2_, final float p_70663_3_) {
        float var4 = MathHelper.wrapDegrees(p_70663_2_ - p_70663_1_);
        if (var4 > p_70663_3_) {
            var4 = p_70663_3_;
        }
        if (var4 < -p_70663_3_) {
            var4 = -p_70663_3_;
        }
        return p_70663_1_ + var4;
    }

    private float getSensitivityMultiplier() {
        float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        return (f * f * f * 8.0F) * 0.15F;
    }

    private void attackEntity(Entity entityLivingBase) {
        final ItemStack stack = mc.player.getHeldItem(EnumHand.OFF_HAND);
        if (stack != ItemStack.EMPTY && stack.getItem() == Items.SHIELD) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
        }
        mc.player.connection.sendPacket(new CPacketUseEntity(entityLivingBase));
        mc.player.swingArm(EnumHand.MAIN_HAND);
        mc.player.resetCooldown();
        lastHit = stopwatch.getLastRecorded();
    }

    long lastHit = -1L;

    @Override
    public void onToggle() {
        this.yaw = -1L;
        this.pitch = -1L;
        this.isLooking = false;
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(EventUpdateWalkingPlayer event) {
        if (mc.world == null || mc.player == null)
            return;
        if (event.getStage() == ForgeEvent.Stage.PRE) {
            if(rotationModeSetting.getValue() == RotationMode.UNIVERSAL) {
                RotationUtil.updateRotations();
            }
            target = findTarget();
            if (target != null) {
                if(autoSwitch.getValue()) {
                    equipBestWeapon();
                }
                if(rotate.getValue()) {
                    final float[] rots = getRotationsToward(target);
                    float sens = getSensitivityMultiplier();
                    float yawGCD = (Math.round(rots[0] / sens) * sens);
                    float pitchGCD = (Math.round(rots[1] / sens) * sens);
                    boolean doRotate = true;
                    if(mc.player.isInWeb) {
                        if(!rotateInWebs.getValue()) {
                            doRotate = false;
                        }
                    }
                    if(doRotate) {
                        if(rotationModeSetting.getValue() == RotationMode.CLIENT) {
                            event.setYaw(yawGCD);
                            event.setPitch(pitchGCD);
                            event.rotationUsed = true;
                        } else if(rotationModeSetting.getValue() == RotationMode.UNIVERSAL) {
                            RotationUtil.setPlayerRotations(yawGCD, pitchGCD);
                        }
                    }
                }
            }
        } else {
            if(target == null) {
                this.setModuleInfo("");
                return;
            }
            if(target instanceof EntityPlayer) {
                this.setModuleInfo(target.getName());
            }
            if(hitDelay.getValue()) {
                final float ticks = 20.0f - Dactyl.INSTANCE.getTickRateManager().getTickRate();
                final boolean canAttack = mc.player.getCooledAttackStrength(-ticks) >= 1;
                if (canAttack) {
                    attackEntity(target);
                }
            } else {
                if(target == null) {
                    return;
                }
                if (System.nanoTime() / 1000000L - this.lastHit >=  attackSpeed.getValue() * 50) {
                    if(target != null) {
                        attackEntity(target);
                    }
                }
            }
            RotationUtil.restoreRotations();
        }
    }


    private Entity findTarget() {
        Entity ent = null;

        double maxDist = range.getValue();

        for (Entity e : mc.world.loadedEntityList) {
            if (e != null) {
                if (this.canHit(e) && isValid(e)) {
                    float currentDist = mc.player.getDistance(e);

                    if (currentDist <= maxDist) {
                        maxDist = currentDist;
                        ent = e;
                    }
                }
            }
        }
        if(attackLogic.getValue() == AttackMode.AXE && !(mc.player.getHeldItemMainhand().getItem() instanceof ItemAxe)) {
            ent = null;
        }
        if(attackLogic.getValue() == AttackMode.SWORD && !(mc.player.getHeldItemMainhand().getItem() instanceof ItemSword)) {
            ent = null;
        }

        return ent;
    }

    private boolean canHit(Entity e) {
        if(e instanceof EntityPlayer && players.getValue()) {
            if(Dactyl.friendManager.isFriend(e.getName())) {
                return false;
            } else {
                return true;
            }
        }
        if (mobs.getValue() && e instanceof IMob) {
            return true;
        }
        if(e instanceof IAnimals && !(e instanceof IMob) && animals.getValue()) {
            return true;
        }
        if((e instanceof EntityBoat || e instanceof EntityMinecart || e instanceof EntityMinecartContainer) && vehicles.getValue()) {
            return true;
        }
        return false;
    }

    private boolean isValid(Entity e) {
        if(!(e instanceof EntityLivingBase)) {
            return false;
        }
        EntityLivingBase ent = (EntityLivingBase)e;
        if(ent.getHealth() <= 0 || ent.isDead) {
            return false;
        }
        if(e == mc.player) {
            return false;
        }
        return true;
    }

    public static void equipBestWeapon() {
        int bestSlot = -1;
        double maxDamage = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof ItemSword) {
                double damage = (((ItemSword) stack.getItem()).getAttackDamage() + (double) EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED));
                if (damage > maxDamage) {
                    maxDamage = damage;
                    bestSlot = i;
                }
            }
        }
        if (bestSlot != -1) equip(bestSlot);
    }

    private static void equip(int slot) {
        mc.player.inventory.currentItem = slot;
    }

    public enum RotationMode {
        UNIVERSAL,
        CLIENT
    }


    public enum AttackMode {
        SWORD,
        AXE,
        ALWAYS
    }
}
