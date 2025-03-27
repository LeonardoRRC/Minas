package me.meowquantum.com.minas.Placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.meowquantum.com.minas.Minas;
import org.bukkit.OfflinePlayer;

public class MinaExpansion extends PlaceholderExpansion {

    private final Minas plugin;

    public MinaExpansion(Minas plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "minas";
    }

    @Override
    public String getAuthor() {
        return "TuNombre";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        // %minas_timereset_<mina>%
        if (identifier.startsWith("timereset_")) {
            String mina = identifier.replace("timereset_", "");
            int restante = plugin.getResetTask().getTiempoRestante(mina);
            return formatTiempo(restante);
        }

        // %minas_totalblocks_<mina>%
        if (identifier.startsWith("totalblocks_")) {
            String mina = identifier.replace("totalblocks_", "");
            return String.valueOf(plugin.getMineManager().getTotalBlocks(mina));
        }

        // %minas_remainingblocks_<mina>%
        if (identifier.startsWith("remainingblocks_")) {
            String mina = identifier.replace("remainingblocks_", "");
            return String.valueOf(plugin.getMineManager().getRemainingBlocks(mina));
        }

        return null;
    }

    private String formatTiempo(int segundos) {
        int days = segundos / 86400;
        int hours = (segundos % 86400) / 3600;
        int minutes = (segundos % 3600) / 60;
        int secs = segundos % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        sb.append(secs).append("s");

        return sb.toString().trim();
    }
}
