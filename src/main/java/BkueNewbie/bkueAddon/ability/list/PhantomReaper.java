package BkueNewbie.bkueAddon.ability.list;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.google.common.base.Predicate;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;

@AbilityManifest(name = "팬텀 리퍼", rank = Rank.L, species = Species.UNDEAD, explain = {
        "§7철괴 우클릭 §8- §b팬텀 슬래시§f: 바라보는 방향으로 12블록 공간을 리핑하며 즉시 순간이동합니다.",
        "- 궤적 상에 위치한 모든 적들에게 8의 방어 무시 피해를 입힙니다.",
        "§7패시브 §8- §0사신의 기습§f: 적의 후방(등 뒤 120도 각도 범위)에서 타격 시",
        "- 4의 추가 고정 대미지를 적용합니다"
})
public class PhantomReaper extends AbilityBase implements ActiveHandler {

    public PhantomReaper(Participant participant) {
        super(participant);
    }

    public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(
            PhantomReaper.class, "cooldown", 12, "# 팬텀 슬래시 쿨타임") {
        @Override
        public boolean condition(Integer value) { return value >= 0; }
        @Override
        public String toString() { return Formatter.formatCooldown(getValue()); }
    };

    private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue());

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
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
            if (cooldown.isCooldown()) return false;

            Location origin = getPlayer().getLocation();
            Vector direction = origin.getDirection().setY(0).normalize();
            if (direction.lengthSquared() == 0) {
                direction = getPlayer().getLocation().getDirection().normalize();
            }

            Location targetLoc = origin.clone().add(direction.multiply(12));
            targetLoc.setY(getPlayer().getWorld().getHighestBlockAt(targetLoc).getLocation().getY() + 1);
            targetLoc.setDirection(origin.getDirection());

            double distance = origin.distance(targetLoc);
            for (double d = 0; d < distance; d += 0.5) {
                Location trail = origin.clone().add(origin.getDirection().normalize().multiply(d));
                ParticleLib.SMOKE_LARGE.spawnParticle(trail, 0, 0, 0, 1, 0.0);
            }

            for (Entity entity : getPlayer().getWorld().getNearbyEntities(origin, 14, 14, 14)) {
                if (entity instanceof LivingEntity && targetFilter.test(entity)) {
                    LivingEntity victim = (LivingEntity) entity;
                    if (origin.distance(victim.getLocation()) <= 12) {
                        victim.damage(8.0, getPlayer());
                        ParticleLib.CRIT_MAGIC.spawnParticle(victim.getLocation().add(0, 1, 0), 0.2f, 0.4f, 0.2f, 5, 0.1);
                    }
                }
            }

            getPlayer().teleport(targetLoc);
            SoundLib.ENTITY_ENDERMAN_TELEPORT.playSound(getPlayer().getLocation(), 1.0f, 0.5f);
            SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer().getLocation(), 1.0f, 0.5f);

            cooldown.start();
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager().equals(getPlayer()) && e.getEntity() instanceof LivingEntity) {
            LivingEntity victim = (LivingEntity) e.getEntity();

            Vector attackerDir = getPlayer().getLocation().getDirection().setY(0).normalize();
            Vector victimDir = victim.getLocation().getDirection().setY(0).normalize();

            double dot = attackerDir.dot(victimDir);

            if (dot > 0.5) {
                e.setDamage(e.getDamage() + 4.0);
                SoundLib.ENTITY_PIGLIN_JEALOUS.playSound(victim.getLocation(), 0.8f, 0.6f);
                ParticleLib.SQUID_INK.spawnParticle(victim.getLocation().add(0, 1, 0), 0.3f, 0.5f, 0.3f, 8, 0.05);
            }
        }
    }

    private boolean isPointNearLine(Location p, Location start, Location end, double maxDistance) {
        Vector line = end.toVector().subtract(start.toVector());
        Vector toPoint = p.toVector().subtract(start.toVector());
        double lineLength = line.length();
        if (lineLength == 0) return start.distance(p) <= maxDistance;

        line.normalize();
        double dot = toPoint.dot(line);
        if (dot < 0 || dot > lineLength) return false;

        Vector closestPoint = start.toVector().add(line.multiply(dot));
        return p.toVector().distance(closestPoint) <= maxDistance;
    }
}