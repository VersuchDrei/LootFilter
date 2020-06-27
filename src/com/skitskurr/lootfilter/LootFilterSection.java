package com.skitskurr.lootfilter;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_16_R1.MinecraftServer;

public class LootFilterSection {
	
	private static final String CONFIG_KEY_FILTER_CONDITION_CACHE_TIME = "filterConditionCacheTime";
	
	private static final String CONFIG_KEY_NEVER = "never";
	private static final String CONFIG_KEY_MIN_EMPTY_SLOTS = "minEmptySlots";
	private static final String CONFIG_KEY_MAX_STACKS = "maxStacks";
	private static final String CONFIG_KEY_TYPE ="type";
	private static final String CONFIG_KEY_IS_ENABLED = "isEnabled";
	
	public static Optional<LootFilterSection> getSection(final Player player, final Material type){
		final Optional<LootFilter> optionalFilter = LootFilter.getLootFilter(player);
		if(optionalFilter.isEmpty()) {
			return Optional.empty();
		}
		
		return optionalFilter.get().getSection(type);
	}
	
	static LootFilterSection fromConfig(final ConfigurationSection config) {
		final boolean never = config.getBoolean(LootFilterSection.CONFIG_KEY_NEVER);
		final int minEmptySlots = config.getInt(LootFilterSection.CONFIG_KEY_MIN_EMPTY_SLOTS);
		final int maxStacks = config.getInt(LootFilterSection.CONFIG_KEY_MAX_STACKS);
		final Material type = Material.valueOf(config.getString(LootFilterSection.CONFIG_KEY_TYPE));
		final boolean isEnabled = config.getBoolean(LootFilterSection.CONFIG_KEY_IS_ENABLED);
		
		return new LootFilterSection(never, minEmptySlots, maxStacks, type, isEnabled);
	}
	
	private boolean never;
	private int minEmptySlots;
	private int maxStacks;
	private int maxItems;
	private final Material type;
	
	private boolean isEnabled;
	private final int filterConditionCacheTime;
	private int nextCheck = 0;
	
	public LootFilterSection(final boolean never, final int minEmptySlots, final int maxStacks, final Material type, final boolean isEnabled) {
		this.never = never;
		this.minEmptySlots = minEmptySlots;
		this.maxStacks = maxStacks;
		this.maxItems = type.getMaxStackSize() * maxStacks;
		this.type = type;
		this.isEnabled = isEnabled;
		
		final Optional<Main> optionalPlugin = Main.getCurrent();
		if(optionalPlugin.isEmpty()) {
			this.filterConditionCacheTime = 0;
			return;
		}
		this.filterConditionCacheTime = optionalPlugin.get().getConfig().getInt(LootFilterSection.CONFIG_KEY_FILTER_CONDITION_CACHE_TIME);
	}

	public void writeInto(final ConfigurationSection config) {
		config.set(LootFilterSection.CONFIG_KEY_NEVER, this.never);
		config.set(LootFilterSection.CONFIG_KEY_MIN_EMPTY_SLOTS, this.minEmptySlots);
		config.set(LootFilterSection.CONFIG_KEY_MAX_STACKS, this.maxStacks);
		// when loading the yaml config from a String it is unable to find a constructor for Material
		// so we have to save it as a String value
		config.set(LootFilterSection.CONFIG_KEY_TYPE, this.type.toString());
		config.set(LootFilterSection.CONFIG_KEY_IS_ENABLED, this.isEnabled);
	}
	
	public boolean check(final Inventory inventory, final int amount) {
		if(!isEnabled) {
			return true;
		}
		
		if(this.filterConditionCacheTime != 0 && MinecraftServer.currentTick < this.nextCheck) {
			return false;
		}
		
		final boolean check = checkInner(inventory, amount);
		if(!check) {
			this.nextCheck = MinecraftServer.currentTick + filterConditionCacheTime;
		}
		
		return check;
	}
	
	private boolean checkInner(final Inventory inventory, final int amount) {
		if(this.never) {
			return false;
		}
		
		if(this.minEmptySlots == 0 && this.maxItems == 0) {
			return true;
		}
		
		int totalAmount = amount;
		int empty = 0;
		int overhead = amount;
		
		for(int i = 0; i < 36; i++) {
			final ItemStack item = inventory.getItem(i);
			if(item == null) {
				empty++;
				if(this.maxItems == 0 && empty > this.minEmptySlots) {
					return true;
				}
			} else {
				if(item.getType() == this.type) {
					if(this.maxItems != 0) {
						totalAmount += item.getAmount();
					}
					
					overhead -= (this.type.getMaxStackSize() - item.getAmount());
					if(overhead <= 0) {
						return true;
					}
				}
			}
		}
		
		if(this.minEmptySlots != 0 && empty <= this.minEmptySlots) {
			return false;
		}
		if(this.maxItems != 0 && totalAmount > this.maxItems) {
			return false;
		}
		
		return true;
	}
	
	public boolean isEnabled() {
		return this.isEnabled;
	}
	
	public boolean getNever() {
		return this.never;
	}
	
	public int getMinEmptySlots() {
		return this.minEmptySlots;
	}
	
	public int getMaxStacks() {
		return this.maxStacks;
	}
	
	public Material getType() {
		return this.type;
	}
	
	public void setEnabled(final boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
	public void setNever(final boolean never) {
		this.never = never;
	}
	
	public void setMinEmptySlots(final int minEmptySlots) {
		this.minEmptySlots = minEmptySlots;
	}
	
	public void setMaxStacks(final int maxStacks) {
		this.maxStacks = maxStacks;
		this.maxItems = type.getMaxStackSize() * maxStacks;
	}

}
