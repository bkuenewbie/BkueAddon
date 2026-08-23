package com.bkueAddon.ability.list;

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
        "§7패시브 §8- §c분노§f: 저주에 대한 분노로 모든 공격의 대미지가 §c1.2배§f로 적용됩니다.",
        "§7아이디어 제공 §8- §6goodhyojun"
}, summarize = {
        "§7주기적 순간이동§f: 일정 시간마다 무작위 위치로 이동하며, 공격력이 1.2배 증가합니다."
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
            setPeriod(TimeUnit.SECONDS, 1);
            setBehavior(RestrictionBehavior.PAUSE_RESUME);
        }

        @Override
        protected void onStart() {
            resetRandomTime();
        }

        @Override
        protected void run(int count) {
            if (--timeToTeleport > 0) return;

            Location currentLoc = getPlayer().getLocation();
            Location targetLoc = null;

            for (int attempts = 0; attempts < 15; attempts++) {
                Location loc = LocationUtil.getRandomLocation(currentLoc, ThreadLocalRandom.current().nextInt(15, 36));

                if (loc != null && isInsideWorldBorder(loc) && !loc.getBlock().getType().isSolid() && !loc.clone().add(0, 1, 0).getBlock().getType().isSolid()) {
                    targetLoc = loc;
                    break;
                }
            }

            if (targetLoc != null) {
                ParticleLib.PORTAL.spawnParticle(currentLoc.add(0, 1, 0), 1, 1, 1, 40, 0.2);
                SoundLib.ENTITY_ENDERMAN_TELEPORT.playSound(currentLoc, 1.2f, 0.8f);

                getPlayer().teleport(targetLoc.add(0.5, 0.1, 0.5));

                ParticleLib.PORTAL.spawnParticle(getPlayer().getLocation().add(0, 1, 0), 1, 1, 1, 40, 0.2);
                SoundLib.ENTITY_ENDERMAN_TELEPORT.playSound(getPlayer().getLocation(), 1.2f, 1.0f);
            }

            resetRandomTime();
        }

        private void resetRandomTime() {
            timeToTeleport = ThreadLocalRandom.current().nextInt(45, 61);
        }

        private boolean isInsideWorldBorder(Location loc) {
            WorldBorder border = loc.getWorld().getWorldBorder();
            double size = border.getSize() / 2;
            double x = border.getCenter().getX();
            double z = border.getCenter().getZ();
            return loc.getX() >= x - size && loc.getX() <= x + size && loc.getZ() >= z - size && loc.getZ() <= z + size;
        }
    }

    @Override
    protected void onUpdate(Update update) {
        if (update == Update.RESTRICTION_CLEAR) {
            teleportTimer.start();
        } else if (update == Update.ABILITY_DESTROY) {
            teleportTimer.stop(false);
        }
    }

    @SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager().equals(getPlayer()) && event.getEntity() instanceof LivingEntity) {
            event.setDamage(event.getDamage() * 1.2);
        }
    }
}