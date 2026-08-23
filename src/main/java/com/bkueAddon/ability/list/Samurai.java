package com.bkueAddon.ability.list;

import com.bkueAddon.util.LocationUtilEx;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.*;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Rooted;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@AbilityManifest(name = "사무라이", rank = Rank.S, species = Species.HUMAN, explain = {
        "§7철괴 우클릭 §8- §c섬광베기§f: 주변 $[RANGE_CONFIG]칸 내 적들에게 순식간에 이동하며 3초간 속박합니다. $[COOLDOWN_CONFIG]"
}, summarize = {
        "주변 적들을 순식간에 베며 속박합니다."
})
public class Samurai extends AbilityBase implements ActiveHandler {

    public Samurai(Participant participant) {
        super(participant);
    }

    public static final AbilitySettings.SettingObject<Integer> COOLDOWN_CONFIG =
            abilitySettings.new SettingObject<Integer>(Samurai.class, "cooldown", 60, "# 쿨타임") {
                @Override
                public boolean condition(Integer value) {
                    return value >= 0;
                }

                @Override
                public String toString() {
                    return Formatter.formatCooldown(getValue());
                }
            };

    public static final AbilitySettings.SettingObject<Integer> RANGE_CONFIG =
            abilitySettings.new SettingObject<Integer>(Samurai.class, "range", 10, "# 스킬 범위") {
                @Override
                public boolean condition(Integer value) {
                    return value >= 1 && value <= 50;
                }
            };

    private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());

    private Location originalLocation;
    private List<Player> targets;
    private int index;

    private final AbilityTimer countdown = new AbilityTimer() {

        @Override
        protected void onStart() {
            originalLocation = getPlayer().getLocation().clone();
            targets = new ArrayList<>(LocationUtilEx.getNearbyEnemies(getPlayer(), RANGE_CONFIG.getValue()));

            if (targets.isEmpty()) {
                stop(true);
                return;
            }

            index = 0;
            SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer(), 1, 1.5f);
        }

        @Override
        protected void run(int count) {
            if (index >= targets.size()) {
                stop(false);
                return;
            }

            Player target = targets.get(index++);

            if (!target.isValid() || target.isDead()) return;

            Location targetLoc = target.getLocation().clone();

            getPlayer().teleport(targetLoc);

            ParticleLib.SWEEP_ATTACK.spawnParticle(targetLoc.add(0, 1, 0), 0, 0, 0, 1, 0);
            ParticleLib.CRIT.spawnParticle(targetLoc.add(0, 0.5, 0), 0.4, 0.6, 0.4, 8, 0.1);
            SoundLib.ENTITY_PLAYER_ATTACK_CRIT.playSound(targetLoc, 1, 1.2f);

            target.damage(6, getPlayer());
            Rooted.apply(getGame().getParticipant(target), TimeUnit.SECONDS, 3);
        }

        @Override
        protected void onEnd() {
            getPlayer().teleport(originalLocation);
            ParticleLib.CLOUD.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 0.5, 0.5, 0.5, 10, 0.05);
        }

        @Override
        protected void onSilentEnd() {
            getPlayer().teleport(originalLocation);
        }

    }.setPeriod(TimeUnit.TICKS, 2);

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK
                && !cooldownTimer.isCooldown() && !countdown.isRunning()) {

            countdown.start();
            cooldownTimer.start();

            SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer(), 1, 0.7f);
            ParticleLib.CLOUD.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 0.5, 0.5, 0.5, 12, 0.1);

            return true;
        }

        return false;
    }
}