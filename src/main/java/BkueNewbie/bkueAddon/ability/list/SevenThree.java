package BkueNewbie.bkueAddon.ability.list;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.object.AbilitySelect.AbilityCollector;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@AbilityManifest(name = "777", rank = Rank.A, species = Species.HUMAN, explain = {
        "§7패시브 §8- §e슬롯머신§f: 게임 시작 시 0~9 사이의 서로 다른 무작위 숫자 3개를 총 5번 뽑습니다.",
        "- 5번의 기회 중 §e가장 높은 결과(최대 7의 개수)§f 하나만 판정하여 단 한 번 적용합니다.",
        "- §b[7이 1개]§f: 무작위 S랭크 능력을 추가로 탈취하여 획득합니다.",
        "- §c[7이 2개]§f: 무작위 L랭크 능력을 추가로 탈취하여 획득합니다.",
        "- §4[7이 3개]§f: 모든 공격의 대미지가 §410배§f로 증가합니다.",
        "§7아이디어 제공 §8- §6goodhyojun"
})
public class SevenThree extends AbilityBase {

    private boolean tenTimesDamage = false;
    private final List<AbilityBase> extraAbilities = new ArrayList<>();

    public SevenThree(Participant participant) {
        super(participant);
    }

    @Override
    protected void onUpdate(Update update) {
        super.onUpdate(update);
        if (update == Update.RESTRICTION_CLEAR) {
            runSlotMachine();
        } else if (update == Update.RESTRICTION_SET) {
            for (AbilityBase ability : extraAbilities) {
                if (ability != null) ability.setRestricted(true);
            }
        } else if (update == Update.ABILITY_DESTROY) {
            for (AbilityBase ability : extraAbilities) {
                if (ability != null) ability.destroy();
            }
            extraAbilities.clear();
        }
    }

    private void runSlotMachine() {
        getPlayer().sendMessage("§e[777] §f슬롯머신이 작동합니다! (총 5회 추첨 후 최고 기록 반영)");

        List<Class<? extends AbilityBase>> allAbilities = AbilityCollector.EVERY_ABILITY_EXCLUDING_BLACKLISTED.collect(getGame().getClass());
        int maxSevenCount = 0;

        for (int i = 1; i <= 5; i++) {
            List<Integer> pool = new ArrayList<>();
            for (int n = 0; n <= 9; n++) pool.add(n);
            Collections.shuffle(pool);

            int n1 = pool.get(0);
            int n2 = pool.get(1);
            int n3 = pool.get(2);

            int roundSeven = 0;
            if (n1 == 7) roundSeven++;
            if (n2 == 7) roundSeven++;
            if (n3 == 7) roundSeven++;

            getPlayer().sendMessage("§7[" + i + "회차] §f결과: §e[ " + n1 + " ] [ " + n2 + " ] [ " + n3 + " ] §7(7: " + roundSeven + "개)");

            if (roundSeven > maxSevenCount) {
                maxSevenCount = roundSeven;
            }
        }

        getPlayer().sendMessage("§e[777] §f최종 결과 판정: §e7이 총 " + maxSevenCount + "개§f 검출되었습니다!");

        if (maxSevenCount == 1) {
            giveRandomRankAbility(allAbilities, Rank.S, "§bS랭크");
        } else if (maxSevenCount == 2) {
            giveRandomRankAbility(allAbilities, Rank.L, "§cL랭크");
        } else if (maxSevenCount == 3) {
            getPlayer().sendMessage("§4 -> ★잭팟★ 7이 3개 당첨! 10배 대미지 활성화!");
            tenTimesDamage = true;
            SoundLib.UI_TOAST_CHALLENGE_COMPLETE.playSound(getPlayer().getLocation(), 1.0f, 1.0f);
            ParticleLib.VILLAGER_HAPPY.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 30, 0.1);
        } else {
            getPlayer().sendMessage("§c -> 7이 한 개도 나오지 않아 아무런 효과도 얻지 못했습니다.");
            SoundLib.ENTITY_VILLAGER_NO.playSound(getPlayer().getLocation(), 1.0f, 1.0f);
        }
    }

    private void giveRandomRankAbility(List<Class<? extends AbilityBase>> sourceList, Rank targetRank, String rankName) {
        List<Class<? extends AbilityBase>> rankFiltered = new ArrayList<>();
        for (Class<? extends AbilityBase> clazz : sourceList) {
            AbilityManifest manifest = clazz.getAnnotation(AbilityManifest.class);
            if (manifest != null && manifest.rank() == targetRank && !clazz.equals(this.getClass())) {
                rankFiltered.add(clazz);
            }
        }

        if (!rankFiltered.isEmpty()) {
            Class<? extends AbilityBase> selectedClass = rankFiltered.get(ThreadLocalRandom.current().nextInt(rankFiltered.size()));
            try {
                AbilityBase newAbility = AbilityBase.create(selectedClass, getParticipant());
                if (newAbility != null) {
                    newAbility.setRestricted(false);
                    extraAbilities.add(newAbility);

                    getPlayer().sendMessage("§e -> " + rankName + " 능력 당첨! §f[§a" + newAbility.getName() + "§f] 획득!");
                    for (String line : Formatter.formatAbilityInfo(newAbility)) {
                        getPlayer().sendMessage(line);
                    }

                    SoundLib.ENTITY_PLAYER_LEVELUP.playSound(getPlayer().getLocation(), 1.0f, 1.4f);
                    ParticleLib.SPELL_WITCH.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 15, 0.1);
                }
            } catch (Exception e) {
                AbilityWar.getPlugin().getLogger().warning("777 random sub-ability mapping failed: " + e.getMessage());
            }
        } else {
            getPlayer().sendMessage("§c -> " + rankName + " 풀에 사용 가능한 능력이 없어 조건 획득에 실패했습니다.");
        }
    }

    @SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (tenTimesDamage && event.getDamager().equals(getPlayer()) && event.getEntity() instanceof LivingEntity) {
            event.setDamage(event.getDamage() * 10.0);
        }
    }
}