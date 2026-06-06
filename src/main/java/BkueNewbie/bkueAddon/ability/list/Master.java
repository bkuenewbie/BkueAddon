package BkueNewbie.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.TargetHandler;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

@AbilityManifest(name = "주인님", rank = Rank.A, species = Species.HUMAN, explain = {
        "§7철괴 플레이어 우클릭 §8- §e노예 계약§f: 바라보는 대상 플레이어를 노예로 지정합니다. $[COOLDOWN_CONFIG]",
        "§7패시브 §8- §4동반 자살§f: 계약된 노예가 자신(주인님)을 처치할 경우, 저주가 발동하여 노예 또한 그 즉시 무조건 §4즉사§f합니다.",
        "§7아이디어 제공 §8- §6goodhyojun"
})
public class Master extends AbilityBase implements TargetHandler {

    public static final AbilitySettings.SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Master.class, "cooldown", 30,
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
    private Player slavePlayer = null;

    public Master(Participant participant) {
        super(participant);
    }

    @Override
    public void TargetSkill(Material material, LivingEntity entity) {
        if (material == Material.IRON_INGOT && !cooldownTimer.isCooldown()) {
            if (entity instanceof Player) {
                Player target = (Player) entity;
                if (!target.equals(getPlayer())) {
                    slavePlayer = target;

                    getPlayer().sendMessage("§a[주인님] §e" + target.getName() + "§f님을 당신의 노예로 지정했습니다.");
                    target.sendMessage("§c[주인님] §e" + getPlayer().getName() + "§c의 노예가 되었습니다. 주인을 죽이면 죽음뿐입니다.");

                    SoundLib.ENTITY_IRON_GOLEM_ATTACK.playSound(target.getLocation(), 1.0f, 0.5f);
                    ParticleLib.DAMAGE_INDICATOR.spawnParticle(target.getLocation().add(0, 1, 0), 0.3f, 0.3f, 0.3f, 5, 0.1);
                    cooldownTimer.start();
                }
            }
        }
    }

    @daybreak.abilitywar.ability.SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().equals(getPlayer())) {
            Player killer = event.getEntity().getKiller();
            if (killer != null && slavePlayer != null && killer.equals(slavePlayer)) {
                killer.damage(1000.0);
                killer.sendMessage("§4[주인님] §c감히 주인을 살해하여 계약 조건에 따라 처형되었습니다!");
                SoundLib.ENTITY_LIGHTNING_BOLT_THUNDER.playSound(killer.getLocation(), 1.0f, 0.5f);
            }
        }
    }
}