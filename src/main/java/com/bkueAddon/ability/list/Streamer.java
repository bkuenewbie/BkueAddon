package com.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.*;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@AbilityManifest(name = "방송인", rank = Rank.SPECIAL, species = Species.HUMAN, explain = {
        "§7패시브 §8- §b시선 집중§f: 자신을 바라보고 있는 플레이어가 많을수록 강해집니다.",
        "§7- §f관전자는 시선 수에 포함되지 않습니다.",
        "§7아이디어 제공 §8- §6sodaal"
}, summarize = {
        "§f자신을 바라보는 플레이어가 많을수록 강해집니다."
})
public class Streamer extends AbilityBase {

    public Streamer(Participant participant) {
        super(participant);
    }

    private final AbilityTimer viewerCheck = new AbilityTimer() {

        @Override
        protected void run(int count) {
            Player target = getPlayer();
            int viewers = 0;

            for (Participant participant : getGame().getParticipants()) {
                Player player = participant.getPlayer();

                if (player.equals(target)) {
                    continue;
                }

                if (isLookingAt(player, target)) {
                    viewers++;
                }
            }

            if (viewers <= 0) {
                return;
            }

            int amplifier = (viewers - 1) / 2;

            PotionEffects.INCREASE_DAMAGE.addPotionEffect(target, 5, amplifier, true);
        }

    }.setPeriod(TimeUnit.TICKS, 1);

    private boolean isLookingAt(Player viewer, Player target) {
        Location eye = viewer.getEyeLocation();
        Vector direction = eye.getDirection().normalize();

        Vector toTarget = target.getEyeLocation().toVector().subtract(eye.toVector()).normalize();

        if (direction.dot(toTarget) < 0.92) {
            return false;
        }

        if (eye.distanceSquared(target.getEyeLocation()) > 50 * 50) {
            return false;
        }

        return viewer.hasLineOfSight(target);
    }

    @Override
    protected void onUpdate(Update update) {
        if (update == Update.RESTRICTION_CLEAR) {
            viewerCheck.start();
        }

        if (update == Update.ABILITY_DESTROY) {
            viewerCheck.stop(true);
        }
    }
}