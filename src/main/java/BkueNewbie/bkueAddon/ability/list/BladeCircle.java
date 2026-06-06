package BkueNewbie.bkueAddon.ability.list;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.google.common.base.Predicate;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;

@AbilityManifest(name = "블레이드 서클", rank = Rank.A, species = Species.HUMAN, explain = {
        "§7철괴 우클릭 §8- §e검기 장막§f: 5초간 자신을 중심으로 회전하는 서클을 생성합니다.",
        "- 장막 범위(3.5블록) 내로 접근한 적들에게 0.5초마다 3의 피해를 입힙니다.",
        "- 피해를 입은 적들은 시전자의 바깥 방향으로 강하게 밀려납니다."
})
public class BladeCircle extends AbilityBase implements ActiveHandler {

    public BladeCircle(Participant participant) {
        super(participant);
    }

    public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(
            BladeCircle.class, "cooldown", 35, "# 검기 장막 쿨타임") {
        @Override
        public boolean condition(Integer value) { return value >= 0; }
        @Override
        public String toString() { return Formatter.formatCooldown(getValue()); }
    };

    private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue());
    private boolean isCircleActive = false;
    private int circleTimer = 0;

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
            circleLoop.start();
        }
    }

    private final AbilityTimer circleLoop = new AbilityTimer() {
        @Override
        public void run(int count) {
            if (isCircleActive) {
                circleTimer--;

                Location center = getPlayer().getLocation().add(0, 0.5, 0);
                double radius = 3.5;
                for (int i = 0; i < 3; i++) {
                    double angle = Math.toRadians((count * 15 + i * 120) % 360);
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    ParticleLib.SWEEP_ATTACK.spawnParticle(center.clone().add(x, 0, z), 0, 0, 0, 1, 0.0);
                }

                if (count % 10 == 0) {
                    SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer().getLocation(), 0.5f, 1.5f);
                    List<LivingEntity> targets = new ArrayList<>();
                    for (Entity entity : getPlayer().getWorld().getNearbyEntities(getPlayer().getLocation(), 4, 3, 4)) {
                        if (entity instanceof LivingEntity && targetFilter.test(entity)) {
                            if (getPlayer().getLocation().distance(entity.getLocation()) <= 3.8) {
                                targets.add((LivingEntity) entity);
                            }
                        }
                    }

                    for (LivingEntity target : targets) {
                        target.damage(3.0, getPlayer());
                        Vector push = target.getLocation().toVector().subtract(getPlayer().getLocation().toVector()).normalize().setY(0.2);
                        target.setVelocity(push.multiply(0.8));
                        target.sendMessage("§c[블레이드 서클] 회전하는 호신 검격에 닿아 튕겨나갔습니다.");
                    }
                }

                if (circleTimer <= 0) {
                    isCircleActive = false;
                    getPlayer().sendMessage("§7[블레이드 서클] 장막이 해제되었습니다.");
                }
            }
        }
    }.setPeriod(TimeUnit.TICKS, 1).register();

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
            if (isCircleActive) return false;
            if (cooldown.isCooldown()) return false;

            isCircleActive = true;
            circleTimer = 100;

            getPlayer().sendMessage("§e[블레이드 서클] 절대 접근 불가 영역의 검기 장막을 전개합니다.");
            SoundLib.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR.playSound(getPlayer().getLocation(), 1.0f, 1.8f);

            cooldown.start();
            return true;
        }
        return false;
    }
}