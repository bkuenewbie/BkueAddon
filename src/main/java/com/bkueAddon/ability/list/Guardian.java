package com.bkueAddon.ability.list;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;

@AbilityManifest(name = "가디언", rank = Rank.A, species = Species.HUMAN, explain = {
        "§7패시브 §8- §a최후의 저항§f: 체력이 최대 체력의 40% 이하일 때 받는 피해가 20% 감소합니다.",
        "§7철괴 우클릭 §8- §3수호의 방패§f: 5초 동안 받는 피해가 50% 감소합니다. $[COOLDOWN_CONFIG]"
}, summarize = {
        "체력이 낮을수록 더욱 단단해지며, 위기 상황에서 강력한 피해 감소 효과를 통해 생존할 수 있는 방어형 능력입니다."
})
public class Guardian extends AbilityBase implements ActiveHandler {

    public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(
            Guardian.class, "cooldown", 25, "# 수호의 방패 쿨타임") {

        @Override
        public boolean condition(Integer value) {
            return value >= 0;
        }

        @Override
        public String toString() {
            return Formatter.formatCooldown(getValue());
        }
    };

    private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue());

    private final Duration shield = new Duration(100, cooldown) {

        @Override
        protected void onDurationStart() {
            SoundLib.ENTITY_PLAYER_LEVELUP.playSound(getPlayer());
        }

        @Override
        protected void onDurationProcess(int count) {
            ParticleLib.CRIT_MAGIC.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 0.3, 0.5, 0.3, 2, 0.01);
        }

        @Override
        protected void onDurationEnd() {
        }

        @Override
        protected void onDurationSilentEnd() {
        }
    };

    public Guardian(Participant participant) {
        super(participant);
    }

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldown.isCooldown() && !shield.isRunning()) {
            shield.start();
            return true;
        }

        return false;
    }

    @SubscribeEvent
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity().equals(getPlayer())) {
            Player player = getPlayer();
            double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double damage = event.getDamage();

            if (player.getHealth() <= maxHealth * 0.4) {
                damage *= 0.8;
            }

            if (shield.isRunning()) {
                damage *= 0.5;
            }

            event.setDamage(damage);
        }
    }
}