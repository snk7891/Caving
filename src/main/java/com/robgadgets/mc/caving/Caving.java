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

import org.bukkit.plugin.java.JavaPlugin;

/**
 *  Caving - Main
 *  
 *  It starts al the other components.
 *
 *  @author Roberto "Rob" Baldin
*/
public class Caving extends JavaPlugin {
	
	@SuppressWarnings("unused")
	private SpawnEnhancer enhancer;
	
	@SuppressWarnings("unused")
	private WarriorsHut hut;
	
	@SuppressWarnings("unused")
	private GeneralMechanics general;
	
	@SuppressWarnings("unused")
	private MobMechanics mobs;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		
		enhancer = new SpawnEnhancer(this);
		hut = new WarriorsHut(this);
		general = new GeneralMechanics(this);
		mobs = new MobMechanics(this);
		
		getLogger().info("Plugin loaded");
	} // onEnable
	
} // Caving