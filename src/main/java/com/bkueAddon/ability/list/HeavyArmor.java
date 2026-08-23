package com.bkueAddon.ability.list;

import daybreak.abilitywar.ability.Tips.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.*;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;

@AbilityManifest(name = "헤비 아머", rank = Rank.C, species = Species.HUMAN, explain = {
        "§7패시브 §8- §8강철의 중갑§f: 움직임이 둔해지지만 신체가 극도로 단단해집니다.",
        "- 상시 §c구속 1레벨§f과 §a저항 2레벨§f 효과를 획득합니다.",
}, summarize = {
        "이동 속도를 희생하여 강력한 방어력을 얻습니다."
})
public class HeavyArmor extends AbilityBase {

    public HeavyArmor(Participant participant) {
        super(participant);
    }

    @Override
    protected void onUpdate(Update update) {
        if (update == Update.RESTRICTION_CLEAR) {
            heavyArmorLoop.start();
            SoundLib.ITEM_ARMOR_EQUIP_IRON.playSound(getPlayer(), 1, 0.7f);
            ParticleLib.CRIT_MAGIC.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 0.5, 0.8, 0.5, 15, 0.05);
        }
    }

    private final AbilityTimer heavyArmorLoop = new AbilityTimer() {
        @Override
        public void run(int count) {
            getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1, false, false));
            getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 0, false, false));
        }
    }.setPeriod(TimeUnit.TICKS, 20).register();
}