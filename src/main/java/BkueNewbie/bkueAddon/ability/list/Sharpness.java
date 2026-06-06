package BkueNewbie.bkueAddon.ability.list;

import java.util.Random;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;

@AbilityManifest(name = "샤프니스", rank = Rank.C, species = Species.HUMAN, explain = {
        "§7패시브 §8- §e예리한 칼날§f: 적을 기본 타격 시 검끝이 날카롭게 빛납니다.",
        "- 30%의 확률로 기본 대미지에 §c3의 추가 피해§f를 입히고 파티클이 발생합니다."
})
public class Sharpness extends AbilityBase {

    public Sharpness(Participant participant) {
        super(participant);
    }

    private final Random random = new Random();

    @SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager().equals(getPlayer()) && e.getEntity() instanceof LivingEntity) {
            if (random.nextInt(100) < 30) {
                e.setDamage(e.getDamage() + 3.0);

                LivingEntity victim = (LivingEntity) e.getEntity();
                SoundLib.ENTITY_PLAYER_ATTACK_CRIT.playSound(victim.getLocation(), 0.8f, 1.3f);
                ParticleLib.CRIT.spawnParticle(victim.getLocation().add(0, 1, 0), 0.2f, 0.4f, 0.2f, 7, 0.1);
            }
        }
    }
}