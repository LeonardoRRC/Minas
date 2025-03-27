package me.meowquantum.com.minas.Events;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WandListener implements Listener {

    private final Map<UUID, Location[]> selections = new HashMap<>();

    public Map<UUID, Location[]> getSelections() {
        return selections;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!player.hasPermission("mines.admin")) return;

        if (item == null || item.getType() != Material.IRON_AXE) return;
        if (!item.hasItemMeta() || !ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals("Selector de Mina"))
            return;

        Action action = event.getAction();
        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock == null) return;

        Location[] locs = selections.getOrDefault(player.getUniqueId(), new Location[2]);

        if (action == Action.LEFT_CLICK_BLOCK) {
            locs[0] = clickedBlock.getLocation();
            player.sendMessage(ChatColor.YELLOW + "Primer punto seleccionado: " + locToString(locs[0]));
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            locs[1] = clickedBlock.getLocation();
            player.sendMessage(ChatColor.YELLOW + "Segundo punto seleccionado: " + locToString(locs[1]));
        }

        selections.put(player.getUniqueId(), locs);
        event.setCancelled(true);
    }

    private String locToString(Location loc) {
        return "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
    }
}
