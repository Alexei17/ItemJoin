package me.RockinChaos.itemjoin.giveitems.listeners;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.RockinChaos.itemjoin.ItemJoin;
import me.RockinChaos.itemjoin.giveitems.utils.ObtainItem;
import me.RockinChaos.itemjoin.giveitems.utils.ItemMap;
import me.RockinChaos.itemjoin.handlers.ConfigHandler;
import me.RockinChaos.itemjoin.handlers.PlayerHandler;
import me.RockinChaos.itemjoin.utils.Hooks;
import me.RockinChaos.itemjoin.utils.Utils;
import me.RockinChaos.itemjoin.utils.sqlite.SQLData;

public class PlayerJoin implements Listener {
	
	private static String enabledCommandWorlds;
	private static List<String> globalCommands;
	private static boolean globalCommandsEnabled = false;

	@EventHandler
	private void giveOnJoin(PlayerJoinEvent event) {
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
		runGlobalCmds(player);
		ObtainItem.safeSet(player, "Join");
		Bukkit.getScheduler().scheduleSyncDelayedTask(ItemJoin.getInstance(), new Runnable() {
			public void run() {
				String Probable = ObtainItem.getProbabilityItem(player);
				for (ItemMap item : ObtainItem.getItems()) {
					if (item.isGiveOnJoin() && item.inWorld(player.getWorld()) 
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
	
	private void runGlobalCmds(Player player) {
		if (globalCommandsEnabled && inCommandsWorld(player.getWorld().getName(), "enabled-worlds") && globalCommands != null) {
			for (String command: globalCommands) {
				if (!SQLData.hasFirstCommanded(player, command)) {
					String Command = Utils.translateLayout(command, player).replace("first-join: ", "").replace("first-join:", "");
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), Command);
					if (Utils.containsIgnoreCase(command, "first-join:")) {
						SQLData.saveToDatabase(player, "NULL", command, "");
					}
				}
			}
		}
	}
	
	private boolean inCommandsWorld(String world, String stringLoc) {
		if (enabledCommandWorlds != null) {
			String[] compareWorlds = enabledCommandWorlds.split(",");
			for (String compareWorld: compareWorlds) {
				if (compareWorld.equalsIgnoreCase(world) || compareWorld.equalsIgnoreCase("ALL") || compareWorld.equalsIgnoreCase("GLOBAL")) {
					return true;
				}
			}
		} else if (enabledCommandWorlds == null) {
			return true;
		}
		return false;
	}
	
	public static void setRunCommands() {
		if (ConfigHandler.getConfig("config.yml").getBoolean("enabled-global-commands") == true) {
			enabledCommandWorlds = ConfigHandler.getConfig("config.yml").getString("enabled-worlds").replace(" ", "");
			globalCommands = ConfigHandler.getConfig("config.yml").getStringList("global-commands");
			globalCommandsEnabled = ConfigHandler.getConfig("config.yml").getBoolean("enabled-global-commands");
		}
	}
}