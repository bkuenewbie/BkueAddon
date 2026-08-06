package com.bkueAddon.game.list.Infection;

import com.google.common.base.Strings;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.event.GameCreditEvent;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.module.InfiniteDurability;
import daybreak.abilitywar.game.script.manager.ScriptManager;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.Seasons;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@GameManifest(
        name = "좀비 감염",
        description = {
                "§f좀비가 생존자를 감염시키는 게임입니다."
        }
)
public class Infection extends Game implements DefaultKitHandler {

    private final Set<Player> zombies = new HashSet<>();
    private final Set<Player> survivors = new HashSet<>();

    private ZombieDeathHandler deathHandler;

    private Team zombieGlowTeam;

    public Infection() {
        super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
        setRestricted(Settings.InvincibilitySettings.isEnabled());
    }

    @Override
    protected void progressGame(int seconds) {
        switch (seconds) {

            case 1:
                List<String> lines = Messager.asList(
                        "§6==== §e게임 참여자 목록 §6===="
                );

                int count = 0;

                for (Participant participant : getParticipants()) {
                    count++;

                    lines.add(
                            "§a" + count +
                                    ". §f" +
                                    participant.getPlayer().getName()
                    );
                }

                lines.add("§e총 인원수 : " + count + "명");
                lines.add("§6===========================");

                for (String line : lines) {
                    Bukkit.broadcastMessage(line);
                }

                if (getParticipants().size() < 2) {
                    Bukkit.broadcastMessage(
                            "§c최소 참가자 수를 충족하지 못하여 게임을 중지합니다. §8(§72명§8)"
                    );

                    stop();
                }

                break;

            case 3:
                lines = Messager.asList(
                        "§cBkueAddon §f- §6좀비 감염",
                        "§e버전 §7: §f" +
                                AbilityWar.getPlugin()
                                        .getDescription()
                                        .getVersion(),
                        "§b개발자 §7: §f블루 (BkueNewbie)"
                );

                GameCreditEvent event = new GameCreditEvent(this);
                Bukkit.getPluginManager().callEvent(event);
                lines.addAll(event.getCredits());

                for (String line : lines) {
                    Bukkit.broadcastMessage(line);
                }

                break;

            case 5:

                if (Settings.getDrawAbility()) {

                    for (String line : Messager.asList(
                            "§f플러그인에 총 §b" +
                                    AbilityList.nameValues().size() +
                                    "개§f의 능력이 등록되어 있습니다.",
                            "§7능력을 무작위로 할당합니다..."
                    )) {
                        Bukkit.broadcastMessage(line);
                    }

                    try {
                        startAbilitySelect();
                    } catch (OperationNotSupportedException ignored) {
                    }
                }

                break;

            case 6:

                if (Settings.getDrawAbility()) {

                    Bukkit.broadcastMessage(
                            "§f모든 참가자가 능력을 §b확정§f했습니다."
                    );

                } else {

                    Bukkit.broadcastMessage(
                            "§f능력자 게임 설정에 따라 §b능력§f을 추첨하지 않습니다."
                    );
                }

                break;

            case 8:

                Bukkit.broadcastMessage(
                        "§e잠시 후 게임이 시작됩니다."
                );

                break;

            case 10:

                Bukkit.broadcastMessage(
                        "§e게임이 §c5§e초 후에 시작됩니다."
                );

                SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();

                break;

            case 11:

                Bukkit.broadcastMessage(
                        "§e게임이 §c4§e초 후에 시작됩니다."
                );

                SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();

                break;

            case 12:

                Bukkit.broadcastMessage(
                        "§e게임이 §c3§e초 후에 시작됩니다."
                );

                SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();

                break;

            case 13:

                Bukkit.broadcastMessage(
                        "§e게임이 §c2§e초 후에 시작됩니다."
                );

                SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();

                break;

            case 14:

                Bukkit.broadcastMessage(
                        "§e게임이 §c1§e초 후에 시작됩니다."
                );

                SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();

                break;

            case 15:

                if (Seasons.isChristmas()) {

                    final String blocks =
                            Strings.repeat("§c■§2■", 22);

                    Bukkit.broadcastMessage(blocks);

                    Bukkit.broadcastMessage(
                            "§f            §cBkueAddon §f- §6좀비 감염"
                    );

                    Bukkit.broadcastMessage(
                            "§f                   게임 시작"
                    );

                    Bukkit.broadcastMessage(blocks);

                } else {

                    for (String line : Messager.asList(
                            "§e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■",
                            "§f             §cBkueAddon §f- §6좀비 감염",
                            "§f                    게임 시작",
                            "§e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"
                    )) {

                        Bukkit.broadcastMessage(line);
                    }
                }

                giveDefaultKit(getParticipants());

                if (Settings.getSpawnEnable()) {

                    Location spawn =
                            Settings.getSpawnLocation().toBukkitLocation();

                    for (Participant participant : getParticipants()) {
                        participant.getPlayer().teleport(spawn);
                    }
                }

                if (Settings.getNoHunger()) {

                    Bukkit.broadcastMessage(
                            "§2배고픔 무제한§a이 적용됩니다."
                    );

                } else {

                    Bukkit.broadcastMessage(
                            "§4배고픔 무제한§c이 적용되지 않습니다."
                    );
                }

                if (Settings.getInfiniteDurability()) {
                    addModule(new InfiniteDurability());
                } else {

                    Bukkit.broadcastMessage(
                            "§4내구도 무제한§c이 적용되지 않습니다."
                    );
                }

                if (Settings.getClearWeather()) {

                    for (World world : Bukkit.getWorlds()) {
                        world.setStorm(false);
                    }
                }

                if (isRestricted()) {

                    getInvincibility().start(false);

                } else {

                    Bukkit.broadcastMessage(
                            "§4초반 무적§c이 적용되지 않습니다."
                    );

                    setRestricted(false);
                }

                ScriptManager.runAll(this);

                setupTeams();
                registerDeathHandler();

                startGame();

                break;
        }
    }

