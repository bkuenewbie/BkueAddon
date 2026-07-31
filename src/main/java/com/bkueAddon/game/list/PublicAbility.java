package com.bkueAddon.game.list;

import com.google.common.base.Strings;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.AbstractGame.Observer;
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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import javax.naming.OperationNotSupportedException;
import java.util.List;

@GameManifest(
        name = "능력 표시",
        description = {
                "§f플레이어의 닉네임에 자신의 능력을 표시합니다."
        }
)
public class PublicAbility extends Game implements DefaultKitHandler, Observer {

    public PublicAbility() {
        super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
        setRestricted(Settings.InvincibilitySettings.isEnabled());
        attachObserver(this);
    }

    @Override
    protected void progressGame(int seconds) {
        switch (seconds) {

            case 1:
                List<String> lines = Messager.asList(
                        "§6==== §e게임 참여자 목록 §6===="
                );

                int count = 0;

                for (Participant p : getParticipants()) {
                    count++;
                    lines.add("§a" + count + ". §f" + p.getPlayer().getName());
                }

                lines.add("§e총 인원수 : " + count + "명");
                lines.add("§6===========================");

                for (String line : lines) {
                    Bukkit.broadcastMessage(line);
                }

                if (getParticipants().size() < 1) {
                    stop();
                    Bukkit.broadcastMessage(
                            "§c최소 참가자 수를 충족하지 못하여 게임을 중지합니다. §8(§71명§8)"
                    );
                }
                break;

            case 3:
                lines = Messager.asList(
                        "§cBkueAddon §f- §6능력 표시",
                        "§e버전 §7: §f" + AbilityWar.getPlugin().getDescription().getVersion(),
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
                            "§f플러그인에 총 §b" + AbilityList.nameValues().size() + "개§f의 능력이 등록되어 있습니다.",
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
                Bukkit.broadcastMessage("§e잠시 후 게임이 시작됩니다.");
                break;

            case 10:
                Bukkit.broadcastMessage("§e게임이 §c5§e초 후에 시작됩니다.");
                SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
                break;

            case 11:
                Bukkit.broadcastMessage("§e게임이 §c4§e초 후에 시작됩니다.");
                SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
                break;

            case 12:
                Bukkit.broadcastMessage("§e게임이 §c3§e초 후에 시작됩니다.");
                SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
                break;

            case 13:
                Bukkit.broadcastMessage("§e게임이 §c2§e초 후에 시작됩니다.");
                SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
                break;

            case 14:
                Bukkit.broadcastMessage("§e게임이 §c1§e초 후에 시작됩니다.");
                SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
                break;
            case 15:
                if (Seasons.isChristmas()) {
                    final String blocks = Strings.repeat("§c■§2■", 22);

                    Bukkit.broadcastMessage(blocks);
                    Bukkit.broadcastMessage("§f            §cBkueAddon §f- §6능력 표시");
                    Bukkit.broadcastMessage("§f                   게임 시작");
                    Bukkit.broadcastMessage(blocks);
                } else {
                    for (String line : Messager.asList(
                            "§e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■",
                            "§f             §cBkueAddon §f- §6능력 표시",
                            "§f                    게임 시작",
                            "§e■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"
                    )) {
                        Bukkit.broadcastMessage(line);
                    }
                }

                giveDefaultKit(getParticipants());

                if (Settings.getSpawnEnable()) {
                    Location spawn = Settings.getSpawnLocation().toBukkitLocation();

                    for (Participant participant : getParticipants()) {
                        participant.getPlayer().teleport(spawn);
                    }
                }

                if (Settings.getNoHunger()) {
                    Bukkit.broadcastMessage("§2배고픔 무제한§a이 적용됩니다.");
                } else {
                    Bukkit.broadcastMessage("§4배고픔 무제한§c이 적용되지 않습니다.");
                }

                if (Settings.getInfiniteDurability()) {
                    addModule(new InfiniteDurability());
                } else {
                    Bukkit.broadcastMessage("§4내구도 무제한§c이 적용되지 않습니다.");
                }

                if (Settings.getClearWeather()) {
                    for (World world : Bukkit.getWorlds()) {
                        world.setStorm(false);
                    }
                }

                if (isRestricted()) {
                    getInvincibility().start(false);
                } else {
                    Bukkit.broadcastMessage("§4초반 무적§c이 적용되지 않습니다.");
                    setRestricted(false);
                }

                ScriptManager.runAll(this);

                startGame();

                updateAbilityNames();

                break;
        }
    }

    private void updateAbilityNames() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        if (scoreboard == null) {
            return;
        }

        for (Participant participant : getParticipants()) {
            Player player = participant.getPlayer();

            player.setScoreboard(scoreboard);

            String teamName = "aw_" + player.getUniqueId().toString().substring(0, 12);

            Team team = scoreboard.getTeam(teamName);

            if (team == null) {
                team = scoreboard.registerNewTeam(teamName);
            }

            for (Team otherTeam : scoreboard.getTeams()) {
                if (!otherTeam.equals(team) && otherTeam.hasEntry(player.getName())) {
                    otherTeam.removeEntry(player.getName());
                }
            }

            if (participant.hasAbility()) {
                team.setPrefix("§7[" + participant.getAbility().getName() + "] §f");
            } else {
                team.setPrefix("§7[능력 없음] §f");
            }

            team.addEntry(player.getName());
        }
    }

    private void removeAbilityNames() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        if (scoreboard == null) {
            return;
        }

        for (Participant participant : getParticipants()) {
            Player player = participant.getPlayer();

            String teamName = "aw_" +
                    player.getUniqueId().toString().substring(0, 12);

            Team team = scoreboard.getTeam(teamName);

            if (team != null) {
                team.removeEntry(player.getName());
                team.unregister();
            }

            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    @Override
    public void update(GameUpdate update) {
        if (update == GameUpdate.END) {
            removeAbilityNames();
        }
    }
}