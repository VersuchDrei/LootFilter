package com.versuchdrei.lootfilter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.versuchdrei.datamanager.DataManager;
import com.versuchdrei.menumanager.utils.MetadataUtils;

/**
 * a lootfilter, which gets attached to a player via metadata
 * @author VersuchDrei
 * @version 1.0
 */
public class LootFilter {
	
	static final String METADATA_KEY_LOOT_FILTER = "lootFilter";
	
	private static final String PLUGIN_KEY = "versuchdrei_lootfilter";
	private static final String DATA_KEY_LOOT_FILTER = "lootfilter";
	
	public static Optional<LootFilter> getLootFilter(final Player player) {
		final Optional<Main> optionalPlugin = Main.getCurrent();
		if(optionalPlugin.isEmpty()) {
			return Optional.empty();
		}
		
		return MetadataUtils.getMetadata(optionalPlugin.get(), player, LootFilter.METADATA_KEY_LOOT_FILTER, LootFilter.class);
	}
	
	public static LootFilter loadLootFilter(final Player player) {
		final Optional<String> optionalFilterString = DataManager.Players.getString(player, LootFilter.PLUGIN_KEY, LootFilter.DATA_KEY_LOOT_FILTER);
		if(!optionalFilterString.isPresent()) {
			return new LootFilter();
		}
		
		final YamlConfiguration config = new YamlConfiguration();
		try {
			config.loadFromString(optionalFilterString.get());
		} catch (final InvalidConfigurationException ex) {
			ex.printStackTrace();
			return new LootFilter();
		}
		return LootFilter.fromConfig(config);
	}
	
	private static LootFilter fromConfig(final YamlConfiguration config) {
		final LootFilter filter = new LootFilter();
		
		final Set<String> keys = config.getKeys(false);
		for(final String key: keys) {
			filter.addSection(LootFilterSection.fromConfig(config.getConfigurationSection(key)));
		}
		
		return filter;
	}
	
	private final Map<Material, LootFilterSection> filterSections = new HashMap<>();
	
	private YamlConfiguration toConfig() {
		final YamlConfiguration config = new YamlConfiguration();
		for(final LootFilterSection section: filterSections.values()) {
			final ConfigurationSection configSection = config.createSection(section.getType().toString());
			section.writeInto(configSection);
		}
		return config;
	}
	
	@Override
	public String toString() {
		return toConfig().saveToString();
	}
	
	public void addSection(final LootFilterSection section) {
		filterSections.put(section.getType(), section);
	}
	
	public boolean check(final Inventory inventory, final Item item) {
		final ItemStack itemStack = item.getItemStack();
		final LootFilterSection section = filterSections.get(itemStack.getType());
		if(section == null) {
			return true;
		}
		return section.check(inventory, itemStack.getAmount());
	}
	
	public void save(final Player player) {
		DataManager.Players.set(player, LootFilter.PLUGIN_KEY, LootFilter.DATA_KEY_LOOT_FILTER, toString());
	}
	
	public Optional<LootFilterSection> getSection(final Material type) {
		final LootFilterSection section = filterSections.get(type);
		if(section == null) {
			return Optional.empty();
		}
		
		return Optional.of(section);
	}
	
	public LootFilterSection getSectionOrCreate(final Material type) {
		if(filterSections.containsKey(type)) {
			return filterSections.get(type);
		} else {
			final LootFilterSection section = new LootFilterSection(false, 0, 0, type, false);
			filterSections.put(type, section);
			return section;
		}
	}

}
