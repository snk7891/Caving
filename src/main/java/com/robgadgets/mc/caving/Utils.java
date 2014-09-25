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

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Utils {

	private static boolean checkLocation(World w, Location l, int height) {
		Block terrain = w.getBlockAt(l);

		boolean toRet = false;

		switch (terrain.getType()) {
		case GRASS:
		case DIRT:
		case GRAVEL:
		case SAND:
		case STONE:
		case COBBLESTONE:
		case SNOW:
		case ICE:
		case BEDROCK:
		case OBSIDIAN:
		case SANDSTONE:
			Block airCheck1 = terrain.getRelative(BlockFace.UP);
			Block airCheck2 = airCheck1.getRelative(BlockFace.UP);
			toRet = terrain.getLightFromSky() <= 7
					&& airCheck1.getType() == Material.AIR
					&& airCheck2.getType() == Material.AIR
					&& (height <= 2 || airCheck2.getRelative(BlockFace.UP)
							.getType() == Material.AIR);
		default:
			break;
		} // switch

		return toRet;
	} // checkLocation

	public static Location findSuitableSpawnLocationNearby(World w, Location l,
			long minRadius, long maxRadius, int height) {

		if (maxRadius <= minRadius)
			return null;

		double x = l.getX();
		double y = l.getY();
		double z = l.getZ();

		Random r = new Random();
		double randomX = x + minRadius + (maxRadius - minRadius)
				* r.nextDouble() * (r.nextBoolean() ? -1 : 1);
		double randomZ = z + minRadius + (maxRadius - minRadius)
				* r.nextDouble() * (r.nextBoolean() ? -1 : 1);

		for (int incY = -2; incY < 2; incY++) {
			Location tryLoc = new Location(w, randomX, incY + y, randomZ);
			if (checkLocation(w, tryLoc, height))
				return new Location(w, randomX, incY + y + 1, randomZ);
		} // for

		return findSuitableSpawnLocationNearby(w, l, minRadius, maxRadius - 1);

	} // findSuitableSpawnLocationNearby

	public static Location findSuitableSpawnLocationNearby(World w, Location l,
			long minRadius, long maxRadius) {

		return findSuitableSpawnLocationNearby(w, l, minRadius, maxRadius - 1,
				2);

	} // findSuitableSpawnLocationNearby

	public static Chunk getChunckForPlayer(Player p) {
		World world = p.getWorld();
		return world.getChunkAt(p.getLocation());
	}

	public static void createDelayedExplosion(JavaPlugin plugin, final Block block, long delay) {
		createDelayedExplosion(plugin, block, delay, 2.0f);
	} // createDelayedExplosion
	
	public static void createDelayedExplosion(JavaPlugin plugin, final Block block, long delay, final float power) {
		
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		
		scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {

			@Override
			public void run() {
				block.getWorld().createExplosion(block.getLocation(), power);
			} // run
			
		}, delay);
		
	} // createDelayedExplosion
	
	public static String getMetadata(JavaPlugin plugin, Metadatable obj, String key) {
		List<MetadataValue> values = obj.getMetadata(key);  
		  for (MetadataValue value : values)
		     if (value.getOwningPlugin() == plugin)
		        return (String) value.value();
		  return "";
	} // getMetadata
	
	public static void setMetadata(JavaPlugin plugin, Metadatable obj, String key, String value) {
		obj.setMetadata(key, new FixedMetadataValue(plugin, value));
	} // setMetadata

} // Utils