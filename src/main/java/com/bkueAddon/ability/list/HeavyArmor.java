package com.bkueAddon.ability.list;

import daybreak.abilitywar.ability.Tips.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.game.AbstractGame.Participant;

@AbilityManifest(name = "헤비 아머", rank = Rank.C, species = Species.HUMAN, explain = {
        "§7패시브 §8- §8강철의 중갑§f: 움직임이 둔해지지만 신체가 극도로 단단해집니다.",
        "- 상시 §c구속 1레벨§f과 §a저항 1레벨§f 효과를 획득합니다."
}, summarize = {
        "상시 적용되는 저항 버프를 통해 생존력을 높이는 대신, 약간의 이동 속도 감소를 감수해야 하는 패시브 방어형 능력입니다."
})
public class HeavyArmor extends AbilityBase {

    public HeavyArmor(Participant participant) {
        super(participant);
    }

    @Override
    protected void onUpdate(Update update) {
        if (update == Update.RESTRICTION_CLEAR) {
            heavyArmorLoop.start();
        }
    }

    private final AbilityTimer heavyArmorLoop = new AbilityTimer() {
        @Override
        public void run(int count) {
            getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0, false, false));
            getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100, 0, false, false));
        }
    }.setPeriod(TimeUnit.TICKS, 1).register();
}