package BkueNewbie.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.SoundLib;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@AbilityManifest(name = "바운티 헌터", rank = Rank.A, species = Species.HUMAN, explain = {
        "§7패시브 §8- §6무작위 수배§f: 게임 시작 시 무작위 플레이어 한 명이 §c현상수배 대상§f으로 지정되며, 모든 플레이어에게 공지됩니다.",
        "§7패시브 §8- §c사냥꾼의 직감§f: §c현상수배 대상§f의 실시간 위치와 거리를 파악하며, 대상에게 §c1.5배§f의 피해를 입힙니다.",
        "§7패시브 §8- §e현상금 수금§f: 누구든 §c현상수배 대상§f을 처치하면 현상금 보상으로 §a최대 체력(하트 2칸)§f이 영구적으로 증가합니다.",
        "§7철괴 우클릭 §8- §b공간 추적§f: §c현상수배 대상§f의 등 뒤로 즉시 공간을 넘어 순간이동하며, 3초간 §e신속 II§f 버프를 획득합니다. $[COOLDOWN_CONFIG]",
        "§6사냥 순환§f: §c현상수배 대상§f이 사망하면 즉시 새로운 무작위 플레이어가 수배되며 사냥이 계속 반복됩니다."
})
public class BountyHunter extends AbilityBase implements ActiveHandler {

    public static final AbilitySettings.SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(BountyHunter.class, "cooldown", 60,
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

    private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
    private final TrackerTimer trackerTimer = new TrackerTimer();
    private Participant currentBountyTarget = null;
    private final Map<UUID, Double> bonusHealthMap = new HashMap<>();

    public BountyHunter(Participant participant) {
        super(participant);
    }

    private class TrackerTimer extends AbilityTimer {
        public TrackerTimer() {
            super(TaskType.NORMAL, Integer.MAX_VALUE);
            setPeriod(TimeUnit.TICKS, 5);
            setBehavior(RestrictionBehavior.PAUSE_RESUME);
        }

        @Override
        protected void run(int count) {
            if (currentBountyTarget == null || currentBountyTarget.getPlayer() == null || !currentBountyTarget.getPlayer().isOnline()) return;

            Player targetPlayer = currentBountyTarget.getPlayer();
            Location targetLoc = targetPlayer.getLocation();

            String message;
            if (!getPlayer().getWorld().equals(targetPlayer.getWorld())) {
                message = String.format("§c[수배 대상: %s] §7다른 차원에 존재함 §8| §f위치: §a차원 상이", targetPlayer.getName());
            } else {
                double distance = getPlayer().getLocation().distance(targetLoc);
                message = String.format("§c[수배 대상: %s] §f거리: §e%.1fm §8| §f위치: §aX:%d Y:%d Z:%d",
                        targetPlayer.getName(), distance, targetLoc.getBlockX(), targetLoc.getBlockY(), targetLoc.getBlockZ());
            }

            getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        }
    }

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldownTimer.isCooldown()) {
            if (currentBountyTarget == null || currentBountyTarget.getPlayer() == null || !currentBountyTarget.getPlayer().isOnline()) {
                getPlayer().sendMessage("§c현재 추적할 수 있는 현상수배 대상이 없습니다.");
                return false;
            }

            Player targetPlayer = currentBountyTarget.getPlayer();
            if (!getPlayer().getWorld().equals(targetPlayer.getWorld())) {
                getPlayer().sendMessage("§c대상이 다른 차원에 있어 공간 추적이 불가능합니다.");
                return false;
            }

            Location targetLoc = targetPlayer.getLocation();
            Vector direction = targetLoc.getDirection().normalize();
            Location teleportLoc = targetLoc.clone().subtract(direction.multiply(1.2));
            teleportLoc.setDirection(direction);

            getPlayer().teleport(teleportLoc);
            getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1));

            SoundLib.ENTITY_ENDERMAN_TELEPORT.playSound(getPlayer().getLocation(), 1.0f, 1.2f);
            SoundLib.ENTITY_ENDERMAN_TELEPORT.playSound(targetPlayer.getLocation(), 1.0f, 1.2f);
            getPlayer().sendMessage("§b[공간 추적] §c" + targetPlayer.getName() + "§f의 등 뒤로 공간을 넘어 도약했습니다.");

            return cooldownTimer.start();
        }
        return false;
    }

    private Participant getRandomParticipant() {
        List<Participant> eligibleParticipants = new ArrayList<>();

        for (Participant p : getGame().getParticipants()) {
            if (p == null || p.getPlayer() == null || p.equals(getParticipant())) continue;
            if (!p.attributes().TARGETABLE.getValue()) continue;
            eligibleParticipants.add(p);
        }

        return eligibleParticipants.isEmpty() ? null : eligibleParticipants.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(eligibleParticipants.size()));
    }

    public void setBounty(Participant target) {
        currentBountyTarget = target;
        if (target != null && target.getPlayer() != null) {
            Player targetPlayer = target.getPlayer();
            Bukkit.broadcastMessage("§6[바운티 헌터] §c" + targetPlayer.getName() + "§e 님이 새로운 현상수배 대상으로 지정되었습니다! 모두 추격을 시작하세요!");

            if (targetPlayer.getWorld() != null) {
                targetPlayer.getWorld().playSound(targetPlayer.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.5f);
            }
        }
    }

    @Override
    protected void onUpdate(Update update) {
        super.onUpdate(update);
        if (update == Update.RESTRICTION_CLEAR) {
            trackerTimer.start();
            Participant randomPlayer = getRandomParticipant();
            if (randomPlayer != null) {
                setBounty(randomPlayer);
            }
        } else if (update == Update.ABILITY_DESTROY) {
            trackerTimer.stop(false);
            currentBountyTarget = null;
            clearBonusHealth();
        }
    }

    private void clearBonusHealth() {
        for (Map.Entry<UUID, Double> entry : bonusHealthMap.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (maxHealthAttr != null) {
                    double currentMax = maxHealthAttr.getBaseValue();
                    maxHealthAttr.setBaseValue(Math.max(20.0, currentMax - entry.getValue()));
                }
            }
        }
        bonusHealthMap.clear();
    }

    @SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager().equals(getPlayer()) && currentBountyTarget != null) {
            if (event.getEntity().equals(currentBountyTarget.getPlayer())) {
                event.setDamage(event.getDamage() * 1.5);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        if (currentBountyTarget != null && victim.equals(currentBountyTarget.getPlayer())) {
            Player killer = victim.getKiller();

            if (killer != null) {
                AttributeInstance maxHealthAttr = killer.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (maxHealthAttr != null) {
                    double currentMax = maxHealthAttr.getBaseValue();
                    maxHealthAttr.setBaseValue(currentMax + 4.0);

                    bonusHealthMap.put(killer.getUniqueId(), bonusHealthMap.getOrDefault(killer.getUniqueId(), 0.0) + 4.0);

                    killer.sendMessage("§e[현상금 수금] §c현상수배 대상§f을 처치하여 §a최대 체력(하트 2칸)§f이 영구적으로 증가했습니다!");
                }
            }

            currentBountyTarget = null;

            Bukkit.getScheduler().runTaskLater(daybreak.abilitywar.AbilityWar.getPlugin(), () -> {
                Participant nextTarget = getRandomParticipant();
                if (nextTarget != null) {
                    setBounty(nextTarget);
                } else {
                    Bukkit.broadcastMessage("§6[바운티 헌터] §f더 이상 수배할 수 있는 대상이 없어 사냥이 종료됩니다.");
                }
            }, 20L);
        }
    }
}