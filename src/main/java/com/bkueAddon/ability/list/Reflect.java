package com.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.*;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AbilityManifest(name = "반사", rank = Rank.A, species = Species.HUMAN, explain = {
        "§7철괴 우클릭 §8- §b반사§f: 다음에 받는 피해를 무효화하고 저장합니다. $[COOLDOWN_CONFIG]",
        "저장한 피해는 다음 공격에 추가됩니다."
}, summarize = {
        "다음에 받는 피해를 저장하여 다음 공격에 추가합니다."
})
public class Reflect extends AbilityBase implements ActiveHandler {
    public Reflect(Participant participant) {
        super(participant);
    }

    public static final AbilitySettings.SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Reflect.class, "cooldown", 30,
            "# 쿨타임") {

        @Override
        public boolean condition(Integer value) {
            return value >= 0;
        }

        @Override
        public String toString() {
            return Formatter.formatCooldown(getValue());
        }
    };

    private boolean skill;
    private double damage;

    private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldownTimer.isCooldown() && !skill) {
            skill = true;
            SoundLib.BLOCK_ANVIL_PLACE.playSound(getPlayer());
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (skill && e.getEntity().equals(getPlayer())) {
            damage = e.getFinalDamage();
            skill = false;
            cooldownTimer.start();
            e.setCancelled(true);
            return;
        }

        if (e.getDamager().equals(getPlayer()) && damage > 0) {
            e.setDamage(e.getDamage() + damage);
            damage = 0;
        }
    }
}
