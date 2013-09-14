package de.howaner.SimpleShop.command;

import de.howaner.SimpleShop.SimpleShopPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Dieser Befehl ist nur für Spieler!");
			return true;
		}
		Player player = (Player)sender;
		if (args.length != 0) {
			player.sendMessage(ChatColor.RED + "Benutzung: /shop");
			return true;
		}
		if (!player.hasPermission("SimpleShop.use")) {
			player.sendMessage(ChatColor.RED + "Keine Rechte!");
			return true;
		}
		player.closeInventory();
		SimpleShopPlugin.getManager().openShop(player);
		return true;
	}
	
}