    private void setupTeams() {

        zombies.clear();
        survivors.clear();

        List<Player> players = new ArrayList<>();

        for (Participant participant : getParticipants()) {
            players.add(participant.getPlayer());
        }

        Collections.shuffle(players);

        if (players.isEmpty()) {
            return;
        }

        Player firstZombie = players.get(0);

        zombies.add(firstZombie);

        for (int i = 1; i < players.size(); i++) {
            survivors.add(players.get(i));
        }

        setupZombieGlow();

        setZombie(firstZombie);

        for (Player player : survivors) {

            player.setPlayerListName(
                    "§a[생존자] §f" +
                            player.getName()
            );
        }

        Bukkit.broadcastMessage(
                "§c" + firstZombie.getName() +
                        "§f이(가) 최초의 §c좀비§f가 되었습니다!"
        );
    }

    private void setupZombieGlow() {

        Scoreboard scoreboard =
                Bukkit.getScoreboardManager().getMainScoreboard();

        if (scoreboard == null) {
            return;
        }

        zombieGlowTeam =
                scoreboard.getTeam("bkue_zombie_glow");

        if (zombieGlowTeam == null) {

            zombieGlowTeam =
                    scoreboard.registerNewTeam("bkue_zombie_glow");
        }

        zombieGlowTeam.setColor(ChatColor.RED);
    }

    private void setZombie(Player player) {

        player.setPlayerListName(
                "§c[좀비] §f" +
                        player.getName()
        );

        player.setGlowing(true);

        if (zombieGlowTeam != null &&
                !zombieGlowTeam.hasEntry(player.getName())) {

            zombieGlowTeam.addEntry(player.getName());
        }
    }

    public void infect(Player player) {

        if (!isParticipating(player)) {
            return;
        }

        if (zombies.contains(player)) {
            return;
        }

        survivors.remove(player);
        zombies.add(player);

        setZombie(player);

        Bukkit.broadcastMessage(
                "§c" + player.getName() +
                        "§f이(가) §c좀비§f가 되었습니다!"
        );

        checkWin();
    }

    private void checkWin() {

        if (!survivors.isEmpty()) {
            return;
        }

        if (zombies.isEmpty()) {
            return;
        }

        Bukkit.broadcastMessage(
                "§c모든 생존자가 감염되었습니다!"
        );

        Bukkit.broadcastMessage(
                "§c좀비 팀 승리!"
        );

        stop();
    }

    public boolean isZombie(Player player) {
        return zombies.contains(player);
    }

    public boolean isSurvivor(Player player) {
        return survivors.contains(player);
    }

    public Set<Player> getZombies() {
        return new HashSet<>(zombies);
    }

    public Set<Player> getSurvivors() {
        return new HashSet<>(survivors);
    }

    private void registerDeathHandler() {

        if (deathHandler != null) {
            HandlerList.unregisterAll(deathHandler);
        }

        deathHandler =
                new ZombieDeathHandler(this);

        Bukkit.getPluginManager().registerEvents(
                deathHandler,
                AbilityWar.getPlugin()
        );
    }

    private void unregisterDeathHandler() {

        if (deathHandler != null) {

            HandlerList.unregisterAll(deathHandler);

            deathHandler = null;
        }
    }

    private void removeZombieGlow() {

        Scoreboard scoreboard =
                Bukkit.getScoreboardManager().getMainScoreboard();

        if (scoreboard == null) {
            return;
        }

        if (zombieGlowTeam != null) {

            for (Player player : zombies) {

                zombieGlowTeam.removeEntry(
                        player.getName()
                );

                player.setGlowing(false);
            }

            zombieGlowTeam.unregister();
            zombieGlowTeam = null;

        } else {

            for (Player player : zombies) {
                player.setGlowing(false);
            }
        }
    }

    @Override
    protected void onEnd() {

        unregisterDeathHandler();

        removeZombieGlow();

        for (Participant participant : getParticipants()) {

            Player player =
                    participant.getPlayer();

            player.setGlowing(false);

            player.setPlayerListName(
                    player.getName()
            );
        }

        zombies.clear();
        survivors.clear();
    }
}