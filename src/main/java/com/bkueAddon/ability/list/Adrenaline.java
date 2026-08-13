package com.bkueAddon.ability.list;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.event.participant.ParticipantDeathEvent;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;

@AbilityManifest(name = "아드레날린", rank = Rank.A, species = Species.HUMAN, explain = {
        "§7패시브 §8- §c전투 흥분§f: 적을 처치하면 5초 동안 §b신속 I§f과 §d재생 I§f을 획득합니다."
}, summarize = {
        "적 처치 시 일시적으로 이동속도와 생명력 재생이 증가하여 연속 전투에 유리합니다."
})
public class Adrenaline extends AbilityBase {

    public Adrenaline(Participant participant) {
        super(participant);
    }

    @SubscribeEvent
    public void onParticipantDeath(ParticipantDeathEvent e) {
        Player killer = e.getPlayer().getKiller();
        if (killer == null) return;

        if (killer.equals(getPlayer())) {
            getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0));
            getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0));

            SoundLib.ENTITY_PLAYER_LEVELUP.playSound(getPlayer());
            ParticleLib.VILLAGER_HAPPY.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 0.5, 0.5, 0.5, 15, 0.1);
        }
    }
}