package me.RockinChaos.itemjoin.listeners;

import me.RockinChaos.itemjoin.handlers.ConfigHandler;
import me.RockinChaos.itemjoin.handlers.PlayerHandler;
import me.RockinChaos.itemjoin.utils.Utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class Pickups implements Listener {

	@EventHandler
	public void onGlobalPickup(EntityPickupItemEvent event) {
	  	Entity entity = event.getEntity();
	  	if (entity instanceof Player) {
	  		Player player = (Player) event.getEntity();
	  		if (Utils.containsIgnoreCase(ConfigHandler.isPreventPickups(), "true") || Utils.containsIgnoreCase(ConfigHandler.isPreventPickups(), player.getWorld().getName())
	  			|| Utils.containsIgnoreCase(ConfigHandler.isPreventPickups(), "ALL") || Utils.containsIgnoreCase(ConfigHandler.isPreventPickups(), "GLOBAL")) {
	  			if (ConfigHandler.isPreventAllowOpBypass() && player.isOp() || ConfigHandler.isPreventAllowCreativeBypass() && PlayerHandler.isCreativeMode(player)) { } 
	  			else { event.setCancelled(true); }
	  		}
	  	}
	}
}