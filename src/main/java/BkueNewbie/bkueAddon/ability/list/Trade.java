package BkueNewbie.bkueAddon.ability.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.object.AbilitySelect.AbilityCollector;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.random.Random;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

@AbilityManifest(name = "거래", rank = Rank.S, species = Species.HUMAN, explain = {
        "§7철괴 플레이어 우클릭 §8- §e동등한 교환§f: 플레이어에게 철괴를 우클릭하면, 대상의 능력을 복사해 §b자신의 능력§f으로 탈취하고, 대상에게는 §c무작위 새로운 능력§f을 강제로 쥐여줍니다. $[COOLDOWN_CONFIG]"
}, summarize = {
        "§7철괴 플레이어 우클릭 시§f 대상의 능력을 가져오고, 대상의 능력은 무작위로 교체합니다."
})
public class Trade extends AbilityBase implements TargetHandler {

    public static final AbilitySettings.SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Trade.class, "cooldown", 90,
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
    private final Random random = new Random();

    public Trade(Participant participant) {
        super(participant);
    }

    @Override
    public void TargetSkill(Material material, LivingEntity entity) {
        if (material == Material.IRON_INGOT && !cooldownTimer.isCooldown()) {
            if (entity instanceof Player) {
                Player targetPlayer = (Player) entity;

                if (getGame().isParticipating(targetPlayer.getUniqueId())) {
                    Participant targetParticipant = getGame().getParticipant(targetPlayer.getUniqueId());

                    if (targetParticipant.attributes().TARGETABLE.getValue()) {
                        Class<? extends AbilityBase> targetAbilityClass = targetParticipant.getAbility().getClass();

                        if (targetAbilityClass.equals(this.getClass())) {
                            getPlayer().sendMessage("§c[거래] 동일한 능력을 가진 대상과는 거래할 수 없습니다.");
                            return;
                        }

                        try {
                            List<Class<? extends AbilityBase>> abilities = AbilityCollector.EVERY_ABILITY_EXCLUDING_BLACKLISTED.collect(getGame().getClass());
                            Class<? extends AbilityBase> randomAbility = random.pick(abilities);

                            targetParticipant.setAbility(randomAbility);
                            getParticipant().setAbility(targetAbilityClass);

                            SoundLib.ENTITY_PLAYER_LEVELUP.playSound(getPlayer().getLocation(), 1.0f, 1.2f);
                            SoundLib.ENTITY_PLAYER_LEVELUP.playSound(targetPlayer.getLocation(), 1.0f, 0.8f);
                            ParticleLib.SPELL_WITCH.spawnParticle(targetPlayer.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 30, 0.2);

                            getPlayer().sendMessage("§a[거래] §e" + targetPlayer.getName() + "§f의 능력을 완벽하게 빼앗았습니다!");
                            targetPlayer.sendMessage("§c[거래] 능력을 강탈당하고 계약의 대가로 새로운 무작위 능력이 할당되었습니다!");

                            cooldownTimer.start();
                        } catch (Exception e) {
                            getPlayer().sendMessage("§c[거래] 능력 교환 도중 데이터 구조적 오류가 발생했습니다.");
                            AbilityWar.getPlugin().getLogger().warning("Trade registration lifecycle failure: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
}