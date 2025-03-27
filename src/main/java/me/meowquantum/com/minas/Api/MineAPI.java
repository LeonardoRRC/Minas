package me.meowquantum.com.minas.Api;

import me.meowquantum.com.minas.Managers.MineManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.World;

import java.io.File;

public class MineAPI {

    private final MineManager mineManager;

    public MineAPI(MineManager mineManager) {
        this.mineManager = mineManager;
    }

    public boolean isInMine(Player player, String mineName) {
        Location loc = player.getLocation();
        return isLocationInMine(loc, mineName);
    }

    public boolean isLocationInMine(Location loc, String mineName) {
        File file = mineManager.getMineFile(mineName);
        if (!file.exists()) return false;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (!config.getString("world").equals(loc.getWorld().getName())) return false;

        Location pos1 = mineManager.deserializeLocation(config.getConfigurationSection("pos1"), loc.getWorld());
        Location pos2 = mineManager.deserializeLocation(config.getConfigurationSection("pos2"), loc.getWorld());

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return isBetween(x, pos1.getBlockX(), pos2.getBlockX())
                && isBetween(y, pos1.getBlockY(), pos2.getBlockY())
                && isBetween(z, pos1.getBlockZ(), pos2.getBlockZ());
    }

    private boolean isBetween(int val, int min, int max) {
        return val >= Math.min(min, max) && val <= Math.max(min, max);
    }
}
