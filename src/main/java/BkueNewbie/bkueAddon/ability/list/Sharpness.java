package BkueNewbie.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.*;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.concurrent.ThreadLocalRandom;

@AbilityManifest(name = "샤프니스", rank = Rank.B, species = Species.HUMAN, explain = {
        "§7패시브 §8- §c예리함§f: 공격할 때 20% 확률로 피해가 3 증가합니다."
}, summarize = {
        "일정 확률로 추가 피해를 입힙니다."
})
public class Sharpness extends AbilityBase {
    public Sharpness(Participant participant) {
        super(participant);
    }

    public static final SettingObject<Integer> CHANCE_CONFIG = abilitySettings.new SettingObject<Integer>(Sharpness.class, "chance", 20,
            "# 발동 확률(%)") {
        @Override
        public boolean condition(Integer value) {
            return value >= 0 && value <= 100;
        }
    };

    public static final SettingObject<Double> DAMAGE_CONFIG = abilitySettings.new SettingObject<Double>(Sharpness.class, "bonus-damage", 3.0,
            "# 추가 피해") {
        @Override
        public boolean condition(Double value) {
            return value >= 0;
        }
    };

    @SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!e.getDamager().equals(getPlayer())) return;
        if (!(e.getEntity() instanceof Player)) return;

        if (ThreadLocalRandom.current().nextInt(100) < CHANCE_CONFIG.getValue()) {
            e.setDamage(e.getDamage() + DAMAGE_CONFIG.getValue());
        }
    }
}
