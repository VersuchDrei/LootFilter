package com.skitskurr.lootfilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.EnumUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{
	
	private static final String CONFIG_KEY_MATERIALS = "materials";
	
	private static Main current;
	
	private LootFilterMenu menu;
	private final List<Material> materials = new ArrayList<Material>();
	
	public static Optional<Main> getCurrent(){
		if(current == null) {
			return Optional.empty();
		}
		
		return Optional.of(current);
	}
	
	@Override
	public void onEnable() {
		super.saveDefaultConfig();
		Bukkit.getPluginManager().registerEvents(new EventListener(this), this);
		
		final List<String> materialStrings = super.getConfig().getStringList(Main.CONFIG_KEY_MATERIALS);
		for(final String materialString: materialStrings) {
			final String upperMaterialString = materialString.toUpperCase();
			if(EnumUtils.isValidEnum(Material.class, upperMaterialString)) {
				materials.add(Material.valueOf(upperMaterialString));
			}
		}
		menu = new LootFilterMenu(materials);
		current = this;
	}
	
	@Override
	public void onDisable() {
		current = null;
	}
	
	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label,
			final String[] args) {
		
		if(label.equalsIgnoreCase("lootfilter")) {
			
			if(!(sender instanceof Player)) {
				sender.sendMessage("Only players can setup loot filters.");
				return true;
			}
			
			this.menu.open((Player) sender);
			
			return true;
		}
		
		return false;
	}
	
	List<Material> getMaterials(){
		return this.materials;
	}

}
