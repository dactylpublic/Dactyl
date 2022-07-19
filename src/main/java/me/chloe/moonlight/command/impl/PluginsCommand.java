package me.chloe.moonlight.command.impl;

import me.chloe.moonlight.event.impl.network.PacketEvent;
import me.chloe.moonlight.util.ChatUtil;
import me.chloe.moonlight.util.TimeUtil;
import me.chloe.moonlight.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketTabComplete;
import net.minecraft.network.play.server.SPacketTabComplete;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class PluginsCommand extends Command {
    public PluginsCommand() {
        super("Plugins", new String[] {"plugins", "pl"},"Attempts to show the servers plugins");
        MinecraftForge.EVENT_BUS.register(this);
    }
    private final TimeUtil timer = new TimeUtil();
    boolean run = false;
    @Override
    public void run(String[] args) {
        Minecraft.getMinecraft().player.connection.sendPacket(new CPacketTabComplete("/", null, false));
        timer.reset();
        run = true;
    }

    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event) {
        if(timer.hasPassed(10000) && run) {
            ChatUtil.printMsg("Plugins: None Found!", true, true, -123);
            run = false;
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent event) {
        if(run) {
            if (event.getType() == PacketEvent.PacketType.INCOMING) {
                if (event.getPacket() instanceof SPacketTabComplete) {
                    SPacketTabComplete packet = (SPacketTabComplete)event.getPacket();
                    String[] commands = packet.getMatches();
                    String message = "";
                    int size = 0;
                    String[] array;
                    int length = (array = commands).length;
                    for (int i = 0; i < length; i++) {
                        String command = array[i];
                        String pluginName = command.split(":")[0].substring(1);
                        if ((!message.contains(pluginName)) && (command.contains(":")) && (!pluginName.equalsIgnoreCase("minecraft")) && (!pluginName.equalsIgnoreCase("bukkit")))
                        {
                            size++;
                            if (message.isEmpty()) {
                                message = String.valueOf(message) + pluginName;
                            } else {
                                message = String.valueOf(message) + "&8, &7" + pluginName;
                            }
                        }
                    }
                    if (!message.isEmpty()) {
                        ChatUtil.printMsg("Plugins (" + size + "): &7" + message + "&8.", true, true, -123);
                    } else {
                        ChatUtil.printMsg("Plugins: None Found!", true, true, -123);
                    }
                    event.setCanceled(true);
                    run = false;
                }
            }
        }
    }

    /*
    if ((event.getPacket() instanceof S3APacketTabComplete))
    {
        S3APacketTabComplete packet = (S3APacketTabComplete)event.getPacket();
        String[] commands = packet.func_149630_c();
        String message = "";
        int size = 0;
        String[] array;
        int length = (array = commands).length;
        for (int i = 0; i < length; i++) {
               String command = array[i];
               String pluginName = command.split(":")[0].substring(1);
               if ((!message.contains(pluginName)) && (command.contains(":")) && (!pluginName.equalsIgnoreCase("minecraft")) && (!pluginName.equalsIgnoreCase("bukkit")))
               {
                   size++;
                   if (message.isEmpty()) {
                     message = String.valueOf(message) + pluginName;
                   } else {
                     message = String.valueOf(message) + "§8, §7" + pluginName;
                   }
               }
        }
        if (!message.isEmpty()) {
            Logger.getLogger().printToChat("Plugins (" + size + "): §7" + message + "§8.");
        } else {
            Logger.getLogger().printToChat("Plugins: None Found!");
        }
        event.setCanceled(true);
        Gun.getInstance().getEventManager().unregister(this);
    }
    if (stopwatch.hasCompleted(15000)) {
        Gun.getInstance().getEventManager().unregister(this);
        Logger.getLogger().printToChat("Could not listen for a S3APacketTabComplete! (15s)! ");
    }
     */
}
