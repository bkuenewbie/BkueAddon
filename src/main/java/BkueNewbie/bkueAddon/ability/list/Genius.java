package BkueNewbie.bkueAddon.ability.list;

import daybreak.abilitywar.AbilityWar;
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
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

@AbilityManifest(name = "천재", rank = Rank.A, species = Species.HUMAN, explain = {
        "§7패시브 §8- §b두뇌 가동§f: 30초마다 화면에 1차 방정식 문제가 출제됩니다.",
        "- 문제를 15초 이내에 채팅창으로 맞히면 강력한 §a무작위 버프§f를 획득합니다.",
        "- 제한 시간 초과 시 아무런 효과도 주어지지 않습니다."
})
public class Genius extends AbilityBase {

    private final QuizLoopTimer quizLoopTimer = new QuizLoopTimer();
    private final QuizSolveTimer quizSolveTimer = new QuizSolveTimer();

    private int currentAnswer = -999;
    private boolean isQuizActive = false;

    public Genius(Participant participant) {
        super(participant);
    }

    private class QuizLoopTimer extends AbilityTimer {
        public QuizLoopTimer() {
            super(TaskType.NORMAL, Integer.MAX_VALUE);
            setPeriod(TimeUnit.SECONDS, 30);
            setBehavior(RestrictionBehavior.PAUSE_RESUME);
        }

        @Override
        protected void run(int count) {
            generateEquation();
        }
    }

    private class QuizSolveTimer extends AbilityTimer {
        public QuizSolveTimer() {
            super(TaskType.NORMAL, 15);
            setPeriod(TimeUnit.SECONDS, 1);
            setBehavior(RestrictionBehavior.PAUSE_RESUME);
        }

        @Override
        protected void run(int count) {
            int remaining = 15 - count;
            if (remaining <= 5 && remaining > 0) {
                getPlayer().sendMessage("§c[천재] 수학 문제 제한 시간 §e" + remaining + "초§c 남음!");
                SoundLib.BLOCK_NOTE_BLOCK_PLING.playSound(getPlayer().getLocation(), 1.0f, 1.2f);
            }
        }

        @Override
        protected void onEnd() {
            if (isQuizActive) {
                isQuizActive = false;
                getPlayer().sendMessage("§c[천재] 시간 초과! 문제를 풀지 못했습니다.");
                SoundLib.ENTITY_VILLAGER_NO.playSound(getPlayer().getLocation(), 1.0f, 1.0f);
            }
        }
    }

    private void generateEquation() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        int a = random.nextInt(2, 6);
        currentAnswer = random.nextInt(1, 11);
        int b = random.nextInt(1, 21);
        int c = (a * currentAnswer) + b;

        isQuizActive = true;
        getPlayer().sendMessage("§b========================================");
        getPlayer().sendMessage("§e[천재] 두뇌 가동! 다음 1차 방정식을 풀고 X값을 채팅에 입력하세요. (제한시간 15초)");
        getPlayer().sendMessage("§f식: §a" + a + "X + " + b + " = " + c);
        getPlayer().sendMessage("§b========================================");
        SoundLib.ENTITY_EXPERIENCE_ORB_PICKUP.playSound(getPlayer().getLocation(), 1.0f, 0.5f);

        quizSolveTimer.start();
    }

    private void applyRandomBuff() {
        Player player = getPlayer();
        int roll = ThreadLocalRandom.current().nextInt(3);

        switch (roll) {
            case 0:
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 160, 0));
                player.sendMessage("§a[천재] 정답! §4[힘 I (8초)]§a 버프를 획득했습니다.");
                break;
            case 1:
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
                player.sendMessage("§a[천재] 정답! §b[신속 II (10초)]§a 버프를 획득했습니다.");
                break;
            case 2:
                player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 160, 1));
                player.sendMessage("§a[천재] 정답! §5[저항 II (8초)]§a 버프를 획득했습니다.");
                break;
        }

        SoundLib.ENTITY_PLAYER_LEVELUP.playSound(player.getLocation(), 1.0f, 1.5f);
        ParticleLib.VILLAGER_HAPPY.spawnParticle(player.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 15, 0.1);
    }

    @Override
    protected void onUpdate(Update update) {
        super.onUpdate(update);
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
        if (isQuizActive && event.getPlayer().equals(getPlayer())) {
            String message = event.getMessage().trim();

            if (message.equals(String.valueOf(currentAnswer))) {
                event.setCancelled(true);
                isQuizActive = false;

                Bukkit.getScheduler().runTask(AbilityWar.getPlugin(), () -> {
                    quizSolveTimer.stop(false);
                    applyRandomBuff();
                });
            }
        }
    }
}