package BkueNewbie.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@AbilityManifest(name = "인챈트 부자", rank = Rank.L, species = Species.HUMAN, explain = {
        "§7패시브 §8- §e황금의 손길§f: 게임 시작 시 가지고 있는 아이템을 포함하여, 인벤토리에 들어오는 모든 장비와 무기에 자동으로 최고의 인챈트가 부여됩니다.",
        "- §b[보호 IV / 내구성 III / 날카로움 V / 발화 II]",
        "§7아이디어 제공 §8- §6goodhyojun"
})
public class EnchantRich extends AbilityBase {

    public EnchantRich(Participant participant) {
        super(participant);
    }

    private void applyEnchants(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;

        Material type = item.getType();
        boolean updated = false;

        boolean isWeapon = type.name().endsWith("SWORD") || type.name().endsWith("AXE");
        boolean isArmor = type.name().endsWith("HELMET") || type.name().endsWith("CHESTPLATE") || type.name().endsWith("LEGGINGS") || type.name().endsWith("BOOTS");

        if (isWeapon || isArmor || type == Material.BOW || type == Material.CROSSBOW || type == Material.TRIDENT || type == Material.SHIELD || type.name().endsWith("PICKAXE") || type.name().endsWith("SHOVEL") || type.name().endsWith("HOE")) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (isArmor) {
                    if (!meta.hasEnchant(Enchantment.PROTECTION_ENVIRONMENTAL) || meta.getEnchantLevel(Enchantment.PROTECTION_ENVIRONMENTAL) < 4) {
                        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4, true);
                        updated = true;
                    }
                }
                if (isWeapon) {
                    if (!meta.hasEnchant(Enchantment.DAMAGE_ALL) || meta.getEnchantLevel(Enchantment.DAMAGE_ALL) < 5) {
                        meta.addEnchant(Enchantment.DAMAGE_ALL, 5, true);
                        updated = true;
                    }
                    if (!meta.hasEnchant(Enchantment.FIRE_ASPECT) || meta.getEnchantLevel(Enchantment.FIRE_ASPECT) < 2) {
                        meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
                        updated = true;
                    }
                }
                if (!meta.hasEnchant(Enchantment.DURABILITY) || meta.getEnchantLevel(Enchantment.DURABILITY) < 3) {
                    meta.addEnchant(Enchantment.DURABILITY, 3, true);
                    updated = true;
                }

                if (updated) {
                    item.setItemMeta(meta);
                }
            }
        }
    }

    private void enchantAllInventory() {
        Player player = getPlayer();
        if (player == null) return;

        for (ItemStack item : player.getInventory().getContents()) {
            applyEnchants(item);
        }
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            applyEnchants(armor);
        }
        applyEnchants(player.getInventory().getItemInOffHand());
    }

    @Override
    protected void onUpdate(Update update) {
        super.onUpdate(update);
        if (update == Update.RESTRICTION_CLEAR) {
            enchantAllInventory();
            SoundLib.BLOCK_ENCHANTMENT_TABLE_USE.playSound(getPlayer().getLocation(), 1.0f, 1.0f);
            getPlayer().sendMessage("§e[인챈트 부자] §f소지 중인 모든 장비에 축복이 깃들었습니다.");
        }
    }

    @SubscribeEvent
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity().equals(getPlayer())) {
            ItemStack item = event.getItem().getItemStack();
            applyEnchants(item);
        }
    }

    @SubscribeEvent
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked().equals(getPlayer())) {
            if (event.getCurrentItem() != null) {
                applyEnchants(event.getCurrentItem());
            }
            if (event.getCursor() != null) {
                applyEnchants(event.getCursor());
            }
        }
    }
}