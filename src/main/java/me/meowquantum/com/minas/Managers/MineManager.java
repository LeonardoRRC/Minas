package me.meowquantum.com.minas.Managers;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class MineManager {

    private final File minesFolder;

    public MineManager(File pluginDataFolder) {
        this.minesFolder = new File(pluginDataFolder, "mines");
        if (!minesFolder.exists()) minesFolder.mkdirs();
    }

    public void fillMine(String name) {
        File file = new File(minesFolder, name + ".yml");
        if (!file.exists()) {
            Bukkit.getLogger().warning("La mina '" + name + "' no existe.");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.getBoolean("enable", true)) {
            Bukkit.getLogger().info("La mina '" + name + "' está desactivada.");
            return;
        }

        World world = Bukkit.getWorld(config.getString("world"));
        if (world == null) {
            Bukkit.getLogger().warning("El mundo '" + config.getString("world") + "' no está cargado.");
            return;
        }

        Location pos1 = deserializeLocation(config.getConfigurationSection("pos1"), world);
        Location pos2 = deserializeLocation(config.getConfigurationSection("pos2"), world);

        Map<Material, Integer> blockChances = new HashMap<>();
        ConfigurationSection blockSection = config.getConfigurationSection("blocks");
        if (blockSection == null || blockSection.getKeys(false).isEmpty()) {
            Bukkit.getLogger().warning("La mina '" + name + "' no tiene bloques definidos.");
            return;
        }

        int total = 0;
        for (String key : blockSection.getKeys(false)) {
            try {
                Material mat = Material.valueOf(key);
                int value = blockSection.getInt(key);
                blockChances.put(mat, value);
                total += value;
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Bloque inválido: " + key);
            }
        }

        if (blockChances.isEmpty()) return;

        Map<Material, Double> finalChances = new HashMap<>();
        for (Map.Entry<Material, Integer> entry : blockChances.entrySet()) {
            finalChances.put(entry.getKey(), (double) entry.getValue() / total);
        }

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        Random random = new Random();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    double roll = random.nextDouble();
                    double current = 0;

                    for (Map.Entry<Material, Double> entry : finalChances.entrySet()) {
                        current += entry.getValue();
                        if (roll <= current) {
                            world.getBlockAt(x, y, z).setType(entry.getKey());
                            break;
                        }
                    }
                }
            }
        }

        Bukkit.getLogger().info("La mina '" + name + "' fue rellenada correctamente.");
    }

    public Location deserializeLocation(ConfigurationSection section, World world) {
        int x = section.getInt("x");
        int y = section.getInt("y");
        int z = section.getInt("z");
        return new Location(world, x, y, z);
    }

    public File getMineFile(String name) {
        return new File(minesFolder, name + ".yml");
    }

    public void fillMineGradually(String name, int blocksPerTick) {
        File file = new File(minesFolder, name + ".yml");
        if (!file.exists()) {
            Bukkit.getLogger().warning("La mina '" + name + "' no existe.");
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.getBoolean("enable", true)) return;

        World world = Bukkit.getWorld(config.getString("world"));
        if (world == null) return;

        Location pos1 = deserializeLocation(config.getConfigurationSection("pos1"), world);
        Location pos2 = deserializeLocation(config.getConfigurationSection("pos2"), world);

        Map<Material, Integer> blockChances = new HashMap<>();
        ConfigurationSection blockSection = config.getConfigurationSection("blocks");
        if (blockSection == null || blockSection.getKeys(false).isEmpty()) return;

        int total = 0;
        for (String key : blockSection.getKeys(false)) {
            try {
                Material mat = Material.valueOf(key);
                int value = blockSection.getInt(key);
                blockChances.put(mat, value);
                total += value;
            } catch (IllegalArgumentException ignored) {}
        }

        if (blockChances.isEmpty()) return;

        Map<Material, Double> finalChances = new HashMap<>();
        for (Map.Entry<Material, Integer> entry : blockChances.entrySet()) {
            finalChances.put(entry.getKey(), (double) entry.getValue() / total);
        }

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        List<Location> allLocations = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    allLocations.add(new Location(world, x, y, z));
                }
            }
        }

        Collections.shuffle(allLocations);

        new BukkitRunnable() {
            int index = 0;
            final Random random = new Random();

            @Override
            public void run() {
                int placed = 0;
                while (placed < blocksPerTick && index < allLocations.size()) {
                    Location loc = allLocations.get(index);
                    double roll = random.nextDouble();
                    double chance = 0;

                    for (Map.Entry<Material, Double> entry : finalChances.entrySet()) {
                        chance += entry.getValue();
                        if (roll <= chance) {
                            loc.getBlock().setType(entry.getKey());
                            break;
                        }
                    }

                    index++;
                    placed++;
                }

                if (index >= allLocations.size()) {
                    cancel();
                    Bukkit.getLogger().info("La mina '" + name + "' fue rellenada progresivamente.");
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&8[&3Minas&8] ▶ &fLa mina &3" + name + " &fha sido reiniciada."));
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("Minas"), 0L, 1L);
    }

    public int getTotalBlocks(String name) {
        Location[] area = getMineArea(name);
        if (area == null) return 0;

        Location pos1 = area[0];
        Location pos2 = area[1];

        int dx = Math.abs(pos1.getBlockX() - pos2.getBlockX()) + 1;
        int dy = Math.abs(pos1.getBlockY() - pos2.getBlockY()) + 1;
        int dz = Math.abs(pos1.getBlockZ() - pos2.getBlockZ()) + 1;

        return dx * dy * dz;
    }

    public int getRemainingBlocks(String name) {
        Location[] area = getMineArea(name);
        if (area == null) return 0;

        int count = 0;
        Location pos1 = area[0];
        Location pos2 = area[1];

        World world = pos1.getWorld();

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Material mat = world.getBlockAt(x, y, z).getType();
                    if (mat != Material.AIR) count++;
                }
            }
        }

        return count;
    }

    private Location[] getMineArea(String name) {
        File file = getMineFile(name);
        if (!file.exists()) return null;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        World world = Bukkit.getWorld(config.getString("world"));
        if (world == null) return null;

        Location pos1 = deserializeLocation(config.getConfigurationSection("pos1"), world);
        Location pos2 = deserializeLocation(config.getConfigurationSection("pos2"), world);

        return new Location[]{pos1, pos2};
    }


}
