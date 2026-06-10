package BkueNewbie.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@AbilityManifest(name = "시소", rank = Rank.B, species = Species.HUMAN, explain = {
        "§7철괴 우클릭 §8- §e균형 맞추기§f: 자신보다 체력 비율이 높은 대상과 서로의 체력 퍼센트(%)를 합산한 뒤 정확히 반으로 나누어 동일한 비율로 재조정합니다. 단, 대상의 체력 비율이 낮거나 같으면 발동하지 않습니다. $[COOLDOWN_CONFIG]"
})
public class Seesaw extends AbilityBase implements TargetHandler {

    public static final AbilitySettings.SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Seesaw.class, "cooldown", 45,
            "# 균형 맞추기 쿨타임") {
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

    public Seesaw(Participant participant) {
        super(participant);
    }

    @Override
    public void TargetSkill(Material material, LivingEntity entity) {
        if (material == Material.IRON_INGOT) {
            if (getParticipant().hasEffect(Stun.registration)) return;

            if (cooldownTimer.isCooldown()) return;

            if (entity instanceof Player) {
                Player target = (Player) entity;
                if (!target.equals(getPlayer())) {

                    double myMax = getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    double myCurrent = getPlayer().getHealth();
                    double myPercent = myCurrent / myMax;

                    double targetMax = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    double targetCurrent = target.getHealth();
                    double targetPercent = targetCurrent / targetMax;

                    if (targetPercent <= myPercent) {
                        getPlayer().sendMessage("§c[시소] 당신보다 체력 비율이 낮거나 같은 적에게는 시소를 탈 수 없습니다.");
                        return;
                    }

                    double balancedPercent = (myPercent + targetPercent) / 2.0;

                    getPlayer().setHealth(myMax * balancedPercent);
                    target.setHealth(targetMax * balancedPercent);

                    getPlayer().sendMessage("§e[시소] §f적과의 체력 균형을 맞췄습니다! §7(" + (int)(balancedPercent * 100) + "%)");
                    target.sendMessage("§c[시소] §e" + getPlayer().getName() + "§f님이 당신과 체력 수평을 맞췄습니다! §7(" + (int)(balancedPercent * 100) + "%)");

                    SoundLib.BLOCK_NOTE_BLOCK_XYLOPHONE.playSound(getPlayer().getLocation(), 1.0f, 1.0f);
                    SoundLib.BLOCK_NOTE_BLOCK_XYLOPHONE.playSound(target.getLocation(), 1.0f, 0.6f);

                    ParticleLib.VILLAGER_HAPPY.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 10, 0.1);
                    ParticleLib.DAMAGE_INDICATOR.spawnParticle(target.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 10, 0.1);

                    cooldownTimer.start();
                }
            }
        }
    }
}