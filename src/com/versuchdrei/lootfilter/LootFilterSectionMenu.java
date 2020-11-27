package com.versuchdrei.lootfilter;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.versuchdrei.menumanager.ClickEvent;
import com.versuchdrei.menumanager.Menu;
import com.versuchdrei.menumanager.utils.ItemUtils;
import com.versuchdrei.menumanager.utils.MetadataUtils;

/**
 * a menu to configure a loot filter section
 * @author VersuchDrei
 * @version 1.0
 */
public class LootFilterSectionMenu extends Menu{
	
	private final Material type;
	private final String title;
	
	private final MaxStacksMenu maxStacksMenu;
	private final MinEmptySlotsMenu minEmptySlotsMenu;
	
	public LootFilterSectionMenu(final Material type) {
		this.type = type;
		this.title = ItemUtils.getItemName(type);
		
		this.maxStacksMenu = new MaxStacksMenu(type);
		this.minEmptySlotsMenu = new MinEmptySlotsMenu(type);
	}

	@Override
	protected Inventory getInventory(final Player player) {
		final Inventory inventory = Bukkit.createInventory(null, 27, title);
		
		final Optional<Main> optionalPlugin = Main.getCurrent();
		if(optionalPlugin.isEmpty()) {
			return inventory;
		}
		
		final Optional<LootFilter> optionalFilter = MetadataUtils.getMetadata(optionalPlugin.get(), player, LootFilter.METADATA_KEY_LOOT_FILTER, LootFilter.class);
		if(optionalFilter.isEmpty()) {
			return inventory;
		}
		
		final LootFilter filter = optionalFilter.get();
		final LootFilterSection section = filter.getSectionOrCreate(type);
		
		final boolean never = section.getNever();
		final int minEmptySlots = section.getMinEmptySlots();
		final int maxStacks = section.getMaxStacks();
		
		final ItemStack neverItem = ItemUtils.newItem(Material.BARRIER, "never pick up", never ? "§7click to disable" : "§7click to enable");
		if(never) {
			ItemUtils.enchant(neverItem);
		}
		
		ItemStack minEmptySlotsItem;
		if(minEmptySlots == 0) {
			minEmptySlotsItem = ItemUtils.newItem(Material.CHEST, "minimum free space", "§7don't pick up the item if only this many empty slots remain", "§7left click to configure");
		} else {
			final String[] lore = {"§7don't pick up the item if only §8" + minEmptySlots + "§7 empty slots remain", "§7left click to configure", "§7right click to disable"};
			minEmptySlotsItem = ItemUtils.enchantAndSetNameAndLore(new ItemStack(Material.TRAPPED_CHEST, minEmptySlots), "minimum free space", lore);
		}
		
		ItemStack maxStacksItem;
		if(maxStacks == 0) {
			maxStacksItem = ItemUtils.newItem(this.type, "maximum stacks to pick up", "§7don't pick up the item if you already have this many stacks of it", "§7left click to configure");
		} else {
			final String[] lore = {"§7don't pick up the item if you already have §8" + maxStacks + "§7 stacks of it", "§7left click to configure", "§7right click to disable"};
			maxStacksItem = ItemUtils.enchantAndSetNameAndLore(new ItemStack(this.type, maxStacks), "maximum stacks to pick up", lore);
		}
		
		final ItemStack backItem = ItemUtils.newItem(Material.OAK_DOOR, "go back", "§7return to the loot filter menu");
		
		inventory.setItem(10, neverItem);
		inventory.setItem(12, minEmptySlotsItem);
		inventory.setItem(14, maxStacksItem);
		inventory.setItem(16, backItem);
		
		return inventory;
	}
	
	@Override
	public void onClick(final ClickEvent event) {
		final Player player = event.getPlayer();
		switch(event.getSlot()) {
		case 10:
			LootFilterSection.getSection(player, this.type).ifPresent(section -> section.setNever(!section.getNever()));
			super.redraw(player);
			break;
		case 12:
			switch(event.getClickType()) {
			case LEFT:
			case SHIFT_LEFT:
				this.minEmptySlotsMenu.open(player);
				break;
			case RIGHT:
			case SHIFT_RIGHT:
				LootFilterSection.getSection(player, this.type).ifPresent(section -> section.setMinEmptySlots(0));
				super.redraw(player);
				break;
			default:
				break;
			}
			break;
		case 14:
			switch(event.getClickType()) {
			case LEFT:
			case SHIFT_LEFT:
				this.maxStacksMenu.open(player);
				break;
			case RIGHT:
			case SHIFT_RIGHT:
				LootFilterSection.getSection(player, this.type).ifPresent(section -> section.setMaxStacks(0));
				super.redraw(player);
				break;
			default:
				break;
			}
			break;
		case 16:
			super.back(player);
			break;
		}
	}
	
	@Override
	protected void onClose(final Player player) {
		LootFilterSection.getSection(player, this.type).ifPresent(section -> section.setEnabled(true));
	}

}
