package com.versuchdrei.lootfilter;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.versuchdrei.menumanager.implementations.FixedScrollableMenu;

/**
 * a menu for the lootfilters for all meterials
 * @author VersuchDrei
 * @version 1.0
 */
public class LootFilterMenu extends FixedScrollableMenu{
	
	private static List<LootFilterMenuItem> getItems(final List<Material> types) {
		final List<LootFilterMenuItem> items = new ArrayList<>(types.size());
		
		for(final Material type: types) {
			items.add(new LootFilterMenuItem(type));
		}
		
		return items;
	}

	public LootFilterMenu(final List<Material> types) {
		super("loot filter", getItems(types));
	}
	
	@Override
	protected void onClose(final Player player) {
		LootFilter.getLootFilter(player).ifPresent(filter -> filter.save(player));
	}

}
