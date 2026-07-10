package BkueNewbie.bkueAddon.ability.list;

import BkueNewbie.bkueAddon.util.LocationUtilEx;
import BkueNewbie.bkueAddon.util.SnapshotUtil;
import BkueNewbie.bkueAddon.util.TeamUtil;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.*;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.Rooted;
import daybreak.abilitywar.utils.base.Formatter;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.PotionEffects;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

@AbilityManifest(name = "블루", rank = Rank.SPECIAL, species = Species.HUMAN, explain = {
        "§7패시브 §8- §b빙결 적응§f: 얼음 위에 있을 때 §6힘 I§f, §3저항 I§f, §b신속 I§f를 얻습니다.",
        "- 얼음 위에서 공격하면 피해가 30% 증가하고 대상을 1초간 속박합니다.",
        "§7패시브 §8- §b영구 동토§f: 플레이어를 처치하면 해당 위치 주변 $[RANGE_CONFIG]칸이 얼음으로 변합니다.",
        "§7철괴 우클릭 §8- §b빙결 지대§f: 5초 동안 주변 $[RANGE_CONFIG]칸을 얼음으로 바꾸고 범위 내 적을 3초간 속박합니다. $[COOLDOWN_CONFIG]"
}, summarize = {
        "얼음 위에서 강해지고 적 처치 시 얼음 지형을 생성합니다.",
        "빙결 지대로 적을 속박합니다."
})
public class Blue extends AbilityBase implements ActiveHandler {
    public Blue(Participant participant) {
        super(participant);
    }

    public static final AbilitySettings.SettingObject<Integer> COOLDOWN_CONFIG = abilitySettings.new SettingObject<Integer>(Blue.class, "cooldown", 30,
            "# 쿨타임") {

        @Override
        public boolean condition(Integer value) {
            return value >= 0;
        }

        @Override
        public String toString() {
            return Formatter.formatCooldown(getValue());
        }
    };

    public static final AbilitySettings.SettingObject<Integer> DURATION_CONFIG = abilitySettings.new SettingObject<Integer>(Blue.class, "duration", 5,
            "# 지속 시간") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }

    };

    public static final AbilitySettings.SettingObject<Integer> RANGE_CONFIG = abilitySettings.new SettingObject<Integer>(Blue.class, "range", 12,
            "# 스킬 사용 시 얼음 지형으로 바꿀 범위") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1 && value <= 50;
        }

    };

    private final SnapshotUtil snapshots = new SnapshotUtil();
    private final SnapshotUtil permanentIce = new SnapshotUtil();

    private final Cooldown cooldownTimer = new Cooldown(COOLDOWN_CONFIG.getValue());

    private final AbilityTimer countdown = new AbilityTimer() {
        @Override
        protected void run(int count) {
            Material below = getPlayer().getLocation().clone().subtract(0, 1, 0).getBlock().getType();

            if (below == Material.ICE) {
                PotionEffects.INCREASE_DAMAGE.addPotionEffect(getPlayer(), 10, 0, true);
                PotionEffects.DAMAGE_RESISTANCE.addPotionEffect(getPlayer(), 10, 0, true);
                PotionEffects.SPEED.addPotionEffect(getPlayer(), 10, 0, true);
            }
        }
    }.setPeriod(TimeUnit.TICKS, 1);

    private final Duration duration = new Duration(DURATION_CONFIG.getValue(), cooldownTimer) {
        @Override
        protected void onDurationProcess(int count) {
        }
        @Override
        protected void onDurationStart() {
            snapshots.fillCircle(getPlayer().getLocation(), RANGE_CONFIG.getValue(), Material.ICE);
            for (Player player : LocationUtilEx.getNearbyPlayers(getPlayer().getLocation(), RANGE_CONFIG.getValue())) {
                if (player.equals(getPlayer())) continue;
                Block below = player.getLocation().clone().subtract(0, 1, 0).getBlock();

                if (snapshots.getBlocks().contains(below) && getGame().isParticipating(player) && !TeamUtil.isTeammate(getPlayer(), player)) {
                    Rooted.apply(getGame().getParticipant(player), TimeUnit.SECONDS, 3);
                }
            }
        }
        @Override
        protected void onDurationEnd() {
            snapshots.restore();
        }
        @Override
        protected void onDurationSilentEnd() {
            snapshots.restore();
        }
    }.setPeriod(TimeUnit.SECONDS, 1);

    @Override
    protected void onUpdate(Update update) {
        if (update == Update.RESTRICTION_CLEAR) {
            countdown.start();
            getPlayer().getInventory().addItem(new ItemStack(Material.ICE, 64), new ItemStack(Material.ICE, 64));
        }
        if (update == Update.ABILITY_DESTROY) {
            permanentIce.restore();
            snapshots.restore();
        }
    }

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK && !cooldownTimer.isCooldown() && !duration.isDuration()) {
            duration.start();
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!e.getDamager().equals(getPlayer())) return;
        if (!(e.getEntity() instanceof Player)) return;
        Material below = getPlayer().getLocation().clone().subtract(0, 1, 0).getBlock().getType();
        if (below == Material.ICE) {
            e.setDamage(e.getDamage() * 1.3);

            Player victim = (Player) e.getEntity();

            if (getGame().isParticipating(victim) && !TeamUtil.isTeammate(getPlayer(), victim)) {
                Rooted.apply(getGame().getParticipant(victim), TimeUnit.SECONDS, 1);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        if (!getPlayer().equals(victim.getKiller())) return;

        permanentIce.fillCircle(victim.getLocation(), RANGE_CONFIG.getValue(), Material.ICE);
    }
}
