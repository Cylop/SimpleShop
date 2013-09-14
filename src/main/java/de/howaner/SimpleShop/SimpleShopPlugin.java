package de.howaner.SimpleShop;

import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleShopPlugin extends JavaPlugin {
	public static Logger log;
	private static SimpleShopPlugin instance;
	private static ShopManager manager;
	
	@Override
	public void onLoad() {
		log = this.getLogger();
		instance = this;
		manager = new ShopManager(this);
	}
	
	@Override
	public void onEnable() {
		if (log == null) log = this.getLogger();
		if (manager == null) manager = new ShopManager(this);
		manager.onEnable();
		log.info("Plugin aktiviert!");
	}
	
	@Override
	public void onDisable() {
		manager.onDisable();
		log.info("Plugin deaktiviert!");
	}
	
	public static SimpleShopPlugin getPlugin() {
		if (instance == null) {
			Plugin plugin = Bukkit.getPluginManager().getPlugin("SimpleShop");
			if (plugin != null && plugin instanceof SimpleShopPlugin)
				instance = (SimpleShopPlugin)plugin;
		}
		return instance;
	}
	
	public static ShopManager getManager() {
		return manager;
	}
	
}
