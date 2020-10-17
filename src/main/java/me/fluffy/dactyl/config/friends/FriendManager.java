package me.fluffy.dactyl.config.friends;

import me.fluffy.dactyl.config.ConfigUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendManager {
    private final List<Friend> friendsList;

    File dactylFriends;

    public FriendManager() {
        friendsList = new ArrayList<>();
        try {
            dactylFriends = ConfigUtil.createFileIfNotExists("friends", "yml");
            initializeFriends();
        } catch(IOException exception) {
            exception.printStackTrace();
        }
    }

    public boolean isFriend(String username) {
        for(Friend friend : friendsList) {
            if(friend.getName().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    public List<Friend> getFriendsList() {
        return this.friendsList;
    }

    public void initializeFriends() throws IOException {
        InputStream friendStream = new FileInputStream(dactylFriends);
        Map<String, Map<String, Object>> yamlObj = new Yaml().load(friendStream);
        if(yamlObj != null) {
            for (Map.Entry<String, Map<String, Object>> pathEntry : yamlObj.entrySet()) {
                friendsList.add(new Friend(pathEntry.getKey()));
            }
        }
    }

    public void addFriend(String name) {
        friendsList.add(new Friend(name));
    }

    public void removeFriend(String name) {
        friendsList.removeIf(friend->friend.getName().equalsIgnoreCase(name));
    }

    public void save() throws IOException {
        ConfigUtil.clearFile(dactylFriends);
        Map<String, Object> yamlData = new HashMap<String, Object>();
        for (Friend friend : friendsList) {
            yamlData.put(friend.getName(), "");
        }
        Yaml yaml = new Yaml();
        BufferedWriter bufferedWriter = ConfigUtil.makeWriter(dactylFriends, false);
        yaml.dump(yamlData, bufferedWriter);
        ConfigUtil.closeWriter(bufferedWriter);
    }
}
