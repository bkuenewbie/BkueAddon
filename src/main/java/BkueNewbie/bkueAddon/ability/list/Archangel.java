package BkueNewbie.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Rooted;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.SimpleTimer;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.Formatter;

import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.util.Vector;
import java.util.*;

@AbilityManifest(name = "대천사", rank = Rank.L, species = Species.GOD, explain = {
        "§7패시브 §8- §b천사의 검§f: 적을 처치할 때마다 공격력이 영구히 §c1§f씩 상승합니다.",
        "§7패시브 §8- §e천상의 현신§f: 30초마다 15칸 이내의 적 한 명에게 §6빛의 심판§f을 시전합니다.",
        "§6빛의 심판§f: 대상의 현재 체력을 시전자의 현재 체력 비율(%)과 동일하게 변경합니다.",
        "§7철괴 우클릭 §8- §e천상의 쇠사슬§f: 10칸 이내의 적을 빛의 쇠사슬로 연결합니다.",
        "§f- 3초간 대상의 이동을 제한하며, 유지되는 동안 적이 받는 모든 피해량이 20% 증가합니다."
})
public class Archangel extends AbilityBase implements ActiveHandler, Listener {

    public static final AbilitySettings.SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Archangel.class, "cooldown", 30,
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

    private final Random random = new Random();
    private final BossBar bossBar = Bukkit.createBossBar("§e천상의 현신", BarColor.YELLOW, BarStyle.SOLID);
    private final JudgmentTimer judgmentTimer = new JudgmentTimer();
    private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
    private final Circle rootCircle = Circle.of(1, 15);

    private final Map<UUID, Integer> chainedTargets = new HashMap<>();
    private final List<AbilityTimer> activeTimers = new ArrayList<>();

    private int killStacks = 0;

    public Archangel(Participant participant) {
        super(participant);
    }

    private class JudgmentTimer extends AbilityTimer {
        public JudgmentTimer() {
            super(TaskType.NORMAL, 600);
            setPeriod(TimeUnit.TICKS, 1);
            setBehavior(RestrictionBehavior.PAUSE_RESUME);
        }

        @Override
        protected void run(int count) {
            bossBar.setProgress((double) count / 600);
        }

        @Override
        protected void onEnd() {
            Participant target = getNearbyEnemy(15);
            if (target != null) {
                castLightJudgment(target);
            }
            setCount(0);
            start();
        }
    }

    @Override
    protected void onUpdate(Update update) {
        super.onUpdate(update);
        if (update == Update.RESTRICTION_CLEAR) {
            bossBar.addPlayer(getPlayer());
            judgmentTimer.start();
        } else if (update == Update.ABILITY_DESTROY) {
            bossBar.removeAll();
            judgmentTimer.stop(false);
            for (AbilityTimer timer : activeTimers) {
                timer.stop(false);
            }
            activeTimers.clear();
            chainedTargets.clear();
        }
    }

    private Participant getNearbyEnemy(double range) {
        List<Participant> participants = new ArrayList<>();
        for (Participant p : getGame().getParticipants()) {
            if (p.getPlayer().equals(getPlayer()) || !p.attributes().TARGETABLE.getValue()) continue;

            if (p.getPlayer().getLocation().distanceSquared(getPlayer().getLocation()) <= range * range) {
                participants.add(p);
            }
        }
        return participants.isEmpty() ? null : participants.get(random.nextInt(participants.size()));
    }

    private void castLightJudgment(Participant target) {
        Player targetPlayer = target.getPlayer();
        double casterRatio = getPlayer().getHealth() / getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double targetMaxHealth = targetPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double calculatedHealth = targetMaxHealth * casterRatio;

        targetPlayer.setHealth(Math.max(1.0, Math.min(calculatedHealth, targetMaxHealth)));

        SoundLib.ENTITY_LIGHTNING_BOLT_THUNDER.playSound(getPlayer().getLocation(), 0.5f, 1.5f);
        getPlayer().sendMessage("§6빛의 심판§f을 시전하여 대상(§e" + targetPlayer.getName() + "§f)의 체력을 동기화했습니다.");
        targetPlayer.sendMessage("§e대천사§f의 §6빛의 심판§f으로 인해 체력이 동기화되었습니다.");
    }

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldownTimer.isCooldown()) {

            double range = 10;
            List<Player> targets = LocationUtil.getEntitiesInCircle(Player.class, getPlayer().getLocation(), range, entity -> {
                return !entity.equals(getPlayer()) && getGame().getParticipant(entity) != null;
            });

            if (targets.isEmpty()) {
                getPlayer().sendMessage("§c주변에 적이 없습니다.");
                return false;
            }

            SoundLib.BLOCK_GRINDSTONE_USE.playSound(getPlayer());
            cooldownTimer.start();

            for (Player target : targets) {
                final Participant participant = getGame().getParticipant(target);
                if (participant == null) continue;

                Rooted.apply(participant, TimeUnit.TICKS, 60);
                chainedTargets.put(target.getUniqueId(), 60);

                AbilityTimer effectTimer = new AbilityTimer(SimpleTimer.TaskType.NORMAL, 60) {
                    @Override
                    protected void run(int count) {
                        UUID uuid = target.getUniqueId();
                        if (chainedTargets.containsKey(uuid)) {
                            int remaining = chainedTargets.get(uuid) - 1;
                            if (remaining <= 0) {
                                chainedTargets.remove(uuid);
                            } else {
                                chainedTargets.put(uuid, remaining);
                            }
                        }

                        for (Vector vector : rootCircle) {
                            ParticleLib.REDSTONE.spawnParticle(target.getLocation().clone().add(vector).add(0, (count % 20) / 10.0, 0), RGB.YELLOW);
                        }
                    }

                    @Override
                    protected void onEnd() {
                        activeTimers.remove(this);
                    }
                };

                effectTimer.setPeriod(TimeUnit.TICKS, 1);
                activeTimers.add(effectTimer);
                effectTimer.start();
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null && killer.equals(getPlayer())) {
            killStacks++;
            getPlayer().sendMessage("§b[천사의 검] §f적을 처치하여 공격력이 §e1§f 상승했습니다! (현재 영구 증가치: +" + killStacks + ")");
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager().equals(getPlayer())) {
            event.setDamage(event.getDamage() + killStacks);
        }

        if (chainedTargets.containsKey(event.getEntity().getUniqueId())) {
            event.setDamage(event.getDamage() * 1.2);
        }
    }
}