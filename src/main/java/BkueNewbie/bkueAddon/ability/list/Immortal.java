package BkueNewbie.bkueAddon.ability.list;

import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;

@AbilityManifest(name = "이모탈", rank = Rank.B, species = Species.HUMAN, explain = {
        "§7패시브 §8- §e불사의 각성§f: 게임 중 단 1회, 사망에 이르는 치명적인 피해를 입으면",
        "- 해당 피해를 무효화하고 체력을 1(반 칸)로 고정합니다.",
        "- 이후 4초간 완전 무적 상태가 되며 재생 5레벨과 속도 4레벨을 획득합니다."
})
public class Immortal extends AbilityBase {

    public Immortal(Participant participant) {
        super(participant);
    }

    private boolean hasTriggered = false;
    private boolean isInvulnerable = false;

    @SubscribeEvent
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity().equals(getPlayer()) && !e.isCancelled()) {
            if (isInvulnerable) {
                e.setCancelled(true);
                return;
            }

            if (!hasTriggered && getPlayer().getHealth() - e.getFinalDamage() <= 0) {
                e.setCancelled(true);
                hasTriggered = true;
                isInvulnerable = true;

                getPlayer().setHealth(1.0);
                getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, 4));
                getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 3));

                getPlayer().sendMessage("§e[이모탈] 치명상을 버텨내고 불사조의 권능으로 각성합니다! (4초간 무적)");
                SoundLib.ENTITY_WITHER_SPAWN.playSound(getPlayer().getLocation(), 1.0f, 1.2f);
                ParticleLib.FLAME.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 30, 0.1);

                new AbilityTimer(80) {
                    @Override
                    public void onEnd() {
                        isInvulnerable = false;
                        getPlayer().sendMessage("§7[이모탈] 불사의 각성 상태가 종료되었습니다.");
                    }
                }.setPeriod(TimeUnit.TICKS, 1).register();
            }
        }
    }
}