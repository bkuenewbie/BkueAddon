package BkueNewbie.bkueAddon.ability.list;

import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.concurrent.ThreadLocalRandom;

@AbilityManifest(name = "저주받은 자", rank = Rank.B, species = Species.HUMAN, explain = {
        "§7패시브 §8- §b순간이동§f: 저주 때문에 45~60초 사이의 랜덤한 시간마다 안전한 무작위 위치로 순간이동합니다. (월드보더 내부 한정)",
        "§7패시브 §8- §c분노§f: 저주에 대한 분노로 모든 공격의 대미지가 §c1.3배§f로 적용됩니다.",
        "§7아이디어 제공 §8- §6goodhyojun"
})
public class CursedOne extends AbilityBase {

    private final TeleportTimer teleportTimer = new TeleportTimer();

    public CursedOne(Participant participant) {
        super(participant);
    }

    private class TeleportTimer extends AbilityTimer {
        private int timeToTeleport;

        public TeleportTimer() {
            super(TaskType.NORMAL, Integer.MAX_VALUE);
            setPeriod(TimeUnit.SECONDS, 1); // 1초마다 무조건 실행되도록 고정
            setBehavior(RestrictionBehavior.PAUSE_RESUME);
        }

        @Override
        protected void onStart() {
            resetRandomTime();
        }

        @Override
        protected void run(int count) {
            timeToTeleport--;

            // 무작위로 설정된 시간이 다 되었을 때만 순간이동 로직 수행
            if (timeToTeleport <= 0) {
                Location currentLoc = getPlayer().getLocation();
                Location targetLoc = null;
                int attempts = 0;

                while (attempts < 15) {
                    attempts++;

                    int randomRadius = ThreadLocalRandom.current().nextInt(15, 36);
                    Location potentialLoc = LocationUtil.getRandomLocation(currentLoc, randomRadius);

                    if (potentialLoc != null && isInsideWorldBorder(potentialLoc)) {
                        Block feet = potentialLoc.getBlock();
                        Block head = potentialLoc.clone().add(0, 1, 0).getBlock();

                        if (!feet.getType().isSolid() && !head.getType().isSolid()) {
                            targetLoc = potentialLoc;
                            break;
                        }
                    }
                }

                if (targetLoc != null) {
                    SoundLib.ENTITY_ENDERMAN_TELEPORT.playSound(currentLoc, 1.0f, 0.8f);
                    ParticleLib.PORTAL.spawnParticle(currentLoc.add(0, 1, 0), 0.5f, 0.5f, 0.5f, 20, 0.2);

                    getPlayer().teleport(targetLoc.add(0.5, 0.1, 0.5));

                    SoundLib.ENTITY_ENDERMAN_TELEPORT.playSound(getPlayer().getLocation(), 1.0f, 1.0f);
                    ParticleLib.PORTAL.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 20, 0.2);
                    getPlayer().sendMessage("§4[저주] §b알 수 없는 힘에 의해 강제로 순간이동당했습니다.");
                }

                // 순간이동이 끝나면 다음 순간이동 시간 재설정
                resetRandomTime();
            }
        }

        private void resetRandomTime() {
            this.timeToTeleport = ThreadLocalRandom.current().nextInt(45, 61);
        }

        private boolean isInsideWorldBorder(Location loc) {
            WorldBorder border = loc.getWorld().getWorldBorder();
            double size = border.getSize() / 2.0;
            double centerX = border.getCenter().getX();
            double centerZ = border.getCenter().getZ();

            return (loc.getX() >= centerX - size && loc.getX() <= centerX + size) &&
                    (loc.getZ() >= centerZ - size && loc.getZ() <= centerZ + size);
        }
    }

    @Override
    protected void onUpdate(Update update) {
        super.onUpdate(update);
        if (update == Update.RESTRICTION_CLEAR) {
            teleportTimer.start();
        } else if (update == Update.ABILITY_DESTROY) {
            teleportTimer.stop(false);
        }
    }

    @SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager().equals(getPlayer()) && event.getEntity() instanceof LivingEntity) {
            event.setDamage(event.getDamage() * 1.3);
        }
    }
}