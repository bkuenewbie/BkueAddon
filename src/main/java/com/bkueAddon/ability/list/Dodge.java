package com.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.*;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.concurrent.ThreadLocalRandom;

@AbilityManifest(name = "회피", rank = Rank.B, species = Species.HUMAN, explain = {
        "§7패시브 §8- §b회피§f: 공격받을 때 $[CHANCE_CONFIG]% 확률로 피해를 받지 않습니다."
}, summarize = {
        "일정 확률로 공격을 회피합니다."
})
public class Dodge extends AbilityBase {

    public Dodge(Participant participant) {
        super(participant);
    }

    public static final AbilitySettings.SettingObject<Integer> CHANCE_CONFIG = abilitySettings.new SettingObject<Integer>(Dodge.class, "chance", 10, "# 발동 확률(%)") {
        @Override
        public boolean condition(Integer value) {
            return value >= 0 && value <= 100;
        }
    };

    @SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!e.getEntity().equals(getPlayer()) || !(e.getDamager() instanceof Player)) return;

        if (ThreadLocalRandom.current().nextInt(100) < CHANCE_CONFIG.getValue()) {
            e.setCancelled(true);
            ParticleLib.CLOUD.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 0.3f, 0.5f, 0.3f, 12, 0.1);
            SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer(), 1, 1.8f);
        }
    }
}