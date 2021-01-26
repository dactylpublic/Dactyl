package me.fluffy.dactyl.module.impl.combat;

import me.fluffy.dactyl.Dactyl;
import me.fluffy.dactyl.event.ForgeEvent;
import me.fluffy.dactyl.event.impl.action.EventKeyPress;
import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.event.impl.player.EventUpdateWalkingPlayer;
import me.fluffy.dactyl.event.impl.world.EntityRemovedEvent;
import me.fluffy.dactyl.event.impl.world.Render3DEvent;
import me.fluffy.dactyl.injection.inj.access.ICPacketPlayer;
import me.fluffy.dactyl.injection.inj.access.ICPacketUseEntity;
import me.fluffy.dactyl.injection.inj.access.IVec3i;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.module.impl.client.Colors;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.Bind;
import me.fluffy.dactyl.util.CombatUtil;
import me.fluffy.dactyl.util.RotationUtil;
import me.fluffy.dactyl.util.TimeUtil;
import me.fluffy.dactyl.util.render.RenderUtil;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AutoCrystal extends Module {
    Setting<SettingPage> settingPage = new Setting<SettingPage>("Setting", SettingPage.PLACE);
    // place
    Setting<Boolean> doCaPlace = new Setting<Boolean>("Place", true, vis->settingPage.getValue() == SettingPage.PLACE);
    Setting<Boolean> tracePlace = new Setting<Boolean>("PlaceTrace", false, vis->settingPage.getValue() == SettingPage.PLACE&&doCaPlace.getValue());
    Setting<Integer> placeDelay = new Setting<Integer>("PlaceDelay", 45, 1, 250, vis->settingPage.getValue() == SettingPage.PLACE && doCaPlace.getValue());
    Setting<Boolean> antiSuiPlace = new Setting<Boolean>("AntiSelfPop", true, vis->settingPage.getValue() == SettingPage.PLACE&&doCaPlace.getValue());
    Setting<Double> placeMaxSelf = new Setting<Double>("MaxSelfPlace", 10.0D, 1.0D, 13.5D, vis->settingPage.getValue() == SettingPage.PLACE&&doCaPlace.getValue()&&antiSuiPlace.getValue());
    Setting<Boolean> placeRotate = new Setting<Boolean>("PlaceRotate", true, vis->settingPage.getValue() == SettingPage.PLACE && doCaPlace.getValue());
    Setting<Boolean> antiMultiLethal = new Setting<Boolean>("LethalNoMulti", true, vis->settingPage.getValue() == SettingPage.PLACE && doCaPlace.getValue());
    Setting<Double> lethalMin = new Setting<Double>("LethalMin", 10.0D, 1.0D, 12.0D, vis->settingPage.getValue() == SettingPage.PLACE&&doCaPlace.getValue()&&antiMultiLethal.getValue());
    Setting<Boolean> antiping = new Setting<Boolean>("AntiMulti", true, vis->settingPage.getValue() == SettingPage.PLACE && doCaPlace.getValue());
    Setting<Double> minPlaceDMG = new Setting<Double>("MinDamage", 6.0D, 1.0D, 12.0D, vis->settingPage.getValue() == SettingPage.PLACE&&doCaPlace.getValue());
    Setting<Double> facePlaceStart = new Setting<Double>("FacePlaceH", 8.0D, 1.0D, 36.0D, vis->settingPage.getValue() == SettingPage.PLACE&&doCaPlace.getValue());
    Setting<Boolean> doublePlace = new Setting<Boolean>("DoublePlace", false, vis->settingPage.getValue() == SettingPage.PLACE && doCaPlace.getValue(), "Weird exploit");
    Setting<Boolean> oneBlockCA = new Setting<Boolean>("1.13+", false, vis->settingPage.getValue() == SettingPage.PLACE && doCaPlace.getValue());
    public Setting<Double> placeRange = new Setting<Double>("PlaceRange", 5.5D, 1.0D, 6.0D, vis->settingPage.getValue() == SettingPage.PLACE && doCaPlace.getValue());
    Setting<Double> wallsPlace = new Setting<Double>("WallsPlace", 4.5D, 1.0D, 6.0D, vis->settingPage.getValue() == SettingPage.PLACE && doCaPlace.getValue() && tracePlace.getValue());
    Setting<Boolean> antiRecalc = new Setting<Boolean>("AntiRecalc", true, vis->settingPage.getValue() == SettingPage.PLACE && doCaPlace.getValue());
    Setting<Boolean> countFacePlace = new Setting<Boolean>("CountFace", true, vis->settingPage.getValue() == SettingPage.PLACE && doCaPlace.getValue());
    Setting<Integer> maxInRange = new Setting<Integer>("MaxPlaced", 1, 1, 5, vis->settingPage.getValue() == SettingPage.PLACE && doCaPlace.getValue());
    Setting<Bind> facePlaceKey = new Setting<Bind>("FacePlaceKey", new Bind(Keyboard.KEY_NONE), vis->settingPage.getValue() == SettingPage.PLACE && doCaPlace.getValue());

    // break
    Setting<Boolean> doCaBreak = new Setting<Boolean>("Break", true, vis->settingPage.getValue() == SettingPage.BREAK);
    Setting<Boolean> offhandSwing = new Setting<Boolean>("OffhandSwing", false, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue());
    Setting<Boolean> traceBreak = new Setting<Boolean>("BreakTrace", false, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue());
    Setting<AttackLogic> attackLogic = new Setting<AttackLogic>("AttackLogic", AttackLogic.CSLOT, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue());
    Setting<Integer> breakDelay = new Setting<Integer>("BreakDelay", 65, 1, 350, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue());
    Setting<Boolean> antiStuck = new Setting<Boolean>("AntiStuck", true, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue());
    Setting<Integer> hitAttempts = new Setting<Integer>("HitAttempts", 5, 1, 15, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue()&&antiStuck.getValue());
    Setting<BreakLogic> breakLogic = new Setting<BreakLogic>("BreakLogic", BreakLogic.DOESDMG, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue());
    Setting<Double> doesDamageMin = new Setting<Double>("DoesDMGMin", 2.3D, 0.1D, 13.5D, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue() && (breakLogic.getValue() == BreakLogic.DOESDMG || breakLogic.getValue() == BreakLogic.BOTH));
    Setting<Boolean> antiSuicide = new Setting<Boolean>("AntiSuicide", true, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue());
    Setting<Double> maxSelfDMG = new Setting<Double>("MaxSelfDMG", 8.0D, 1.0D, 13.5D, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue() && antiSuicide.getValue());
    Setting<Boolean> breakRotate = new Setting<Boolean>("BreakRotate", true, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue());
    Setting<Double> breakRange = new Setting<Double>("BreakRange", 5.5D, 1.0D, 6.0D, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue());
    Setting<Double> wallsBreak = new Setting<Double>("WallsBreak", 4.5D, 1.0D, 6.0D, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue() && traceBreak.getValue());
    Setting<Boolean> predict = new Setting<Boolean>("Predict", true, vis->settingPage.getValue() == SettingPage.BREAK && doCaBreak.getValue());

    // misc
    Setting<AuraLogic> auraOrder = new Setting<AuraLogic>("Order", AuraLogic.BREAKPLACE, vis->settingPage.getValue() == SettingPage.MISC);
    Setting<UpdateLogic> updateLogic = new Setting<UpdateLogic>("RotateLogic", UpdateLogic.PACKET, vis->settingPage.getValue() == SettingPage.MISC);
    Setting<Boolean> yawStep = new Setting<Boolean>("YawStep", false, vis->settingPage.getValue() == SettingPage.MISC);
    Setting<Integer> yawStepTicks = new Setting<Integer>("StepAmt", 10, 1, 300, vis->yawStep.getValue()&&settingPage.getValue() == SettingPage.MISC);
    //Setting<Boolean> extraRotPackets = new Setting<Boolean>("ExtraPackets", false, vis->settingPage.getValue() == SettingPage.MISC);
    Setting<Boolean> constRotate = new Setting<Boolean>("ConstRotate", false, vis->settingPage.getValue() == SettingPage.MISC);
    Setting<Double> enemyRange = new Setting<Double>("EnemyRange", 10.0D, 1.0D, 13.0D, vis->settingPage.getValue() == SettingPage.MISC);
    Setting<Boolean> rotateHead = new Setting<Boolean>("RotateHead", true, vis->settingPage.getValue() == SettingPage.MISC);
    Setting<SwingLogic> swingSetting = new Setting<SwingLogic>("Swing", SwingLogic.BREAK, vis->settingPage.getValue() == SettingPage.MISC);
    Setting<Boolean> fasterResetRot = new Setting<Boolean>("FastRReset", false, v->settingPage.getValue() == SettingPage.MISC);
    Setting<Boolean> cancelSwap = new Setting<Boolean>("CancelOnSwap", false, vis->settingPage.getValue() == SettingPage.MISC);
    Setting<Boolean> africanMode = new Setting<Boolean>("AfricanMode", false, vis->settingPage.getValue() == SettingPage.MISC);

    // render
    Setting<Boolean> renderESP = new Setting<Boolean>("Render", true, vis->settingPage.getValue() == SettingPage.RENDER);
    Setting<Boolean> damageText = new Setting<Boolean>("Damage", true, vis->settingPage.getValue() == SettingPage.RENDER);
    Setting<Boolean> colorSync = new Setting<Boolean>("ColorSync", false, vis->settingPage.getValue() == SettingPage.RENDER);
    Setting<Boolean> outline = new Setting<Boolean>("Outline", true, vis->settingPage.getValue() == SettingPage.RENDER);
    Setting<Boolean> slowerClear = new Setting<Boolean>("SlowerReset", true, vis->settingPage.getValue() == SettingPage.RENDER);
    Setting<Double> lineWidth = new Setting<Double>("LineWidth", 1.5d, 0.1d, 2.0d, vis->settingPage.getValue() == SettingPage.RENDER&&outline.getValue());
    Setting<Integer> boxAlpha = new Setting<Integer>("BoxAlpha", 45, 1, 255, vis->settingPage.getValue() == SettingPage.RENDER);
    Setting<Integer> colorRed = new Setting<Integer>("Red", 5, 1, 255, vis->settingPage.getValue() == SettingPage.RENDER&&!colorSync.getValue());
    Setting<Integer> colorGreen = new Setting<Integer>("Green", 175, 1, 255, vis->settingPage.getValue() == SettingPage.RENDER&&!colorSync.getValue());
    Setting<Integer> colorBlue = new Setting<Integer>("Blue", 255, 1, 255, vis->settingPage.getValue() == SettingPage.RENDER&&!colorSync.getValue());


    public static AutoCrystal INSTANCE;


    private final ConcurrentHashMap<EntityEnderCrystal, Integer> attackedCrystals = new ConcurrentHashMap<>();

    private final TimeUtil placeTimer = new TimeUtil();
    private final TimeUtil breakTimer = new TimeUtil();
    private final TimeUtil checkTimer = new TimeUtil();
    private final TimeUtil antiStuckTimer = new TimeUtil();
    private final TimeUtil placeResetTimer = new TimeUtil();
    private final TimeUtil modInfoTimer = new TimeUtil();
    private final TimeUtil novolaTimer = new TimeUtil();


    private static float yaw;
    private static float pitch;
    private static boolean isRotating;
    private float oldYaw, oldPitch;
    private static boolean togglePitch = false;
    private static BlockPos crystalRender = null;
    private static BlockPos oldPlacePos = null;
    private static double damage = 0.0d;
    private static int yawTicks = 0;
    private EntityEnderCrystal currentAttacking = null;
    public boolean faceplaceKeyOn = false;

    private final Set<BlockPos> placedCrystals = new HashSet<>();

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
        if(event.getType() == PacketEvent.PacketType.INCOMING) {
            if(antiStuck.getValue()) {
                if (event.getPacket() instanceof SPacketSoundEffect) {
                    SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();
                    if (packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                        for (Entity e : mc.world.loadedEntityList) {
                            if(e == null) continue;
                            if (e instanceof EntityEnderCrystal) {
                                if (e.getDistance(packet.getX(), packet.getY(), packet.getZ()) <= 6.0f) {
                                    e.setDead();
                                    try {
                                        Iterator<EntityEnderCrystal> crystalIterator = attackedCrystals.keySet().iterator();
                                        while (crystalIterator.hasNext()) {
                                            crystalIterator.next();
                                            if (attackedCrystals.containsKey(e)) {
                                                crystalIterator.remove();
                                                //attackedCrystals.remove(e);
                                            }
                                        }
                                    } catch(Exception exceeept) {}
                                }
                            }
                        }
                    }
                }
            }
            if(event.getPacket() instanceof SPacketSpawnObject) {
                SPacketSpawnObject packetSpawnObject = (SPacketSpawnObject)event.getPacket();
                if(doCaBreak.getValue() && predict.getValue()) {
                    if(packetSpawnObject.getType() == 51) {
                        BlockPos pos = new BlockPos(packetSpawnObject.getX(), packetSpawnObject.getY(), packetSpawnObject.getZ());
                        if (placedCrystals.contains(pos.down())) {
                            for (EntityPlayer p : mc.world.playerEntities) {
                                if (p == null || mc.player.equals(p) || p.getDistanceSq(pos) > (((enemyRange.getValue() + placeRange.getValue()))*((enemyRange.getValue() + placeRange.getValue()))) || !Dactyl.friendManager.isFriend(p.getName())) {
                                    continue;
                                }
                                float playerHealth = p.getHealth() + p.getAbsorptionAmount();
                                if (CombatUtil.calculateDamage(pos, (Entity)p) > playerHealth + 0.5D) {
                                    return;
                                }
                            }
                            CPacketUseEntity attackPacket = new CPacketUseEntity();
                            ((ICPacketUseEntity)attackPacket).setEntityId(packetSpawnObject.getEntityID());
                            ((ICPacketUseEntity)attackPacket).setAction(CPacketUseEntity.Action.ATTACK);
                            mc.player.connection.sendPacket(attackPacket);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onKeyPress(EventKeyPress event) {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(facePlaceKey.getValue().getKey() != Keyboard.KEY_NONE) {
            if(event.getKey() == facePlaceKey.getValue().getKey()) {
                faceplaceKeyOn = !faceplaceKeyOn;
            }
        }
    }


    @SubscribeEvent
    public void onRemoveEntity(EntityRemovedEvent event) {
        if(event.getEntity() == null) {
            return;
        }
        if(event.getEntity() instanceof EntityEnderCrystal) {
            /*if(attackedCrystals.containsKey(event.getEntity())) {
                attackedCrystals.remove(event.getEntity());
            }*/
            try {
                Iterator<EntityEnderCrystal> crystalIterator = attackedCrystals.keySet().iterator();
                while (crystalIterator.hasNext()) {
                    crystalIterator.next();
                    if (attackedCrystals.containsKey(event.getEntity())) {
                        crystalIterator.remove();
                        //attackedCrystals.remove(e);
                    }
                }
            } catch(Exception fuckyou){}
        }
    }


    public void doAutoCrystal(EventUpdateWalkingPlayer eventUpdateWalkingPlayer) {
        if(antiStuckTimer.hasPassed(1000)) {
            attackedCrystals.clear();
        }
        if(placeResetTimer.hasPassed(5000)) {
            placedCrystals.clear();
        }
        if(auraOrder.getValue() == AuraLogic.BREAKPLACE) {
            doBreak(eventUpdateWalkingPlayer);
            doPlace(eventUpdateWalkingPlayer);
        } else {
            doPlace(eventUpdateWalkingPlayer);
            doBreak(eventUpdateWalkingPlayer);
        }
    }

    private void doBreak(EventUpdateWalkingPlayer eventUpdateWalkingPlayer) {
        if(!doCaBreak.getValue()) {
            return;
        }
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
                .filter(entity -> CombatUtil.isBreakableCrystal((EntityEnderCrystal) entity, traceBreak.getValue(), wallsBreak.getValue(), antiSuicide.getValue(), maxSelfDMG.getValue(), breakLogic.getValue(), doesDamageMin.getValue(), enemyRange.getValue()))
                .filter(entity -> CombatUtil.wontSelfPop((EntityEnderCrystal) entity, antiSuicide.getValue(), maxSelfDMG.getValue()))
                .filter(this::passesAntiStuck)
                .min(Comparator.comparing(entity -> mc.player.getDistance(entity)))
                .orElse(null);
        if (crystal == null || cancelSwap.getValue() && !Offhand.INSTANCE.lastSwitch.hasPassed(65)) {
            resetRots();
            currentAttacking = null;
            return;
        }
        if(!breakTimer.hasPassed(breakDelay.getValue())) {
            currentAttacking = null;
            return;
        }
        if(updateLogic.getValue() == UpdateLogic.WALKING) {
            if(eventUpdateWalkingPlayer != null && eventUpdateWalkingPlayer.getStage() == ForgeEvent.Stage.PRE) {
                if (breakRotate.getValue()) {
                    double[] rots = CombatUtil.calculateLookAt(crystal.posX, crystal.posY, crystal.posZ);
                    eventUpdateWalkingPlayer.setYaw((float) rots[0]);
                    eventUpdateWalkingPlayer.setPitch((float) rots[1]);
                    eventUpdateWalkingPlayer.rotationUsed = true;
                }
            }
            if(eventUpdateWalkingPlayer != null && eventUpdateWalkingPlayer.getStage() == ForgeEvent.Stage.POST) {
                currentAttacking = crystal;
                attackCrystal(crystal);
            }
        } else if(updateLogic.getValue() == UpdateLogic.PACKET){
            if(breakRotate.getValue()) {
                //if(extraRotPackets.getValue()) {
                //    mc.player.connection.sendPacket(new CPacketPlayer(mc.player.onGround));
                //}
                //float[] rots = CombatUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), crystal.getPositionVector());
                double[] rots = CombatUtil.calculateLookAt(crystal.posX, crystal.posY, crystal.posZ);
                setRotations(rots[0], rots[1]);
            }
            currentAttacking = crystal;
            attackCrystal(crystal);
        } else if(updateLogic.getValue() == UpdateLogic.FIELD){
            if(breakRotate.getValue()) {
                double[] rots = CombatUtil.calculateLookAt(crystal.posX, crystal.posY, crystal.posZ);
                RotationUtil.setPlayerRotations((float)rots[0], (float)rots[1]);
            }
            currentAttacking = crystal;
            attackCrystal(crystal);
        }

    }

    private boolean passesAntiStuck(Entity entity) {
        return !(attackedCrystals.containsKey(entity) && attackedCrystals.get(entity) > (hitAttempts.getValue()) && antiStuck.getValue());
    }

    private void doPlace(EventUpdateWalkingPlayer eventUpdateWalkingPlayer) {
        if(!doCaPlace.getValue()) {
            this.setModuleInfo("");
            resetRots();
            return;
        }

        if(mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL && mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL) {
            this.setModuleInfo("");
            crystalRender = null;
            damage = 0.0d;
            resetRots();
            checkTimer.reset();
            return;
        }
        if(!checkTimer.hasPassed(125L)) {
            return;
        }
        boolean doRecalc = true;
        if(antiRecalc.getValue()) {
            doRecalc = false;
            if(oldPlacePos == null) {
                doRecalc = true;
            } else {
                if(CombatUtil.placePosStillSatisfies(oldPlacePos, antiSuiPlace.getValue(), placeMaxSelf.getValue(), minPlaceDMG.getValue(), (faceplaceKeyOn ? 36.0d : facePlaceStart.getValue()), tracePlace.getValue(), wallsPlace.getValue(), enemyRange.getValue(), oneBlockCA.getValue(), placeRange.getValue())) {
                    doRecalc = false;
                } else {
                    doRecalc = true;
                }
            }
        }
        BlockPos placePosition = CombatUtil.getBestPlacePosition(antiSuiPlace.getValue(), placeMaxSelf.getValue(), minPlaceDMG.getValue(), (faceplaceKeyOn ? 36.0d : facePlaceStart.getValue()), tracePlace.getValue(), wallsPlace.getValue(), enemyRange.getValue(), oneBlockCA.getValue(), placeRange.getValue());

        if(oldPlacePos != null && !doRecalc) {
            placePosition = oldPlacePos;
        }


        EnumHand placeHand = null;

        if(CombatUtil.getBestPlacePosIgnoreAlreadyPlaced(antiSuicide.getValue(), placeMaxSelf.getValue(), minPlaceDMG.getValue(), (faceplaceKeyOn ? 36.0d : facePlaceStart.getValue()), tracePlace.getValue(), wallsPlace.getValue(), enemyRange.getValue(), oneBlockCA.getValue(), placeRange.getValue()) == null) {
            this.setModuleInfo("");
        }

        if(mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) {
            placeHand = EnumHand.MAIN_HAND;
        } else if(mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
            placeHand = EnumHand.OFF_HAND;
        }
        if(placePosition != null) {
            if(getCrystalsInRange() >= maxInRange.getValue()) {
                boolean doReset = true;
                if(slowerClear.getValue()) {
                    if(crystalRender != null) {
                        if(CombatUtil.renderPosStillSatisfies(crystalRender, antiSuiPlace.getValue(), placeMaxSelf.getValue(), minPlaceDMG.getValue(), (faceplaceKeyOn ? 36.0d : facePlaceStart.getValue()), tracePlace.getValue(), wallsPlace.getValue(), enemyRange.getValue(), oneBlockCA.getValue(), placeRange.getValue())) {
                            doReset = false;
                        }
                    }
                }
                if(doReset) {
                    crystalRender = null;
                    damage = 0.0d;
                }
                //resetRots();
                return;
            }
            crystalRender = placePosition;
            oldPlacePos = placePosition;
            damage = CombatUtil.getDamageBestPos(antiSuicide.getValue(), placeMaxSelf.getValue(), minPlaceDMG.getValue(), (faceplaceKeyOn ? 36.0d : facePlaceStart.getValue()), tracePlace.getValue(), wallsPlace.getValue(), enemyRange.getValue(), oneBlockCA.getValue(), placeRange.getValue());
            double[] rots = CombatUtil.calculateLookAt(placePosition.getX()+ 0.5, placePosition.getY() - 0.5, placePosition.getZ() + 0.5);
            double[] constRots = CombatUtil.calculateLookAt(placePosition.getX()+ 0.5, placePosition.getY() + 0.5, placePosition.getZ() + 0.5);
            if(placeRotate.getValue()) {
                if(updateLogic.getValue() == UpdateLogic.WALKING && eventUpdateWalkingPlayer != null && eventUpdateWalkingPlayer.getStage() == ForgeEvent.Stage.PRE) {
                    if(constRotate.getValue()) {
                        eventUpdateWalkingPlayer.setYaw((float) constRots[0]);
                        eventUpdateWalkingPlayer.setPitch((float) constRots[1]);
                        eventUpdateWalkingPlayer.rotationUsed = true;
                    } else {
                        eventUpdateWalkingPlayer.setYaw((float) rots[0]);
                        eventUpdateWalkingPlayer.setPitch((float) rots[1]);
                        eventUpdateWalkingPlayer.rotationUsed = true;
                    }
                } else if(updateLogic.getValue() == UpdateLogic.PACKET){
                    if(constRotate.getValue()) {
                        setRotations(constRots[0], constRots[1]);
                    } else {
                        setRotations(rots[0], rots[1]);
                    }
                } else if(updateLogic.getValue() == UpdateLogic.FIELD) {
                    if(constRotate.getValue()) {
                        RotationUtil.setPlayerRotations((float)constRots[0], (float)constRots[1]);
                    } else {
                        RotationUtil.setPlayerRotations((float)rots[0], (float)rots[1]);
                    }
                }
            }
            RayTraceResult rayTraceResult = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX+0.5, mc.player.posY + 1.0, mc.player.posZ+0.5), new Vec3d(placePosition.getX()+ 0.5, placePosition.getY() - 0.5, placePosition.getZ() + 0.5));
            EnumFacing facing = null;
            // placing that works on 2b :^)
            if (rayTraceResult == null || rayTraceResult.sideHit == null) {
                rayTraceResult = new RayTraceResult(new Vec3d(0.5, 1.0, 0.5), EnumFacing.UP);
                if(rayTraceResult != null) {
                    if(rayTraceResult.sideHit != null) {
                        facing = rayTraceResult.sideHit;
                    }
                }
            } else {
                facing = rayTraceResult.sideHit;
            }
            if(placeHand == null || cancelSwap.getValue() && !Offhand.INSTANCE.lastSwitch.hasPassed(65)) {
                this.setModuleInfo("");
                resetRots();
                return;
            } else {
                if(CombatUtil.getGreatestDamageOnPlayer(enemyRange.getValue(), placePosition) != null) {
                    this.setModuleInfo(CombatUtil.getGreatestDamageOnPlayer(enemyRange.getValue(), placePosition).getName() + (faceplaceKeyOn ? " | " + TextFormatting.GREEN + "FP" : ""));
                }
            }
            if(placeTimer.hasPassed(placeDelay.getValue())) {
                boolean finalizePlace = true;
                if(updateLogic.getValue() == UpdateLogic.WALKING && eventUpdateWalkingPlayer.getStage() != ForgeEvent.Stage.POST) {
                    finalizePlace = false;
                }
                if(finalizePlace) {
                    if (placeHand == null || cancelSwap.getValue() && !Offhand.INSTANCE.lastSwitch.hasPassed(65)) {
                        this.setModuleInfo("");
                        resetRots();
                        return;
                    }
                    //if(extraRotPackets.getValue()) {
                    //    mc.player.connection.sendPacket(new CPacketPlayer(mc.player.onGround));
                    //}
                    if (africanMode.getValue()) {
                        for(int i = 0; i < 10; i++) {
                            mc.player.connection.sendPacket(new CPacketPlayer(false));
                        }
                    }
                    placedCrystals.add(placePosition);
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(placePosition, facing, placeHand, (float) rayTraceResult.hitVec.x, (float) rayTraceResult.hitVec.y, (float) rayTraceResult.hitVec.z));
                    if(swingSetting.getValue() == SwingLogic.PLACE || swingSetting.getValue() == SwingLogic.BOTH) {
                        mc.player.swingArm(placeHand);
                    }
                    placeTimer.reset();
                }
            }
            if (isRotating) {
                if (togglePitch) {
                    mc.player.rotationPitch += (float) 4.0E-4;
                    togglePitch = false;
                } else {
                    mc.player.rotationPitch -= (float) 4.0E-4;
                    togglePitch = true;
                }
            }
        } else {
            //this.setModuleInfo("");
            boolean doReset = true;
            if(slowerClear.getValue()) {
                if(crystalRender != null) {
                    if(CombatUtil.renderPosStillSatisfies(crystalRender, antiSuiPlace.getValue(), placeMaxSelf.getValue(), minPlaceDMG.getValue(), (faceplaceKeyOn ? 36.0d : facePlaceStart.getValue()), tracePlace.getValue(), wallsPlace.getValue(), enemyRange.getValue(), oneBlockCA.getValue(), placeRange.getValue())) {
                        doReset = false;
                    }
                }
            }
            if(doReset) {
                crystalRender = null;
                damage = 0.0d;
                if(!fasterResetRot.getValue()) {
                    resetRots();
                }
            }
            if(fasterResetRot.getValue()) {
                resetRots();
            }
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if(renderESP.getValue()) {
            if (crystalRender != null && CombatUtil.isHoldingCrystal()) {
                if(!colorSync.getValue()) {
                    RenderUtil.drawBoxESP(crystalRender, new Color(colorRed.getValue(), colorGreen.getValue(), colorBlue.getValue(), 255), lineWidth.getValue().floatValue(), outline.getValue(), true, boxAlpha.getValue());
                } else {
                    Color syncColor = Colors.INSTANCE.convertHex(Colors.INSTANCE.getColor(1, false));
                    RenderUtil.drawBoxESP(crystalRender, syncColor, lineWidth.getValue().floatValue(), outline.getValue(), true, boxAlpha.getValue());
                }
            }
        }
        if(damageText.getValue()) {
            if (crystalRender != null && CombatUtil.isHoldingCrystal()) {
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
                        dmgCalculation = CombatUtil.calculateDamage(crystalRender, entity);
                    } catch(NullPointerException exception) {}
                    if(dmgCalculation > highestDMG) {
                        highestDMG = dmgCalculation;
                    }
                }
                //String dmgTextRender = ((Math.floor(damage) == damage) ? Integer.valueOf((int)damage) : String.format("%.1f", damage)) + "";
                String dmgTextRender = ((Math.floor(highestDMG) == highestDMG) ? Integer.valueOf((int)highestDMG) : String.format("%.1f", highestDMG)) + "";
                RenderUtil.drawText(crystalRender, dmgTextRender);
            }
        }
    }

    private void attackCrystal(Entity entity) {
        Criticals.INSTANCE.ignoring = true;
        mc.playerController.attackEntity(mc.player, entity);
        if(swingSetting.getValue() == SwingLogic.BREAK || swingSetting.getValue() == SwingLogic.BOTH) {
            mc.player.swingArm(offhandSwing.getValue() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
        }
        if (attackedCrystals.containsKey(entity)) {
            attackedCrystals.put((EntityEnderCrystal) entity, attackedCrystals.get(entity) + 1);
        } else {
            attackedCrystals.put((EntityEnderCrystal) entity, 1);
        }
        BlockPos brokenPos = new BlockPos(entity.posX, entity.posY, entity.posZ);
        if(CombatUtil.getGreatestDamageOnPlayer(enemyRange.getValue(), brokenPos) != null) {
            this.setModuleInfo(CombatUtil.getGreatestDamageOnPlayer(enemyRange.getValue(), brokenPos).getName()  + (faceplaceKeyOn ? " | " + TextFormatting.GREEN + "FP" : ""));
        }
        Criticals.INSTANCE.ignoring = false;
        if(doublePlace.getValue() && (attackLogic.getValue() == AttackLogic.CSLOT) && crystalRender != null) {
            RayTraceResult rayTraceResult = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX+0.5, mc.player.posY + 1.0, mc.player.posZ+0.5), new Vec3d(crystalRender));
            EnumFacing facing = null;
            // placing that works on 2b :^)
            if (rayTraceResult == null || rayTraceResult.sideHit == null) {
                rayTraceResult = new RayTraceResult(new Vec3d(0.5, 1.0, 0.5), EnumFacing.UP);
                if(rayTraceResult != null) {
                    if(rayTraceResult.sideHit != null) {
                        facing = rayTraceResult.sideHit;
                    }
                }
            } else {
                facing = rayTraceResult.sideHit;
            }
            EnumHand placeHand = (mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(crystalRender, facing, placeHand, (float) rayTraceResult.hitVec.x,  (float) rayTraceResult.hitVec.y,  (float) rayTraceResult.hitVec.z));
        }
        breakTimer.reset();
    }

    private void setRotations(double newYaw, double newPitch) {
        if(yawStep.getValue()) {
            if(newYaw >= 0) {
                yawTicks += yawStepTicks.getValue();
                if (yawTicks >= newYaw) {
                    yaw = (float) newYaw;
                } else {
                    yaw = (float) yawTicks;
                }
            } else {
                // negative
                yawTicks -= yawStepTicks.getValue();
                if (yawTicks <= newYaw) {
                    yaw = (float) newYaw;
                } else {
                    yaw = (float) yawTicks;
                }
            }
        } else {
            yaw = (float) newYaw;
        }
        pitch = (float) newPitch;
        isRotating = true;
    }

    private static void resetRots() {
        if (isRotating) {
            yaw = mc.player.rotationYaw;
            pitch = mc.player.rotationPitch;
            isRotating = false;
            yawTicks = 0;
        }
    }



    private int getCrystalsInRange() {
        int crystalCount = 0;
        if(damageMap() == null) {
            return 0;
        }
        for (Map.Entry<Entity, Float> entry : this.damageMap().entrySet()) {
            Entity crystal = entry.getKey();
            float damage = ((Float)entry.getValue()).floatValue();
            boolean isFacePlaceCrystal = CombatUtil.isFacePlaceCrystal((EntityEnderCrystal) crystal, (faceplaceKeyOn ? 36.0d : facePlaceStart.getValue()), tracePlace.getValue(), wallsPlace.getValue(), placeRange.getValue(), enemyRange.getValue());
            if((damage >= minPlaceDMG.getValue() || (isFacePlaceCrystal && countFacePlace.getValue())) && ((mc.player.getDistance(crystal) <= placeRange.getValue()))) {
                if(antiMultiLethal.getValue() && damage >= lethalMin.getValue()) {
                    crystalCount = maxInRange.getValue();
                    return crystalCount;
                }
                crystalCount++;
            }
        }
        if(crystalCount == 0 && (maxInRange.getValue() == 1)) {
            if(currentAttacking != null) {
                crystalCount+=1;
            }
        }
        if(!novolaTimer.hasPassed(50) && antiping.getValue()) {
            if(crystalCount == 0) {
                crystalCount = 1;
            }
        }
        return crystalCount;
    }

    private HashMap<Entity, Float> damageMap() {
        HashMap<Entity, Float> dmgMap = new HashMap<>();
        List<Entity> playerEnts = new ArrayList<Entity>((Collection<? extends Entity>) mc.world.playerEntities.stream().filter(entityPlayer -> !Dactyl.friendManager.isFriend(entityPlayer.getName())).collect(Collectors.toList()));
        double damage = 2.0;
        for(Entity loadedEntity : mc.world.loadedEntityList) {
            if(!(loadedEntity instanceof EntityEnderCrystal)) {
                continue;
            }
            if(mc.player.getDistance(loadedEntity) > breakRange.getValue()) {
                continue;
            }
            if(!CombatUtil.getMapCrystalPlace((EntityEnderCrystal) loadedEntity, placeRange.getValue(), tracePlace.getValue(), wallsPlace.getValue(), antiSuicide.getValue(), maxSelfDMG.getValue(), breakLogic.getValue(), doesDamageMin.getValue(), enemyRange.getValue())) {
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
                if(CombatUtil.calculateDamage((EntityEnderCrystal)loadedEntity, entity) > highestDMG) {
                    highestDMG = CombatUtil.calculateDamage((EntityEnderCrystal)loadedEntity, entity);
                }
            }
            if(dmgMap.get(loadedEntity) == null) {
                dmgMap.put(loadedEntity, highestDMG);
            }
        }
        return dmgMap;
    }

    @SubscribeEvent
    public void onUpdateWalking(EventUpdateWalkingPlayer event) {
        if(updateLogic.getValue() == UpdateLogic.WALKING) {
            doAutoCrystal(event);
        } else if(updateLogic.getValue() == UpdateLogic.FIELD) {
            if(event.getStage() == ForgeEvent.Stage.PRE) {
                RotationUtil.updateRotations();
                doAutoCrystal(event);
            } else if(event.getStage() == ForgeEvent.Stage.POST) {
                RotationUtil.restoreRotations();
            }
        }
    }

    @Override
    public void onClientUpdate() {
        if(mc.player == null || mc.world == null) {
            return;
        }
        if(modInfoTimer.hasPassed(100)) {
            if(StringUtils.stripControlCodes(this.getModuleInfo()).contains(" | FP") && !faceplaceKeyOn) {
                this.setModuleInfo(StringUtils.stripControlCodes(this.getModuleInfo()).replace(" | FP", ""));
            }
            modInfoTimer.reset();
        }
        if(updateLogic.getValue() == UpdateLogic.PACKET) {
            doAutoCrystal(null);
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
        placeResetTimer.reset();
        modInfoTimer.reset();
        novolaTimer.reset();
        antiStuckTimer.reset();
        placedCrystals.clear();
        attackedCrystals.clear();
        crystalRender = null;
        oldPlacePos = null;
        currentAttacking = null;
        damage = 0.0d;
        faceplaceKeyOn = false;
        resetRots();
    }

    public Set<BlockPos> getPlacedCrystals() {
        return this.placedCrystals;
    }

    private enum SwingLogic {
        PLACE,
        BREAK,
        BOTH,
        NONE
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
        PACKET,
        FIELD
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
