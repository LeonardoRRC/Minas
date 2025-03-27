package me.meowquantum.com.minas;

import me.meowquantum.com.minas.Api.MineAPI;
import me.meowquantum.com.minas.Commands.MineCommand;
import me.meowquantum.com.minas.Events.WandListener;
import me.meowquantum.com.minas.Managers.MineManager;
import me.meowquantum.com.minas.Placeholders.MinaExpansion;
import me.meowquantum.com.minas.Tasks.MineResetTask;
import org.bukkit.plugin.java.JavaPlugin;

public final class Minas extends JavaPlugin {

    private WandListener wandListener;
    private MineManager mineManager;
    private MineResetTask resetTask;
    private MineAPI mineAPI;

    @Override
    public void onEnable() {
        this.wandListener = new WandListener();
        this.mineManager = new MineManager(getDataFolder());
        this.resetTask = new MineResetTask(this);
        this.mineAPI = new MineAPI(mineManager);
        this.resetTask.runTaskTimer(this, 20L, 20L);

        getCommand("mine").setExecutor(new MineCommand(this));
        getServer().getPluginManager().registerEvents(wandListener, this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new MinaExpansion(this).register();
        }
    }

    public WandListener getWandListener() {
        return wandListener;
    }

    public MineManager getMineManager() {
        return mineManager;
    }

    public MineResetTask getResetTask() {
        return resetTask;
    }

    public void setResetTask(MineResetTask resetTask) {
        this.resetTask = resetTask;
    }

    public MineAPI getMineAPI() {
        return mineAPI;
    }
}
