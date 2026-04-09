package ru.fantastworld.changerVault;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChangerCommand implements CommandExecutor {

    private final ChangerVault plugin;

    public ChangerCommand(ChangerVault plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Utils.color(" &f[&cОшибка&f]&c Эта команда только для игроков"));
            return true;
        }

        if (!player.hasPermission("changervault.toggle")) {
            String noPermMsg = plugin.getConfig().getString("messages.no-permission",
                    " &f[&cОшибка&f]&c У вас недостаточно прав");
            player.sendMessage(Utils.color(noPermMsg));
            return true;
        }

        plugin.toggleActivated();
        boolean activated = plugin.isActivated();

        String messagePath = activated ? "messages.enabled" : "messages.disabled";
        String defaultMsg = activated ?
                " &f[&aУспех&f]&a Режим изменения шансов включен" :
                " &f[&cУспех&f]&a Режим изменения шансов выключен";
        String message = plugin.getConfig().getString(messagePath, defaultMsg);

        player.sendMessage(Utils.color(message));

        plugin.getConfig().set("activated", activated);
        plugin.saveConfig();

        return true;
    }
}