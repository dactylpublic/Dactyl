package me.fluffy.dactyl.event.impl.action;

import me.fluffy.dactyl.event.ForgeEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class CommandSendEvent extends ForgeEvent {
    private String cmd;
    private String[] args;
    public CommandSendEvent(String cmd, String[] args) {
        this.setStage(Stage.PRE);
        this.cmd = cmd;
        this.args = args;
    }

    public String getCmd() {
        return this.cmd;
    }

    public String[] getArgs() {
        return this.args;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }
}
