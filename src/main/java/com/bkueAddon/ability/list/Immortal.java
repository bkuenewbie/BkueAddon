package com.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.*;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@AbilityManifest(name = "이모탈", rank = Rank.A, species = Species.OTHERS, explain = {
        "§7패시브 §8- §b불멸§f: 치명적인 피해를 받으면 한 번 죽음을 무시하고 부활합니다.",
        "- 부활 후 §a재생 II§f와 §b흡수 II§f를 얻습니다."
}, summarize = {
        "한 번 죽음을 무시하고 부활합니다."
})
public class Immortal extends AbilityBase {

    private boolean revived = false;

    public Immortal(Participant participant) {
        super(participant);
    }

    @SubscribeEvent
    public void onDamage(EntityDamageEvent e) {
        if (!e.getEntity().equals(getPlayer()) || revived) return;

        if (e.getFinalDamage() >= getPlayer().getHealth()) {
            revived = true;
            e.setCancelled(true);

            getPlayer().setHealth(Math.min(10.0, getPlayer().getMaxHealth()));
            getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
            getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1));

            SoundLib.ITEM_TOTEM_USE.playSound(getPlayer(), 1, 1);
            ParticleLib.TOTEM.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 0.5, 0.8, 0.5, 30, 0.1);

            getPlayer().sendMessage("§a§l[이모탈] §f죽음을 극복했습니다!");
        }
    }
}