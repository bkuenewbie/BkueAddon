package com.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.*;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AbilityManifest(name = "반사", rank = Rank.A, species = Species.HUMAN, explain = {
        "§7철괴 우클릭 §8- §b반사§f: 다음에 받는 피해를 무효화하고 저장합니다. $[COOLDOWN_CONFIG]",
        "- 저장한 피해는 다음 공격에 추가됩니다."
}, summarize = {
        "받은 피해를 저장하여 다음 공격에 추가합니다."
})
public class Reflect extends AbilityBase implements ActiveHandler {

    public Reflect(Participant participant) {
        super(participant);
    }

    public static final AbilitySettings.SettingObject<Integer> COOLDOWN_CONFIG =
            abilitySettings.new SettingObject<Integer>(Reflect.class, "cooldown", 25, "# 쿨타임") {
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
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK
                && !cooldownTimer.isCooldown() && !skill) {

            skill = true;

            SoundLib.BLOCK_BEACON_ACTIVATE.playSound(getPlayer(), 1, 1.5f);
            ParticleLib.CRIT_MAGIC.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 0.5, 0.8, 0.5, 15, 0.1);

            getPlayer().sendMessage("§b§l[반사] §f다음 공격을 흡수합니다!");
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

            SoundLib.ITEM_SHIELD_BLOCK.playSound(getPlayer(), 1, 1.2f);
            ParticleLib.CRIT_MAGIC.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 0.8, 0.8, 0.8, 25, 0.15);

            getPlayer().sendMessage("§a§l[반사] §f" + String.format("%.1f", damage) + " 피해를 저장했습니다!");
            return;
        }

        if (e.getDamager().equals(getPlayer()) && damage > 0) {
            e.setDamage(e.getDamage() + damage);

            SoundLib.ENTITY_PLAYER_ATTACK_CRIT.playSound(e.getEntity().getLocation(), 1, 0.8f);
            ParticleLib.CRIT_MAGIC.spawnParticle(e.getEntity().getLocation().add(0, 1, 0), 0.5, 0.5, 0.5, 15, 0.1);

            damage = 0;
        }
    }
}