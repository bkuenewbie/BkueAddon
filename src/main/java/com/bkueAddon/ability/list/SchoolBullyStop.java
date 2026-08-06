package com.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.*;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AbilityManifest(name = "학교 폭력 멈춰", rank = Rank.S, species = Species.HUMAN, explain = {
        "§7철괴 우클릭 §8- §b학교 폭력 멈춰§f: 10초 동안 모든 플레이어가 공격으로 피해를 줄 수 없게 합니다. $[COOLDOWN_CONFIG]",
        "§7아이디어 제공 §8- §6sodaal"
}, summarize = {
        "10초 동안 모든 플레이어의 공격을 막습니다."
})
public class SchoolBullyStop extends AbilityBase implements ActiveHandler {
    public SchoolBullyStop(Participant participant) {
        super(participant);
    }

    public static final AbilitySettings.SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(SchoolBullyStop.class, "cooldown", 30,
            "# 쿨타임") {

        @Override
        public boolean condition(Integer value) {
            return value >= 0;
        }

        @Override
        public String toString() {
            return Formatter.formatCooldown(getValue());
        }
    };

    public static final AbilitySettings.SettingObject<Integer> DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(SchoolBullyStop.class, "duration", 10,
            "# 지속 시간") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }

    };

    private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());

    private boolean enabled = false;

    private final Duration duration = new Duration(DURATION_CONFIG.getValue(), cooldownTimer) {
        @Override
        protected void onDurationProcess(int count) {
        }
        @Override
        protected void onDurationStart() {
            enabled = true;
        }
        @Override
        protected void onDurationEnd() {
            enabled = false;
        }
    }.setPeriod(TimeUnit.SECONDS, 1);

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldownTimer.isCooldown() && !duration.isRunning()) {
            duration.start();
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!enabled) return;

        if (e.getDamager() instanceof Player) {
            e.setCancelled(true);
        }
    }
}