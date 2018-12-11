package me.RockinChaos.itemjoin.listeners;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import me.RockinChaos.itemjoin.ItemJoin;
import me.RockinChaos.itemjoin.handlers.ItemHandler;
import me.RockinChaos.itemjoin.handlers.PlayerHandler;
import me.RockinChaos.itemjoin.handlers.ServerHandler;
import me.RockinChaos.itemjoin.utils.Utils;

public class InvClickCreative implements Listener {
	private static HashMap < String, ItemStack[] > mySavedItems = new HashMap < String, ItemStack[] > ();
	private static HashMap < String, ItemStack[] > mySavedArmor = new HashMap < String, ItemStack[] > ();
	private static HashMap < String, Boolean > isCreative = new HashMap < String, Boolean > ();
	private static HashMap < String, Boolean > isGlitchSwap = new HashMap < String, Boolean > ();
	private static HashMap < String, Integer > cooldown = new HashMap < String, Integer > ();
	public static HashMap < String, Boolean > dropGlitch = new HashMap < String, Boolean > ();

	@EventHandler
	public void onCreativeSwitch(PlayerGameModeChangeEvent event) {
		Player player = event.getPlayer();
		GameMode gamemode = event.getNewGameMode();
		isCreative(player, gamemode);
	}
	
	@EventHandler
	public void onCreativeInventoryModify(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if (PlayerHandler.isCreativeMode(player)) {
			Initialize(player);
			ItemStack item = null;
			if (cooldown.get(PlayerHandler.getPlayerID(player)) == 1) {
				cooldown.put(PlayerHandler.getPlayerID(player), 1);
				event.setCancelled(true);
				player.getInventory().clear();
				player.getInventory().setHelmet(null);
				player.getInventory().setChestplate(null);
				player.getInventory().setLeggings(null);
				player.getInventory().setBoots(null);
				if (ServerHandler.hasCombatUpdate()) {
					player.getInventory().setItemInOffHand(null);
				}
				ItemStack readd = new ItemStack(event.getCursor());
				restoreInventory(player, readd);
				PlayerHandler.delayUpdateInventory(player, 5L);
			} else if (cooldown.get(PlayerHandler.getPlayerID(player)) != 1) {
				if (Utils.containsIgnoreCase(event.getAction().name(), "HOTBAR")) {
					item = event.getView().getBottomInventory().getItem(event.getHotbarButton());
					if (item == null && event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
						item = event.getCursor();
					} else if (event.getCursor() == null) {
						item = event.getCurrentItem();
					}
				} else if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
					item = event.getCursor();
				} else {
					item = event.getCurrentItem();
				}
				if (event.getCurrentItem() != null && event.getCursor() != null && event.getCursor().getType() != Material.AIR && event.getCurrentItem().getType() != Material.AIR && !hasItem(player, event.getCursor())) {
					isGlitchSwap.put(PlayerHandler.getPlayerID(player), true);
				}
				if (cooldown.get(PlayerHandler.getPlayerID(player)) != 1 && !ItemHandler.isAllowed(player, item, "inventory-modify")) {
					saveInventory(player);
				}
				if (!ServerHandler.hasCombatUpdate()) {
					InvClickSurvival.dropClick.put(PlayerHandler.getPlayerID(player), true);
					ItemStack[] Inv = player.getInventory().getContents().clone();
					ItemStack[] Armor = player.getInventory().getArmorContents().clone();
					InvClickSurvival.LegacyDropEvent(player, Inv, Armor);
				}
				if (!ItemHandler.isAllowed(player, item, "inventory-modify") || event.getCurrentItem() != null && event.getCursor() != null && !hasItem(player, event.getCursor()) && !ItemHandler.isAllowed(player, event.getCurrentItem(), "inventory-modify")) {
					cooldown.put(PlayerHandler.getPlayerID(player), 1);
					event.setCancelled(true);
					player.getInventory().clear();
					player.getInventory().setHelmet(null);
					player.getInventory().setChestplate(null);
					player.getInventory().setLeggings(null);
					player.getInventory().setBoots(null);
					if (ServerHandler.hasCombatUpdate()) {
						player.getInventory().setItemInOffHand(null);
					}
					ItemStack readd = new ItemStack(event.getCursor());
					restoreInventory(player, readd);
					PlayerHandler.delayUpdateInventory(player, 5L);
				} else if (!ServerHandler.hasCombatUpdate() && !ItemHandler.isAllowed(player, item, "inventory-modify")) {
					ItemStack itemFinal = item;
					Bukkit.getScheduler().scheduleSyncDelayedTask(ItemJoin.getInstance(), new Runnable() {
						public void run() {
							if (dropGlitch.get(PlayerHandler.getPlayerID(player)) != null && dropGlitch.get(PlayerHandler.getPlayerID(player)) == true) {
								player.getInventory().removeItem(itemFinal);
								PlayerHandler.updateInventory(player);
								dropGlitch.remove(PlayerHandler.getPlayerID(player));
							}
						}
					}, 1L);
				}
			}
		}
	}
	
	public static void setRunnable(Player player) {
		new BukkitRunnable() {
			public void run() {
				if (player.isOnline() && PlayerHandler.isCreativeMode(player) && isCreative.get(PlayerHandler.getPlayerID(player)) == true) {
					saveInventory(player);
				} else if (isCreative.get(PlayerHandler.getPlayerID(player)) != true) {
					this.cancel();
				} else if (!player.isOnline()) {
					isCreative.put(PlayerHandler.getPlayerID(player), false);
					this.cancel();
				}
			}
		}.runTaskTimerAsynchronously(ItemJoin.getInstance(), 20L, 20L);
	}
	
	public static void Initialize(Player player) {
		if (isCreative.get(PlayerHandler.getPlayerID(player)) == null) {
			isCreative.put(PlayerHandler.getPlayerID(player), false);
		}
		if (isGlitchSwap.get(PlayerHandler.getPlayerID(player)) == null) {
			isGlitchSwap.put(PlayerHandler.getPlayerID(player), false);
		}
		if (cooldown.get(PlayerHandler.getPlayerID(player)) == null) {
			cooldown.put(PlayerHandler.getPlayerID(player), 0);
		}
	}
	
	public static void isCreative(Player player, GameMode gamemode) {
		GameMode creative = GameMode.CREATIVE;
		Initialize(player);
		if (isCreative.get(PlayerHandler.getPlayerID(player)) == false && gamemode == creative) {
			isCreative.put(PlayerHandler.getPlayerID(player), true);
			setRunnable(player);
		} else if (gamemode != creative) {
			isCreative.put(PlayerHandler.getPlayerID(player), false);
		}
	}
	
	public static boolean hasItems(Player player) {
		for (ItemStack item: player.getInventory().getContents()) {
			if (item != null) return true;
		}
		for (ItemStack item: player.getInventory().getArmorContents()) {
			if (item != null) return true;
		}
		return false;
	}
	
	public static boolean hasItem(Player player, ItemStack cursorItem) {
		if (mySavedItems.get(PlayerHandler.getPlayerID(player)) == null || mySavedArmor.get(PlayerHandler.getPlayerID(player)) == null) {
			saveInventory(player);
		}
		if (mySavedItems.get(PlayerHandler.getPlayerID(player)) != null) {
			for (ItemStack inPlayerInventory: mySavedItems.get(PlayerHandler.getPlayerID(player))) {
				if (cursorItem != null && inPlayerInventory != null && inPlayerInventory.isSimilar(cursorItem) && ItemHandler.isCountSimilar(inPlayerInventory, cursorItem)) { // possibly need itemmap is similar
					return true;
				}
			}
		} else if (mySavedArmor.get(PlayerHandler.getPlayerID(player)) != null) {
			for (ItemStack inPlayerArmor: mySavedArmor.get(PlayerHandler.getPlayerID(player))) {
				if (cursorItem != null && inPlayerArmor != null && inPlayerArmor.isSimilar(cursorItem) && ItemHandler.isCountSimilar(inPlayerArmor, cursorItem)) { // possibly need itemmap is similar
					return true;
				}
			}
		}
		return false;
	}
	
	public static void saveInventory(Player player) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(ItemJoin.getInstance(), new Runnable() {
			public void run() {
				if (hasItems(player) && cooldown.get(PlayerHandler.getPlayerID(player)) != 1) {
					mySavedItems.put(PlayerHandler.getPlayerID(player), player.getInventory().getContents());
					mySavedArmor.put(PlayerHandler.getPlayerID(player), player.getInventory().getArmorContents());
				}
			}
		}, 1L);
	}
	
	private static void restoreInventory(Player player, ItemStack readd) {
		if (mySavedItems.get(PlayerHandler.getPlayerID(player)) != null) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(ItemJoin.getInstance(), (Runnable) new Runnable() {
				public void run() {
					player.closeInventory();
					player.getInventory().setContents(mySavedItems.get(PlayerHandler.getPlayerID(player)));
					player.getInventory().setArmorContents(mySavedArmor.get(PlayerHandler.getPlayerID(player)));
					cooldown.put(PlayerHandler.getPlayerID(player), 0);
					if (isGlitchSwap.get(PlayerHandler.getPlayerID(player)) == true) {
						isGlitchSwap.put(PlayerHandler.getPlayerID(player), false);
						player.getInventory().addItem(readd);
						saveInventory(player);
					}
				}
			}, 3L);
		}
	}
}