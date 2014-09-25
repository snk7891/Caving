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

import java.util.Date;
import java.util.Random;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

public class SpawnEnhancer implements Listener {

	private JavaPlugin plugin;
	private long lastAmbushTimestamp;
	
	private Random r;
	
	private final boolean spawningEnhancementsEnabled;
	private final long spawningEnhancementsPeriod;
	private final int sneakyAttackMinRadius, sneakyAttackMaxRadius,sneakyAttackChance;
	private final int ambushMinZombieCount, ambushMaxZombieCount, ambushMinSkeletonCount, ambushMaxSkeletonCount, ambushMinRadius, ambushMaxRadius, ambushChance, ambushFakeTargets, ambushDelay, ambushFakeChance;
	private final long ambushBlackout;

	public SpawnEnhancer(JavaPlugin instance) {
		plugin = instance;
		
		lastAmbushTimestamp = 0;
		ConfigurationSection enhancements = plugin.getConfig().getConfigurationSection("spawningEnhancements");
		spawningEnhancementsEnabled = enhancements.getBoolean("enabled", true);
		spawningEnhancementsPeriod = enhancements.getLong("period", 60L);

		ConfigurationSection sneaky = enhancements.getConfigurationSection("sneaky");
		sneakyAttackMinRadius = sneaky.getInt("minRadius", 2);
		sneakyAttackMaxRadius = sneaky.getInt("maxRadius", 10);
		sneakyAttackChance = sneaky.getInt("chance", 10);
		
		ConfigurationSection ambush = enhancements.getConfigurationSection("ambush");
		ambushMinZombieCount = ambush.getInt("minZombieCount", 1);
		ambushMaxZombieCount = ambush.getInt("maxZombieCount", 3);
		ambushMinSkeletonCount = ambush.getInt("minSkeletonCount", 1);
		ambushMaxSkeletonCount = ambush.getInt("maxSkeletonCount", 3);
		ambushMinRadius = ambush.getInt("minRadius", 1);
		ambushMaxRadius = ambush.getInt("maxRadius", 15);
		ambushChance = ambush.getInt("chance", 5);
		ambushBlackout = ambush.getInt("blackout", 300) * 1000;
		ambushFakeTargets = ambush.getInt("fakeTargets", 2);
		ambushDelay = ambush.getInt("delay", 20);
		ambushFakeChance = ambush.getInt("fakeChance", 30);
		
		lastAmbushTimestamp = new Date().getTime();
		
		r = new Random();
		
		// TODO check config
		
		if(spawningEnhancementsEnabled) {
			BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
			scheduler.scheduleSyncRepeatingTask(plugin, new MainTask(scheduler), 0L, 20L * spawningEnhancementsPeriod);
		} // if
	} // SpawnEnhancer

	void sneaky(Player player) {
		if (player != null) {
			Location spawnLocation = Utils.findSuitableSpawnLocationNearby(
					player.getWorld(), player.getLocation(), sneakyAttackMinRadius, sneakyAttackMaxRadius);
			if (spawnLocation != null) {
				int spawnChoice = r.nextInt(20);
				if (spawnChoice == 0) { // 1/20 chance
					player.getWorld().spawn(spawnLocation, Witch.class);
				} else if (spawnChoice < 10) { // 9/20 chance
					Skeleton sk = player.getWorld().spawn(spawnLocation,
							Skeleton.class);
					EntityEquipment eq = sk.getEquipment();
					eq.setHelmet(getProtectingHelmet());
				} else { // 10/20 chance
					Zombie zom = player.getWorld().spawn(spawnLocation,
							Zombie.class);
					EntityEquipment eq = zom.getEquipment();
					eq.setHelmet(getProtectingHelmet());
				} // else
			} // if
		} // if
	} // sneaky
	
