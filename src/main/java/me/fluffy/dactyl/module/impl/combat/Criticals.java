package me.fluffy.dactyl.module.impl.combat;

import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Criticals extends Module {
    Setting<CritMode> critModeSetting = new Setting<CritMode>("Mode", CritMode.MODERN);
    Setting<Boolean> onlyAura = new Setting<Boolean>("RequireAura", true);

    public static Criticals INSTANCE;
    public Criticals() {
        super("Criticals", Category.COMBAT);
        INSTANCE = this;
    }

    public boolean ignoring = false;

    @Override
    public void onClientUpdate() {
        if(critModeSetting.getValue() == CritMode.JUMP) {
            this.setModuleInfo("Jump");
        } else if(critModeSetting.getValue() == CritMode.MINI) {
            this.setModuleInfo("Mini");
        } else {
            this.setModuleInfo("Packet");
        }
    }

    @SubscribeEvent
    public void onAttack(PacketEvent event) {
        if(onlyAura.getValue()) {
            if(Killaura.INSTANCE.isEnabled() && Killaura.target == null) {
                return;
            }
        }
        if(event.getType() == PacketEvent.PacketType.OUTGOING) {
            if(event.getPacket() instanceof CPacketUseEntity) {
                if(((CPacketUseEntity)event.getPacket()).getAction() == CPacketUseEntity.Action.ATTACK) {
                    if(mc.player.onGround && !mc.gameSettings.keyBindJump.isKeyDown() && ((CPacketUseEntity)event.getPacket()).getEntityFromWorld(mc.world) instanceof EntityLivingBase) {
                        if(!ignoring) {
                            switch ((CritMode)critModeSetting.getValue()) {
                                case JUMP:
                                    mc.player.jump();
                                    break;
                                case MINI:
                                    mc.player.jump();
                                    mc.player.motionY /= 2.0D;
                                    break;
                                case MODERN:
                                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.11, mc.player.posZ, false));
                                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.11, mc.player.posZ, false));
                                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1100013579, mc.player.posZ, false));
                                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1100013579, mc.player.posZ, false));
                                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1100013579, mc.player.posZ, false));
                                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1100013579, mc.player.posZ, false));
                                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                                    break;
                            }
                        }
                    }
                }
            }
        }
    }


    private enum CritMode {
        JUMP,
        MINI,
        MODERN
    }
}
