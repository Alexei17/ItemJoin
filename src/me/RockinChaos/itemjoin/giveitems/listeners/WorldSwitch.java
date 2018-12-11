package me.RockinChaos.itemjoin.giveitems.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.RockinChaos.itemjoin.ItemJoin;
import me.RockinChaos.itemjoin.giveitems.utils.ObtainItem;
import me.RockinChaos.itemjoin.giveitems.utils.ItemMap;
import me.RockinChaos.itemjoin.handlers.ConfigHandler;
import me.RockinChaos.itemjoin.handlers.PlayerHandler;
import me.RockinChaos.itemjoin.utils.Hooks;
import me.RockinChaos.itemjoin.utils.sqlite.SQLData;

public class WorldSwitch implements Listener {

	@EventHandler
	private void giveOnWorldSwitch(PlayerChangedWorldEvent event) {
		final Player player = event.getPlayer();
		if (RegionEnter.getPlayerRegions().get(player) != null) { RegionEnter.delPlayerRegion(player); }
		if (Hooks.hasAuthMe() == true) { setAuthenticating(player); } 
		else { setItems(player); }
	}
	
	private void setAuthenticating(final Player player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (Hooks.hasAuthMe() == true && fr.xephi.authme.api.v3.AuthMeApi.getInstance().isAuthenticated(player)) {
					setItems(player);
					this.cancel();
				}
			}
		}.runTaskTimer(ItemJoin.getInstance(), 0, 20);
	}
	
	private void setItems(final Player player) {
		ObtainItem.safeSet(player, "WorldChanged");
		Bukkit.getScheduler().scheduleSyncDelayedTask(ItemJoin.getInstance(), new Runnable() {
			public void run() {
				String Probable = ObtainItem.getProbabilityItem(player);
				for (ItemMap item : ObtainItem.getItems()) { 
					if (item.isGiveOnWorldChange() && item.inWorld(player.getWorld()) 
							&& ObtainItem.isChosenProbability(item, Probable) && SQLData.isEnabled(player)
							&& item.hasPermission(player) && ObtainItem.isObtainable(player, item)) {
						item.giveTo(player, true); 
					}
					item.setAnimations(player);
				}
				ObtainItem.sendFailCount(player);
				PlayerHandler.delayUpdateInventory(player, 15L);
			}
		}, ConfigHandler.getItemDelay());
	}
}