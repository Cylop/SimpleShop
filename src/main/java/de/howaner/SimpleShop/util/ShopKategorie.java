package de.howaner.SimpleShop.util;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class ShopKategorie {
	private final String name;
	private final Material item;
	private List<ShopItem> items = new ArrayList<ShopItem>();
	
	public ShopKategorie(String name, Material item) {
		this.name = name;
		this.item = item;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Material getItem() {
		return this.item;
	}
	
	public List<ShopItem> getItems() {
		return this.items;
	}
	
	public void addItem(ShopItem item) {
		this.items.add(item);
	}
	
	public void removeItem(ShopItem item) {
		this.items.remove(item);
	}
	
}
