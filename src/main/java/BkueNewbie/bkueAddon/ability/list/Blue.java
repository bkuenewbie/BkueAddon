package BkueNewbie.bkueAddon.ability.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Frost;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.google.common.base.Predicate;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;

@AbilityManifest(name = "블루", rank = Rank.SPECIAL, species = Species.SPECIAL, explain = {
        "§7패시브 §8- §b절대영도 알고리즘§f: 이동하는 모든 발자국 바닥(반경 3블록)이 즉시 §b푸른얼음§f 블록으로 변경됩니다.",
        "- 생성된 푸른얼음은 §a3초§f 뒤에 자동으로 원래 블록 상태로 복구됩니다.",
        "- 자신이 만든 푸른얼음 위를 달릴 때 영구적으로 §b신속 II§f 효과를 획득합니다.",
        "§7패시브 §8- §3마찰력 제로§f: 변경된 §b푸른얼음§f 위에 서 있는 동안 적의 공격에 의한 §c넉백을 전혀 받지 않으며§f, 자신 혹은 타격당한 적 중 단 한 명이라도 이 §b푸른얼음§f 위에 있다면 근접 대미지가 §92배§f로 폭발적으로 증폭됩니다.",
        "§7패시브 §8- §f빙결 트랙§f: 적이 §b푸른얼음§f을 밟으면 극심한 관성 미끄러짐과 함께 §8구속 IV§f 효과가 적용됩니다.",
        "§7철괴 우클릭 §8- §3코드 프리즈§f: 정면의 적(최대 20블록)을 타겟팅하여 발밑(반경 3블록)을 즉시 얼려버리고, 2.5초간 대상을 완전히 §b빙결(기절)§f 시키며, 대상을 향해 빠르게 §9돌진§f합니다. $[COOLDOWN_CONFIG]",
}, summarize = {
        "이동 경로에 §b푸른얼음§f 장판을 생성하며, 그 위에서 §b신속 II§f 및 §c넉백 무시§f 효과를 받습니다.",
        "자신 혹은 적이 푸른얼음 위에 있을 시 §9근접 대미지가 2배§f로 증폭되며, 적이 밟으면 §8구속 IV§f에 걸립니다.",
        "§7철괴 우클릭 시§f 원거리의 적을 2.5초간 §b빙결(Frost)§f시키고 적을 향해 강하게 §9돌진§f합니다."
})

public class Blue extends AbilityBase implements ActiveHandler {

    public Blue(Participant participant) {
        super(participant);
    }

    public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(
            Blue.class, "cooldown", 45, "# 우클릭 스킬 쿨타임") {
        @Override
        public boolean condition(Integer value) { return value >= 0; }
        @Override
        public String toString() { return Formatter.formatCooldown(getValue()); }
    };

