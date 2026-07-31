package com.bkueAddon.ability.list.IDieYouDie;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.GameManager;
import daybreak.abilitywar.game.event.participant.ParticipantDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@AbilityManifest(name = "너죽고 나죽고", rank = Rank.B, species = Species.OTHERS,
        explain = { "이 능력은 당신을 죽인 사람도 함께 즉시 사망합니다." }
)

public class IDieYouDie extends AbilityBase {

    public IDieYouDie(Participant participant) {
        super(participant);
    }

    @SubscribeEvent(onlyRelevant = true)
    private void onPlayerDeath(ParticipantDeathEvent e) {
        final Player killer = getPlayer().getKiller();
        final Participant killerParticipant = GameManager.getGame().getParticipant(killer);
        if (killer != null && killerParticipant != null) {
            final IDieYouDieDeathEvent event = new IDieYouDieDeathEvent(this, killerParticipant);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                killer.setHealth(0);
            }
        }
    }
}