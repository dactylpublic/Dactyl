package me.chloe.dactyl.module.impl.combat;

import me.chloe.dactyl.Dactyl;
import me.chloe.dactyl.event.ForgeEvent;
import me.chloe.dactyl.event.impl.network.PacketEvent;
import me.chloe.dactyl.event.impl.player.EventUpdateWalkingPlayer;
import me.chloe.dactyl.util.Bind;
import me.chloe.dactyl.util.CombatUtil;
import me.chloe.dactyl.util.TimeUtil;
import me.chloe.dactyl.event.impl.action.EventKeyPress;
import me.chloe.dactyl.event.impl.world.Render3DEvent;
import me.chloe.dactyl.injection.inj.access.ICPacketUseEntity;
import me.chloe.dactyl.module.Module;
import me.chloe.dactyl.module.impl.client.Colors;
import me.chloe.dactyl.setting.Setting;
import me.chloe.dactyl.util.*;
import me.chloe.dactyl.util.render.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class BetaCrystal extends Module {

    public Setting<SettingPage> page = new Setting<SettingPage>("Setting", SettingPage.PLACE);

    // place
    public Setting<Boolean> doPlace = new Setting<Boolean>("Place", true, v->isViewPlace());
    public Setting<WallsRange> placeTrace = new Setting<WallsRange>("PlaceTrace", WallsRange.RANGE, v->isViewPlace() && doPlace.getValue());
    public Setting<Double> placeSpeed = new Setting<Double>("PlaceSpeed", 20d, 1d, 20d, v->isViewPlace() && doPlace.getValue());
    public Setting<Boolean> antiSuiPlace = new Setting<Boolean>("AntiSelfPop", true, v->isViewPlace() && doPlace.getValue());
    public Setting<Double> placeMaxSelf = new Setting<Double>("MaxSelfPlace", 10.0d, 1.0d, 13.0d, v->isViewPlace() && doPlace.getValue() && antiSuiPlace.getValue());
    public Setting<Boolean> placeRotate = new Setting<Boolean>("PlaceRotate", true, v->isViewPlace() && doPlace.getValue());
    public Setting<Double> minDamage = new Setting<Double>("MinDamage", 6.0d, 1.0d, 13.0d, v->isViewPlace() && doPlace.getValue());
    public Setting<Double> facePlaceH = new Setting<Double>("FPHealth", 10.0d, 1.0d, 36.0d, v->isViewPlace() && doPlace.getValue());
    public Setting<Boolean> ignoreValidExploit = new Setting<Boolean>("IgnoreInvalid", true, v->isViewPlace() && doPlace.getValue(), "PlaceExploit from old ca");
    //public Setting<Boolean> spawnPacketExploit = new Setting<Boolean>("SpawnExploit", true, v->isViewPlace() && doPlace.getValue(), "New PlaceExploit lol");
    public Setting<Boolean> noPlaceAttack = new Setting<Boolean>("NoPlaceAttack", true, v->isViewPlace() && doPlace.getValue());
    public Setting<Boolean> oneBlockCA = new Setting<Boolean>("1.13+", false, v->isViewPlace() && doPlace.getValue());
    public Setting<Double> placeRange = new Setting<Double>("PlaceRange", 6.0d, 1.0d, 6.0d, v->isViewPlace() && doPlace.getValue());
    public Setting<Double> wallsPlace = new Setting<Double>("WallsPlace", 3.0d, 1.0d, 6.0d, v->isViewPlace() && doPlace.getValue() && (placeTrace.getValue() == WallsRange.RANGE));
    //public Setting<Boolean> strictDirection = new Setting<Boolean>("StrictDirection", false, v->isViewPlace() && doPlace.getValue());
    public Setting<Boolean> antiRecalc = new Setting<Boolean>("AntiRecalc", true, v->isViewPlace() && doPlace.getValue());
    public Setting<Boolean> doRecalcOverride = new Setting<Boolean>("Override", false, v->isViewPlace() && doPlace.getValue());
    public Setting<Double> recalcDmgOverride = new Setting<Double>("RecalcOverride", 8.5d, 1.0d, 16.0d, v->isViewPlace() && doPlace.getValue() && antiRecalc.getValue() && doRecalcOverride.getValue());
    public Setting<Boolean> countFacePlace = new Setting<Boolean>("CountFP", true, v->isViewPlace() && doPlace.getValue());
    public Setting<Double> minCountDmg = new Setting<Double>("MinCountDmg", 3.0d, 0.1d, 12.0d, v->isViewPlace() && doPlace.getValue());
    public Setting<Integer> maxInRange = new Setting<Integer>("MaxPlaced", 1, 1, 5, v->isViewPlace() && doPlace.getValue());
    public Setting<Boolean> facePlaceHold = new Setting<Boolean>("FPHold", true, v->isViewPlace() && doPlace.getValue());
    public Setting<Bind> facePlaceKey = new Setting<Bind>("FPKey", new Bind(Keyboard.KEY_NONE), v->isViewPlace() && doPlace.getValue());

    // break
    public Setting<Boolean> doBreak = new Setting<Boolean>("Break", true, v->isViewBreak());
    public Setting<WallsRange> breakTrace = new Setting<WallsRange>("BreakTrace", WallsRange.RANGE, v->isViewBreak() && doBreak.getValue());
    public Setting<BreakLogic> runLogic = new Setting<BreakLogic>("RunLogic", BreakLogic.HOLDING, v->isViewBreak() && doBreak.getValue());
    public Setting<Double> breakSpeed = new Setting<Double>("BreakSpeed", 20.0d, 1.0d, 20.0d, v->isViewBreak() && doBreak.getValue());
    public Setting<Integer> hitsPerSecond = new Setting<Integer>("HitsPerSec", 5, 1, 20, v->isViewBreak() && doBreak.getValue());
    public Setting<Boolean> soundRemove = new Setting<Boolean>("SoundRemove", true, v->isViewBreak() && doBreak.getValue());
    public Setting<Boolean> breakRotate = new Setting<Boolean>("BreakRotate", true, v->isViewBreak() && doBreak.getValue());
    public Setting<PassLogic> passLogic = new Setting<PassLogic>("HitLogic", PassLogic.DOESDMG, v->isViewBreak() && doBreak.getValue());
    public Setting<Double> minHitDamage = new Setting<Double>("MinHitDamage", 1.2d, 0.5d, 10.0d, v->isViewBreak() && doBreak.getValue() && passLogic.getValue() == PassLogic.DOESDMG);
    public Setting<Double> breakRange = new Setting<Double>("BreakRange", 6.0d, 1.0d, 6.0d, v->isViewBreak() && doBreak.getValue());
    public Setting<Double> wallsBreak = new Setting<Double>("WallsBreak", 3.5d, 1.0d, 6.0d, v->isViewBreak() && doBreak.getValue() && breakTrace.getValue() == WallsRange.RANGE);
    public Setting<Boolean> antiSuiBreak = new Setting<Boolean>("AntiSuicide", true, v->isViewBreak() && doBreak.getValue());
    public Setting<Double> maxSelfBreak = new Setting<Double>("MaxSelfBreak", 8.0d, 1.0d, 13.0d, v->isViewBreak() && doBreak.getValue() && antiSuiBreak.getValue());
    public Setting<Integer> ticksExisted = new Setting<Integer>("TicksExisted", 0, 0, 10, v->isViewBreak() && doBreak.getValue());
    public Setting<Boolean> preAttackRotate = new Setting<Boolean>("PreRotate", true, v->isViewBreak() && doBreak.getValue() && breakRotate.getValue());
    public Setting<Boolean> preAttack = new Setting<Boolean>("PreAttack", true, v->isViewBreak() && doBreak.getValue());

    // general
    public Setting<YawStepEnum> yawStepEnum = new Setting<YawStepEnum>("YawStep", YawStepEnum.BOTH, v->isViewGeneral());
    public Setting<Integer> yawStep = new Setting<Integer>("StepAmount", 55, 5, 180, v->isViewGeneral() && yawStepEnum.getValue() != YawStepEnum.OFF);
    public Setting<Integer> stepTicks = new Setting<Integer>("StepTicks", 1, 1, 20, v->isViewGeneral() && yawStepEnum.getValue() != YawStepEnum.OFF);
    public Setting<SwingLogic> swingSetting = new Setting<SwingLogic>("Swing", SwingLogic.BOTH, v->isViewGeneral());
    public Setting<Integer> loginWaitTicks = new Setting<Integer>("LoginTicks", 0, 1, 500, v->isViewGeneral());
    public Setting<Boolean> multiPoint = new Setting<Boolean>("MultiPoint", true, v->isViewGeneral());
    public Setting<Boolean> multiPointRotations = new Setting<Boolean>("PointRotations", false, v->isViewGeneral());
    public Setting<Boolean> debugRotate = new Setting<Boolean>("DebugRotate", false, v->isViewGeneral());
    public Setting<Boolean> rotateHead = new Setting<Boolean>("RotateHead", true, v->isViewGeneral());
    public Setting<Double> enemyRange = new Setting<Double>("EnemyRange", 10.0D, 1.0D, 16.0D, v->isViewGeneral());

    // render
    public Setting<Boolean> renderESP = new Setting<Boolean>("Render", true, v->isViewRender());
    public Setting<Boolean> damageText = new Setting<Boolean>("Damage", true, v->isViewRender());
    public Setting<Boolean> colorSync = new Setting<Boolean>("ColorSync", false, v->isViewRender());
    public Setting<Boolean> fadeOut = new Setting<Boolean>("FadeOut", true, v->isViewRender());
    //public Setting<Double> fadeOutTime = new Setting<Double>("FadeTime", 0.7d, 0.1d, 10.0d, v->isViewRender() && fadeOut.getValue());
    public Setting<Boolean> outline = new Setting<Boolean>("Outline", true, v->isViewRender());
    public Setting<Double> lineWidth = new Setting<Double>("LineWidth", 1.5d, 0.1d, 2.0d, v->isViewRender() && outline.getValue());
    public Setting<Integer> boxAlpha = new Setting<Integer>("BoxAlpha", 45, 1, 255, v->isViewRender());
    public Setting<Integer> colorRed = new Setting<Integer>("Red", 5, 1, 255, v->isViewRender() && !colorSync.getValue());
    public Setting<Integer> colorGreen = new Setting<Integer>("Green", 175, 1, 255, v->isViewRender() && !colorSync.getValue());
    public Setting<Integer> colorBlue = new Setting<Integer>("Blue", 255, 1, 255, v->isViewRender() && !colorSync.getValue());


    public BetaCrystal() {
        super("AutoCrystal", Category.COMBAT,
                "An attempt at making a better ca for both strict and non-strict servers");
    }

    private final TimeUtil breakTimer = new TimeUtil();
    private final TimeUtil placeTimer = new TimeUtil();

    private final TimeUtil checkTimer = new TimeUtil();

    private final ArrayList<AttackedCrystal> attackedCrystals = new ArrayList<>();
    private final ArrayList<RenderPosition> renderPositions = new ArrayList<>();
    private final Set<BlockPos> placedCrystals = new HashSet<>();

    private int breakStepTicks = 0;
    private int placeStepTicks = 0;

    private float yaw;
    private float pitch;
    private boolean isRotatingBreak;
    private boolean isRotatingPlace;
    private boolean isRotatingUpdates;

    private float lastSteppedPlace;
    private float lastSteppedBreak;

    private static BlockPos placeRender = null;
    private static BlockPos oldPlacePos = null;
    private static double damage = 0.0d;
    public boolean faceplaceKeyOn = false;

    private EntityEnderCrystal currentAttacking = null;

    @SubscribeEvent
    public void onUpdate(EventUpdateWalkingPlayer event) {
        doFPKey();
        if(event.getStage() == ForgeEvent.Stage.PRE) {
            breakCrystals();
        } else {
            placeCrystals();
        }
    }

    private void doFPKey() {
        if (facePlaceHold.getValue()) {
            if (facePlaceKey.getValue().getKey() != Keyboard.KEY_NONE) {
                if (Keyboard.isKeyDown(facePlaceKey.getValue().getKey())) {
                    faceplaceKeyOn = true;
                } else {
                    faceplaceKeyOn = false;
                }
            }
        }
    }


    private void breakCrystals() {
        if(!doBreak.getValue()) return;

        if (runLogic.getValue() == BreakLogic.HOLDING) {
            if(!canDoBreak()) {
                return;
            }
        }

        EntityEnderCrystal crystal = (EntityEnderCrystal) mc.world.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityEnderCrystal)
                .filter(entity -> mc.player.getDistance(entity) <= breakRange.getValue())
                .filter(entity -> CombatUtil.crystalIsBreakable(multiPoint.getValue(), (EntityEnderCrystal) entity, breakTrace.getValue(), wallsBreak.getValue(), antiSuiBreak.getValue(), maxSelfBreak.getValue(), passLogic.getValue(), (faceplaceKeyOn ? 1.0d : minHitDamage.getValue()), enemyRange.getValue()))
                .filter(entity -> CombatUtil.wontSelfPop((EntityEnderCrystal) entity, antiSuiBreak.getValue(), maxSelfBreak.getValue()))
                .filter(entity -> CombatUtil.passesStrictBreak((EntityEnderCrystal) entity, true, ticksExisted.getValue()))
                .filter(entity -> canAttackCrystal((EntityEnderCrystal) entity))
                .min(Comparator.comparing(entity -> mc.player.getDistance(entity)))
                .orElse(null);
        if (crystal == null) {
            resetRots();
            resetRotField(false);
            currentAttacking = null;
            return;
        }
        if(crystal != null) {
            currentAttacking = crystal;
        }
        if (!breakTimer.hasPassed((long) (1000L / breakSpeed.getValue()))) {
            return;
        }
        double[] rots = CombatUtil.calculateLookAt(crystal.posX, crystal.posY, crystal.posZ);

        if (breakRotate.getValue()) {
            if(yawStepEnum.getValue() == YawStepEnum.BOTH || yawStepEnum.getValue() == YawStepEnum.BREAK) {
                float relativeYaw = (float)rots[0];
                float yawDiff = (float) MathHelper.wrapDegrees(relativeYaw - mc.player.lastReportedYaw);
                boolean finishedStep = true;
                float currentSteppedYaw = 0f;
                breakStepTicks++;
                if (Math.abs(yawDiff) > yawStep.getValue().floatValue() && !(lastSteppedBreak == relativeYaw || lastSteppedPlace == relativeYaw)) {
                    if(breakStepTicks >= stepTicks.getValue()) {
                        relativeYaw = (float) (mc.player.lastReportedYaw + (yawDiff * ((yawStep.getValue()) / Math.abs(yawDiff))));
                        breakStepTicks = 0;
                    }
                    finishedStep = false;
                }
                if(lastSteppedBreak == relativeYaw || (lastSteppedPlace == relativeYaw)) {
                    relativeYaw = lastSteppedBreak;
                    finishedStep = true;
                    currentSteppedYaw = relativeYaw;
                } else {
                    if(finishedStep) {
                        currentSteppedYaw = relativeYaw;
                    }
                }
                yaw = relativeYaw;
                pitch = (float) rots[1];
                isRotatingUpdates = true;
                isRotatingBreak = true;
                if(!finishedStep) {
                    return;
                }
                currentAttacking = crystal;
                lastSteppedBreak = currentSteppedYaw;
                attackCrystal(crystal);
            } else {
                yaw = (float) rots[0];
                pitch = (float) rots[1];
                isRotatingUpdates = true;
                isRotatingBreak = true;
                currentAttacking = crystal;
                attackCrystal(crystal);
            }
            return;
        }
        isRotatingBreak = false;
        attackCrystal(crystal);
    }

    private void placeCrystals() {
        if (!doPlace.getValue()) {
            this.setModuleInfo("");
            resetRots();
            resetRotField(true);
            return;
        }
        if(!canDoPlace()) {
            return;
        }
        boolean doRecalc = true;
        if (antiRecalc.getValue()) {
            doRecalc = false;
            if (oldPlacePos == null) {
                doRecalc = true;
            } else {
                if (CombatUtil.placePosStillValid(multiPoint.getValue(), oldPlacePos, antiSuiPlace.getValue(), placeMaxSelf.getValue(), minDamage.getValue(), (faceplaceKeyOn ? 36.0d : facePlaceH.getValue()), placeTrace.getValue(), wallsPlace.getValue(), enemyRange.getValue(), oneBlockCA.getValue(), placeRange.getValue())) {
                    doRecalc = false;
                } else {
                    doRecalc = true;
                }
            }
        }
        if(faceplaceKeyOn) {
            doRecalc = true;
        }

        BlockPos placePosition = CombatUtil.getBestPlacePosNew(loginWaitTicks.getValue(), multiPoint.getValue(), antiSuiPlace.getValue(), placeMaxSelf.getValue(), minDamage.getValue(), (faceplaceKeyOn ? 36.0d : facePlaceH.getValue()), placeTrace.getValue(), wallsPlace.getValue(), enemyRange.getValue(), oneBlockCA.getValue(), placeRange.getValue());

        if(!doRecalc) {
            if(doRecalcOverride.getValue() && CombatUtil.getDamageBestPosNew(multiPoint.getValue(), antiSuiPlace.getValue(), placeMaxSelf.getValue(), minDamage.getValue(), (faceplaceKeyOn ? 36.0d : facePlaceH.getValue()), placeTrace.getValue(), wallsPlace.getValue(), enemyRange.getValue(), oneBlockCA.getValue(), placeRange.getValue()) >= recalcDmgOverride.getValue()) {
                doRecalc = true;
            }
        }


        if (oldPlacePos != null && !doRecalc) {
            placePosition = oldPlacePos;
        }


        EnumHand placeHand = null;

        if (CombatUtil.getBestPlacePosIgnoreAlreadyPlacedNew(multiPoint.getValue(), antiSuiPlace.getValue(), placeMaxSelf.getValue(), minDamage.getValue(), (faceplaceKeyOn ? 36.0d : facePlaceH.getValue()), placeTrace.getValue(), wallsPlace.getValue(), enemyRange.getValue(), oneBlockCA.getValue(), placeRange.getValue()) == null) {
            this.setModuleInfo("");
        }

        if (mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) {
            placeHand = EnumHand.MAIN_HAND;
        } else if (mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
            placeHand = EnumHand.OFF_HAND;
        }
        if (placePosition != null) {
            boolean isAttacking = false;
            if(noPlaceAttack.getValue()) {
                if(currentAttacking == null) {
                    EntityEnderCrystal crystal = (EntityEnderCrystal) mc.world.loadedEntityList.stream()
                            .filter(entity -> entity instanceof EntityEnderCrystal)
                            .filter(entity -> mc.player.getDistance(entity) <= breakRange.getValue())
                            .filter(entity -> CombatUtil.crystalIsBreakable(multiPoint.getValue(), (EntityEnderCrystal) entity, breakTrace.getValue(), wallsBreak.getValue(), antiSuiBreak.getValue(), maxSelfBreak.getValue(), passLogic.getValue(), minHitDamage.getValue(), enemyRange.getValue()))
                            .filter(entity -> CombatUtil.wontSelfPop((EntityEnderCrystal) entity, antiSuiBreak.getValue(), maxSelfBreak.getValue()))
                            .filter(entity -> CombatUtil.passesStrictBreak((EntityEnderCrystal) entity, true, ticksExisted.getValue()))
                            .min(Comparator.comparing(entity -> mc.player.getDistance(entity)))
                            .orElse(null);
                    isAttacking = (crystal != null);
                } else {
                    isAttacking = true;
                }
            }
            if (getCrystalsInRange() >= maxInRange.getValue() || (noPlaceAttack.getValue() && isAttacking)) {
                boolean doReset = true;
                if (placeRender != null) {
                    if (CombatUtil.renderPosStillValid(multiPoint.getValue(), placeRender, antiSuiPlace.getValue(), placeMaxSelf.getValue(), minDamage.getValue(), (faceplaceKeyOn ? 36.0d : facePlaceH.getValue()), placeTrace.getValue(), wallsPlace.getValue(), enemyRange.getValue(), oneBlockCA.getValue(), placeRange.getValue())) {
                        doReset = false;
                    }
                }
                if (doReset) {
                    placeRender = null;
                    damage = 0.0d;
                }
                return;
            }
            placeRender = placePosition;
            oldPlacePos = placePosition;
            boolean finalizePlace = true;
            damage = CombatUtil.getDamageBestPosNew(multiPoint.getValue(), antiSuiPlace.getValue(), placeMaxSelf.getValue(), minDamage.getValue(), (faceplaceKeyOn ? 36.0d : facePlaceH.getValue()), placeTrace.getValue(), wallsPlace.getValue(), enemyRange.getValue(), oneBlockCA.getValue(), placeRange.getValue());
            double[] finalRots = CombatUtil.calculateLookAt(placePosition.getX() + 0.5, placePosition.getY() - 0.5, placePosition.getZ() + 0.5);
            if(multiPointRotations.getValue()) {
                finalRots = CombatUtil.calculateLookAtBlock(placePosition);
            }
            if (placeRotate.getValue()) {
                if(yawStepEnum.getValue() == YawStepEnum.PLACE || yawStepEnum.getValue() == YawStepEnum.BOTH) {
                    float relativeYaw = (float)finalRots[0];
                    float yawDiff = (float) MathHelper.wrapDegrees(relativeYaw - mc.player.lastReportedYaw);
                    boolean finishedStep = true;
                    float currentSteppedYaw = 0f;
                    placeStepTicks++;
                    if (Math.abs(yawDiff) > yawStep.getValue().floatValue() && !(lastSteppedPlace == relativeYaw)) {
                        if(placeStepTicks >= stepTicks.getValue()) {
                            relativeYaw = (float) (mc.player.lastReportedYaw + (yawDiff * ((yawStep.getValue()) / Math.abs(yawDiff))));
                            placeStepTicks = 0;
                        }
                        finishedStep = false;
                    }
                    if(lastSteppedPlace == relativeYaw) {
                        relativeYaw = lastSteppedPlace;
                        finishedStep = true;
                        currentSteppedYaw = relativeYaw;
                    } else {
                        if(finishedStep) {
                            currentSteppedYaw = relativeYaw;
                        }
                    }
                    yaw = relativeYaw;
                    pitch = (float) finalRots[1];
                    isRotatingUpdates = true;
                    isRotatingPlace = true;
                    if(!finishedStep) {
                        finalizePlace = false;
                    } else {
                        lastSteppedPlace = currentSteppedYaw;
                    }
                } else {
                    yaw = (float) finalRots[0];
                    pitch = (float) finalRots[1];
                    isRotatingUpdates = true;
                    isRotatingPlace = true;
                }
            }
            if (placeHand == null) {
                this.setModuleInfo("");
                resetRots();
                resetRotField(true);
                return;
            } else {
                if (CombatUtil.getGreatestDamageOnPlayer(enemyRange.getValue(), placePosition) != null) {
                    this.setModuleInfo(CombatUtil.getGreatestDamageOnPlayer(enemyRange.getValue(), placePosition).getName() + (faceplaceKeyOn ? " | " + TextFormatting.GREEN + "FP" : ""));
                }
            }
            if (placeTimer.hasPassed((long) (1000 / placeSpeed.getValue()))) {
                if (finalizePlace) {
                    if (placeHand == null) {
                        this.setModuleInfo("");
                        resetRots();
                        resetRotField(true);
                        return;
                    }
                    placedCrystals.add(placePosition);
                    RayTraceResult res = CombatUtil.getPlaceDirection(placePosition, multiPoint.getValue());
                    CombatUtil.AutoCrystalTraceResult traceResult = new CombatUtil.AutoCrystalTraceResult(res.sideHit, res);
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(placePosition, traceResult.facing, placeHand, (float) traceResult.result.hitVec.x, (float) traceResult.result.hitVec.y, (float) traceResult.result.hitVec.z));
                    if (swingSetting.getValue() == SwingLogic.PLACE || swingSetting.getValue() == SwingLogic.BOTH) {
                        mc.player.swingArm(placeHand);
                    }
                    placeTimer.reset();
                }
            }
        } else {
            boolean doReset = true;
            if (placeRender != null) {
                if (CombatUtil.renderPosStillValid(multiPoint.getValue(), placeRender, antiSuiPlace.getValue(), placeMaxSelf.getValue(), minDamage.getValue(), (faceplaceKeyOn ? 36.0d : facePlaceH.getValue()), placeTrace.getValue(), wallsPlace.getValue(), enemyRange.getValue(), oneBlockCA.getValue(), placeRange.getValue())) {
                    doReset = false;
                }
            }
            if (doReset) {
                placeRender = null;
                damage = 0.0d;
                resetRots();
                resetRotField(true);
            }
        }
    }

    private EnumFacing getStrictDirection(BlockPos pos) {
        double lastDist = 69.0d;
        EnumFacing finalFacing = EnumFacing.UP;
        for(EnumFacing facing : EnumFacing.values()) {
            Vec3d offset = new Vec3d(pos).add(0.5d, 0.5f, 0.5d).add(new Vec3d(facing.getDirectionVec()).scale(0.5d));
            double dist = mc.player.getDistance(offset.x, offset.y, offset.z);
            if(dist <= lastDist) {
                if(facing != EnumFacing.UP && facing != EnumFacing.DOWN) {
                    if(dist > 0.5 && (mc.world.getBlockState(pos.offset(facing.getOpposite())) != null && mc.world.getBlockState(pos.offset(facing)).getBlock() != null && mc.world.getBlockState(pos.offset(facing)).getBlock() != Blocks.AIR)) {
                        System.out.println(mc.world.getBlockState(pos.offset(facing)).getBlock().getLocalizedName());
                        finalFacing = facing;
                    }
                } else {
                    finalFacing = facing;
                }
                lastDist = dist;
            }
        }
        return finalFacing;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (renderESP.getValue()) {
            if(fadeOut.getValue()) {
                renderPositions.removeIf(pos -> pos.alpha <= 0);
                Iterator<RenderPosition> renderPositionIterator = renderPositions.iterator();
                try {
                    while (renderPositionIterator.hasNext()) {
                        RenderPosition next = renderPositionIterator.next();
                        next.update();
                        if (!colorSync.getValue()) {
                            RenderUtil.drawBoxESP(next.pos, new Color(colorRed.getValue(), colorGreen.getValue(), colorBlue.getValue(), next.alpha), lineWidth.getValue().floatValue(), outline.getValue(), true, boxAlpha.getValue());
                        } else {
                            Color syncColor = Colors.INSTANCE.convertHex(Colors.INSTANCE.changeAlpha(Colors.INSTANCE.getColor(1, false), next.alpha));
                            RenderUtil.drawBoxESP(next.pos, syncColor, lineWidth.getValue().floatValue(), outline.getValue(), true, boxAlpha.getValue());
                        }
                    }
                } catch (Exception e) {}
            }
            if (placeRender != null && CombatUtil.isHoldingCrystal()) {
                if(fadeOut.getValue()) {
                    if (!isRenderedPosition(placeRender)) {
                        renderPositions.add(new RenderPosition(placeRender));
                    }
                }
                if (!colorSync.getValue()) {
                    RenderUtil.drawBoxESP(placeRender, new Color(colorRed.getValue(), colorGreen.getValue(), colorBlue.getValue(), 255), lineWidth.getValue().floatValue(), outline.getValue(), true, boxAlpha.getValue());
                } else {
                    Color syncColor = Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false));
                    RenderUtil.drawBoxESP(placeRender, syncColor, lineWidth.getValue().floatValue(), outline.getValue(), true, boxAlpha.getValue());
                }
            }
        }
        if (damageText.getValue()) {
            if (placeRender != null && CombatUtil.isHoldingCrystal()) {
                List<Entity> playerEnts = new ArrayList<Entity>((Collection<? extends Entity>) mc.world.playerEntities.stream().filter(entityPlayer -> !Dactyl.friendManager.isFriend(entityPlayer.getName())).collect(Collectors.toList()));
                double highestDMG = 0f;
                for (Entity entity : playerEnts) {
                    if (mc.player.getDistance(entity) > enemyRange.getValue()) {
                        continue;
                    }
                    if (entity == mc.player) {
                        continue;
                    }
                    if (((EntityLivingBase) entity).getHealth() <= 0.0f || ((EntityLivingBase) entity).isDead) {
                        continue;
                    }
                    float dmgCalculation = 0.0f;
                    try {
                        dmgCalculation = CombatUtil.calculateDamage(placeRender, entity);
                    } catch (NullPointerException exception) {
                    }
                    if (dmgCalculation > highestDMG) {
                        highestDMG = dmgCalculation;
                    }
                }
                String dmgTextRender = ((Math.floor(highestDMG) == highestDMG) ? Integer.valueOf((int) highestDMG) : String.format("%.1f", highestDMG)) + "";
                RenderUtil.drawText(placeRender, dmgTextRender);
            }
        }
    }

    private int getCrystalsInRange() {
        int crystalCount = 0;
        if (damageMap() == null) {
            return 0;
        }
        for (Map.Entry<Entity, Float> entry : this.damageMap().entrySet()) {
            Entity crystal = entry.getKey();
            float damage = ((Float) entry.getValue()).floatValue();
            boolean isFacePlaceCrystal = CombatUtil.isFacePlaceCrystalNew(multiPoint.getValue(), (EntityEnderCrystal) crystal, (faceplaceKeyOn ? 36.0d : facePlaceH.getValue()), placeTrace.getValue(), wallsPlace.getValue(), placeRange.getValue(), enemyRange.getValue());
            if ((damage >= minCountDmg.getValue() || (isFacePlaceCrystal && countFacePlace.getValue())) && ((mc.player.getDistance(crystal) <= placeRange.getValue()))) {
                crystalCount++;
            }
        }
        if (crystalCount == 0 && (maxInRange.getValue() == 1)) {
            if (currentAttacking != null) {
                crystalCount += 1;
            }
        }
        return crystalCount;
    }

    private HashMap<Entity, Float> damageMap() {
        HashMap<Entity, Float> dmgMap = new HashMap<>();
        List<Entity> playerEnts = new ArrayList<Entity>((Collection<? extends Entity>) mc.world.playerEntities.stream().filter(entityPlayer -> !Dactyl.friendManager.isFriend(entityPlayer.getName())).collect(Collectors.toList()));
        double damage = 2.0;
        for (Entity loadedEntity : mc.world.loadedEntityList) {
            if (!(loadedEntity instanceof EntityEnderCrystal)) {
                continue;
            }
            if (mc.player.getDistance(loadedEntity) > breakRange.getValue()) {
                continue;
            }
            if (!CombatUtil.getMapCrystalPlaceNew(multiPoint.getValue(), (EntityEnderCrystal) loadedEntity, placeRange.getValue(), placeTrace.getValue(), wallsPlace.getValue(), antiSuiPlace.getValue(), placeMaxSelf.getValue(), passLogic.getValue(), minHitDamage.getValue(), enemyRange.getValue())) {
                continue;
            }
            float highestDMG = 0f;
            for (Entity entity : playerEnts) {
                if (mc.player.getDistance(entity) > enemyRange.getValue()) {
                    continue;
                }
                if (entity == mc.player) {
                    continue;
                }
                if (((EntityLivingBase) entity).getHealth() <= 0.0f || ((EntityLivingBase) entity).isDead) {
                    continue;
                }
                if (CombatUtil.calculateDamage((EntityEnderCrystal) loadedEntity, entity) > highestDMG) {
                    highestDMG = CombatUtil.calculateDamage((EntityEnderCrystal) loadedEntity, entity);
                }
            }
            if (dmgMap.get(loadedEntity) == null) {
                dmgMap.put(loadedEntity, highestDMG);
            }
        }
        return dmgMap;
    }

    private boolean canDoPlace() {
        if (mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL && mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL) {
            this.setModuleInfo("");
            placeRender = null;
            damage = 0.0d;
            resetRots();
            resetRotField(true);
            checkTimer.reset();
            return false;
        }
        if (!checkTimer.hasPassed(125L)) {
            return false;
        }
        return true;
    }

    private boolean placedOnPos(BlockPos pos) {
        Iterator<BlockPos> placed = placedCrystals.iterator();
        try {
            while (placed.hasNext()) {
                BlockPos next = placed.next();
                if(pos.getX() == next.getX() && pos.getY() == next.getY() && pos.getZ() == next.getZ()) {
                    return true;
                }
            }
        } catch (Exception e) {}
        return false;
    }

    private void attackCrystal(Entity entity) {
        Criticals.INSTANCE.ignoring = true;
        mc.playerController.attackEntity(mc.player, entity);
        if (swingSetting.getValue() == SwingLogic.BREAK || swingSetting.getValue() == SwingLogic.BOTH) {
            mc.player.swingArm(mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
        }
        if(!alreadyAttacked(entity)) {
            attackedCrystals.add(new AttackedCrystal((EntityEnderCrystal)entity, 1));
        } else {
            incrementAttacked((EntityEnderCrystal) entity);
        }
        Criticals.INSTANCE.ignoring = false;
        if (ignoreValidExploit.getValue() && (runLogic.getValue() == BreakLogic.HOLDING) && placeRender != null) {
            EnumHand placeHand = (mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
            RayTraceResult res = CombatUtil.getPlaceDirection(placeRender, multiPoint.getValue());
            CombatUtil.AutoCrystalTraceResult traceResult = new CombatUtil.AutoCrystalTraceResult(res.sideHit, res);
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(placeRender, traceResult.facing, placeHand, (float) traceResult.result.hitVec.x, (float) traceResult.result.hitVec.y, (float) traceResult.result.hitVec.z));
        }
        breakTimer.reset();
    }

    private boolean canDoBreak() {
        if (mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL && mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL) {
            this.setModuleInfo("");
            resetRots();
            resetRotField(false);
            currentAttacking = null;
            checkTimer.reset();
            return false;
        }
        if (!checkTimer.hasPassed(75L)) {
            return false;
        } else {
            return true;
        }
    }



    private void incrementAttacked(EntityEnderCrystal entity) {
        Iterator<AttackedCrystal> crystals = attackedCrystals.iterator();
        try {
            while (crystals.hasNext()) {
                AttackedCrystal next = crystals.next();
                if(next.attacked == entity) {
                    next.amount++;
                }
            }
        } catch (Exception e) {}
    }

    private boolean alreadyAttacked(Entity entity) {
        Iterator<AttackedCrystal> crystals = attackedCrystals.iterator();
        try {
            while (crystals.hasNext()) {
                AttackedCrystal next = crystals.next();
                if(next.attacked == (EntityEnderCrystal)entity) {
                    return true;
                }
            }
        } catch (Exception e) {}
        return false;
    }

    private boolean canAttackCrystal(EntityEnderCrystal crystal) {
        Iterator<AttackedCrystal> crystals = attackedCrystals.iterator();
        try {
            while (crystals.hasNext()) {
                AttackedCrystal next = crystals.next();
                if(next.attacked == crystal && mc.world.getEntityByID(next.attacked.entityId) == null) {
                    crystals.remove();
                    break;
                }
                if(next.timer.hasPassed(1000)) {
                    next.amount = 0;
                    next.timer.reset();
                }
                if(next.attacked == crystal) {
                    if(next.amount > hitsPerSecond.getValue()) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {}
        return true;
    }

    @SubscribeEvent
    public void onKeyPress(EventKeyPress event) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (facePlaceKey.getValue().getKey() != Keyboard.KEY_NONE) {
            if (!facePlaceHold.getValue()) {
                if (event.getKey() == facePlaceKey.getValue().getKey()) {
                    faceplaceKeyOn = !faceplaceKeyOn;
                }
            }
        } else {
            faceplaceKeyOn = false;
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.INCOMING) {
            if (soundRemove.getValue()) {
                if (event.getPacket() instanceof SPacketSoundEffect) {
                    SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();
                    if (packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                        Iterator<Entity> entityLoadedList = mc.world.loadedEntityList.iterator();
                        try {
                            while (entityLoadedList.hasNext()) {
                                Entity e = entityLoadedList.next();
                                if (e == null) continue;
                                if (e instanceof EntityEnderCrystal) {
                                    if (e.getDistance(packet.getX(), packet.getY(), packet.getZ()) <= 6.0f) {
                                        e.setDead();
                                    }
                                }
                            }
                        } catch (Exception e) {}
                    }
                }
            }

            if(preAttack.getValue()) {
                if (event.getPacket() instanceof SPacketSpawnObject) {
                    SPacketSpawnObject packetSpawnObject = (SPacketSpawnObject) event.getPacket();
                    if (doBreak.getValue()) {
                        if (packetSpawnObject.getType() == 51) {
                            BlockPos pos = new BlockPos(packetSpawnObject.getX(), packetSpawnObject.getY(), packetSpawnObject.getZ());
                            BlockPos placedPos = pos.down();
                            if (placedOnPos(pos.down())) {
                                for (EntityPlayer p : mc.world.playerEntities) {
                                    if (p == null || mc.player.equals(p) || p.getDistanceSq(pos) > (((enemyRange.getValue() + placeRange.getValue())) * ((enemyRange.getValue() + placeRange.getValue()))) || !Dactyl.friendManager.isFriend(p.getName())) {
                                        continue;
                                    }
                                    float playerHealth = p.getHealth() + p.getAbsorptionAmount();
                                    if (CombatUtil.calculateDamage(pos, (Entity) p) > playerHealth + 0.5D) {
                                        return;
                                    }
                                }
                                if(preAttackRotate.getValue()) {
                                    double[] rots = CombatUtil.calculateLookAt(pos.getX() + 0.5, pos.getY() - 0.5, pos.getZ() + 0.5);
                                    yaw = (float) rots[0];
                                    pitch = (float) rots[1];
                                    isRotatingUpdates = true;
                                    isRotatingBreak = true;
                                }
                                CPacketUseEntity attackPacket = new CPacketUseEntity();
                                ((ICPacketUseEntity) attackPacket).setEntityId(packetSpawnObject.getEntityID());
                                ((ICPacketUseEntity) attackPacket).setAction(CPacketUseEntity.Action.ATTACK);
                                mc.player.connection.sendPacket(attackPacket);
                                /*if(spawnPacketExploit.getValue()) {
                                    if ((runLogic.getValue() == BreakLogic.HOLDING) && placeRender != null) {
                                        CombatUtil.AutoCrystalTraceResult traceResult = CombatUtil.getNormalTrace(placeRender);
                                        EnumHand placeHand = (mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
                                        if(!(mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) && !(mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL)) {
                                            return;
                                        }
                                        if(placeHand != null && traceResult != null && traceResult.result != null && traceResult.facing != null) {
                                            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(placeRender, traceResult.facing, placeHand, (float) traceResult.result.hitVec.x, (float) traceResult.result.hitVec.y, (float) traceResult.result.hitVec.z));
                                        }
                                    }
                                }*/
                            }
                        }
                    }
                }
            }
        }
    }

    /*@Override
    public void onScreen2D(float partialTicks) {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(isRotatingUpdates) {
            Dactyl.fontUtil.drawStringWithShadow("Yaw: " + String.valueOf(yaw), 1, 20, 0xfffce700);
            Dactyl.fontUtil.drawStringWithShadow("Pitch: " + String.valueOf(pitch), 1, 30, 0xfffce700);
        }
    }*/


    @SubscribeEvent
    public void onUpdateRotate(EventUpdateWalkingPlayer event) {
        if(!placeRotate.getValue() && !breakRotate.getValue()) {
            return;
        }
        if(isRotatingUpdates) {
            event.setYaw(yaw);
            if(rotateHead.getValue()) {
                mc.player.rotationYawHead = yaw;
            }
            event.setPitch(pitch);
            if(debugRotate.getValue()) {
                mc.player.rotationYaw = yaw;
                mc.player.rotationPitch = pitch;
            }
        }
    }

    private void resetRots() {
        if((isRotatingBreak || isRotatingPlace)) {
            return;
        }
        if(isRotatingUpdates) {
            yaw = mc.player.rotationYaw;
            pitch = mc.player.rotationPitch;
            isRotatingUpdates = false;
        }
    }

    private void resetRotField(boolean place) {
        if(place) {
            isRotatingPlace = false;
        } else {
            isRotatingBreak = false;
        }
    }

    @Override
    public void onDisable() {
        isRotatingBreak = false;
        isRotatingPlace = false;
        isRotatingUpdates = false;
        breakStepTicks = placeStepTicks = 0;
        breakTimer.reset();
        checkTimer.reset();
        attackedCrystals.clear();
        placedCrystals.clear();
    }

    public boolean isRenderedPosition(BlockPos pos) {
        RenderPosition renderPosition = renderPositions.stream()
                .filter(renderPos -> renderPos.pos == pos)
                .min(Comparator.comparing(renderPos -> renderPos.timer.getPassedTime()))
                .orElse(null);
        return renderPosition != null;
    }

    public class AttackedCrystal {
        public EntityEnderCrystal attacked;
        public TimeUtil timer;
        public int amount;
        public AttackedCrystal(EntityEnderCrystal attacked, int amount) {
            this.attacked = attacked;
            this.amount = amount;
            this.timer = new TimeUtil();
        }
    }

    public class RenderPosition {
        public BlockPos pos;
        public int alpha = 0xAA;
        public TimeUtil timer;
        public RenderPosition(BlockPos pos) {
            this.pos = pos;
            this.timer = new TimeUtil();
        }

        private void update() {
            if (this.alpha > 0)
                this.alpha -= 2;
        }
    }

    public enum YawStepEnum {
        OFF,
        PLACE,
        BREAK,
        BOTH
    }

    public enum SwingLogic {
        PLACE,
        BREAK,
        BOTH,
        NONE
    }

    public enum PassLogic {
        DOESDMG,
        ALWAYS
    }

    public enum BreakLogic {
        HOLDING,
        ALWAYS
    }

    public enum WallsRange {
        OFF,
        RANGE,
        ANTI
    }


    public enum SettingPage {
        PLACE,
        BREAK,
        GENERAL,
        RENDER
    }

    private boolean isViewPlace() {
        return page.getValue() == SettingPage.PLACE;
    }

    private boolean isViewBreak() {
        return page.getValue() == SettingPage.BREAK;
    }

    private boolean isViewGeneral() {
        return page.getValue() == SettingPage.GENERAL;
    }

    private boolean isViewRender() {
        return page.getValue() == SettingPage.RENDER;
    }
}
