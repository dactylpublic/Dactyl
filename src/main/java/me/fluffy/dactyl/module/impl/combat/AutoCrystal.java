package me.fluffy.dactyl.module.impl.combat;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.event.ForgeEvent;
import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.event.impl.player.EventUpdateWalkingPlayer;
import me.fluffy.dactyl.event.impl.world.EntityRemovedEvent;
import me.fluffy.dactyl.injection.inj.access.ICPacketPlayer;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.CombatUtil;
import me.fluffy.dactyl.util.TimeUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AutoCrystal extends Module {
    Setting<SettingPage> settingPage = new Setting<SettingPage>("Setting", SettingPage.PLACE);

    // place

    // break
    Setting<Boolean> doCaBreak = new Setting<Boolean>("Break", true, vis->settingPage.getValue() == SettingPage.BREAK);
    Setting<Boolean> offhandSwing = new Setting<Boolean>("OffhandSwing", false, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue());
    Setting<AttackLogic> attackLogic = new Setting<AttackLogic>("AttackLogic", AttackLogic.CSLOT, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue());
    Setting<Integer> breakDelay = new Setting<Integer>("BreakDelay", 65, 1, 350, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue());
    Setting<Boolean> antiSuck = new Setting<Boolean>("AntiStuck", true, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue());
    Setting<BreakLogic> breakLogic = new Setting<BreakLogic>("BreakLogic", BreakLogic.DOESDMG, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue());
    Setting<Double> doesDamageMin = new Setting<Double>("DoesDMGMin", 2.3D, 0.1D, 13.5D, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue() && (breakLogic.getValue() == BreakLogic.DOESDMG || breakLogic.getValue() == BreakLogic.BOTH));
    Setting<Boolean> breakRotate = new Setting<Boolean>("BreakRotate", true, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue());
    Setting<Double> breakRange = new Setting<Double>("BreakRange", 5.5D, 1.0D, 6.0D, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue());

    // misc
    Setting<AuraLogic> auraOrder = new Setting<AuraLogic>("AuraOrder", AuraLogic.BREAKPLACE, vis->settingPage.getValue() == SettingPage.MISC);
    Setting<UpdateLogic> updateLogic = new Setting<UpdateLogic>("UpdateLogic", UpdateLogic.PACKET, vis->settingPage.getValue() == SettingPage.MISC);
    Setting<Double> enemyRange = new Setting<Double>("EnemyRange", 10.0D, 1.0D, 13.0D, vis->settingPage.getValue() == SettingPage.MISC);
    Setting<Boolean> rotateHead = new Setting<Boolean>("RotateHead", true, vis->settingPage.getValue() == SettingPage.MISC);
    // render


    public static AutoCrystal INSTANCE;


    private final ConcurrentHashMap<EntityEnderCrystal, Integer> attackedCrystals = new ConcurrentHashMap<>();

    private final TimeUtil placeTimer = new TimeUtil();
    private final TimeUtil breakTimer = new TimeUtil();
    private final TimeUtil checkTimer = new TimeUtil();
    private final TimeUtil antiStuckTimer = new TimeUtil();

    private static float yaw;
    private static float pitch;
    private static boolean isRotating;
    private float oldYaw, oldPitch;

    private List<Vec3d> placedCrystals = new ArrayList<>();

    public AutoCrystal() {
        super("AutoCrystal",
                Category.COMBAT,
                "Place and break crystals (used for Player Versus Player EnderCrystal combat)");
        INSTANCE = this;
    }


    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(updateLogic.getValue() == UpdateLogic.PACKET) {
            if (event.getType() == PacketEvent.PacketType.OUTGOING) {
                if (event.getPacket() instanceof CPacketPlayer && isRotating) {
                    CPacketPlayer packet = (CPacketPlayer) event.getPacket();
                    ((ICPacketPlayer) packet).setYaw(yaw);
                    if(rotateHead.getValue()) {
                        mc.player.rotationYawHead = yaw;
                    }
                    ((ICPacketPlayer) packet).setPitch(pitch);
                }
            }
        }
    }

    @SubscribeEvent
    public void onRemoveEntity(EntityRemovedEvent event) {
        if(event.getEntity() instanceof EntityEnderCrystal) {
            if(attackedCrystals.containsKey(event.getEntity())) {
                attackedCrystals.remove(event.getEntity());
            }
        }
    }


    public void doAutoCrystal(EventUpdateWalkingPlayer eventUpdateWalkingPlayer, UpdateStage updateStage) {
        if(antiStuckTimer.hasPassed(1000)) {
            attackedCrystals.clear();
        }
        if(auraOrder.getValue() == AuraLogic.BREAKPLACE) {
            doBreak(eventUpdateWalkingPlayer, updateStage);
            //doPlace(eventUpdateWalkingPlayer, updateStage);
        } else {
            //doPlace(eventUpdateWalkingPlayer, updateStage);
            doBreak(eventUpdateWalkingPlayer, updateStage);
        }
    }

    private void doBreak(EventUpdateWalkingPlayer eventUpdateWalkingPlayer, UpdateStage updateStage) {
        if(attackLogic.getValue() == AttackLogic.CSLOT) {
            if(mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL && mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL) {
                this.setModuleInfo("");
                resetRots();
                checkTimer.reset();
                return;
            }
            if(!checkTimer.hasPassed(75L)) {
                return;
            }
        }
        EntityEnderCrystal crystal = (EntityEnderCrystal) mc.world.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityEnderCrystal)
                .filter(entity -> mc.player.getDistance(entity) <= breakRange.getValue())
                .filter(entity -> CombatUtil.isBreakableCrystal((EntityEnderCrystal) entity, breakLogic.getValue(), doesDamageMin.getValue(), enemyRange.getValue()))
                .filter(entity -> (!(attackedCrystals.contains(entity) && attackedCrystals.get(entity) > 5 && antiSuck.getValue())))
                .min(Comparator.comparing(entity -> mc.player.getDistance(entity)))
                .orElse(null);
        if (crystal == null || !breakTimer.hasPassed(breakDelay.getValue())) {
            resetRots();
            return;
        }
        if(updateLogic.getValue() == UpdateLogic.WALKING) {
            if (updateStage == UpdateStage.PRE) {
                oldYaw = mc.player.rotationYaw;
                oldPitch = mc.player.rotationPitch;
                if (breakRotate.getValue()) {
                    double[] rots = CombatUtil.calculateLookAt(crystal.posX, crystal.posY, crystal.posZ);
                    mc.player.rotationYaw = (float) rots[0];
                    mc.player.rotationPitch = (float) rots[1];
                }
            } else if (updateStage == UpdateStage.POST) {
                attackCrystal(crystal);
                mc.player.rotationYaw = oldYaw;
                mc.player.rotationPitch = oldPitch;
            }
        } else {
            if(breakRotate.getValue()) {
                float[] rots = CombatUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), crystal.getPositionVector());
                //double[] rots = CombatUtil.calculateLookAt(crystal.posX, crystal.posY, crystal.posZ);
                setRotations(rots[0], rots[1]);
            }
            attackCrystal(crystal);
        }

    }

    private void doPlace(UpdateStage updateStage) {

    }

    private void attackCrystal(Entity entity) {
        mc.playerController.attackEntity(mc.player, entity);
        mc.player.swingArm(offhandSwing.getValue() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
        if (attackedCrystals.containsKey(entity)) {
            attackedCrystals.put((EntityEnderCrystal) entity, attackedCrystals.get(entity) + 1);
        } else {
            attackedCrystals.put((EntityEnderCrystal) entity, 1);
        }
        breakTimer.reset();
    }

    private void setRotations(double newYaw, double newPitch) {
        yaw = (float) newYaw;
        pitch = (float) newPitch;
        isRotating = true;
    }

    private static void resetRots() {
        if (isRotating) {
            yaw = mc.player.rotationYaw;
            pitch = mc.player.rotationPitch;
            isRotating = false;
        }
    }

    @SubscribeEvent
    public void onUpdateWalking(EventUpdateWalkingPlayer event) {
        if(updateLogic.getValue() == UpdateLogic.WALKING) {
            if (event.getStage() == ForgeEvent.Stage.PRE) {
                doAutoCrystal(event, UpdateStage.PRE);
            } else if (event.getStage() == ForgeEvent.Stage.POST) {
                doAutoCrystal(event, UpdateStage.POST);
            }
        }
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(updateLogic.getValue() == UpdateLogic.PACKET) {
            doAutoCrystal(null, UpdateStage.NONE);
        }
    }


    @Override
    public void onToggle() {
        resetToggle();
    }

    private void resetToggle() {
        placeTimer.reset();
        breakTimer.reset();
        checkTimer.reset();
        placedCrystals.clear();
        attackedCrystals.clear();
        resetRots();
    }


    private enum AttackLogic {
        CSLOT,
        ALWAYS
    }

    private enum UpdateStage {
        PRE,
        POST,
        NONE
    }

    private enum AuraLogic {
        BREAKPLACE,
        PLACEBREAK
    }

    private enum UpdateLogic {
        WALKING,
        PACKET
    }

    public enum BreakLogic {
        ALWAYS,
        PLACED,
        DOESDMG,
        BOTH
    }

    private enum SettingPage {
        PLACE,
        BREAK,
        MISC,
        RENDER
    }
}
