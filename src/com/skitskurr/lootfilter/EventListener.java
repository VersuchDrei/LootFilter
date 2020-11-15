package com.skitskurr.lootfilter;

import java.util.Optional;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.versuchdrei.menumanager.utils.MetadataUtils;

/**
 * the event listener of the loot filter, 
 * handles pickup events and cancels them if they fall through the filter
 * @author VersuchDrei
 * @version 1.0
 */
public class EventListener implements Listener{

	private static final String CONFIG_KEY_DELETE_FILTERED_ITEMS = "deleteFilteredItems";
	private static final String CONFIG_KEY_FILTERED_ITEM_PICKUP_DELAY = "filteredItemPickupDelay";
	
	private final Main plugin;
	
	private final boolean deleteFilteredItems;
	private final int filteredItemPickupDelay;
	
	public EventListener(final Main plugin) {
		this.plugin = plugin;
		
		final FileConfiguration config = plugin.getConfig();
		this.deleteFilteredItems = config.getBoolean(EventListener.CONFIG_KEY_DELETE_FILTERED_ITEMS);
		this.filteredItemPickupDelay = config.getInt(EventListener.CONFIG_KEY_FILTERED_ITEM_PICKUP_DELAY);
	}
	
	@EventHandler
	public void onJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		player.setMetadata(LootFilter.METADATA_KEY_LOOT_FILTER, new FixedMetadataValue(this.plugin, LootFilter.loadLootFilter(player)));
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onPickup(final EntityPickupItemEvent event) {
		final Entity entity = event.getEntity();
		if(!(entity instanceof Player)) {
			return;
		}
		
		final Optional<LootFilter> optionalFilter = MetadataUtils.getMetadata(plugin, entity, LootFilter.METADATA_KEY_LOOT_FILTER, LootFilter.class);
		if(optionalFilter.isEmpty()) {
			return;
		}
		
		final Player player = (Player) entity;
		final Item item = event.getItem();
		if(optionalFilter.get().check(player.getInventory(), item)) {
			return;
		}
		
		event.setCancelled(true);
		
		if(this.deleteFilteredItems) {
			item.remove();
			return;
		}
		
		item.setPickupDelay(this.filteredItemPickupDelay);
	}

}