    private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue());

    private final Map<Block, Long> iceBlocks = new HashMap<>();
    private final Map<Block, Material> originalMaterials = new HashMap<>();

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
            iceRestoreTimer.start();
        }
    }

    private final AbilityTimer iceRestoreTimer = new AbilityTimer() {
        @Override
        public void run(int count) {
            long now = System.currentTimeMillis();
            List<Block> toRemove = new ArrayList<>();

            for (Map.Entry<Block, Long> entry : iceBlocks.entrySet()) {
                if (now >= entry.getValue()) {
                    Block b = entry.getKey();
                    b.setType(originalMaterials.getOrDefault(b, Material.AIR));
                    originalMaterials.remove(b);
                    toRemove.add(b);
                }
            }
            for (Block b : toRemove) {
                iceBlocks.remove(b);
            }

            Block standingBlock = getPlayer().getLocation().subtract(0, 0.1, 0).getBlock();
            if (standingBlock.getType() == Material.BLUE_ICE && iceBlocks.containsKey(standingBlock)) {
                if (!getPlayer().hasPotionEffect(PotionEffectType.SPEED)) {
                    getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10, 1, false, false));
                }
                if (count % 5 == 0) {
                    ParticleLib.CLOUD.spawnParticle(getPlayer().getLocation(), 0.2f, 0.0f, 0.2f, 1, 0.0);
                }
            }
        }
    }.setPeriod(TimeUnit.TICKS, 1).register();

    private void createIceField(Location centerLocation) {
        Location baseLoc = centerLocation.clone().subtract(0, 1, 0);
        int radius = 3;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if ((x * x) + (z * z) <= radius * radius) {
                    Block block = baseLoc.clone().add(x, 0, z).getBlock();

                    if (block.getType().isSolid() && block.getType() != Material.BLUE_ICE && !block.getType().name().contains("COMMAND")) {
                        if (!iceBlocks.containsKey(block)) {
                            originalMaterials.put(block, block.getType());
                        }
                        block.setType(Material.BLUE_ICE);
                        iceBlocks.put(block, System.currentTimeMillis() + 3000);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onMove(PlayerMoveEvent e) {
        if (e.getPlayer().equals(getPlayer())) {
            createIceField(getPlayer().getLocation());
            return;
        }

        Player runner = e.getPlayer();
        if (getGame().isParticipating(runner.getUniqueId()) && !runner.equals(getPlayer())) {
            Block standingBlock = runner.getLocation().subtract(0, 0.1, 0).getBlock();
            if (standingBlock.getType() == Material.BLUE_ICE && iceBlocks.containsKey(standingBlock)) {
                if (!runner.hasPotionEffect(PotionEffectType.SLOW)) {
                    runner.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 3));
                    ParticleLib.SNOWBALL.spawnParticle(runner.getLocation().add(0, 1, 0), 0.2f, 0.2f, 0.2f, 3, 0.0);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager().equals(getPlayer()) && e.getEntity() instanceof LivingEntity) {
            Block myBlock = getPlayer().getLocation().subtract(0, 0.1, 0).getBlock();
            Block enemyBlock = e.getEntity().getLocation().subtract(0, 0.1, 0).getBlock();

            boolean conditionMyIce = (myBlock.getType() == Material.BLUE_ICE && iceBlocks.containsKey(myBlock));
            boolean conditionEnemyIce = (enemyBlock.getType() == Material.BLUE_ICE && iceBlocks.containsKey(enemyBlock));

            if (conditionMyIce || conditionEnemyIce) {
                e.setDamage(e.getDamage() * 2.0);
                SoundLib.BLOCK_GLASS_BREAK.playSound(getPlayer().getLocation(), 1.0f, 1.4f);
                SoundLib.ENTITY_PLAYER_ATTACK_CRIT.playSound(getPlayer().getLocation(), 0.8f, 1.2f);
                ParticleLib.CRIT.spawnParticle(e.getEntity().getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 15, 0.5);
            }
        }
    }

    @SubscribeEvent
    public void onVelocity(PlayerVelocityEvent e) {
        if (e.getPlayer().equals(getPlayer())) {
            Block standingBlock = getPlayer().getLocation().subtract(0, 0.1, 0).getBlock();
            if (standingBlock.getType() == Material.BLUE_ICE && iceBlocks.containsKey(standingBlock)) {
                e.setCancelled(true);
            }
        }
    }

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldown.isCooldown()) {
            Location eyeLoc = getPlayer().getEyeLocation();
            Vector direction = eyeLoc.getDirection().normalize();
            LivingEntity target = null;

            for (int i = 1; i <= 20; i++) {
                Location checkLoc = eyeLoc.clone().add(direction.clone().multiply(i));
                for (Entity entity : checkLoc.getWorld().getNearbyEntities(checkLoc, 1.2, 1.5, 1.2)) {
                    if (entity instanceof LivingEntity && targetFilter.test(entity)) {
                        target = (LivingEntity) entity;
                        break;
                    }
                }
                if (target != null) break;
            }

            if (target != null) {
                createIceField(target.getLocation());

                if (target instanceof Player) {
                    Frost.apply(getGame(), (Player) target, TimeUnit.TICKS, 50);
                } else {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 9, false, false));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 50, 200, false, false));
                }

                SoundLib.BLOCK_GLASS_BREAK.playSound(target.getLocation(), 1.0f, 0.8f);
                SoundLib.ENTITY_ILLUSIONER_CAST_SPELL.playSound(getPlayer().getLocation(), 1.0f, 1.5f);
                SoundLib.ENTITY_ENDER_DRAGON_FLAP.playSound(getPlayer().getLocation(), 1.0f, 1.6f);
                ParticleLib.SNOWBALL.spawnParticle(target.getLocation().add(0, 1, 0), 0.5f, 1.0f, 0.5f, 30, 0.2);

                Vector dashVector = target.getLocation().toVector().subtract(getPlayer().getLocation().toVector()).normalize();
                dashVector.setY(0.15);
                dashVector.multiply(1.6);
                getPlayer().setVelocity(dashVector);

                getPlayer().sendMessage("§3[블루] §b코드 프리즈§f가 " + target.getName() + "에게 적중하여 빙결시키고 돌진했습니다!");
                return cooldown.start();
            } else {
                getPlayer().sendMessage("§c[블루] 사정거리(20블록) 내에 타겟팅할 수 있는 적이 없습니다.");
            }
        }
        return false;
    }
}