	void ambush(Player player) {
		if(player != null) {
			int zombiePartyCount = ambushMinZombieCount + r.nextInt(ambushMaxZombieCount);
			int skeletonPartyCount = ambushMinSkeletonCount + r.nextInt(ambushMaxSkeletonCount);
			
			Location bossLocation = Utils.findSuitableSpawnLocationNearby(player.getWorld(), player.getLocation(), ambushMinRadius, ambushMaxRadius);
			Location[] zombieLocations = new Location[zombiePartyCount];
			for(int i = 0; i < zombiePartyCount; i++)
				zombieLocations[i] = Utils.findSuitableSpawnLocationNearby(player.getWorld(), player.getLocation(), ambushMinRadius, ambushMaxRadius);
			
			Location[] skeletonLocations = new Location[skeletonPartyCount];
			for(int i = 0; i < skeletonPartyCount; i++)
				skeletonLocations[i] = Utils.findSuitableSpawnLocationNearby(player.getWorld(), player.getLocation(), ambushMinRadius, ambushMaxRadius);
			
			if(bossLocation != null) {
				Enderman boss = player.getWorld().spawn(bossLocation, Enderman.class);
				boss.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 120, 1));
				boss.setTarget(player);
			} // if
			
			for(int i = 0; i < zombiePartyCount; i++)
				if(zombieLocations[i] != null) {
					Zombie zom = player.getWorld().spawn(zombieLocations[i], Zombie.class);
					zom.setTarget(player);
				} // if
			
			for(int i = 0; i < skeletonPartyCount; i++)
				if(skeletonLocations[i] != null) {
					Skeleton sk = player.getWorld().spawn(skeletonLocations[i], Skeleton.class);
					sk.setTarget(player);
				} // if
		} // if
	} // ambush

	class MainTask implements Runnable {

		private BukkitScheduler scheduler;

		public MainTask(BukkitScheduler sch) {
			scheduler = sch;
		} // MainTask

		@Override
		public void run() {
			if (r.nextInt(100) <= sneakyAttackChance) { // Do sneaky attack
				Player[] players = plugin.getServer().getOnlinePlayers();
				if(players.length > 0) {
					int unluckyPlayer = r.nextInt(players.length);
					plugin.getLogger().info("Sneaky attack on " + players[unluckyPlayer].getName());
					sneaky(players[unluckyPlayer]);
				} // if
			} // if
			
			final long now = new Date().getTime();
			if(now - lastAmbushTimestamp > ambushBlackout) {
				if (r.nextInt(100) <= ambushChance) { // Do ambush
					final Player[] players = plugin.getServer().getOnlinePlayers();
					if(players.length > 0) {
						final int unluckyPlayer = r.nextInt(players.length);
						plugin.getLogger().info("Ambush set on " + players[unluckyPlayer].getName() + " in " + ambushDelay + " seconds");
						
						final Vector<Integer> fakeTargets = new Vector<>(ambushFakeTargets + 1);
						fakeTargets.add(unluckyPlayer);
						for(int i = 0; i < ambushFakeTargets && i < players.length - 1; i++) {
							int newRandom;
							do {
								newRandom = r.nextInt(players.length);
							} while(fakeTargets.contains(newRandom));
							fakeTargets.add(newRandom);
						} // for
						
						for(int i = 0; i < fakeTargets.size(); i++)
							players[fakeTargets.elementAt(i)].sendMessage("You heard a noise behind you..");
						
						scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {

							@Override
							public void run() {
								
								for(int i = 1; i < fakeTargets.size(); i++)
									players[fakeTargets.elementAt(i)].sendMessage("No, that was just your imagination");
								
								if(r.nextInt(100) >= ambushFakeChance &&
									players[unluckyPlayer].isOnline() &&
									players[unluckyPlayer].getLocation().getBlock().getBiome() != Biome.HELL &&
									players[unluckyPlayer].getLocation().getBlock().getBiome() != Biome.SKY) {
									
									ambush(players[unluckyPlayer]);
									players[unluckyPlayer].sendMessage("Watch out!");
									lastAmbushTimestamp = now;
								} else
									players[unluckyPlayer].sendMessage("No, that was just your imagination");
								
							} // run
						}, 20L * ambushDelay);
					} // if
				} // if
			} // if
			
		} // run

	} // MainTask
	
	private static ItemStack getProtectingHelmet() {
		ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
		helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
		return helmet;
	} // getProtectingHelmet

} // SpawnEnhancer


