package com.bkueAddon.game.list.Infection;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ZombieDeathHandler implements Listener {

    private final Infection infection;

    public ZombieDeathHandler(Infection infection) {
        this.infection = infection;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        if (!infection.isParticipating(victim)) {
            return;
        }

        if (!infection.isParticipating(attacker)) {
            return;
        }

        if (!infection.isZombie(attacker)) {
            return;
        }

        if (!infection.isSurvivor(victim)) {
            return;
        }

        if (victim.getHealth() - event.getFinalDamage() <= 0) {
            event.setCancelled(true);
            infection.infect(victim);
        }
    }
}