package me.meowquantum.com.minas.Tasks;

import me.meowquantum.com.minas.Minas;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MineResetTask extends BukkitRunnable {

    private final Minas plugin;
    private final Map<String, Integer> timers = new HashMap<>();

    public MineResetTask(Minas plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        File minesFolder = new File(plugin.getDataFolder(), "mines");
        if (!minesFolder.exists()) return;

        for (File file : minesFolder.listFiles()) {
            if (!file.getName().endsWith(".yml")) continue;

            String mineName = file.getName().replace(".yml", "");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            if (!config.getBoolean("enable", true)) continue;

            int resetTime = config.getInt("time", -1);
            if (resetTime <= 0) continue;

            int currentTime = timers.getOrDefault(mineName, 0) + 1;

            if (currentTime >= resetTime) {
                plugin.getMineManager().fillMineGradually(mineName, 15);
                timers.put(mineName, 0);
            } else {
                timers.put(mineName, currentTime);
            }
        }
    }

    public int getTiempoRestante(String mina) {
        File file = new File(plugin.getDataFolder(), "mines/" + mina + ".yml");
        if (!file.exists()) return 0;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        int time = config.getInt("time", -1);
        if (time <= 0) return 0;

        int actual = timers.getOrDefault(mina, 0);
        return Math.max(0, time - actual);
    }

}
