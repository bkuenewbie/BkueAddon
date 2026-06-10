package BkueNewbie.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

@AbilityManifest(name = "철퇴", rank = Rank.L, species = Species.HUMAN, explain = {
        "§7패시브 §8- §c낙하 공격§f: 낙하 중 적을 타격하면 낙하한 거리에 비례하여 추가 대미지를 입립니다.",
        "§7철괴 우클릭 §8- §b업드래프트§f: 수직 위 방향으로 강하게 솟구쳐 오릅니다. $[COOLDOWN_CONFIG]"
})
public class Mace extends AbilityBase implements ActiveHandler, Listener {

    public static final AbilitySettings.SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Mace.class, "COOLDOWN_CONFIG", 60,
            "# 업드래프트 쿨타임") {
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

    public Mace(Participant participant) {
        super(participant);
    }

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (getParticipant().hasEffect(Stun.registration)) return false;

        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
            if (cooldownTimer.isCooldown()) return false;

            getPlayer().setVelocity(new Vector(0, 1.3, 0));
            SoundLib.ENTITY_FIREWORK_ROCKET_LAUNCH.playSound(getPlayer().getLocation(), 1.0f, 0.8f);
            ParticleLib.CLOUD.spawnParticle(getPlayer().getLocation(), 0.5f, 0.1f, 0.5f, 15, 0.1);

            cooldownTimer.start();
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager().equals(getPlayer())) {
            Player attacker = getPlayer();

            if (event.getCause() == EntityDamageByEntityEvent.DamageCause.ENTITY_ATTACK) {
                if (attacker.getFallDistance() > 0.0F && !attacker.isOnGround() && !attacker.isInsideVehicle()) {
                    float fallBlocks = attacker.getFallDistance();
                    float extraDamage = fallBlocks * 2;

                    double originalDamage = event.getDamage();
                    event.setDamage(originalDamage + extraDamage);

                    if (fallBlocks >= 5.0F) {
                        SoundLib.ENTITY_ZOMBIE_ATTACK_IRON_DOOR.playSound(event.getEntity().getLocation(), 1.0f, 0.5f);
                        ParticleLib.EXPLOSION_LARGE.spawnParticle(event.getEntity().getLocation(), 0.2f, 0.2f, 0.2f, 1, 0.0);
                    } else {
                        SoundLib.BLOCK_ANVIL_PLACE.playSound(event.getEntity().getLocation(), 0.6f, 1.2f);
                    }

                    ParticleLib.CRIT.spawnParticle(event.getEntity().getLocation(), 0.3f, 0.5f, 0.3f, (int) Math.min(30, 5 + fallBlocks), 0.2);
                    attacker.setFallDistance(0.0F);
                }
            }
        }
    }
}