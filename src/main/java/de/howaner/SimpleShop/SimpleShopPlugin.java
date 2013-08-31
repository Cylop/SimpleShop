package de.howaner.SimpleShop;

import java.util.logging.Logger;
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
		if (instance == null) instance = this;
		if (manager == null) manager = new ShopManager(this);
		manager.onEnable();
		log.info("Plugin enabled!");
    }
	
    @Override 
    public void onDisable() {
        manager.onDisable();
		log.info("Plugin disabled!");
    }
	
}
