package me.fluffy.dactyl.managers;

import me.fluffy.dactyl.command.Command;
import me.fluffy.dactyl.command.impl.ConfigCommand;
import me.fluffy.dactyl.command.impl.FriendCommand;
import me.fluffy.dactyl.command.impl.HelpCommand;
import me.fluffy.dactyl.module.impl.client.HUD;
import me.fluffy.dactyl.util.ChatUtil;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    private List<Command> commandList;

    public CommandManager() {
        commandList = new ArrayList<>();
        commandList.add(new FriendCommand());
        commandList.add(new HelpCommand());
        commandList.add(new ConfigCommand());
    }

    public List<Command> getCommandList() {
        return this.commandList;
    }

    public boolean matchesCommand(String testedCmd) {
        for(Command cmd : commandList) {
            for(String alias : cmd.getAliases()) {
                if(alias.toLowerCase().equalsIgnoreCase(testedCmd.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void sendHelp() {
        ChatUtil.printMsg("&cInvalid command!", true, false);
    }

    public void processCommand(String cmd, String[] args) {
        for(Command command : commandList) {
            for(String alias : command.getAliases()) {
                if(alias.toLowerCase().equalsIgnoreCase(cmd.toLowerCase())) {
                    command.run(args);
                }
            }
        }
    }

    public String getPrefix() {
        return HUD.INSTANCE.commandPrefix.getValue();
    }
}
