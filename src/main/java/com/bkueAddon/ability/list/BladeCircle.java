package com.bkueAddon.ability.list;

import daybreak.abilitywar.ability.*;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

@AbilityManifest(name = "블레이드 서클", rank = AbilityManifest.Rank.A, species = AbilityManifest.Species.HUMAN, explain = {
        "§7철괴 우클릭 §8- §e검기 장막§f: 5초간 자신을 중심으로 회전하는 서클을 생성합니다.",
        "- 장막 범위(3.5블록) 내 적에게 0.5초마다 3의 피해를 입히고 밀쳐냅니다. $[COOLDOWN_CONFIG]"
}, summarize = {
        "§7철괴 우클릭§f: 자신 주위에 검기 장막을 생성해 적을 공격하고 밀쳐냅니다."
})
public class BladeCircle extends AbilityBase implements ActiveHandler {

    public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(BladeCircle.class, "cooldown", 35, "# 검기 장막 쿨타임") {
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

    private final Duration duration = new Duration(100) {
        @Override
        protected void onDurationProcess(int count) {
            Location center = getPlayer().getLocation().add(0, 0.5, 0);

            for (int i = 0; i < 3; i++) {
                double angle = Math.toRadians(count * 15 + i * 120);
                Location point = center.clone().add(Math.cos(angle) * 3.5, 0, Math.sin(angle) * 3.5);
                ParticleLib.SWEEP_ATTACK.spawnParticle(point, 0, 0, 0, 1, 0);
                ParticleLib.CRIT.spawnParticle(point, 0, 0, 0, 2, 0.05);
            }

            if (count % 10 != 0) return;

            Location loc = getPlayer().getLocation();
            SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(loc, 0.5f, 1.5f);

            for (Entity entity : getPlayer().getWorld().getNearbyEntities(loc, 3.5, 3, 3.5)) {
                if (!(entity instanceof LivingEntity target) || entity.equals(getPlayer()) || loc.distanceSquared(target.getLocation()) > 12.25) continue;

                target.damage(3, getPlayer());

                Vector direction = target.getLocation().toVector().subtract(loc.toVector()).setY(0);
                if (direction.lengthSquared() > 0) target.setVelocity(direction.normalize().setY(0.35).multiply(0.8));
            }
        }
    }.setPeriod(TimeUnit.TICKS, 1);

    public BladeCircle(Participant participant) {
        super(participant);
    }

    @Override
    protected void onUpdate(Update update) {
        if (update == Update.ABILITY_DESTROY) duration.stop(true);
    }

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !duration.isRunning() && !cooldown.isCooldown()) {
            duration.start();
            cooldown.start();
            SoundLib.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR.playSound(getPlayer().getLocation(), 1, 1.8f);
            return true;
        }
        return false;
    }
}