package BkueNewbie.bkueAddon.ability.list;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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

@AbilityManifest(name = "기하학", rank = Rank.L, species = Species.HUMAN, explain = {
        "§7철괴 우클릭 §8- §e삼각 결계§f: 바닥에 순차적으로 꼭짓점을 지정합니다.",
        "- 3개의 꼭짓점이 모두 지정되면 즉시 거대한 삼각형 결계가 형성됩니다.",
        "- 결계 내부의 모든 적에게 15의 마법 피해와 위더, 구속 3레벨을 5초간 부여합니다.",
        "- 결계가 유지되는 8초 동안 시전자는 결계 안에서 저항 2와 힘 1 버프를 얻습니다."
})
public class Geometry extends AbilityBase implements ActiveHandler {

    public Geometry(Participant participant) {
        super(participant);
    }

    public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(
            Geometry.class, "cooldown", 45, "# 결계 완성 후 쿨타임") {
        @Override
        public boolean condition(Integer value) { return value >= 0; }
        @Override
        public String toString() { return Formatter.formatCooldown(getValue()); }
    };

    private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue());
    private final List<Location> points = new ArrayList<>();
    private boolean isFieldActive = false;
    private int fieldTimer = 0;

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
            geometryTimer.start();
        }
    }

    private final AbilityTimer geometryTimer = new AbilityTimer() {
        @Override
        public void run(int count) {
            if (isFieldActive) {
                fieldTimer--;
                drawTriangleParticles();

                if (isInsideTriangle(getPlayer().getLocation(), points.get(0), points.get(1), points.get(2))) {
                    getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 25, 1, false, false));
                    getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 25, 0, false, false));
                }

                if (fieldTimer <= 0) {
                    isFieldActive = false;
                    points.clear();
                    getPlayer().sendMessage("§7[기하학] 삼각 결계가 소멸했습니다.");
                }
            } else {
                if (!points.isEmpty() && count % 10 == 0) {
                    for (Location loc : points) {
                        ParticleLib.VILLAGER_HAPPY.spawnParticle(loc, 0.1f, 0.1f, 0.1f, 3, 0.0);
                    }
                }
            }
        }
    }.setPeriod(TimeUnit.TICKS, 1).register();

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
            if (isFieldActive) return false;
            if (cooldown.isCooldown()) return false;

            Location loc = getPlayer().getLocation().getBlock().getLocation().add(0.5, 0.1, 0.5);
            points.add(loc);

            getPlayer().sendMessage("§e[기하학] 꼭짓점 " + points.size() + "번을 지정했습니다.");
            SoundLib.BLOCK_NOTE_BLOCK_CHIME.playSound(getPlayer().getLocation(), 1.0f, 0.5f * points.size());

            if (points.size() >= 3) {
                triggerTriangleField();
                cooldown.start();
            }
            return true;
        }
        return false;
    }

    private void triggerTriangleField() {
        isFieldActive = true;
        fieldTimer = 160;

        getPlayer().sendMessage("§a[기하학] 삼각 결계가 완성되었습니다! 구역을 지배합니다.");
        SoundLib.ENTITY_WITHER_SPAWN.playSound(getPlayer().getLocation(), 0.7f, 1.5f);

        for (Entity entity : getPlayer().getWorld().getNearbyEntities(getPlayer().getLocation(), 30, 15, 30)) {
            if (entity instanceof LivingEntity && targetFilter.test(entity)) {
                LivingEntity target = (LivingEntity) entity;
                if (isInsideTriangle(target.getLocation(), points.get(0), points.get(1), points.get(2))) {
                    target.damage(15.0, getPlayer());
                    target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2));
                    ParticleLib.EXPLOSION_LARGE.spawnParticle(target.getLocation(), 0.2f, 0.5f, 0.2f, 2, 0.0);
                    target.sendMessage("§c[기하학] 수학자의 절대 결계 내부에 갇혀 차원 압축 피해를 입었습니다.");
                }
            }
        }
    }

    private void drawTriangleParticles() {
        drawLine(points.get(0), points.get(1));
        drawLine(points.get(1), points.get(2));
        drawLine(points.get(2), points.get(0));
    }

    private void drawLine(Location loc1, Location loc2) {
        Vector dir = loc2.toVector().subtract(loc1.toVector());
        double len = dir.length();
        dir.normalize();
        for (double d = 0; d < len; d += 0.5) {
            Location p = loc1.clone().add(dir.clone().multiply(d));
            ParticleLib.SPELL_INSTANT.spawnParticle(p, 0, 0, 0, 1, 0.0);
        }
    }

    private boolean isInsideTriangle(Location p, Location a, Location b, Location c) {
        double d1 = sign(p, a, b);
        double d2 = sign(p, b, c);
        double d3 = sign(p, c, a);
        boolean has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);
        return !(has_neg && has_pos);
    }

    private double sign(Location p1, Location p2, Location p3) {
        return (p1.getX() - p3.getX()) * (p2.getZ() - p3.getZ()) - (p2.getX() - p3.getX()) * (p1.getZ() - p3.getZ());
    }
}