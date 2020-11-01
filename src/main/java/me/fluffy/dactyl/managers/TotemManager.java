package me.fluffy.dactyl.managers;

import me.fluffy.dactyl.event.impl.player.UpdatePopEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;

public class TotemManager {
    private final HashMap<String, Integer> poppedUsers;

    public TotemManager() {
        poppedUsers = new HashMap<>();
    }

    public void updatePopped(String user) {
        boolean postEvent = false;
        if(poppedUsers.get(user) != null) {
            poppedUsers.put(user, (poppedUsers.get(user)+1));
            postEvent = true;
        }
        if(poppedUsers.get(user) == null) {
            poppedUsers.put(user, 1);
            postEvent = true;
        }
        if(postEvent) {
            MinecraftForge.EVENT_BUS.post(new UpdatePopEvent(user, poppedUsers.get(user)));
        }
    }

    public int getPops(String user) {
        if(poppedUsers.get(user) != null) {
            return poppedUsers.get(user);
        }
        return 0;
    }

    public void clearUser(String user) {
        if(poppedUsers.get(user) != null) {
            poppedUsers.remove(user);
        }
    }

    public void clear() {
        poppedUsers.clear();
    }
}
