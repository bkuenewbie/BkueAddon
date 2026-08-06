package com.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.*;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.event.entity.EntityDamageEvent;

@AbilityManifest(name = "쏘달", rank = Rank.S, species = Species.HUMAN, explain = {
        "§7패시브 §8- §c분노§f: 체력이 절반 이하가 되면 발작하여 공격력과 공격 속도가 증가합니다.",
        "§7패시브 §8- §4폭주§f: 체력이 25% 이하가 되면 더욱 강력한 힘과 신속을 얻습니다.",
        "§7아이디어 제공 §8- §6sodaal"
}, summarize = {
        "체력이 낮아질수록 공격력과 공격 속도가 증가합니다."
})

public class Sodaal extends AbilityBase {
    public Sodaal(Participant participant) {
        super(participant);
    }

    private final AbilityTimer countdown = new AbilityTimer() {

        @Override
        protected void run(int count) {
            AttributeInstance attribute = getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH);

            if (attribute == null) {
                return;
            }

            double maxHealth = attribute.getValue();
            double health = getPlayer().getHealth();

            if (health <= maxHealth / 4) {
                PotionEffects.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 10, 1, true);
                PotionEffects.SPEED.addPotionEffect(getPlayer(), 10, 1, true);
            } else if (health <= maxHealth / 2) {
                PotionEffects.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 10, 0, true);
                PotionEffects.SPEED.addPotionEffect(getPlayer(), 10, 0, true);
            }
        }

    }.setPeriod(TimeUnit.TICKS, 1);

    @Override
    protected void onUpdate(Update update) {
        if (update == Update.RESTRICTION_CLEAR) {
            countdown.start();
        }

        if (update == Update.ABILITY_DESTROY) {
            countdown.stop(true);
        }
    }
}