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

@AbilityManifest(name = "포이즌 가든", rank = Rank.A, species = Species.HUMAN, explain = {
        "§7철괴 우클릭 §8- §a독성 가든§f: 발밑 지면에 7초간 유지되는 독무 구역을 결착합니다.",
        "- 구역 반경 5.5블록 내부의 모든 적들에게 포이즌 디버프와 구속을 갱신합니다.",
        "- 안개 속에 상주하는 적들은 0.5초마다 2의 직접 부식 마법 피해를 추가로 입습니다."
})
public class PoisonGarden extends AbilityBase implements ActiveHandler {

    public PoisonGarden(Participant participant) {
        super(participant);
    }

    public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(
            PoisonGarden.class, "cooldown", 32, "# 독성 가든 쿨타임") {
        @Override
        public boolean condition(Integer value) { return value >= 0; }
        @Override
        public String toString() { return Formatter.formatCooldown(getValue()); }
    };

    private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue());
    private Location gardenCenter = null;
    private boolean isGardenActive = false;
    private int gardenTimer = 0;

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
            gardenLoop.start();
        }
    }

    private final AbilityTimer gardenLoop = new AbilityTimer() {
        @Override
        public void run(int count) {
            if (isGardenActive) {
                gardenTimer--;

                if (count % 2 == 0) {
                    for (int i = 0; i < 6; i++) {
                        double angle = Math.random() * Math.PI * 2;
                        double radius = Math.random() * 5.5;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        Location pLoc = gardenCenter.clone().add(x, Math.random() * 1.2, z);
                        ParticleLib.SPELL_MOB.spawnParticle(pLoc, 0f, 1f, 0f, 1, 1.0);
                    }
                }

                if (count % 10 == 0) {
                    List<LivingEntity> targets = new ArrayList<>();
                    for (Entity entity : gardenCenter.getWorld().getNearbyEntities(gardenCenter, 6, 3, 6)) {
                        if (entity instanceof LivingEntity && targetFilter.test(entity)) {
                            if (gardenCenter.distance(entity.getLocation()) <= 5.5) {
                                targets.add((LivingEntity) entity);
                            }
                        }
                    }

                    for (LivingEntity target : targets) {
                        target.damage(2.0, getPlayer());
                        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 50, 1));
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 0));
                    }
                }

                if (gardenTimer <= 0) {
                    isGardenActive = false;
                    gardenCenter = null;
                    getPlayer().sendMessage("§7[포이즌 가든] 역병의 안개가 대기 중으로 기화되었습니다.");
                }
            }
        }
    }.setPeriod(TimeUnit.TICKS, 1).register();

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
            if (isGardenActive) return false;
            if (cooldown.isCooldown()) return false;

            gardenCenter = getPlayer().getLocation().getBlock().getLocation().add(0.5, 0.1, 0.5);
            isGardenActive = true;
            gardenTimer = 140;

            getPlayer().sendMessage("§a[포이즌 가든] 발밑에 치명적인 역병 의사의 바이오 맹독 구역을 살포합니다.");
            SoundLib.BLOCK_BREWING_STAND_BREW.playSound(getPlayer().getLocation(), 1.0f, 0.5f);

            cooldown.start();
            return true;
        }
        return false;
    }
}