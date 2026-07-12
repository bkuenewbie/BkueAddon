package com.bkueAddon.util;

import daybreak.abilitywar.utils.base.math.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import static daybreak.abilitywar.game.GameManager.getGame;

public class LocationUtilEx {

    public static List<Block> getCircleBlocks(Location center, int radius) {
        return LocationUtil.getBlocks2D(center, radius, true, true, true);
    }

    public static List<Block> getRingBlocks(Location center, int radius) {
        return LocationUtil.getBlocks2D(center, radius, false, true, true);
    }

    public static List<Player> getNearbyPlayers(Location center, double radius) {
        return LocationUtil.getEntitiesInCircle(Player.class, center, radius, player -> true);
    }

    public static List<Player> getNearbyPlayers(Location center, double radius, Predicate<Player> predicate) {
        return LocationUtil.getEntitiesInCircle(Player.class, center, radius, predicate);
    }

    public static List<Player> getNearbyEnemies(Player player, double radius) {
        return LocationUtil.getEntitiesInCircle(Player.class, player.getLocation(), radius, target -> !target.equals(player) && getGame().isParticipating(target) && !TeamUtil.isTeammate(player, target));
    }

    public static List<Player> getNearbyTeammates(Player player, double radius) {
        return LocationUtil.getEntitiesInCircle(Player.class, player.getLocation(), radius, target -> !target.equals(player) && TeamUtil.isTeammate(player, target));
    }

    public static Player getNearestPlayer(Location location) {
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().equals(location.getWorld())) continue;

            double distance = player.getLocation().distanceSquared(location);

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = player;
            }
        }

        return nearest;
    }

    public static Player getNearestEnemy(Player player) {
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.equals(player)) continue;
            if (!target.getWorld().equals(player.getWorld())) continue;
            if (!getGame().isParticipating(target)) continue;
            if (TeamUtil.isTeammate(player, target)) continue;

            double distance = target.getLocation().distanceSquared(player.getLocation());

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = target;
            }
        }

        return nearest;
    }

    public static boolean isInsideRadius(Location a, Location b, double radius) {
        if (!a.getWorld().equals(b.getWorld())) return false;

        return a.distanceSquared(b) <= radius * radius;
    }

    public static Location getRandomLocation(Location center, int radius) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        double x = random.nextDouble(-radius, radius);
        double z = random.nextDouble(-radius, radius);

        return center.clone().add(x, 0, z);
    }
}