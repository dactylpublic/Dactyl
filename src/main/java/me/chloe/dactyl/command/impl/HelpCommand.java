package me.chloe.dactyl.command.impl;

import me.chloe.dactyl.Dactyl;
import me.chloe.dactyl.util.ChatUtil;
import me.chloe.dactyl.command.Command;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("Help", new String[] {"help", "?"},"Lists all commands");
    }
    @Override
    public void run(String[] args) {
        ChatUtil.printMsg("&1Dactyl commands: ", false, false);
        for(Command cmd : Dactyl.commandManager.getCommandList()) {
            ChatUtil.printMsg("&1" + cmd.getName() + "&7 - " + cmd.getDescription(), false, false);
        }
    }
}
