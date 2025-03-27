package me.meowquantum.com.minas.Commands;

import me.meowquantum.com.minas.Minas;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MineCommand implements CommandExecutor {

    private final Minas plugin;

    public MineCommand(Minas plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser usado por jugadores.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("mines.admin")) {
            player.sendMessage(ChatColor.RED + "No tienes permiso para usar los comandos de minas.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("wand")) {
            ItemStack wand = new ItemStack(Material.IRON_AXE);
            ItemMeta meta = wand.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "Selector de Mina");
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Selecciona los dos puntos",
                    ChatColor.GRAY + "para definir una mina."
            ));
            wand.setItemMeta(meta);

            player.getInventory().addItem(wand);
            player.sendMessage(ChatColor.GREEN + "¡Has recibido el hacha para seleccionar la mina!");
            return true;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            String name = args[1];
            Location[] locs = plugin.getWandListener().getSelections().get(player.getUniqueId());

            if (locs == null || locs[0] == null || locs[1] == null) {
                player.sendMessage(ChatColor.RED + "¡Primero selecciona ambos puntos!");
                return true;
            }

            File file = new File(plugin.getDataFolder(), "mines/" + name + ".yml");
            file.getParentFile().mkdirs();

            YamlConfiguration config = new YamlConfiguration();
            config.set("world", locs[0].getWorld().getName());
            config.set("pos1", serializeLocation(locs[0]));
            config.set("pos2", serializeLocation(locs[1]));

            config.set("enable", true);
            config.set("time", 300);
            config.set("blocks", new HashMap<>());

            try {
                config.save(file);
                player.sendMessage(ChatColor.GREEN + "¡Mina '" + name + "' creada correctamente!");
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Error al guardar la mina.");
                e.printStackTrace();
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("addblock")) {
            String mineName = args[1];
            File file = new File(plugin.getDataFolder(), "mines/" + mineName + ".yml");

            if (!file.exists()) {
                player.sendMessage(ChatColor.RED + "La mina '" + mineName + "' no existe.");
                return true;
            }

            ItemStack hand = player.getItemInHand();
            if (hand == null || hand.getType() == Material.AIR) {
                player.sendMessage(ChatColor.RED + "Debes tener un bloque en la mano.");
                return true;
            }

            Material type = hand.getType();
            if (!type.isBlock()) {
                player.sendMessage(ChatColor.RED + "Ese no es un bloque válido.");
                return true;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            String blockName = type.name();
            int defaultPercent = 10;

            config.set("blocks." + blockName, defaultPercent);

            try {
                config.save(file);
                player.sendMessage(ChatColor.GREEN + "Bloque " + blockName + " agregado a la mina '" + mineName + "' con un porcentaje de 10%.");
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Error al guardar la mina.");
                e.printStackTrace();
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("settimereset")) {
            String mineName = args[1];
            File file = new File(plugin.getDataFolder(), "mines/" + mineName + ".yml");

            if (!file.exists()) {
                player.sendMessage(ChatColor.RED + "La mina '" + mineName + "' no existe.");
                return true;
            }

            int seconds;
            try {
                seconds = Integer.parseInt(args[2]);
                if (seconds <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Debes ingresar un número válido mayor a 0.");
                return true;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.set("time", seconds);

            try {
                config.save(file);
                player.sendMessage(ChatColor.GREEN + "Tiempo de reinicio de la mina '" + mineName + "' actualizado a " + seconds + " segundos.");
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Error al guardar la mina.");
                e.printStackTrace();
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("forcereset")) {
            String mineName = args[1];
            File file = plugin.getMineManager().getMineFile(mineName);

            if (!file.exists()) {
                player.sendMessage(ChatColor.RED + "La mina '" + mineName + "' no existe.");
                return true;
            }

            plugin.getMineManager().fillMine(mineName);
            player.sendMessage(ChatColor.GREEN + "¡Mina '" + mineName + "' reiniciada correctamente!");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            String mineName = args[1];
            File file = plugin.getMineManager().getMineFile(mineName);

            if (!file.exists()) {
                player.sendMessage(ChatColor.RED + "La mina '" + mineName + "' no existe.");
                return true;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            boolean current = config.getBoolean("enable", true);
            boolean newState = !current;

            config.set("enable", newState);
            try {
                config.save(file);
                player.sendMessage(ChatColor.GREEN + "La mina '" + mineName + "' ahora está " + (newState ? "activada." : "desactivada."));
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "Hubo un error al guardar la mina.");
                e.printStackTrace();
            }
        } else {
            player.sendMessage(ChatColor.RED + "Uso: /mine wand o /mine create <nombre>");
        }
        return true;
    }

    private Map<String, Object> serializeLocation(Location loc) {
        Map<String, Object> map = new HashMap<>();
        map.put("x", loc.getBlockX());
        map.put("y", loc.getBlockY());
        map.put("z", loc.getBlockZ());
        return map;
    }
}
