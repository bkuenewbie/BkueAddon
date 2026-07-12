package com.bkueAddon;

import com.bkueAddon.ability.list.*;
import com.bkueAddon.ability.list.*;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.addon.Addon;
import daybreak.abilitywar.game.Category;
import daybreak.abilitywar.game.event.GameCreditEvent;
import daybreak.abilitywar.game.manager.AbilityList;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BkueAddon extends Addon implements Listener {

    Class<?>[] abilities = {
            Blue.class, Dud.class, HeavyArmor.class, Immortal.class, Sharpness.class,
            BaseballPlayer.class, Dodge.class, Reverse.class, Samurai.class, Ddobear.class
    };

    @Override
    public void onEnable() {
        System.out.println("§b블루 §9애드온§e이 적용되었습니다.");

        registerAllAbilities();

        Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());

        Bukkit.broadcastMessage("§b블루 §9애드온§e이 적용되었습니다.");
        Bukkit.broadcastMessage("§e능력 §f" + abilities.length + "개 적용 완료.");
    }

    private void registerAllAbilities() {

        for (Class<?> clazz : abilities) {
            AbilityFactory.registerAbility((Class<? extends daybreak.abilitywar.ability.AbilityBase>) clazz);
            AbilityList.registerAbility((Class<? extends daybreak.abilitywar.ability.AbilityBase>) clazz);
        }
    }

    @EventHandler
    public void onGameCredit(GameCreditEvent e) {
        if (e.getGame().getRegistration().getCategory().equals(Category.GameCategory.GAME)) {
            e.addCredit("§b블루 §9애드온 §f적용중. 총 §b" + abilities.length + "개§f의 능력이 추가되었습니다.");
            e.addCredit("§b블루 §9애드온 §f제작자 : BkueNewbie [§7디스코드 §f: bkuenewbie]");
        }
    }

    @Override
    public void onDisable() {
        // 플러그인 종료 시 로직
    }
}