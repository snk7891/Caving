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

import java.util.Random;
import java.util.Vector;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class WarriorsHut implements Listener {

	private JavaPlugin plugin;

	private Random r;

	private final boolean warriorsHutsEnabled;
	private final int warriorsHutsChance, warriorsHutsMinHeight,
			warriorsHutsMaxHeight, silverfishChance, maxItemInChest, enderPearlChance;

	private final EntityType[] spawnableEntities;
	
	private final ItemStack[] items;

	private static Building hut;

	public WarriorsHut(JavaPlugin instance) {
		plugin = instance;

		ConfigurationSection whuts = plugin.getConfig().getConfigurationSection("warriorsHuts");
		warriorsHutsEnabled = whuts.getBoolean("enabled", true);
		warriorsHutsChance = whuts.getInt("chance", 45);
		warriorsHutsMinHeight = whuts.getInt("minHeight", 6);
		warriorsHutsMaxHeight = whuts.getInt("maxHeight", 16);
		silverfishChance = whuts.getInt("silverfishChance", 6);
		maxItemInChest = whuts.getInt("chestMaxItemCount", 8);
		enderPearlChance = whuts.getInt("enderPearlChance", 60);

		spawnableEntities = new EntityType[] { EntityType.ZOMBIE,
				EntityType.CREEPER, EntityType.SKELETON, EntityType.SPIDER,
				EntityType.WITCH, EntityType.SILVERFISH };

		buildHut();
		
		r = new Random();
		items = getItemsInChest();

		if (warriorsHutsEnabled)
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
	} // WarriorsHut

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent evt) {
		if(evt.isNewChunk()) {
			int roll = r.nextInt(100);
			if(roll < warriorsHutsChance) {
				for(int y = warriorsHutsMinHeight; y < warriorsHutsMaxHeight; y++) {
					Chunk c = evt.getChunk();
					Block centralBlock = c.getBlock(3, y, 0);
					if(centralBlock.getBiome() != Biome.HELL && centralBlock.getBiome() != Biome.SKY &&  centralBlock.getType() == Material.AIR) {
						// The entrance is clean, so we can build the hut
						for(int i = 0; i < hut.getXSize(); i++)
							for(int j = 0; j < hut.getYSize(); j++)
								for(int k = 0; k < hut.getZSize(); k++) {
									Block block = c.getBlock(i, y + j - 1, k);
									Material mat = hut.getMaterialAt(i, j, k);
									if(mat == Material.MOB_SPAWNER) {
										// Spawner rules
										block.setType(hut.getMaterialAt(i, j, k));
										BlockState state = block.getState();
										if (state instanceof CreatureSpawner) {
											CreatureSpawner cs = (CreatureSpawner) state;
											int randomChosenEntity = r.nextInt(spawnableEntities.length);
											cs.setSpawnedType(spawnableEntities[randomChosenEntity]);
										} // if
									} else if(mat == Material.CHEST) {
										block.setType(mat);
										block.setData((byte) 2); // Rotate north
										BlockState state = block.getState();
										if (state instanceof Chest) {
											Chest chest = (Chest) state;
											Inventory inv = chest.getBlockInventory();
											int nItems = 1 + r.nextInt(Math.min(maxItemInChest, inv.getSize() - 1));
											for(int it = 0; it < nItems; it++) {
												inv.setItem(r.nextInt(inv.getSize() - 1), items[r.nextInt(items.length)]);
											} // for
											
											if(r.nextInt(100) < enderPearlChance)
												inv.setItem(inv.getSize() - 1, new ItemStack(Material.ENDER_PEARL, 1));
										} // if
									} else if(mat == Material.COBBLESTONE || mat == Material.SMOOTH_BRICK) {
										// Monster eggs rules
										int silverfishRoll = r.nextInt(100);
										if(silverfishRoll < silverfishChance) {
											block.setType(Material.MONSTER_EGGS);
											byte mData = (mat == Material.COBBLESTONE ? (byte) 1 : (byte) 2);
											block.setData(mData); // Deprecated but the only way to go as for now
										} else
											block.setType(mat);
									} else if(mat != null) {
										block.setType(mat);
									} // else if
								} // for k
						break;
					} // if
				} // for
			} // if
		} // if
	} // onChunkLoad

	private final static void buildHut() {
		hut = new Building(7, 4, 7);

		// Ground
		hut.setMaterialAt(1, 0, 1, Material.COBBLESTONE);
		hut.setMaterialAt(2, 0, 1, Material.COBBLESTONE);
		hut.setMaterialAt(3, 0, 1, Material.COBBLESTONE);
		hut.setMaterialAt(4, 0, 1, Material.COBBLESTONE);
		hut.setMaterialAt(5, 0, 1, Material.COBBLESTONE);
		hut.setMaterialAt(0, 0, 2, Material.COBBLESTONE);
		hut.setMaterialAt(1, 0, 2, Material.COBBLESTONE);
		hut.setMaterialAt(2, 0, 2, Material.COBBLESTONE);
		hut.setMaterialAt(3, 0, 2, Material.COBBLESTONE);
		hut.setMaterialAt(4, 0, 2, Material.COBBLESTONE);
		hut.setMaterialAt(5, 0, 2, Material.COBBLESTONE);
		hut.setMaterialAt(6, 0, 2, Material.COBBLESTONE);
		hut.setMaterialAt(0, 0, 3, Material.COBBLESTONE);
		hut.setMaterialAt(1, 0, 3, Material.COBBLESTONE);
		hut.setMaterialAt(2, 0, 3, Material.MOB_SPAWNER);
		hut.setMaterialAt(3, 0, 3, Material.COBBLESTONE);
		hut.setMaterialAt(4, 0, 3, Material.MOB_SPAWNER);
		hut.setMaterialAt(5, 0, 3, Material.COBBLESTONE);
		hut.setMaterialAt(6, 0, 3, Material.COBBLESTONE);
		hut.setMaterialAt(0, 0, 4, Material.COBBLESTONE);
		hut.setMaterialAt(1, 0, 4, Material.COBBLESTONE);
		hut.setMaterialAt(2, 0, 4, Material.COBBLESTONE);
		hut.setMaterialAt(3, 0, 4, Material.COBBLESTONE);
		hut.setMaterialAt(4, 0, 4, Material.COBBLESTONE);
		hut.setMaterialAt(5, 0, 4, Material.COBBLESTONE);
		hut.setMaterialAt(6, 0, 4, Material.COBBLESTONE);
		hut.setMaterialAt(1, 0, 5, Material.COBBLESTONE);
		hut.setMaterialAt(2, 0, 5, Material.COBBLESTONE);
		hut.setMaterialAt(3, 0, 5, Material.COBBLESTONE);
		hut.setMaterialAt(4, 0, 5, Material.COBBLESTONE);
		hut.setMaterialAt(5, 0, 5, Material.COBBLESTONE);
		hut.setMaterialAt(2, 0, 6, Material.COBBLESTONE);
		hut.setMaterialAt(3, 0, 6, Material.COBBLESTONE);
		hut.setMaterialAt(4, 0, 6, Material.COBBLESTONE);

		// First level
		hut.setMaterialAt(1, 1, 1, Material.COBBLESTONE);
		hut.setMaterialAt(2, 1, 1, Material.AIR);
		hut.setMaterialAt(3, 1, 1, Material.AIR);
		hut.setMaterialAt(4, 1, 1, Material.AIR);
		hut.setMaterialAt(5, 1, 1, Material.COBBLESTONE);
		hut.setMaterialAt(0, 1, 2, Material.COBBLESTONE);
		hut.setMaterialAt(1, 1, 2, Material.AIR);
		hut.setMaterialAt(2, 1, 2, Material.AIR);
		hut.setMaterialAt(3, 1, 2, Material.AIR);
		hut.setMaterialAt(4, 1, 2, Material.AIR);
		hut.setMaterialAt(5, 1, 2, Material.AIR);
		hut.setMaterialAt(6, 1, 2, Material.COBBLESTONE);
		hut.setMaterialAt(0, 1, 3, Material.COBBLESTONE);
		hut.setMaterialAt(1, 1, 3, Material.AIR);
		hut.setMaterialAt(2, 1, 3, Material.AIR);
		hut.setMaterialAt(3, 1, 3, Material.AIR);
		hut.setMaterialAt(4, 1, 3, Material.AIR);
		hut.setMaterialAt(5, 1, 3, Material.AIR);
		hut.setMaterialAt(6, 1, 3, Material.COBBLESTONE);
		hut.setMaterialAt(0, 1, 4, Material.COBBLESTONE);
		hut.setMaterialAt(1, 1, 4, Material.AIR);
		hut.setMaterialAt(2, 1, 4, Material.AIR);
		hut.setMaterialAt(3, 1, 4, Material.AIR);
		hut.setMaterialAt(4, 1, 4, Material.AIR);
		hut.setMaterialAt(5, 1, 4, Material.AIR);
		hut.setMaterialAt(6, 1, 4, Material.COBBLESTONE);
		hut.setMaterialAt(1, 1, 5, Material.COBBLESTONE);
		hut.setMaterialAt(2, 1, 5, Material.BOOKSHELF);
		hut.setMaterialAt(3, 1, 5, Material.CHEST);
		hut.setMaterialAt(4, 1, 5, Material.BOOKSHELF);
		hut.setMaterialAt(5, 1, 5, Material.COBBLESTONE);
		hut.setMaterialAt(2, 1, 6, Material.COBBLESTONE);
		hut.setMaterialAt(3, 1, 6, Material.COBBLESTONE);
		hut.setMaterialAt(4, 1, 6, Material.COBBLESTONE);

		// Second level
		hut.setMaterialAt(1, 2, 1, Material.COBBLESTONE);
		hut.setMaterialAt(2, 2, 1, Material.AIR);
		hut.setMaterialAt(3, 2, 1, Material.AIR);
		hut.setMaterialAt(4, 2, 1, Material.AIR);
		hut.setMaterialAt(5, 2, 1, Material.COBBLESTONE);
		hut.setMaterialAt(0, 2, 2, Material.COBBLESTONE);
		hut.setMaterialAt(1, 2, 2, Material.AIR);
		hut.setMaterialAt(2, 2, 2, Material.AIR);
		hut.setMaterialAt(3, 2, 2, Material.AIR);
		hut.setMaterialAt(4, 2, 2, Material.AIR);
		hut.setMaterialAt(5, 2, 2, Material.AIR);
		hut.setMaterialAt(6, 2, 2, Material.COBBLESTONE);
		hut.setMaterialAt(0, 2, 3, Material.COBBLESTONE);
		hut.setMaterialAt(1, 2, 3, Material.AIR);
		hut.setMaterialAt(2, 2, 3, Material.AIR);
		hut.setMaterialAt(3, 2, 3, Material.AIR);
		hut.setMaterialAt(4, 2, 3, Material.AIR);
		hut.setMaterialAt(5, 2, 3, Material.AIR);
		hut.setMaterialAt(6, 2, 3, Material.COBBLESTONE);
		hut.setMaterialAt(0, 2, 4, Material.COBBLESTONE);
		hut.setMaterialAt(1, 2, 4, Material.AIR);
		hut.setMaterialAt(2, 2, 4, Material.AIR);
		hut.setMaterialAt(3, 2, 4, Material.AIR);
		hut.setMaterialAt(4, 2, 4, Material.AIR);
		hut.setMaterialAt(5, 2, 4, Material.AIR);
		hut.setMaterialAt(6, 2, 4, Material.COBBLESTONE);
		hut.setMaterialAt(1, 2, 5, Material.COBBLESTONE);
		hut.setMaterialAt(2, 2, 5, Material.AIR);
		hut.setMaterialAt(3, 2, 5, Material.AIR);
		hut.setMaterialAt(4, 2, 5, Material.AIR);
		hut.setMaterialAt(5, 2, 5, Material.COBBLESTONE);
		hut.setMaterialAt(2, 2, 6, Material.COBBLESTONE);
		hut.setMaterialAt(3, 2, 6, Material.COBBLESTONE);
		hut.setMaterialAt(4, 2, 6, Material.COBBLESTONE);

		// Roof
		hut.setMaterialAt(1, 3, 1, Material.SMOOTH_BRICK);
		hut.setMaterialAt(2, 3, 1, Material.SMOOTH_BRICK);
		hut.setMaterialAt(3, 3, 1, Material.SMOOTH_BRICK);
		hut.setMaterialAt(4, 3, 1, Material.SMOOTH_BRICK);
		hut.setMaterialAt(5, 3, 1, Material.SMOOTH_BRICK);
		hut.setMaterialAt(0, 3, 2, Material.SMOOTH_BRICK);
		hut.setMaterialAt(1, 3, 2, Material.SMOOTH_BRICK);
		hut.setMaterialAt(2, 3, 2, Material.SMOOTH_BRICK);
		hut.setMaterialAt(3, 3, 2, Material.SMOOTH_BRICK);
		hut.setMaterialAt(4, 3, 2, Material.SMOOTH_BRICK);
		hut.setMaterialAt(5, 3, 2, Material.SMOOTH_BRICK);
		hut.setMaterialAt(6, 3, 2, Material.SMOOTH_BRICK);
		hut.setMaterialAt(0, 3, 3, Material.SMOOTH_BRICK);
		hut.setMaterialAt(1, 3, 3, Material.SMOOTH_BRICK);
		hut.setMaterialAt(2, 3, 3, Material.SMOOTH_BRICK);
		hut.setMaterialAt(3, 3, 3, Material.SMOOTH_BRICK);
		hut.setMaterialAt(4, 3, 3, Material.SMOOTH_BRICK);
		hut.setMaterialAt(5, 3, 3, Material.SMOOTH_BRICK);
		hut.setMaterialAt(6, 3, 3, Material.SMOOTH_BRICK);
		hut.setMaterialAt(0, 3, 4, Material.SMOOTH_BRICK);
		hut.setMaterialAt(1, 3, 4, Material.SMOOTH_BRICK);
		hut.setMaterialAt(2, 3, 4, Material.SMOOTH_BRICK);
		hut.setMaterialAt(3, 3, 4, Material.SMOOTH_BRICK);
		hut.setMaterialAt(4, 3, 4, Material.SMOOTH_BRICK);
		hut.setMaterialAt(5, 3, 4, Material.SMOOTH_BRICK);
		hut.setMaterialAt(6, 3, 4, Material.SMOOTH_BRICK);
		hut.setMaterialAt(1, 3, 5, Material.SMOOTH_BRICK);
		hut.setMaterialAt(2, 3, 5, Material.SMOOTH_BRICK);
		hut.setMaterialAt(3, 3, 5, Material.SMOOTH_BRICK);
		hut.setMaterialAt(4, 3, 5, Material.SMOOTH_BRICK);
		hut.setMaterialAt(5, 3, 5, Material.SMOOTH_BRICK);
		hut.setMaterialAt(2, 3, 6, Material.SMOOTH_BRICK);
		hut.setMaterialAt(3, 3, 6, Material.SMOOTH_BRICK);
		hut.setMaterialAt(4, 3, 6, Material.SMOOTH_BRICK);
	} // buildHut
	
	private final ItemStack[] getItemsInChest() {
		
		Vector<ItemStack> toRet = new Vector<ItemStack>();
		toRet.add(new ItemStack(Material.DIAMOND, 1 + r.nextInt(2)));
		toRet.add(new ItemStack(Material.GOLD_INGOT, 1 + r.nextInt(3)));
		toRet.add(new ItemStack(Material.IRON_INGOT, 6 + r.nextInt(12)));
	    toRet.add(new ItemStack(Material.COAL, 7 + r.nextInt(18)));
	    toRet.add(new ItemStack(Material.BREAD, 3 + r.nextInt(10)));
	    toRet.add(new ItemStack(Material.COOKED_CHICKEN, 3 + r.nextInt(8)));
	    toRet.add(new ItemStack(Material.COOKED_BEEF, 3 + r.nextInt(8)));
	    toRet.add(new ItemStack(Material.GRILLED_PORK, 3 + r.nextInt(8)));
	    toRet.add(new ItemStack(Material.DIAMOND_SWORD, 1));
	    toRet.add(new ItemStack(Material.DIAMOND_PICKAXE, 1));
	    toRet.add(new ItemStack(Material.DIAMOND_SPADE, 1));
	    toRet.add(new ItemStack(Material.DIAMOND_AXE, 1));
	    toRet.add(new ItemStack(Material.DIAMOND_HELMET, 1));
	    toRet.add(new ItemStack(Material.DIAMOND_CHESTPLATE, 1));
	    toRet.add(new ItemStack(Material.DIAMOND_BOOTS, 1));
	    toRet.add(new ItemStack(Material.DIAMOND_LEGGINGS, 1));
	    toRet.add(new ItemStack(Material.IRON_SWORD, 1));
	    toRet.add(new ItemStack(Material.IRON_PICKAXE, 1));
	    toRet.add(new ItemStack(Material.IRON_SPADE, 1));
	    toRet.add(new ItemStack(Material.IRON_AXE, 1));
	    toRet.add(new ItemStack(Material.IRON_HELMET, 1));
	    toRet.add(new ItemStack(Material.IRON_CHESTPLATE, 1));
	    toRet.add(new ItemStack(Material.IRON_BOOTS, 1));
	    toRet.add(new ItemStack(Material.IRON_LEGGINGS, 1));
	    toRet.add(new ItemStack(Material.BOW, 1));
	    toRet.add(new ItemStack(Material.ARROW, 3 + r.nextInt(8)));
		
		ItemStack sharpnessBook = new ItemStack(Material.ENCHANTED_BOOK, 1);
		EnchantmentStorageMeta sharpnessMeta = (EnchantmentStorageMeta)sharpnessBook.getItemMeta();
		sharpnessMeta.addStoredEnchant(Enchantment.DAMAGE_ALL, 4, false);
		sharpnessBook.setItemMeta(sharpnessMeta);
		toRet.add(sharpnessBook);
		
		ItemStack powerBook = new ItemStack(Material.ENCHANTED_BOOK, 1);
		EnchantmentStorageMeta powerMeta = (EnchantmentStorageMeta)powerBook.getItemMeta();
		powerMeta.addStoredEnchant(Enchantment.ARROW_DAMAGE, 4, false);
		powerBook.setItemMeta(powerMeta);
		toRet.add(powerBook);
		
		ItemStack fireBook = new ItemStack(Material.ENCHANTED_BOOK, 1);
		EnchantmentStorageMeta fireMeta = (EnchantmentStorageMeta)fireBook.getItemMeta();
		fireMeta.addStoredEnchant(Enchantment.FIRE_ASPECT, 1, false);
		fireBook.setItemMeta(fireMeta);
		toRet.add(fireBook);
		
		ItemStack fireArrow = new ItemStack(Material.ENCHANTED_BOOK, 1);
		EnchantmentStorageMeta fireArMeta = (EnchantmentStorageMeta)fireArrow.getItemMeta();
		fireArMeta.addStoredEnchant(Enchantment.ARROW_FIRE, 1, false);
		fireArrow.setItemMeta(fireArMeta);
		toRet.add(fireArrow);
		
		ItemStack infiniteArrow = new ItemStack(Material.ENCHANTED_BOOK, 1);
		EnchantmentStorageMeta infiniteMeta = (EnchantmentStorageMeta)infiniteArrow.getItemMeta();
		infiniteMeta.addStoredEnchant(Enchantment.ARROW_INFINITE, 1, false);
		infiniteArrow.setItemMeta(infiniteMeta);
		toRet.add(infiniteArrow);
		
		ItemStack protectionBook = new ItemStack(Material.ENCHANTED_BOOK, 1);
		EnchantmentStorageMeta protectionMeta = (EnchantmentStorageMeta)protectionBook.getItemMeta();
		protectionMeta.addStoredEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4, false);
		protectionBook.setItemMeta(protectionMeta);
		toRet.add(protectionBook);
		
		ItemStack[] asArray = new ItemStack[toRet.size()];
		return toRet.toArray(asArray);
	} // getItemsInChest

} // WarriorsHut
