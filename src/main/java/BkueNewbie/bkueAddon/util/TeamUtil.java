package BkueNewbie.bkueAddon.util;

import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import org.bukkit.entity.Player;

import static daybreak.abilitywar.game.GameManager.getGame;

public class TeamUtil {
    public static boolean isTeammate(Player p, Player t) {
        if (!(getGame() instanceof Teamable)) return false;

        Teamable teamGame = (Teamable) getGame();

        Participant player = getGame().getParticipant(p);
        Participant target = getGame().getParticipant(t);

        return teamGame.hasTeam(player) && teamGame.hasTeam(target) && teamGame.getTeam(player).equals(teamGame.getTeam(target));
    }
}
