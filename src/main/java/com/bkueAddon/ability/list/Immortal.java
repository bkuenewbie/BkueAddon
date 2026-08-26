package com.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.*;
import daybreak.abilitywar.ability.Materials;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@AbilityManifest(name = "이모탈", rank = Rank.A, species = Species.OTHERS, explain = {
        "§7패시브 §8- §b불멸§f: 게임 시작 시 §e불사의 토템§f을 지급받습니다."
}, summarize = {
        "불사의 토템을 지급받습니다."
})
public class Immortal extends AbilityBase {
    public Immortal(Participant participant) {
        super(participant);
    }

    @Override
    protected void onUpdate(Update update) {
        if (update == Update.RESTRICTION_CLEAR) {
            getPlayer().getInventory().addItem(new ItemStack(Material.TOTEM_OF_UNDYING));
        }
    }
}