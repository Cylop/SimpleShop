package de.howaner.SimpleShop.util;

import org.bukkit.Material;

public class ShopItem {
	public Material mat;
	public double preis;
	
	public ShopItem(Material mat, double preis) {
		this.mat = mat;
		this.preis = preis;
	}
	
	public Material getMaterial() {
		return this.mat;
	}
	
	public double getPreis() {
		return this.preis;
	}
	
}
