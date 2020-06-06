package com.skitskurr.lootfilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.skitskurr.menumanager.implementations.MenuItem;
import com.skitskurr.menumanager.implementations.MenuItemClickEvent;
import com.skitskurr.menumanager.utils.ItemUtils;

public class LootFilterMenuItem extends MenuItem{
	
	private final Material type;
	private final String filterText;
	private final LootFilterSectionMenu menu;
	
	public LootFilterMenuItem(final Material type) {
		this.type = type;
		this.filterText = ItemUtils.getItemName(type);
		this.menu = new LootFilterSectionMenu(type);
	}

	@Override
	protected boolean filter(final String filter) {
		return this.filterText.contains(filter);
	}

	@Override
	protected ItemStack getItem(final Player player) {
		final Optional<LootFilterSection> optionalSection = LootFilterSection.getSection(player, this.type);
		if(optionalSection.isEmpty()) {
			return ItemUtils.setLore(new ItemStack(this.type), "§7not configured", "§7left click to configure");
		} else {
			final LootFilterSection section = optionalSection.get();
			if(!section.isEnabled()) {
				return ItemUtils.setLore(new ItemStack(this.type), "§7disabled", "§7left click to configure", "§7right click to enable");
			}
			
			if(section.getNever()) {
				return ItemUtils.enchantAndSetLore(new ItemStack(this.type), "§7never pick up", "§7left click to configure", "§7right click to disable");
			}
			final int minEmptySlots = section.getMinEmptySlots();
			final int maxStacks = section.getMaxStacks();
			if(minEmptySlots + maxStacks == 0) {
				return ItemUtils.setLore(new ItemStack(this.type), "§7always pick up", "§7left click to configure");
			}
			final List<String> lore = new ArrayList<>();
			lore.add("§7don't pick up if:");
			if(minEmptySlots != 0) {
				lore.add("§7- only §8" + minEmptySlots + "§7 empty slots remain");
			}
			if(maxStacks != 0) {
				lore.add("§7- you already have §8" + maxStacks + "§7 stacks of it");
			}
			lore.add("§7left click to configure");
			lore.add("§7right click to disable");
			return ItemUtils.enchantAndSetLore(new ItemStack(this.type), lore);
		}
	}
	
	@Override
	protected void onClick(final MenuItemClickEvent event) {
		switch(event.getType()) {
		case LEFT:
		case SHIFT_LEFT:
			menu.open(event.getPlayer());
			break;
		case RIGHT:
		case SHIFT_RIGHT:
			LootFilterSection.getSection(event.getPlayer(), this.type).ifPresent(section -> section.setEnabled(!section.isEnabled()));
			event.setRedraw(true);
			break;
		default:
			break;
		}
	}

}
