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

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Spider;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.BlockIterator;

/**
 * 
 * Caving - Empowered mob mechanics
 * 
 * Here we change all the game mechanics in order
 * to create our powerful enemies.
 * 
 * @author Roberto "Rob" Baldin
 *
 */
public class MobMechanics implements Listener {

	private JavaPlugin plugin;
	private Random r;
	
	private final boolean mobMechanicsEnabled;
	private final int pigmenDiamondDroprate, endermanDiamondDroprate, witherHeadDropRate, chargedCreeperSpawnChance, ghastLavaBulletChance, tntSkeletonSpawnChance, empoweredZombieSpawnChance, witchHarmChance, cocoonSpiderSpawnChance, gorgoneEndermanSpawnChance, enderDragonVampireRate;
	
	final static PotionEffect harmPotion, cocoonEffectJump, cocoonEffectSlow, empoweredZombieStrength, enhancedWitherEffect, gorgoneEffectNausea, gorgoneEffectBlindness;
	static {
		harmPotion = new PotionEffect(PotionEffectType.HARM, 1, 1);
		cocoonEffectJump = new PotionEffect(PotionEffectType.JUMP, 9 * 20, 128);
		cocoonEffectSlow = new PotionEffect(PotionEffectType.SLOW, 9 * 20, 5);
		empoweredZombieStrength = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 600 * 20, 2);
		enhancedWitherEffect = new PotionEffect(PotionEffectType.WITHER, 11 * 20, 1);
		gorgoneEffectBlindness = new PotionEffect(PotionEffectType.BLINDNESS, 12 * 20, 1);
		gorgoneEffectNausea = new PotionEffect(PotionEffectType.CONFUSION, 12 * 20, 1);
	} // static

	/**
	 * It reads all the necessary configuration and starts
	 * empowered mobs spawning.
	 * 
	 * @param instance Plugin reference
	 */
	public MobMechanics(JavaPlugin instance) {

		plugin = instance;
		
		r = new Random();
		
		ConfigurationSection monsters = plugin.getConfig().getConfigurationSection("monsters");
		mobMechanicsEnabled = monsters.getBoolean("enabled");
		pigmenDiamondDroprate = monsters.getInt("pigmenDiamondDropRate", 30);
		endermanDiamondDroprate = monsters.getInt("endermanDiamondDropRate", 15);
		chargedCreeperSpawnChance = monsters.getInt("chargedCreeperSpawnChance", 20);
		empoweredZombieSpawnChance = monsters.getInt("empoweredZombieSpawnChance", 20);
		witchHarmChance = monsters.getInt("witchHarmChance", 10);
		ghastLavaBulletChance = monsters.getInt("ghastLavaBulletChance", 30);
		tntSkeletonSpawnChance = monsters.getInt("tntSkeletonSpawnChance", 15);
		cocoonSpiderSpawnChance = monsters.getInt("cocoonSpiderSpawnChance", 15);
		witherHeadDropRate = monsters.getInt("witherHeadDropRate", 20);
		gorgoneEndermanSpawnChance = monsters.getInt("gorgoneEndermanSpawnChance", 20);
		enderDragonVampireRate = 100 / monsters.getInt("enderDragonVampireRate", 25);

		if(mobMechanicsEnabled)
			plugin.getServer().getPluginManager().registerEvents(this, plugin);

	} // MobMechanics

	/**
	 * Here we can change enemy drops.
	 * 
	 * - Pigmen now have a chance to drop diamonds
	 * - Wither skeletons now drop their head more frequently [bug: they could also drop two at the moment]
	 * - Standard endermen do not drop pearls, but they can drop diamonds
	 * 
	 * @param evt
	 */
	@EventHandler
	public void onEntityDeath(EntityDeathEvent evt) {
		
		LivingEntity entity = evt.getEntity();
		if(entity instanceof PigZombie) { // Add diamonds on drop
			
			if(r.nextInt(100) < pigmenDiamondDroprate)
			    evt.getDrops().add(new ItemStack(Material.DIAMOND, 1));

		} else if(entity instanceof Skeleton) { // Add head to drop
			
			Skeleton sk = (Skeleton) entity;
			if(sk.getSkeletonType() == SkeletonType.WITHER && r.nextInt(100) < witherHeadDropRate) {
			    evt.getDrops().add(new ItemStack(Material.SKULL_ITEM, 1, (byte) 1));
			} // if
		} else if(entity instanceof Enderman) { // Switch pearl with diamonds
			
			Enderman end = (Enderman) entity;
			String gorgoneTest = Utils.getMetadata(plugin, end, "gorgone");
			if(!gorgoneTest.equals("true")) {
				evt.getDrops().clear();
				if(r.nextInt(100) < endermanDiamondDroprate)
				    evt.getDrops().add(new ItemStack(Material.DIAMOND, 1));
			} // if

		} // if
		
	} // onEntityDeath
	
	/**
	 * Here we change mob spawning, so we can actually create some empowered monsters.
	 * 
	 * - There's a chance for charged creepers to spawn naturally
	 * - Zombie can spawn with a strength II potion effect
	 * - Skeletons can spawn as "explosive". Here they are only labeled as such, but no change has been made yet.
	 * - Spider can spawn as "coocon", meaning that they can paralyze enemies. Here they are only labeled as such.
	 * - Endermen can spawn as "gorgone", meaning that they can blind and confuse enemies. Here they are only labeled as such.
	 * 
	 * @param evt
	 */
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent evt) {
		
		LivingEntity entity = evt.getEntity();
		if(entity instanceof Creeper) { // Charge the creeper
			
			Creeper creeper = (Creeper) entity;
			if(r.nextInt(100) < chargedCreeperSpawnChance)
			    creeper.setPowered(true);
		} else if(entity instanceof Zombie) { // Empower the zombie
			
			Zombie zombie = (Zombie) entity;
			if(r.nextInt(100) < empoweredZombieSpawnChance) {
				zombie.addPotionEffect(empoweredZombieStrength);
				EntityEquipment eq = zombie.getEquipment();
				eq.setHelmet(getEmpoweringHelmet());
			} // if
			
		} else if(entity instanceof Skeleton && r.nextInt(100) < tntSkeletonSpawnChance) { // Explosive skeleton
			
			Skeleton sk = (Skeleton) entity;
			EntityEquipment eq = sk.getEquipment();
			eq.setHelmet(getEmpoweringHelmet());
			Utils.setMetadata(plugin, sk, "explosive", "true");
			
		} else if(entity instanceof Spider && r.nextInt(100) < cocoonSpiderSpawnChance) { // Cocoon spider
			
			Spider spider = (Spider) entity;
			Utils.setMetadata(plugin, spider, "cocoon", "true");
			
		} else if(entity instanceof Enderman && r.nextInt(100) < gorgoneEndermanSpawnChance) { // Gorgone enderman
			
			Enderman enderman = (Enderman) entity;
			Utils.setMetadata(plugin, enderman, "gorgone", "true");
			
		} // else if
		
	} // onCreatureSpawn
	
	/**
	 * Here we change potion splash effects,
	 * adding a dangerous "harm" effect to witches sometimes.
	 * 
	 * @param evt
	 */
	@EventHandler
	public void onPotionSplash(PotionSplashEvent evt) {
		ThrownPotion potion = evt.getEntity();
		ProjectileSource source = potion.getShooter();
		
		if(source instanceof Witch && r.nextInt(100) < witchHarmChance) {
			Collection<LivingEntity> affecteds = evt.getAffectedEntities();
			Iterator<LivingEntity> it = affecteds.iterator();
			while(it.hasNext()) {
				LivingEntity affected = it.next();
				affected.addPotionEffect(harmPotion);
			} // while
		} // if
	} // onPotionSplash
	
	private enum Shooter {
		SKELETON, GHAST, OTHER
	};
	
	/**
	 * r
	 * @param evt
	 */
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent evt) {
		
		Shooter shooter = Shooter.OTHER;
		
		Projectile proj = evt.getEntity();
		if(proj != null) {
			
			ProjectileSource source = proj.getShooter();
			if(source instanceof Skeleton) {
				Skeleton sk = (Skeleton) source;
				String explosiveTest = Utils.getMetadata(plugin, sk, "explosive");
				if(explosiveTest.equals("true")) // If it's an explosive skeleton, consider it as valid, otherwise don't bother
					shooter = Shooter.SKELETON;
			} // if
			
			else if(source instanceof Ghast)
				shooter = Shooter.GHAST;
			
		} // if
		
		if(shooter != Shooter.OTHER) {
			BlockIterator iterator = new BlockIterator(evt.getEntity().getWorld(), evt.getEntity().getLocation().toVector(), evt.getEntity().getVelocity().normalize(), 0.0D, 4);
			Block hitBlock = null;
			
			while (iterator.hasNext()) {
				hitBlock = iterator.next();
			 
				if (hitBlock.getType() != Material.AIR) 
					break;
			} // while
			
			if(shooter == Shooter.SKELETON) { // Skeletons make explosions
				Utils.createDelayedExplosion(plugin, hitBlock, 5L, 2.0f);
				// Should fix bouncing explosive arrows
				proj.setBounce(false);
				proj.remove();
				
			} else if(shooter == Shooter.GHAST) { // Ghasts turn blocks to lava
				if(r.nextInt(100) < ghastLavaBulletChance)
					hitBlock.setType(Material.LAVA);
			} // else if
			
		} // if
		
	} // onProjectileHit
	
	@EventHandler
	public void onEntityDamaged(EntityDamageByEntityEvent evt) {
		
		Entity attacker = evt.getDamager();
		
		if(attacker instanceof Spider) {
			Spider spider = (Spider) attacker;
			String cooconTest = Utils.getMetadata(plugin, spider, "cocoon");
			
			if(cooconTest.equals("true")) {
				Entity attacked = evt.getEntity();
				if(attacked instanceof Player) {
					Player player = (Player) attacked;
					player.addPotionEffect(cocoonEffectJump, true);
					player.addPotionEffect(cocoonEffectSlow, true);
				} // if
			} // if
			
		} else if(attacker instanceof Skeleton) {
			Skeleton sk = (Skeleton) attacker;
			if(sk.getSkeletonType() == SkeletonType.WITHER) {
				Entity attacked = evt.getEntity();
				if(attacked instanceof Player) {
					Player player = (Player) attacked;
					player.addPotionEffect(enhancedWitherEffect, true);
				} // if
			} // if
			
		} else if(attacker instanceof EnderDragon && evt.getEntity() instanceof Player) {
			EnderDragon dragon = (EnderDragon) attacker;
			double newHealth = dragon.getHealth() + (evt.getDamage() / enderDragonVampireRate);
			if(newHealth < 0)
				newHealth = 0;
			else if(newHealth > dragon.getMaxHealth())
				newHealth = dragon.getMaxHealth();
			dragon.setHealth(newHealth);
			
		} // else if
		
	} // onEntityDamaged
	
	@EventHandler
	public void onEntityAggro(EntityTargetEvent evt) {
		
		Entity attacker = evt.getEntity();
		if(attacker instanceof Enderman) {
			
			Enderman enderman = (Enderman) attacker;
			String gorgoneTest = Utils.getMetadata(plugin, enderman, "gorgone");
			
			if(gorgoneTest.equals("true")) {
				Entity attacked = evt.getTarget();
				if(attacked instanceof Player) {
					Player player = (Player) attacked;
					player.addPotionEffect(gorgoneEffectBlindness);
					player.addPotionEffect(gorgoneEffectNausea);
				} // if
			} // if
		} // if
		
	} // onEntityAggro
	
	private static ItemStack getEmpoweringHelmet() {
		ItemStack helmet = new ItemStack(Material.CHAINMAIL_HELMET);
		helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
		return helmet;
	} // getEmpoweringHelmet

} // GeneralMechanics
