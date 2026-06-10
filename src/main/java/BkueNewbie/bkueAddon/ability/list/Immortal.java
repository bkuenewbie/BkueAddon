package BkueNewbie.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@AbilityManifest(name = "이모탈", rank = Rank.A, species = Species.OTHERS, explain = {
        "§7패시브 §8- §b토템§f: 게임이 시작되면 불사의 토템을 지급받습니다"
})
public class Immortal extends AbilityBase {

    public Immortal(Participant participant) {
        super(participant);
    }

    @Override protected void onUpdate(Update update) {
        if (update == Update.RESTRICTION_CLEAR) {
            ItemStack totem = new ItemStack(Material.TOTEM_OF_UNDYING, 1);
            getPlayer().getInventory().addItem(totem);
        }
    }
}