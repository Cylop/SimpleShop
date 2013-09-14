package de.howaner.SimpleShop;

import de.howaner.SimpleShop.util.ShopItem;
import de.howaner.SimpleShop.util.ShopKategorie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ShopListener implements Listener {
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Inventory inv = event.getInventory();
		if (!ShopManager.isShopInventory(inv)) return;
		event.setCancelled(true);
		if (!(event.getWhoClicked() instanceof Player)) return;
		Player player = (Player)event.getWhoClicked();
		ItemStack item = event.getCurrentItem();
		if (item == null || item.getType() == Material.AIR || item.getItemMeta() == null) return;
		
		//Kategorie
		if (inv.getTitle().equals( ShopManager.getTitle() + " §6Kategorien" )) {
			ShopKategorie kat = SimpleShopPlugin.getManager().getKategorieByName(item.getItemMeta().getDisplayName());
			if (kat == null) return;
			player.closeInventory();
			//Open new Inventory
			Inventory newInv = Bukkit.createInventory(player, 36, ShopManager.getTitle() + " §6" + kat.getName());
			newInv.setContents(SimpleShopPlugin.getManager().getItemContents(kat));
			player.openInventory(newInv);
			return;
		}
		
		//Item
		for (ShopKategorie kat : SimpleShopPlugin.getManager().getKategorien()) {
			if (inv.getTitle().equals( ShopManager.getTitle() + " §6" + kat.getName())) {
				//Zurück
				if (item.getItemMeta().getDisplayName() != null && item.getItemMeta().getDisplayName().equals("§bKategorien") && item.getType() == Material.EMERALD) {
					player.closeInventory();
					SimpleShopPlugin.getManager().openShop(player);
					return;
				}
				//Item kaufen
				if (item.getItemMeta().getDisplayName() == null && item.getItemMeta().getLore() != null) {
					for (ShopItem item2 : kat.getItems()) {
						if (item.getType() != item2.getMaterial()) continue;
						
						int menge = SimpleShopPlugin.getManager().getMenge(event.getClick());
						double preis = item2.getPreis() * menge;
						
						//Verkaufen
						if (kat.getName().equals("Verkaufen")) {
							ItemStack giveItem = new ItemStack(item2.getMaterial(), menge);
							if (!player.getInventory().contains(item2.getMaterial(), menge)) {
								player.sendMessage(ChatColor.RED + "Sie haben keine " + ChatColor.WHITE + menge + " " + item2.getMaterial().name() + ChatColor.RED + " in ihrem Inventar!");
								return;
							}
							ShopManager.economy.depositPlayer(player.getName(), preis);
							SimpleShopPlugin.getManager().removeInventoryItem(giveItem, player.getInventory());
							
							player.sendMessage(ChatColor.GOLD + "Sie haben " + ChatColor.WHITE + menge + " " + item2.getMaterial().name() + ChatColor.GOLD + " für " + ChatColor.WHITE + ShopManager.economy.format(preis) + ChatColor.GOLD + " verkauft!");
							return;
						}
						
						//Kaufen
						if (!ShopManager.economy.has(player.getName(), preis)) {
							player.sendMessage(ChatColor.RED + "Nicht genügend Geld!");
							return;
						}
						ShopManager.economy.withdrawPlayer(player.getName(), preis);
						ItemStack giveItem = new ItemStack(item2.getMaterial(), menge);
						player.getInventory().addItem(giveItem);
						player.sendMessage(ChatColor.GOLD + "Sie haben " + ChatColor.WHITE + menge + " " + item2.getMaterial().name() + ChatColor.GOLD + " gekauft!");
						return;
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onInventoryMoveItem(InventoryMoveItemEvent event) {
		
	}
	
	@EventHandler
	public void onInventoryInteract(InventoryInteractEvent event) {
		
	}
	
}
