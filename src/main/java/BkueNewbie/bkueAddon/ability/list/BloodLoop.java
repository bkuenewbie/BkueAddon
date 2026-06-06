package BkueNewbie.bkueAddon.ability.list;

import java.util.ArrayList;
import java.util.List;

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

@AbilityManifest(name = "블러드 루프", rank = Rank.S, species = Species.HUMAN, explain = {
        "§7철괴 우클릭 §8- §c혈맹의 저주§f: 5초간 생명선 저주 고리를 발동합니다.",
        "- 저주 도중 시전자가 피해를 입으면, 받은 대미지와 동일한 양의 피해를 반경 7블록 내의 모든 적들에게 균등하게 분할하여 즉시 되돌려줍니다.",
        "- 피해를 공유할 적이 많을수록 고통이 분산됩니다."
})
public class BloodLoop extends AbilityBase implements ActiveHandler {

    public BloodLoop(Participant participant) {
        super(participant);
    }

    public static final SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(
            BloodLoop.class, "cooldown", 40, "# 저주 활성화 쿨타임") {
        @Override
        public boolean condition(Integer value) { return value >= 0; }
        @Override
        public String toString() { return Formatter.formatCooldown(getValue()); }
    };

    private final Cooldown cooldown = new Cooldown(COOLDOWN_CONFIG.getValue());
    private boolean isLoopActive = false;
    private int loopTimer = 0;

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
            bloodTimer.start();
        }
    }

    private final AbilityTimer bloodTimer = new AbilityTimer() {
        @Override
        public void run(int count) {
            if (isLoopActive) {
                loopTimer--;
                if (count % 3 == 0) {
                    ParticleLib.REDSTONE.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 0.4f, 0.4f, 0.4f, 5, 0.0);
                }
                if (loopTimer <= 0) {
                    isLoopActive = false;
                    getPlayer().sendMessage("§7[블러드 루프] 저주 결속이 해제되었습니다.");
                }
            }
        }
    }.setPeriod(TimeUnit.TICKS, 1).register();

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
            if (isLoopActive) return false;
            if (cooldown.isCooldown()) return false;

            isLoopActive = true;
            loopTimer = 100;

            getPlayer().sendMessage("§c[블러드 루프] 고통의 인과율을 연결합니다. 받은 상처는 모두에게 각인됩니다.");
            SoundLib.ENTITY_WITHER_HURT.playSound(getPlayer().getLocation(), 1.0f, 0.5f);

            cooldown.start();
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity().equals(getPlayer()) && isLoopActive && !e.isCancelled()) {
            double damageToShare = e.getFinalDamage();
            if (damageToShare <= 0) return;

            List<LivingEntity> targets = new ArrayList<>();
            for (Entity entity : getPlayer().getWorld().getNearbyEntities(getPlayer().getLocation(), 7, 4, 7)) {
                if (entity instanceof LivingEntity && targetFilter.test(entity)) {
                    targets.add((LivingEntity) entity);
                }
            }

            if (!targets.isEmpty()) {
                double splitDamage = damageToShare / targets.size();
                for (LivingEntity target : targets) {
                    target.damage(splitDamage, getPlayer());
                    ParticleLib.DAMAGE_INDICATOR.spawnParticle(target.getLocation().add(0, 1, 0), 0.1f, 0.3f, 0.1f, 3, 0.0);
                    target.sendMessage("§c[블러드 루프] 주술사의 고통이 생명선을 타고 공유되었습니다! §7(피해: " + String.format("%.1f", splitDamage) + ")");
                }
                SoundLib.ENTITY_VEX_HURT.playSound(getPlayer().getLocation(), 0.8f, 0.5f);
            }
        }
    }
}