package BkueNewbie.bkueAddon.ability.list;

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
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

@AbilityManifest(name = "죽음의 유예", rank = Rank.B, species = Species.HUMAN, explain = {
        "§7패시브 §8- §c증오§f: 모든 공격의 대미지가 §c1.5배§f로 증가합니다.",
        "§7패시브 §8- §4유예 선고§f: 킬을 달성하지 못한 상태로 §c1분 30초(90초)§f가 경과하면 저주에 잠식되어 그 즉시 §4즉사§f합니다. 처치 시 제한시간이 초기화됩니다.",
})
public class DeathDeferment extends AbilityBase {

    private final DefermentTimer defermentTimer = new DefermentTimer();

    public DeathDeferment(Participant participant) {
        super(participant);
    }

    private class DefermentTimer extends AbilityTimer {
        private int remainingTime;
        private final BossBar bossBar = Bukkit.createBossBar("§4[죽음의 유예] §c즉사 대기 시간", BarColor.RED, BarStyle.SOLID);

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

            double progress = (double) remainingTime / 90.0;
            if (progress >= 0.0 && progress <= 1.0) {
                bossBar.setProgress(progress);
            }
            bossBar.setTitle("§4[죽음의 유예] §c즉사까지 §e" + remainingTime + "초§c 남음");

            if (remainingTime == 30 || remainingTime == 10 || (remainingTime <= 5 && remainingTime > 0)) {
                getPlayer().sendMessage("§4[죽음의 유예] §c즉사까지 §e" + remainingTime + "초§c 남았습니다!");
                SoundLib.BLOCK_NOTE_BLOCK_PLING.playSound(getPlayer().getLocation(), 1.0f, 0.5f);
            }

            if (remainingTime <= 0) {
                bossBar.removeAll();
                getPlayer().damage(1000.0);
                SoundLib.ENTITY_LIGHTNING_BOLT_THUNDER.playSound(getPlayer().getLocation(), 1.0f, 0.8f);
                ParticleLib.EXPLOSION_HUGE.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 0.0f, 0.0f, 0.0f, 1, 0.0);
                getPlayer().sendMessage("§4[죽음의 유예] §c제한 시간 동안 처치를 달성하지 못해 즉사했습니다.");
                stop(false);
            }
        }

        @Override
        protected void onSilentEnd() {
            bossBar.removeAll();
        }

        public void resetTime() {
            this.remainingTime = 90;
            bossBar.setProgress(1.0);
        }

        public void addPlayer(Player player) {
            bossBar.addPlayer(player);
        }
    }

    @Override
    protected void onUpdate(Update update) {
        super.onUpdate(update);
        if (update == Update.RESTRICTION_CLEAR) {
            defermentTimer.start();
        } else if (update == Update.ABILITY_DESTROY) {
            defermentTimer.stop(false);
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer().equals(getPlayer())) {
            defermentTimer.addPlayer(getPlayer());
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
        Player victim = event.getEntity();
        if (victim.getKiller() != null && victim.getKiller().equals(getPlayer())) {
            if (defermentTimer.isRunning()) {
                defermentTimer.resetTime();
                getPlayer().sendMessage("§a[죽음의 유예] §f적을 처치하여 유예 시간이 §e1분 30초§f로 초기화되었습니다!");
                SoundLib.ENTITY_ZOMBIE_VILLAGER_CURE.playSound(getPlayer().getLocation(), 1.0f, 1.2f);
            }
        }
    }
}