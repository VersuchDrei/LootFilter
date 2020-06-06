package com.skitskurr.lootfilter;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.skitskurr.menumanager.ClickEvent;
import com.skitskurr.menumanager.Menu;
import com.skitskurr.menumanager.utils.ItemUtils;

public class MaxStacksMenu extends Menu{
	
	private final Inventory inventory;
	private final Material type;
	
	public MaxStacksMenu(final Material type) {
		this.inventory = Bukkit.createInventory(null, 36, "maximum stacks to pick up");
		this.type = type;
		
		final int stackSize = type.getMaxStackSize();
		for(int i = 0; i < 36; i++) {
			this.inventory.setItem(i, ItemUtils.setName(new ItemStack(type, i + 1 > stackSize ? 1 : i + 1), "maximum " + (i + 1) + " " + (i == 0 ? "stack" : "stacks")));
		}
	}
	
	@Override
	protected Inventory getInventory(final Player player) {
		return this.inventory;
	}
	
	@Override
	public void onClick(final ClickEvent event) {
		final Player player = event.getPlayer();
		LootFilterSection.getSection(player, this.type).ifPresent(section -> section.setMaxStacks(event.getSlot() + 1));
		super.back(player);
	}

}
