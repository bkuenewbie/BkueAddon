package com.bkueAddon.ability.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.*;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

@AbilityManifest(name = "천재", rank = Rank.S, species = Species.HUMAN, explain = {
        "§7패시브 §8- §b두뇌 가동§f: 25초마다 화면에 1차 방정식 문제가 출제됩니다.",
        "- 문제를 §c12초§f 이내에 채팅창으로 맞히면 §a무작위 버프§f를 획득합니다.",
        "- 제한 시간 초과 시 아무런 효과도 주어지지 않습니다."
}, summarize = {
        "수학 문제를 풀어 무작위 버프를 획득합니다."
})
public class Genius extends AbilityBase {

    private final QuizLoopTimer quizLoopTimer = new QuizLoopTimer();
    private final QuizSolveTimer quizSolveTimer = new QuizSolveTimer();

    private int currentAnswer = -999;
    private boolean isQuizActive;

    public Genius(Participant participant) {
        super(participant);
    }

    private class QuizLoopTimer extends AbilityTimer {
        QuizLoopTimer() {
            super(TaskType.NORMAL, Integer.MAX_VALUE);
            setPeriod(TimeUnit.SECONDS, 25);
            setBehavior(RestrictionBehavior.PAUSE_RESUME);
        }

        @Override
        protected void run(int count) {
            if (!isQuizActive) generateEquation();
        }
    }

    private class QuizSolveTimer extends AbilityTimer {
        QuizSolveTimer() {
            super(TaskType.NORMAL, 12);
            setPeriod(TimeUnit.SECONDS, 1);
            setBehavior(RestrictionBehavior.PAUSE_RESUME);
        }

        @Override
        protected void run(int count) {
            int remaining = 12 - count;

            if (remaining <= 5 && remaining > 0) {
                getPlayer().sendMessage("§c[천재] §e" + remaining + "초 §c남음!");
                SoundLib.BLOCK_NOTE_BLOCK_PLING.playSound(getPlayer(), 1, 1.2f);
            }
        }

        @Override
        protected void onEnd() {
            if (!isQuizActive) return;

            isQuizActive = false;
            getPlayer().sendMessage("§c[천재] 시간 초과!");
            SoundLib.ENTITY_VILLAGER_NO.playSound(getPlayer(), 1, 1);
        }
    }

    private void generateEquation() {
        ThreadLocalRandom r = ThreadLocalRandom.current();

        int a = r.nextInt(2, 8);
        int b = r.nextInt(1, 31);
        currentAnswer = r.nextInt(1, 16);
        int c = a * currentAnswer + b;

        isQuizActive = true;

        getPlayer().sendMessage("§b§l━━━━━━━━━━━━━━━━━━━━");
        getPlayer().sendMessage("§e§l[천재] §f문제를 풀어 X값을 입력하세요!");
        getPlayer().sendMessage("§f식: §a" + a + "X + " + b + " = " + c);
        getPlayer().sendMessage("§c제한시간: 12초");
        getPlayer().sendMessage("§b§l━━━━━━━━━━━━━━━━━━━━");

        SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer(), 1, 0.7f);
        quizSolveTimer.start();
    }

    private void applyRandomBuff() {
        Player p = getPlayer();

        switch (ThreadLocalRandom.current().nextInt(5)) {
            case 0:
                p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 160, 1));
                p.sendMessage("§a[천재] 정답! §4힘 II §f(8초)");
                break;

            case 1:
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
                p.sendMessage("§a[천재] 정답! §b신속 II §f(10초)");
                break;

            case 2:
                p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 160, 1));
                p.sendMessage("§a[천재] 정답! §5저항 II §f(8초)");
                break;

            case 3:
                p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
                p.sendMessage("§a[천재] 정답! §d재생 II §f(5초)");
                break;

            case 4:
                p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 1));
                p.sendMessage("§a[천재] 정답! §6흡수 II §f(10초)");
                break;
        }

        SoundLib.ENTITY_PLAYER_LEVELUP.playSound(p, 1.2f, 1.5f);
        ParticleLib.VILLAGER_HAPPY.spawnParticle(p.getLocation().add(0, 1, 0), 0.7, 0.8, 0.7, 20, 0.15);
        ParticleLib.FIREWORKS_SPARK.spawnParticle(p.getLocation().add(0, 1, 0), 0.5, 0.8, 0.5, 10, 0.1);
    }

    @Override
    protected void onUpdate(Update update) {
        if (update == Update.RESTRICTION_CLEAR) {
            quizLoopTimer.start();
        } else if (update == Update.ABILITY_DESTROY) {
            quizLoopTimer.stop(false);
            quizSolveTimer.stop(false);
            isQuizActive = false;
        }
    }

    @SubscribeEvent
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (!isQuizActive || !event.getPlayer().equals(getPlayer())) return;
        if (!event.getMessage().trim().equals(String.valueOf(currentAnswer))) return;

        event.setCancelled(true);
        isQuizActive = false;

        Bukkit.getScheduler().runTask(AbilityWar.getPlugin(), () -> {
            if (quizSolveTimer.isRunning()) quizSolveTimer.stop(false);
            applyRandomBuff();
        });
    }
}