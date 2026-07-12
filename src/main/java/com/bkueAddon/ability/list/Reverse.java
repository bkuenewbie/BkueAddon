package com.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.*;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.concurrent.ThreadLocalRandom;

@AbilityManifest(name = "리버스", rank = Rank.B, species = Species.HUMAN, explain = {
        "§7패시브 §8- §d반사§f: 공격받을 때 $[CHANCE_CONFIG]% 확률로 피해를 무효화하고 같은 피해를 공격자에게 돌려줍니다."
}, summarize = {
        "일정 확률로 받은 피해를 공격자에게 반사합니다."
})
public class Reverse extends AbilityBase {
    public Reverse(Participant participant) {
        super(participant);
    }

    public static final AbilitySettings.SettingObject<Integer> CHANCE_CONFIG = abilitySettings.new SettingObject<Integer>(Reverse.class, "chance", 20,
            "# 발동 확률(%)") {
        @Override
        public boolean condition(Integer value) {
            return value >= 0 && value <= 100;
        }
    };

    @SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!e.getEntity().equals(getPlayer())) return;
        if (!(e.getDamager() instanceof Player)) return;

        if (ThreadLocalRandom.current().nextInt(100) < CHANCE_CONFIG.getValue()) {
            Player attacker = (Player) e.getDamager();

            e.setCancelled(true);
            attacker.damage(e.getFinalDamage());
        }
    }
}
