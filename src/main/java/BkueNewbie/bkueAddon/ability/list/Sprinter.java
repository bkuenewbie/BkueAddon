package BkueNewbie.bkueAddon.ability.list;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.game.AbstractGame.Participant;

@AbilityManifest(name = "스프린터", rank = Rank.C, species = Species.HUMAN, explain = {
        "§7패시브 §8- §b한계 질주§f: 지속해서 지면을 달릴 때 가속도를 얻습니다.",
        "- 달리기(Sprinting) 상태를 유지하는 동안 §b신속 2레벨§f 효과를 지속해서 얻습니다."
})
public class Sprinter extends AbilityBase {

    public Sprinter(Participant participant) {
        super(participant);
    }

    @Override
    protected void onUpdate(Update update) {
        if (update == Update.RESTRICTION_CLEAR) {
            sprinterLoop.start();
        }
    }

    private final AbilityTimer sprinterLoop = new AbilityTimer() {
        @Override
        public void run(int count) {
            if (getPlayer().isSprinting()) {
                getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 1, false, false));

                if (count % 3 == 0) {
                    ParticleLib.CLOUD.spawnParticle(getPlayer().getLocation(), 0.1f, 0.0f, 0.1f, 1, 0.0);
                }
            }
        }
    }.setPeriod(TimeUnit.TICKS, 1).register();
}