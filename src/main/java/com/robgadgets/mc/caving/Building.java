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

/**
 * Helper bean to manage buildings.
 * It's just a three-dimensional container of Materials
 * 
 * @author Roberto "Rob" Baldin
 */
public class Building {
	
	protected Material[][][] building;
	
	private int xSize, ySize, zSize;
	
	public int getXSize() {
		return xSize;
	}

	public void setXSize(int xSize) {
		this.xSize = xSize;
	}

	public int getYSize() {
		return ySize;
	}

	public void setYSize(int ySize) {
		this.ySize = ySize;
	}

	public int getZSize() {
		return zSize;
	}

	public void setZSize(int zSize) {
		this.zSize = zSize;
	}

	public Building(int xSize, int ySize, int zSize) {
		building = new Material[xSize][ySize][zSize];
		
		this.xSize = xSize;
		this.ySize = ySize;
		this.zSize = zSize;
	} // Building
	
	public Material getMaterialAt(int x, int y, int z) {
		return building[x][y][z];
	} // getMaterialAt
	
	public void setMaterialAt(int x, int y, int z, Material mat) {
		building[x][y][z] = mat;
	} // setMaterialAt

}
