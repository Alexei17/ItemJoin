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
package me.RockinChaos.itemjoin.giveitems.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import me.RockinChaos.itemjoin.giveitems.utils.ItemUtilities;
import me.RockinChaos.itemjoin.handlers.ConfigHandler;

public class WorldSwitch implements Listener {
	
   /**
	* Called on player switching worlds.
	* Gives any available custom items upon switching worlds.
	* 
	* @param event - PlayerChangedWorldEvent
	*/
	@EventHandler
	private void setWorldSwitchItems(PlayerChangedWorldEvent event) {
		final Player player = event.getPlayer();
		final ItemUtilities.TriggerType type = ItemUtilities.TriggerType.WORLDSWITCH;
		if (ConfigHandler.getDepends().authMeEnabled()) { 
			ItemUtilities.setAuthenticating(player, type, org.bukkit.GameMode.ADVENTURE, "GLOBAL"); 
		} else { 
			ItemUtilities.setItems(player, type, org.bukkit.GameMode.ADVENTURE, "GLOBAL"); 
		}
	}
}