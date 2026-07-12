package com.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.*;
import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

@AbilityManifest(name = "야구선수", rank = Rank.A, species = Species.HUMAN, explain = {
        "§7패시브 §8- §6홈런 배트§f: 게임 시작 시 밀치기 V가 부여된 막대를 지급받습니다.",
        "§e아이디어 §b제공: goodhyojun"
}, summarize = {
        "밀치기 V 막대를 가지고 시작합니다."
})
public class BaseballPlayer extends AbilityBase {
    public BaseballPlayer(Participant participant) {
        super(participant);
    }

    @Override
    protected void onUpdate(Update update) {
        if (update == Update.RESTRICTION_CLEAR) {
            ItemStack knockbackStick = new ItemStack(Material.STICK, 1);
            knockbackStick.addUnsafeEnchantment(Enchantment.KNOCKBACK, 5);
            getPlayer().getInventory().addItem(knockbackStick);
        }
    }
}
