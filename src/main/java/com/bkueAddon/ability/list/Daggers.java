package com.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

@AbilityManifest(name = "단검", rank = AbilityManifest.Rank.A, species = AbilityManifest.Species.HUMAN, explain = {
        "§7패시브 §8- §b단검§f: 공격 속도가 빠른 §b다이아몬드 검(휩쓸기, 날카로움 3)§f을 지급받습니다.",
}, summarize = {
        "강력한 단검을 사용합니다."
})
public class Daggers extends AbilityBase {

    public Daggers(Participant participant) {
        super(participant);
    }

    @Override
    protected void onUpdate(Update update) {
        if (update == Update.RESTRICTION_CLEAR) {
            ItemStack dagger = new ItemStack(Material.DIAMOND_SWORD);

            dagger.addEnchantment(Enchantment .SWEEPING_EDGE, 3);
            dagger.addEnchantment(Enchantment.DAMAGE_ALL, 3);

            ItemMeta meta = dagger.getItemMeta();

            if (meta == null) {return;}

            AttributeModifier speed = new AttributeModifier(UUID.randomUUID(), "dagger_speed", +2.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, speed);
            meta.setDisplayName("단검");

            dagger.setItemMeta(meta);

          getPlayer().getInventory().addItem(dagger);
        }
    }
}
