package com.bkueAddon.util;

import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.minecraft.block.Blocks;
import daybreak.abilitywar.utils.base.minecraft.block.IBlockSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SnapshotUtil {

    private final Map<Block, IBlockSnapshot> snapshots = new HashMap<>();

    public void save(Block block) {
        snapshots.putIfAbsent(block, Blocks.createSnapshot(block));
    }

    public void restore() {
        for (IBlockSnapshot snapshot : snapshots.values()) {
            snapshot.apply();
        }
        snapshots.clear();
    }

    public void fillCircle(Location center, int radius, Material material) {
        for (Block block : LocationUtil.getBlocks2D(center, radius, false, true, true)) {
            Block ground = block.getRelative(BlockFace.DOWN);

            save(ground);
            ground.setType(material);
        }
    }

    public void fillRing(Location center, int radius, Material material) {
        for (Block block : LocationUtil.getBlocks2D(center, radius, true, true, true)) {
            Block ground = block.getRelative(BlockFace.DOWN);

            save(ground);
            ground.setType(material);
        }
    }

    public Set<Block> getBlocks() {
        return snapshots.keySet();
    }
}