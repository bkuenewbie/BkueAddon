package BkueNewbie.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Stun;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

@AbilityManifest(name = "포지션 마스터", rank = Rank.S, species = Species.HUMAN, explain = {
        "§7철괴 우클릭 §8- §b위치 표식§f: 바라보는 지면이나 현재 위치를 메모리에 고정합니다. (상시 가능)",
        "§7검 들고 F키 §8- §d차원 왜곡§f: 어디서든 지정된 표식 위치로 즉시 텔레포트합니다. $[COOLDOWN_CONFIG]",
        "§4※ 패널티§f: 텔레포트 직후 §c3초간 기절(이동 및 시야 불가)§f 상태가 됩니다. 단, 적들의 공격은 그대로 받습니다.",
        "§4※ 안전장치§f: 지정된 위치가 안전하지 않거나(월드보더 밖, 블록 내부 등) 월드가 다르면 표식이 파괴됩니다."
})
public class PositionMaster extends AbilityBase implements ActiveHandler {

    public static final AbilitySettings.SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(PositionMaster.class, "cooldown", 30,
            "# 텔레포트 쿨타임") {
        @Override
        public boolean condition(Integer value) {
            return value >= 0;
        }
        @Override
        public String toString() {
            return Formatter.formatCooldown(getValue());
        }
    };

    private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());
    private Location savedLocation = null;

    public PositionMaster(Participant participant) {
        super(participant);
    }

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (getParticipant().hasEffect(Stun.registration)) return false;

        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
            Block targetBlock = getPlayer().getTargetBlockExact(50);
            if (targetBlock != null) {
                savedLocation = targetBlock.getLocation().add(0.5, 1.0, 0.5);
            } else {
                savedLocation = getPlayer().getLocation().clone();
            }

            savedLocation.setYaw(getPlayer().getLocation().getYaw());
            savedLocation.setPitch(getPlayer().getLocation().getPitch());

            SoundLib.BLOCK_NOTE_BLOCK_CHIME.playSound(getPlayer().getLocation(), 1.0f, 1.5f);
            ParticleLib.END_ROD.spawnParticle(savedLocation, 0.3f, 0.5f, 0.3f, 10, 0.05);
            return true;
        }
        return false;
    }

    @daybreak.abilitywar.ability.SubscribeEvent
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent event) {
        if (event.getPlayer().equals(getPlayer())) {
            Material mainHand = getPlayer().getInventory().getItemInMainHand().getType();
            if (mainHand.name().endsWith("_SWORD")) {
                event.setCancelled(true);

                if (getParticipant().hasEffect(Stun.registration)) {
                    return;
                }

                if (cooldownTimer.isCooldown()) {
                    return;
                }

                if (savedLocation == null) {
                    getPlayer().sendMessage("§c[포지션 마스터] 각인된 공간 표식이 존재하지 않습니다.");
                    return;
                }

                if (!isLocationSafe(savedLocation)) {
                    getPlayer().sendMessage("§4[포지션 마스터] 표식 위치가 월드보더 외부이거나 안전하지 않아 표식이 파괴되었습니다!");
                    SoundLib.ENTITY_ITEM_BREAK.playSound(getPlayer().getLocation(), 1.0f, 0.5f);
                    savedLocation = null;
                    return;
                }

                Location currentLoc = getPlayer().getLocation();
                ParticleLib.PORTAL.spawnParticle(currentLoc.add(0, 1, 0), 0.5f, 0.5f, 0.5f, 20, 0.2);
                SoundLib.ENTITY_ENDERMAN_TELEPORT.playSound(currentLoc, 1.0f, 0.8f);

                getPlayer().teleport(savedLocation);

                ParticleLib.PORTAL.spawnParticle(savedLocation.clone().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 20, 0.2);
                SoundLib.ENTITY_ENDERMAN_TELEPORT.playSound(savedLocation, 1.0f, 1.2f);

                Stun.apply(getParticipant(), TimeUnit.SECONDS, 3);
                cooldownTimer.start();
            }
        }
    }

    private boolean isLocationSafe(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;

        if (!loc.getWorld().equals(getPlayer().getWorld())) return false;

        WorldBorder border = loc.getWorld().getWorldBorder();
        double size = border.getSize() / 2.0;
        Location center = border.getCenter();
        double minX = center.getX() - size;
        double maxX = center.getX() + size;
        double minZ = center.getZ() - size;
        double maxZ = center.getZ() + size;

        if (loc.getX() < minX || loc.getX() > maxX || loc.getZ() < minZ || loc.getZ() > maxZ) {
            return false;
        }

        Block feet = loc.getBlock();
        Block head = loc.getBlock().getRelative(0, 1, 0);
        if (feet.getType().isSolid() || head.getType().isSolid()) {
            return false;
        }

        if (feet.getType() == Material.LAVA || head.getType() == Material.LAVA) {
            return false;
        }

        return true;
    }
}