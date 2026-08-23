package com.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

@AbilityManifest(name = "죽음의 유예", rank = Rank.B, species = Species.HUMAN, explain = {
        "§7패시브 §8- §c증오§f: 모든 공격의 대미지가 §c1.5배§f로 증가합니다.",
        "§7패시브 §8- §4유예 선고§f: 킬을 달성하지 못한 상태로 §c90초§f가 경과하면 즉시 §4즉사§f합니다. 처치 시 제한시간이 초기화됩니다."
}, summarize = {
        "§7공격력 강화§f: 1.5배의 피해를 입히지만, 90초마다 적을 처치하지 못하면 즉사합니다."
})
public class DeathDeferment extends AbilityBase {

    private final DefermentTimer defermentTimer = new DefermentTimer();

    public DeathDeferment(Participant participant) {
        super(participant);
    }

    private class DefermentTimer extends AbilityTimer {
        private int remainingTime;
        private final BossBar bossBar = Bukkit.createBossBar("§4[죽음의 유예]", BarColor.RED, BarStyle.SOLID);

        public DefermentTimer() {
            super(TaskType.NORMAL, Integer.MAX_VALUE);
            setPeriod(TimeUnit.SECONDS, 1);
            setBehavior(RestrictionBehavior.PAUSE_RESUME);
        }

        @Override
        protected void onStart() {
            bossBar.addPlayer(getPlayer());
            resetTime();
        }

        @Override
        protected void run(int count) {
            remainingTime--;

            bossBar.setProgress(Math.max(0, Math.min(1, remainingTime / 90.0)));
            bossBar.setTitle("§4[죽음의 유예] §c즉사까지 §e" + remainingTime + "초 §c남음");

            if (remainingTime <= 10) {
                SoundLib.BLOCK_NOTE_BLOCK_BASS.playSound(getPlayer(), 1, 0.8f);
                ParticleLib.REDSTONE.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 0.4f, 0.6f, 0.4f, 8, 0);
            }

            if (remainingTime <= 0) {
                ParticleLib.EXPLOSION_HUGE.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 0, 0, 0, 1, 0);
                SoundLib.ENTITY_LIGHTNING_BOLT_THUNDER.playSound(getPlayer().getLocation(), 1, 0.8f);
                getPlayer().setHealth(0);
                stop(false);
            }
        }

        @Override
        protected void onEnd() {
            bossBar.removeAll();
        }

        @Override
        protected void onSilentEnd() {
            bossBar.removeAll();
        }

        public void resetTime() {
            remainingTime = 90;
            bossBar.setProgress(1);
        }
    }

    @Override
    protected void onUpdate(Update update) {
        if (update == Update.RESTRICTION_CLEAR) {
            defermentTimer.start();
        } else if (update == Update.ABILITY_DESTROY) {
            defermentTimer.stop(false);
        }
    }

    @SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager().equals(getPlayer()) && event.getEntity() instanceof LivingEntity) {
            event.setDamage(event.getDamage() * 1.5);
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (getPlayer().equals(event.getEntity().getKiller())) {
            defermentTimer.resetTime();
            SoundLib.ENTITY_ZOMBIE_VILLAGER_CURE.playSound(getPlayer(), 1, 1.2f);
            ParticleLib.VILLAGER_HAPPY.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 15, 0.1);
        }
    }
}