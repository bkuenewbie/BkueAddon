package com.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.*;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

@AbilityManifest(name = "봄버", rank = Rank.A, species = Species.HUMAN, explain = {
        "§7패시브 §8- §c폭발 면역§f: 모든 폭발 공격에 피해를 입지 않지만, 넉백은 그대로 받습니다.",
        "§7철괴 우클릭 §8- §c수류탄§f: 치명적인 폭발을 일으키는 투사체를 던집니다.",
        "- 폭발에 적중된 모든 플레이어는 30의 고정 피해와 함께 강력한 넉백을 받습니다. $[COOLDOWN_CONFIG]"
})
public class Bomber extends AbilityBase implements ActiveHandler {

    public Bomber(Participant participant) {
        super(participant);
    }

    public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Bomber.class, "cooldown", 30, "# 수류탄 기술의 쿨타임을 설정합니다. (초 단위)") {
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

    private final Predicate<Entity> predicate = entity -> {
        if (entity.equals(getPlayer())) return true;
        if (entity instanceof Player) {
            if (!getGame().isParticipating(entity.getUniqueId())
                    || (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
                    || !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
                return false;
            }
            if (getGame() instanceof Teamable) {
                final Teamable teamGame = (Teamable) getGame();
                final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = getParticipant();
                return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
            }
        }
        return true;
    };

    @SubscribeEvent
    private void onProjectileHit(final ProjectileHitEvent event) {
        if (event.getEntity().hasMetadata("GRENADE") && event.getEntity().getShooter() != null && event.getEntity().getShooter().equals(getPlayer())) {
            Location hitLocation = event.getEntity().getLocation();

            hitLocation.getWorld().createExplosion(hitLocation, 0, false, false);
            SoundLib.ENTITY_GENERIC_EXPLODE.playSound(hitLocation, 1.0f, 1.0f);

            for (Entity entity : hitLocation.getWorld().getNearbyEntities(hitLocation, 5, 5, 5)) {
                if (predicate.test(entity)) {
                    if (entity instanceof Player) {
                        Player p = (Player) entity;
                        if (!p.equals(getPlayer())) {
                            p.damage(30.0, getPlayer());
                        }
                        Vector knockback = p.getLocation().toVector().subtract(hitLocation.toVector()).normalize().multiply(1.5);
                        knockback.setY(0.5);
                        p.setVelocity(knockback);
                    }
                }
            }
            event.getEntity().remove();
        }
    }

    @SubscribeEvent(priority = 5)
    private void onEntityDamage(final EntityDamageEvent e) {
        if (e.getEntity().equals(getPlayer()) &&
                (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
                        e.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)) {
            e.setCancelled(true);
        }
    }

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
            if (cooldown.isCooldown()) return false;

            final Player player = getPlayer();
            Snowball grenade = player.launchProjectile(Snowball.class);
            grenade.setMetadata("GRENADE", new FixedMetadataValue(daybreak.abilitywar.AbilityWar.getPlugin(), true));

            Vector arcVelocity = player.getLocation().getDirection();
            arcVelocity.setY(arcVelocity.getY() + 0.25);
            arcVelocity.multiply(1.3);
            grenade.setVelocity(arcVelocity);

            return cooldown.start();
        }
        return false;
    }
}