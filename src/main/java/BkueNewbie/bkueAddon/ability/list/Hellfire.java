package BkueNewbie.bkueAddon.ability.list;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.google.common.base.Predicate;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.util.Vector;

@AbilityManifest(name = "업화", rank = Rank.L, species = Species.DEMIGOD, explain = {
        "§7철괴 우클릭 §8- §c증오 축적§f: 6초간 복수 상태에 돌입하여 받는 모든 대미지를 기록합니다.",
        "- 상태가 유지되는 동안 시전자는 피해량이 30% 감소하여 적용됩니다.",
        "- 6초가 지나거나 재우클릭 시, 축적된 대미지의 150%만큼 반경 7블록 광역 화염 파동 피해를 입힙니다."
})
public class Hellfire extends AbilityBase implements ActiveHandler {

    public Hellfire(Participant participant) {
        super(participant);
    }

    public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(
            Hellfire.class, "cooldown", 50, "# 파동 방출 후 쿨타임") {
        @Override
        public boolean condition(Integer value) { return value >= 0; }
        @Override
        public String toString() { return Formatter.formatCooldown(getValue()); }
    };

    private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue());
    private boolean isAccumulating = false;
    private double accumulatedDamage = 0;
    private int durationTicks = 0;

    private final Predicate<Entity> targetFilter = new Predicate<Entity>() {
        @Override
        public boolean test(Entity entity) {
            if (entity.equals(getPlayer())) return false;
            if (entity instanceof Player) {
                if (!getGame().isParticipating(entity.getUniqueId()) || !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
                    return false;
                }
            }
            return entity instanceof LivingEntity;
        }
        @Override
        public boolean apply(Entity arg0) { return test(arg0); }
    };

    @Override
    protected void onUpdate(Update update) {
        if (update == Update.RESTRICTION_CLEAR) {
            hellfireTimer.start();
        }
    }

    private final AbilityTimer hellfireTimer = new AbilityTimer() {
        @Override
        public void run(int count) {
            if (isAccumulating) {
                durationTicks--;
                if (count % 3 == 0) {
                    ParticleLib.FLAME.spawnParticle(getPlayer().getLocation().add(0, 0.5, 0), 0.3f, 0.5f, 0.3f, 4, 0.02);
                }
                if (durationTicks <= 0) {
                    releaseHellfire();
                }
            }
        }
    }.setPeriod(TimeUnit.TICKS, 1).register();

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
            if (isAccumulating) {
                releaseHellfire();
                return true;
            }
            if (cooldown.isCooldown()) return false;

            isAccumulating = true;
            accumulatedDamage = 0;
            durationTicks = 120;

            getPlayer().sendMessage("§c[업화] 고통을 축적하기 시작합니다. 받은 상처를 분노로 치환합니다.");
            SoundLib.ENTITY_BLAZE_AMBIENT.playSound(getPlayer().getLocation(), 1.0f, 0.6f);
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity().equals(getPlayer()) && isAccumulating) {
            double originalDamage = e.getDamage();
            accumulatedDamage += originalDamage;

            e.setDamage(originalDamage * 0.7);
            SoundLib.BLOCK_FIRE_AMBIENT.playSound(getPlayer().getLocation(), 0.8f, 1.2f);
        }
    }

    private void releaseHellfire() {
        if (!isAccumulating) return;
        isAccumulating = false;

        double finalDamage = accumulatedDamage * 1.5;
        if (finalDamage < 5.0) finalDamage = 5.0;

        getPlayer().sendMessage("§e[업화] 축적된 인과율의 불꽃을 방출합니다! §7(피해량: " + String.format("%.1f", finalDamage) + ")");
        SoundLib.ENTITY_DRAGON_FIREBALL_EXPLODE.playSound(getPlayer().getLocation(), 1.2f, 0.8f);

        Location center = getPlayer().getLocation();
        for (int i = 0; i < 360; i += 10) {
            double rad = Math.toRadians(i);
            Vector dir = new Vector(Math.cos(rad), 0.1, Math.sin(rad));
            for (double d = 1; d <= 7; d += 1.5) {
                ParticleLib.LAVA.spawnParticle(center.clone().add(dir.clone().multiply(d)), 0, 0, 0, 1, 0.0);
            }
        }

        List<LivingEntity> targets = new ArrayList<>();
        for (Entity entity : getPlayer().getWorld().getNearbyEntities(center, 7, 4, 7)) {
            if (entity instanceof LivingEntity && targetFilter.test(entity)) {
                targets.add((LivingEntity) entity);
            }
        }

        for (LivingEntity target : targets) {
            target.damage(finalDamage, getPlayer());
            target.setFireTicks(80);
            target.sendMessage("§c[업화] 복수귀가 되돌려준 업보의 불꽃 파동에 휩쓸렸습니다.");
        }

        cooldown.start();
    }
}