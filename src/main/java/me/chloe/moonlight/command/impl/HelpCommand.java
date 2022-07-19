package me.chloe.moonlight.command.impl;

import me.chloe.moonlight.Moonlight;
import me.chloe.moonlight.util.ChatUtil;
import me.chloe.moonlight.command.Command;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("Help", new String[] {"help", "?"},"Lists all commands");
    }
    @Override
    public void run(String[] args) {
        ChatUtil.printMsg("&1Dactyl commands: ", false, false);
        for(Command cmd : Moonlight.commandManager.getCommandList()) {
            ChatUtil.printMsg("&1" + cmd.getName() + "&7 - " + cmd.getDescription(), false, false);
        }
    }
}
