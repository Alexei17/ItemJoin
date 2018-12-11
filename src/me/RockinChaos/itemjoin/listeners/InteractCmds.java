package me.RockinChaos.itemjoin.listeners;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.RockinChaos.itemjoin.cacheitems.CreateItems;
import me.RockinChaos.itemjoin.handlers.CommandHandler;
import me.RockinChaos.itemjoin.handlers.ConfigHandler;
import me.RockinChaos.itemjoin.handlers.ItemHandler;
import me.RockinChaos.itemjoin.handlers.PermissionsHandler;
import me.RockinChaos.itemjoin.handlers.PlayerHandler;
import me.RockinChaos.itemjoin.handlers.WorldHandler;
import me.RockinChaos.itemjoin.utils.Utils;

public class InteractCmds implements Listener {

	@EventHandler
	public void onInventoryCmds(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();
		final Player player = (Player) event.getWhoClicked();
		final String world = player.getWorld().getName();
		String action = event.getAction().toString();
		if (setupCommands(player, world, item, action, "INVENTORY")) { event.setCancelled(true); }
	}

	@EventHandler
	public void onInteractCmds(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		final Player player = event.getPlayer();
		String hand = "";
		try { hand = event.getHand().toString(); } catch (Exception e) { }
		if (item == null || item.getType() == Material.AIR) {
			item = PlayerHandler.getHandItem(player);
		}
		
		if (hand != null) {
			if (hand.equalsIgnoreCase("HAND") && !ItemHandler.isSimilar(item, PlayerHandler.getMainHandItem(player))) {
				item = PlayerHandler.getMainHandItem(player);
			} else if (hand.equalsIgnoreCase("OFF_HAND") && !ItemHandler.isSimilar(item, PlayerHandler.getOffHandItem(player))) {
				item = PlayerHandler.getOffHandItem(player);
			}
		}
		final String world = player.getWorld().getName();
		String action = event.getAction().toString();
		if (PlayerHandler.isAdventureMode(player) && !action.contains("LEFT") 
				|| !PlayerHandler.isAdventureMode(player)) {
			if (setupCommands(player, world, item, action, hand)) { event.setCancelled(true); }
		}
	}
	
	@EventHandler
	public void onAdventureInteractCmds(PlayerAnimationEvent event) {
		final Player player = event.getPlayer();
		final String world = player.getWorld().getName();
		ItemStack item = PlayerHandler.getHandItem(player);
		if (item == null || item.getType() == Material.AIR) {
			item = PlayerHandler.getHandItem(player);
		}
		if (PlayerHandler.isAdventureMode(player)) {
			if (setupCommands(player, world, item, "LEFT_CLICK_AIR", "ADVENTURE")) { event.setCancelled(true); }
		}
	}

	public boolean setupCommands(Player player, String world, ItemStack item1, String action, String hand) {
		if (Utils.isConfigurable()) {
			for (String item: ConfigHandler.getConfigurationSection().getKeys(false)) {
				ConfigurationSection items = ConfigHandler.getItemSection(item);
				if (item != null && WorldHandler.inWorld(items, world) && PermissionsHandler.hasItemsPermission(items, item, player)) {
					if (items.getString(".slot") != null) {
						String slotlist = items.getString(".slot").replace(" ", "");
						String[] slots = slotlist.split(",");
						ItemHandler.clearItemID(player);
						for (String slot: slots) {
							String ItemID = ItemHandler.getItemID(player, slot);
							ItemStack inStoredItems = CreateItems.items.get(player.getWorld().getName() + "." + PlayerHandler.getPlayerID(player) + ".items." + ItemID + item);
							if (inStoredItems != null && ItemHandler.isSimilar(item1, inStoredItems) && CommandHandler.isCommandable(action, items)) {
								if (!CommandHandler.onCooldown(items, player, item, item1)) {
									CommandHandler.chargePlayer(items, item, player, action, item1, hand);
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
	
}
