package com.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@AbilityManifest(name = "인챈트 부자", rank = Rank.L, species = Species.HUMAN, explain = {
        "§7패시브 §8- §e황금의 손길§f: 게임 시작 시 가지고 있는 아이템을 포함하여, 인벤토리에 들어오는 모든 장비와 무기에 자동으로 최고의 인챈트가 부여됩니다.",
        "- §b[보호 IV / 내구성 III / 날카로움 V / 발화 II]",
        "§7아이디어 제공 §8- §6goodhyojun"
}, summarize = {
        "획득하는 모든 장비와 무기에 자동으로 최고 수준의 인챈트를 부여하는 패시브 능력입니다."
})
public class EnchantRich extends AbilityBase {

    public EnchantRich(Participant participant) {
        super(participant);
    }

    private boolean applyEnchants(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;

        Material type = item.getType();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        boolean updated = false;

        if (type.name().endsWith("_HELMET") || type.name().endsWith("_CHESTPLATE") ||
                type.name().endsWith("_LEGGINGS") || type.name().endsWith("_BOOTS")) {
            if (!meta.hasEnchant(Enchantment.PROTECTION_ENVIRONMENTAL)) {
                meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4, true);
                updated = true;
            }
        }

        if (type.name().endsWith("_SWORD") || type.name().endsWith("_AXE")) {
            if (!meta.hasEnchant(Enchantment.DAMAGE_ALL)) {
                meta.addEnchant(Enchantment.DAMAGE_ALL, 5, true);
                updated = true;
            }
            if (!meta.hasEnchant(Enchantment.FIRE_ASPECT)) {
                meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
                updated = true;
            }
        }

        if (type.getMaxDurability() > 0 && !meta.hasEnchant(Enchantment.DURABILITY)) {
            meta.addEnchant(Enchantment.DURABILITY, 3, true);
            updated = true;
        }

        if (updated) {
            item.setItemMeta(meta);
        }
        return updated;
    }

    @Override
    protected void onUpdate(Update update) {
        if (update == Update.RESTRICTION_CLEAR) {
            for (ItemStack item : getPlayer().getInventory().getContents()) {
                applyEnchants(item);
            }
            SoundLib.BLOCK_ENCHANTMENT_TABLE_USE.playSound(getPlayer().getLocation(), 1.0f, 1.0f);
        }
    }

    @SubscribeEvent
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity().equals(getPlayer())) {
            applyEnchants(event.getItem().getItemStack());
        }
    }

    @SubscribeEvent
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked().equals(getPlayer())) {
            if (applyEnchants(event.getCurrentItem()) || applyEnchants(event.getCursor())) {
                ((Player) event.getWhoClicked()).updateInventory();
            }
        }
    }

    @SubscribeEvent
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer().equals(getPlayer())) {
            for (ItemStack item : event.getInventory().getContents()) {
                applyEnchants(item);
            }
        }
    }
}