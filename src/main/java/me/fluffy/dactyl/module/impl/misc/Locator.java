package me.fluffy.dactyl.module.impl.misc;

import me.fluffy.dactyl.event.impl.network.PacketEvent;
import me.fluffy.dactyl.module.Module;
import me.fluffy.dactyl.setting.Setting;
import me.fluffy.dactyl.util.ChatUtil;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Locator extends Module {
    public Setting<Boolean> endPortal = new Setting<Boolean>("EndPortals", true);
    public Setting<Boolean> donkey = new Setting<Boolean>("Donkleys", true);
    public Setting<Boolean> llamas = new Setting<Boolean>("Llamas", true);
    public Locator() {
        super("Locator", Category.MISC);
    }


    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(event.getType() == PacketEvent.PacketType.INCOMING) {
            if(event.getPacket() instanceof SPacketSpawnMob) {
                SPacketSpawnMob packet = (SPacketSpawnMob)event.getPacket();
                String x = String.valueOf(Math.round(packet.getX()));
                String y = String.valueOf(Math.round(packet.getY()));
                String z = String.valueOf(Math.round(packet.getZ()));
                switch(packet.getEntityType()) {
                    case 31:
                        if(donkey.getValue()) {
                            handleSpawnedAt("Donkey", x, y, z);
                        }
                        break;
                    case 103:
                        if(llamas.getValue()) {
                            handleSpawnedAt("Llama", x, y, z);
                        }
                        break;
                    default:
                        break;
                }
            }
            if(event.getPacket() instanceof SPacketEffect) {
                SPacketEffect packet = (SPacketEffect)event.getPacket();
                if(packet.getSoundType() == 1038 && endPortal.getValue()) {
                    ChatUtil.printMsg("End portal lit at " + packet.getSoundPos().toString(), true, false);
                }
            }
        }
    }

    private void handleSpawnedAt(String name, String x, String y, String z) {
        ChatUtil.printMsg("&c[Locator]&r " + name + " found at " + x + " " + y + " " + z, true, false);
    }
}
