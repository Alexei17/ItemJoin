/*
 * ItemJoin
 * Copyright (C) CraftationGaming <https://www.craftationgaming.com/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package me.RockinChaos.itemjoin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import me.RockinChaos.itemjoin.handlers.ConfigHandler;
import me.RockinChaos.itemjoin.handlers.ServerHandler;

public class ItemJoin extends JavaPlugin {
	
  	private static ItemJoin instance;
  	
   /**
    * Called when the plugin is loaded.
    */
    @Override
    public void onLoad() {
      instance = this;
    }
    
   /**
    * Called when the plugin is enabled.
    */
  	@Override
	public void onEnable() {
  		ConfigHandler.generateData(getFile()); // possibly call on load?
        ConfigHandler.registerEvents();
        ServerHandler.logInfo("has been Enabled.");
  	}
  	
   /**
    * Called when the plugin is disabled.
    */
  	@Override
	public void onDisable() {
  		ServerHandler.purgeCraftItems(true);
  		Bukkit.getScheduler().cancelTasks(this);
  		ConfigHandler.getProtocolManager().closeProtocol();
  		ConfigHandler.getSQLData().executeLaterStatements();
  		ServerHandler.logInfo("has been Disabled.");
  	}

   /**
    * Gets the static instance of the main class for ItemJoin. 
    * Notice: This class is not the actual API class, this is the main class that extends JavaPlugin. 
    * For API methods, use the static methods available from the class: {@link ItemJoinAPI}.
    *
    * @return ItemJoin instance.
    */  	
  	public static ItemJoin getInstance() {
  		return instance;
  	}
}