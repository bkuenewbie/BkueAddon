package BkueNewbie.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.concurrent.ThreadLocalRandom;

@AbilityManifest(name = "엄마", rank = Rank.L, species = Species.HUMAN, explain = {
        "§7패시브 §8- §c등짝 스매싱§f: 타격 시 30%의 확률로 엄청난 분노를 담아, 대상에게 고정 대미지 §46.0§f을 추가로 입힙니다.",
        "§7아이디어 제공 §8- §6ohsb0703"
})
public class Mom extends AbilityBase {

    public Mom(Participant participant) {
        super(participant);
    }

    @daybreak.abilitywar.ability.SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager().equals(getPlayer()) && event.getEntity() instanceof LivingEntity) {
            LivingEntity victim = (LivingEntity) event.getEntity();

            if (ThreadLocalRandom.current().nextInt(100) < 30) {
                event.setDamage(event.getDamage() + 6.0);

                getPlayer().sendMessage("§c[엄마] §4찰진 등짝 스매싱!§e (+6 대미지)");
                if (victim instanceof org.bukkit.entity.Player) {
                    ((org.bukkit.entity.Player) victim).sendMessage("§4[엄마] §c등짝을 정통으로 맞아 뼈가 울립니다!");
                }

                SoundLib.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR.playSound(victim.getLocation(), 1.2f, 0.6f);
                SoundLib.ENTITY_PLAYER_HURT.playSound(victim.getLocation(), 1.0f, 0.5f);
                ParticleLib.VILLAGER_ANGRY.spawnParticle(victim.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 10, 0.1);
            }
        }
    }
}