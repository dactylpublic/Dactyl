package me.chloe.moonlight.injection.inj;

import com.google.common.collect.Maps;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.Map;

@Mixin(Scoreboard.class)
public class InjScoreboard {

    @Shadow
    @Nullable
    public ScorePlayerTeam getPlayersTeam(String username)
    {
        return this.teamMemberships.get(username);
    }

    @Shadow
    public final Map<String, ScorePlayerTeam> teams = Maps.<String, ScorePlayerTeam>newHashMap();

    @Shadow
    private final Map<String, ScorePlayerTeam> teamMemberships = Maps.<String, ScorePlayerTeam>newHashMap();

    @Shadow
    public void broadcastTeamRemove(ScorePlayerTeam playerTeam) {

    }

    /**
     * @author fluffy
     * @Reason removes the stupid log spam with bad scoreboard setups
     *         "Removes the team from the scoreboard, updates all player memberships and broadcasts the deletion to all players"
     */
    @Overwrite
    public void removeTeam(ScorePlayerTeam playerTeam) {
        if(playerTeam == null) {
            return;
        }
        this.teams.remove(playerTeam.getName());

        for (String s : playerTeam.getMembershipCollection()) {
            this.teamMemberships.remove(s);
        }

        this.broadcastTeamRemove(playerTeam);
    }

    /**
     * @author fluffy
     * @Reason Removes player from a team.
     */
    @Overwrite
    public void removePlayerFromTeam(String username, ScorePlayerTeam playerTeam)
    {
        if (this.getPlayersTeam(username) != playerTeam)
        {
            //throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + playerTeam.getName() + "'.");
        }
        else
        {
            this.teamMemberships.remove(username);
            playerTeam.getMembershipCollection().remove(username);
        }
    }
}
