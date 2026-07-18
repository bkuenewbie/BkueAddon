package com.bkueAddon.ability.list;

import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.Tips;
import daybreak.abilitywar.ability.Tips.Level;
import daybreak.abilitywar.ability.Tips.Stats;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;

@AbilityManifest(name = "바운드 점퍼", rank = Rank.A, species = Species.HUMAN, explain = {
        "§7철괴 우클릭 §8- §b버티컬 도약§f: 바라보는 방향 상공으로 강하게 회전 점프합니다. $[ACTIVE_COOLDOWN]",
        "§7패시브 §8- §e에어 바운드§f: 공중에 떠 있는 상태에서 웅크리기(Shift)를 누르면 공중에서 한 번 더 고도 도약을 실행합니다. (도약마다 개별 쿨타임 적용) $[PASSIVE_COOLDOWN]",
        "- 상시 효과로 상공 지배력 조율을 얻어 낙하 피해를 일절 받지 않습니다."
}, summarize = {
        "§7철괴 우클릭§f: 전방 상공으로 도약하며, 공중에서 웅크리기를 통해 2단 점프를 수행합니다. 낙하 피해 면역 상태를 가집니다."
})
public class BoundJumper extends AbilityBase implements ActiveHandler {

    public BoundJumper(Participant participant) {
        super(participant);
    }

    public static final SettingObject<Integer> ACTIVE_COOLDOWN = abilitySettings.new SettingObject<Integer>(
            BoundJumper.class, "active-cooldown", 10, "# 버티컬 도약 쿨타임") {
        @Override
        public boolean condition(Integer value) { return value >= 0; }
        @Override
        public String toString() { return Formatter.formatCooldown(getValue()); }
    };

    public static final SettingObject<Integer> PASSIVE_COOLDOWN = abilitySettings.new SettingObject<Integer>(
            BoundJumper.class, "passive-cooldown", 7, "# 에어 바운드(2단 점프) 쿨타임") {
        @Override
        public boolean condition(Integer value) { return value >= 0; }
        @Override
        public String toString() { return Formatter.formatCooldown(getValue()); }
    };

    private final Cooldown activeCooldown = new Cooldown(ACTIVE_COOLDOWN.getValue());
    private final Cooldown passiveCooldown = new Cooldown(PASSIVE_COOLDOWN.getValue());

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
            if (activeCooldown.isCooldown()) return false;

            Vector velocity = getPlayer().getLocation().getDirection().multiply(1.2).setY(1.1);
            getPlayer().setVelocity(velocity);

            ParticleLib.CLOUD.spawnParticle(getPlayer().getLocation(), 0.3f, 0.1f, 0.3f, 8, 0.1);
            SoundLib.ENTITY_BAT_TAKEOFF.playSound(getPlayer().getLocation(), 1.0f, 0.8f);

            activeCooldown.start();
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public void onPlayerSneak(PlayerToggleSneakEvent e) {
        if (e.getPlayer().equals(getPlayer()) && e.isSneaking()) {
            if (getPlayer().isOnGround()) return;
            if (passiveCooldown.isCooldown()) return;

            Vector doubleJump = getPlayer().getVelocity().setY(0.85).multiply(1.1);
            getPlayer().setVelocity(doubleJump);

            ParticleLib.FIREWORKS_SPARK.spawnParticle(getPlayer().getLocation().add(0, -0.2, 0), 0.2f, 0.1f, 0.2f, 6, 0.05);
            SoundLib.ENTITY_HORSE_JUMP.playSound(getPlayer().getLocation(), 1.0f, 1.4f);

            passiveCooldown.start();
        }
    }

    @SubscribeEvent
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity().equals(getPlayer()) && e.getCause() == DamageCause.FALL) {
            e.setCancelled(true);
        }
    }
}