/*
 *  Copyright (C) 2014 Roberto Baldin (snk7891)
 *  
 *  This file is part of the Caving Bukkit plugin.
 *
 *  Caving is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Caving is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Caving, in a file named COPYING.  If not, see
 *  <http://www.gnu.org/licenses/>.
 *  
*/


package com.robgadgets.mc.caving;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Caving - Miscellaneous enhancements
 * 
 * Here there are some changes in the normal gameplay.
 * At the moment, these improvements are:
 * 
 *  - Pumpkins cannot be equipped
 *  - Fire res potions cannot be brewed
 *  
 *  @author Roberto "Rob" Baldin
 */
public class GeneralMechanics implements Listener {

	private JavaPlugin plugin;
	
	private final boolean preventEquippingPumpkins, forbidFireResPotions;

	/**
	 * Starts the misc enhancements, registering the new events
	 * 
	 * @param instance Plugin reference
	 */
	public GeneralMechanics(JavaPlugin instance) {

		plugin = instance;
		
		ConfigurationSection general = plugin.getConfig().getConfigurationSection("general");
		preventEquippingPumpkins = general.getBoolean("preventEquippingPumpkins", true);
		forbidFireResPotions = general.getBoolean("forbidFireResPotions", true);

		plugin.getServer().getPluginManager().registerEvents(this, plugin);

	} // GeneralMechanics

	/**
	 * 
	 * Forbids creation of Fire Res potions.
	 * First, we look at the ingredient. If it's a MAGMA_CREAM,
	 * then a Fire Res potion has been created.
	 * In this case, we remove the crafted potion
	 * and we create a little explosion where the
	 * brewstand is, so it looks like a "failed experiment".
	 * 
	 * @param evt
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onBrewCompleted(BrewEvent evt) {
		if(forbidFireResPotions) {
			Block block = evt.getBlock();
			BrewerInventory inv = evt.getContents();
			ItemStack ingredient = inv.getIngredient();
			if (ingredient.getType() == Material.MAGMA_CREAM) {
				evt.setCancelled(true); // Prevent creation of the fire res potion
				Utils.createDelayedExplosion(plugin, block, 10L); // Explode after 0.5s
			} // if
		} // if
	} // onBrewCompleted

	/**
	 * Forbids the equipping of pumpkins.
	 * 
	 * Whenever the inventory is closed by a player, we check if he's
	 * wearing a pumpkin. If so, we delete it and create a dropped item in his
	 * place. We also send a warning to the player.
	 * 
	 * @param evt
	 */
	// Prevent equipping pumpkins
	@EventHandler(priority = EventPriority.LOWEST)
	public void onInventoryClose(InventoryCloseEvent evt) {

		if (preventEquippingPumpkins && evt.getPlayer() instanceof Player) {

			Player p = (Player) evt.getPlayer();
			if (p.getInventory().getHelmet() != null && p.getInventory().getHelmet().getType().equals(Material.PUMPKIN)) {
				p.getWorld().dropItemNaturally(p.getLocation(),p.getInventory().getHelmet());
				p.getInventory().setHelmet(null);
				p.sendMessage("It seems that you can't wear pumpkins this time..");
			} // if

		} // if

	} // onInventoryClose

} // GeneralMechanics
