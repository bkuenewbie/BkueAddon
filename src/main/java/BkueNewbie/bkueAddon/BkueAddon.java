package BkueNewbie.bkueAddon;

import BkueNewbie.bkueAddon.ability.list.*;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.addon.Addon;
import daybreak.abilitywar.game.manager.AbilityList;
import org.bukkit.event.Listener;

public final class BkueAddon extends Addon implements Listener {

    @Override
    public void onEnable() {
        // Plugin enable logic

        System.out.println("§b블루 §9애드온§e이 적용되었습니다.");

        AbilityFactory.registerAbility(Archangel.class);
        AbilityList.registerAbility(Archangel.class);

        AbilityFactory.registerAbility(BountyHunter.class);
        AbilityList.registerAbility(BountyHunter.class);

        AbilityFactory.registerAbility(Blue.class);
        AbilityList.registerAbility(Blue.class);

        AbilityFactory.registerAbility(DeathDeferment.class);
        AbilityList.registerAbility(DeathDeferment.class);

        AbilityFactory.registerAbility(CursedOne.class);
        AbilityList.registerAbility(CursedOne.class);

        AbilityFactory.registerAbility(Trade.class);
        AbilityList.registerAbility(Trade.class);

        AbilityFactory.registerAbility(Genius.class);
        AbilityList.registerAbility(Genius.class);

        AbilityFactory.registerAbility(EnchantRich.class);
        AbilityList.registerAbility(EnchantRich.class);

        AbilityFactory.registerAbility(Dud.class);
        AbilityList.registerAbility(Dud.class);

        AbilityFactory.registerAbility(SevenThree.class);
        AbilityList.registerAbility(SevenThree.class);

        AbilityFactory.registerAbility(Mom.class);
        AbilityList.registerAbility(Mom.class);

        AbilityFactory.registerAbility(Geometry.class);
        AbilityList.registerAbility(Geometry.class);

        AbilityFactory.registerAbility(Hellfire.class);
        AbilityList.registerAbility(Hellfire.class);

        AbilityFactory.registerAbility(PhantomReaper.class);
        AbilityList.registerAbility(PhantomReaper.class);

        AbilityFactory.registerAbility(BloodLoop.class);
        AbilityList.registerAbility(BloodLoop.class);

        AbilityFactory.registerAbility(PoisonGarden.class);
        AbilityList.registerAbility(PoisonGarden.class);

        AbilityFactory.registerAbility(BoundJumper.class);
        AbilityList.registerAbility(BoundJumper.class);

        AbilityFactory.registerAbility(BladeCircle.class);
        AbilityList.registerAbility(BladeCircle.class);

        AbilityFactory.registerAbility(SecondWind.class);
        AbilityList.registerAbility(SecondWind.class);

        AbilityFactory.registerAbility(HeavyArmor.class);
        AbilityList.registerAbility(HeavyArmor.class);

        AbilityFactory.registerAbility(Sprinter.class);
        AbilityList.registerAbility(Sprinter.class);

        AbilityFactory.registerAbility(Sharpness.class);
        AbilityList.registerAbility(Sharpness.class);


        //1.0.1
        AbilityFactory.registerAbility(Immortal.class);
        AbilityList.registerAbility(Immortal.class);

        AbilityFactory.registerAbility(PositionMaster.class);
        AbilityList.registerAbility(PositionMaster.class);

        AbilityFactory.registerAbility(Mace.class);
        AbilityList.registerAbility(Mace.class);

        AbilityFactory.registerAbility(Seesaw.class);
        AbilityList.registerAbility(Seesaw.class);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
