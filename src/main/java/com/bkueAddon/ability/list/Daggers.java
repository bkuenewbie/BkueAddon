package com.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

@AbilityManifest(name = "단검", rank = AbilityManifest.Rank.A, species = AbilityManifest.Species.HUMAN, explain = {
        "§7패시브 §8- §b단검§f: 매우 빠르게 공격할 수 있는 §b강화 다이아몬드 단검§f을 지급받습니다.",
        "- §b날카로움 IV / 휩쓸기 III / 발화 I / 내구성 III"
}, summarize = {
        "빠른 공격 속도와 강력한 인챈트를 가진 단검을 사용합니다."
})
public class Daggers extends AbilityBase {

    public Daggers(Participant participant) {
        super(participant);
    }

    @Override
    protected void onUpdate(Update update) {
        if (update == Update.RESTRICTION_CLEAR) {
            ItemStack dagger = new ItemStack(Material.DIAMOND_SWORD);
            dagger.addUnsafeEnchantment(Enchantment.SWEEPING_EDGE, 3);
            dagger.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 4);
            dagger.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
            dagger.addUnsafeEnchantment(Enchantment.DURABILITY, 3);

            ItemMeta meta = dagger.getItemMeta();
            if (meta == null) return;

            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(), "dagger_speed", 3.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
            meta.setDisplayName("§b§l단검");
            dagger.setItemMeta(meta);

            getPlayer().getInventory().addItem(dagger);

            SoundLib.ENTITY_PLAYER_ATTACK_STRONG.playSound(getPlayer(), 1, 1.5f);
            ParticleLib.CRIT_MAGIC.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 0.3, 0.5, 0.3, 8, 0.1);
        }
    }
}