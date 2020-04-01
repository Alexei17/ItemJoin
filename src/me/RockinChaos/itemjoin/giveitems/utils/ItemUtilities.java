package me.RockinChaos.itemjoin.giveitems.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import me.RockinChaos.itemjoin.ItemJoin;
import me.RockinChaos.itemjoin.handlers.ConfigHandler;
import me.RockinChaos.itemjoin.handlers.ItemHandler;
import me.RockinChaos.itemjoin.handlers.PlayerHandler;
import me.RockinChaos.itemjoin.handlers.ServerHandler;
import me.RockinChaos.itemjoin.utils.Language;
import me.RockinChaos.itemjoin.utils.Utils;
import net.md_5.bungee.api.ChatColor;

public class ItemUtilities {
  	private static List < ItemMap > items = new ArrayList < ItemMap >();
	private static HashMap <Integer, Integer> failCount = new HashMap <Integer, Integer> ();

	public static boolean isAllowed(Player player, ItemStack item, String itemflag) {
		ItemMap itemMap = getItemMap(item, null, player.getWorld());
		if (itemMap != null && itemMap.isAllowedItem(player, item, itemflag)) {
			return false;
		}
		return true;
	}
	
	public static ItemMap getItemMap(ItemStack itemStack, String configName, World world) {
		for (ItemMap itemMap : ItemUtilities.getItems()) {
			if (world != null && configName == null && itemMap.inWorld(world) && itemMap.isSimilar(itemStack)) {
				return itemMap;
			} else if (configName != null && itemMap.getConfigName().equalsIgnoreCase(configName)) {
				return itemMap;
			}
		}
		return null;
	}
	
	public static void closeAnimations(Player player) {
		for (ItemMap item : ItemUtilities.getItems()) {
			if (item.isAnimated() && item.getAnimationHandler().get(player) != null
					|| item.isDynamic() && item.getAnimationHandler().get(player) != null) {
				item.getAnimationHandler().get(player).closeAnimation(player);
				item.removeFromAnimationHandler(player);
			}
		}
	}
	
	public static void closeAnimations() {
		Collection < ? > playersOnlineNew = null;
		Player[] playersOnlineOld;
		try {
			if (Bukkit.class.getMethod("getOnlinePlayers", new Class < ? > [0]).getReturnType() == Collection.class) {
				if (Bukkit.class.getMethod("getOnlinePlayers", new Class < ? > [0]).getReturnType() == Collection.class) {
					playersOnlineNew = ((Collection < ? > ) Bukkit.class.getMethod("getOnlinePlayers", new Class < ? > [0]).invoke(null, new Object[0]));
					for (Object objPlayer: playersOnlineNew) {
						closeAnimations(((Player) objPlayer));
					}
				}
			} else {
				playersOnlineOld = ((Player[]) Bukkit.class.getMethod("getOnlinePlayers", new Class < ? > [0]).invoke(null, new Object[0]));
				for (Player player: playersOnlineOld) {
					closeAnimations(player);
				}
			}
		} catch (Exception e) { ServerHandler.sendDebugTrace(e); }
	}
	
	public static void updateItems(Player player, boolean newAnimation) {
		for (ItemMap item: getItems()) {
			item.updateItem(player);
			if (newAnimation) {
				item.setAnimations(player);
			}
		}
	}
	
	public static void updateItems() {
		Collection < ? > playersOnlineNew = null;
		Player[] playersOnlineOld;
		try {
			if (Bukkit.class.getMethod("getOnlinePlayers", new Class < ? > [0]).getReturnType() == Collection.class) {
				if (Bukkit.class.getMethod("getOnlinePlayers", new Class < ? > [0]).getReturnType() == Collection.class) {
					playersOnlineNew = ((Collection < ? > ) Bukkit.class.getMethod("getOnlinePlayers", new Class < ? > [0]).invoke(null, new Object[0]));
					for (Object objPlayer: playersOnlineNew) {
						Player player = ((Player) objPlayer);
						updateItems(player, true);
					}
				}
			} else {
				playersOnlineOld = ((Player[]) Bukkit.class.getMethod("getOnlinePlayers", new Class < ? > [0]).invoke(null, new Object[0]));
				for (Player player: playersOnlineOld) {
					updateItems(player, true);
				}
			}
		} catch (Exception e) {
			ServerHandler.sendDebugTrace(e);
		}
	}
	
	public static void safeSet(final Player player, final String type) {
		if (!type.equalsIgnoreCase("LIMIT-MODES")) { PlayerHandler.setHotbarSlot(player); }
		Bukkit.getScheduler().scheduleSyncDelayedTask(ItemJoin.getInstance(), new Runnable() {
			@Override
			public void run() {
				if (type.equalsIgnoreCase("JOIN")) {
					ItemUtilities.clearEventItems(player, player.getWorld().getName(), "Join", "");
					Utils.triggerCommands(player);
				} else if (type.equalsIgnoreCase("WORLD-SWITCH")) {
					ItemUtilities.clearEventItems(player, player.getWorld().getName(), "World-Switch", "");
				}
			}
		}, ConfigHandler.getClearDelay());
	}
	
	public static boolean containsObject(String world, String clearEvent) {
		if (clearEvent.replace(" ", "").equalsIgnoreCase("ALL") || clearEvent.replace(" ", "").equalsIgnoreCase("GLOBAL") 
				|| clearEvent.replace(" ", "").equalsIgnoreCase("ENABLED") || clearEvent.replace(" ", "").equalsIgnoreCase("TRUE")) {
			return true;
		} else {
			for (String eventWorld: clearEvent.replace(" ", "").split(",")) {
				if (eventWorld.equalsIgnoreCase(world)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static void clearEventItems(Player player, String world, String event, String region) {
		String clearEvent = ConfigHandler.getConfig("config.yml").getString("Clear-Items." + event);
		if (clearEvent != null && ((region != null && !region.isEmpty() && containsObject(region, clearEvent)) || containsObject(world, clearEvent))) {
			if ((Utils.containsIgnoreCase(ConfigHandler.getConfig("config.yml").getString("Clear-Items.Options"), "PROTECT_OP") && player.isOp())
					|| (Utils.containsIgnoreCase(ConfigHandler.getConfig("config.yml").getString("Clear-Items.Options"), "PROTECT_CREATIVE") && PlayerHandler.isCreativeMode(player))) {} else {
				String clearType = ConfigHandler.getConfig("config.yml").getString("Clear-Items.Type");
				if (clearType != null && (clearType.equalsIgnoreCase("ALL") || clearType.equalsIgnoreCase("GLOBAL") || clearType.equalsIgnoreCase("TRUE") || clearType.equalsIgnoreCase("ENABLE"))) {
					clearAllItems(player, region, event);
				} else if (clearType != null && clearType.equalsIgnoreCase("ITEMJOIN")) {
					setClearItemJoinItems(player, region, event);
				} else if (clearType != null) {
					ServerHandler.logSevere("{ItemMap} " + clearType + " for Clear-Items in the config.yml is not a valid option.");
				}
			}
		}
	}
	
	public static boolean isBlacklisted(String slot, ItemStack item) {
		String[] blacklist = null;
		if (ConfigHandler.getConfig("config.yml").getString("Clear-Items.Blacklist") != null 
			&& ConfigHandler.getConfig("config.yml").getString("Clear-Items.Blacklist").contains("{") 
			&& ConfigHandler.getConfig("config.yml").getString("Clear-Items.Blacklist").contains("}")) {
			blacklist = ConfigHandler.getConfig("config.yml").getString("Clear-Items.Blacklist").split(",");
		}
		
		if (ConfigHandler.getConfig("config.yml").getString("Clear-Items.Blacklist") != null) {
			try {
				for (String value : blacklist) {
					String valType = (Utils.containsIgnoreCase(value, "{id") ? "id" : (Utils.containsIgnoreCase(value, "{slot") ? "slot" : (Utils.containsIgnoreCase(value, "{name") ? "name" : "")));
						String inputResult = StringUtils.substringBetween(value, "{" + valType + ":", "}");
						if (valType.equalsIgnoreCase("id") && item.getType() == ItemHandler.getMaterial(inputResult.trim(), null)) {
							return true;
						} else if (valType.equalsIgnoreCase("slot") && slot.trim().equalsIgnoreCase(inputResult.trim())) {
							return true;
						} else if (valType.equalsIgnoreCase("name") && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
							String displayName = item.getItemMeta().getDisplayName();
							if (ChatColor.stripColor(displayName).trim().equalsIgnoreCase(inputResult.trim())) {
								return true;
							}
						}
				}
			} catch (Exception e) { 
				ServerHandler.logSevere("{ItemMap} It looks like the Blacklist section is missing quotations or apostrohes.");
				ServerHandler.logSevere("{ItemMap} Include quotations or apostrophes at the beginning and the end or this error will persist.");
				ServerHandler.logSevere("{ItemMap} The blacklist should look like '{id:DIAMOND}, {slot:0}' or \"{id:DIAMOND}, {slot:0}\".");
			}
		}
		return false;
	}

	public static void clearAllItems(Player player, String region, String type) {
		List < ItemMap > protectItems = getProtectItems();
		PlayerInventory inventory = player.getInventory();
		Inventory craftView = player.getOpenInventory().getTopInventory();
		saveReturnItems(player, region, type, craftView, inventory, true);
		if (!protectItems.isEmpty() || (ConfigHandler.getConfig("config.yml").getString("Clear-Items.Blacklist") != null && ConfigHandler.getConfig("config.yml").getString("Clear-Items.Blacklist").contains("{"))) {
			for (int i = 0; i < (!protectItems.isEmpty() ? protectItems.size() : 1); i++) {
				ItemMap item = null;
				if (!protectItems.isEmpty()) { item = protectItems.get(i); }
				if (inventory.getHelmet() != null && !isBlacklisted("Helmet", inventory.getHelmet()) && (protectItems.isEmpty() || (!item.isSimilar(inventory.getHelmet()) && i == (protectItems.size() - 1)))) {
					inventory.setHelmet(new ItemStack(Material.AIR));
				} if (inventory.getChestplate() != null && !isBlacklisted("Chestplate", inventory.getChestplate()) && (protectItems.isEmpty() || (!item.isSimilar(inventory.getChestplate()) && i == (protectItems.size() - 1)))) {
					inventory.setChestplate(new ItemStack(Material.AIR));
				} if (inventory.getLeggings() != null && !isBlacklisted("Leggings", inventory.getLeggings()) && (protectItems.isEmpty() || (!item.isSimilar(inventory.getLeggings()) && i == (protectItems.size() - 1)))) {
					inventory.setLeggings(new ItemStack(Material.AIR));
				} if (inventory.getBoots() != null && !isBlacklisted("Boots", inventory.getBoots()) && (protectItems.isEmpty() || (!item.isSimilar(inventory.getBoots()) && i == (protectItems.size() - 1)))) {
					inventory.setBoots(new ItemStack(Material.AIR));
				} if (ServerHandler.hasSpecificUpdate("1_9") && inventory.getItemInOffHand() != null && !isBlacklisted("Offhand", inventory.getItemInOffHand()) && (protectItems.isEmpty() || (!item.isSimilar(inventory.getBoots()) && i == (protectItems.size() - 1)))) {
					PlayerHandler.setOffHandItem(player, new ItemStack(Material.AIR));
				} if (PlayerHandler.isCraftingInv(player.getOpenInventory())) {
					for (int k = 0; k < player.getOpenInventory().getTopInventory().getContents().length; k++) {
						if (player.getOpenInventory().getTopInventory().getItem(k) != null && !isBlacklisted("CRAFTING[" + k + "]", player.getOpenInventory().getTopInventory().getItem(k)) && (protectItems.isEmpty() || (!item.isSimilar(player.getOpenInventory().getTopInventory().getItem(k)) && i == (protectItems.size() - 1)))) {
							craftView.setItem(k, new ItemStack(Material.AIR));
						}
					}
				} for (int f = 0; f < inventory.getSize(); f++) {
					if (inventory.getItem(f) != null && !isBlacklisted(Integer.toString(f), inventory.getItem(f)) && (protectItems.isEmpty() || (!item.isSimilar(inventory.getItem(f)) && i == (protectItems.size() - 1)))) {
						inventory.setItem(f, new ItemStack(Material.AIR));
					}
				}
			}
		} else {
			inventory.clear();
			inventory.setHelmet(new ItemStack(Material.AIR));
			inventory.setChestplate(new ItemStack(Material.AIR));
			inventory.setLeggings(new ItemStack(Material.AIR));
			inventory.setBoots(new ItemStack(Material.AIR));
			PlayerHandler.setOffHandItem(player, new ItemStack(Material.AIR));
			if (PlayerHandler.isCraftingInv(player.getOpenInventory())) {
				for (int k = 0; k < player.getOpenInventory().getTopInventory().getContents().length; k++) {
					craftView.setItem(k, new ItemStack(Material.AIR));
				}
			}
		}
	}
	
	public static void setClearItemJoinItems(Player player, String region, String type) {
		List < ItemMap > protectItems = getProtectItems();
		PlayerInventory inventory = player.getInventory();
		Inventory craftView = player.getOpenInventory().getTopInventory();
		saveReturnItems(player, region, type, craftView, inventory, false);
		for (ItemMap item: ItemUtilities.getItems()) {
			if (inventory.getHelmet() != null && !isBlacklisted("Helmet", inventory.getHelmet()) && ((protectItems.isEmpty() && ItemHandler.containsNBTData(inventory.getHelmet())) || (!protectItems.contains(item) && item.isSimilar(inventory.getHelmet())))) {
				inventory.setHelmet(new ItemStack(Material.AIR));
			} if (inventory.getChestplate() != null && !isBlacklisted("Chestplate", inventory.getChestplate()) 
				&& ((protectItems.isEmpty() && ItemHandler.containsNBTData(inventory.getChestplate())) || (!protectItems.contains(item) && item.isSimilar(inventory.getChestplate())))) {
				inventory.setChestplate(new ItemStack(Material.AIR));
			} if (inventory.getLeggings() != null && !isBlacklisted("Leggings", inventory.getLeggings()) 
				&& ((protectItems.isEmpty() && ItemHandler.containsNBTData(inventory.getLeggings())) || (!protectItems.contains(item) && item.isSimilar(inventory.getLeggings())))) {
				inventory.setLeggings(new ItemStack(Material.AIR));
			} if (inventory.getBoots() != null && !isBlacklisted("Boots", inventory.getBoots()) && ((protectItems.isEmpty() && ItemHandler.containsNBTData(inventory.getBoots())) || (!protectItems.contains(item) && item.isSimilar(inventory.getBoots())))) {
				inventory.setBoots(new ItemStack(Material.AIR));
			} if (ServerHandler.hasSpecificUpdate("1_9") && inventory.getItemInOffHand() != null && !isBlacklisted("Offhand", inventory.getItemInOffHand()) && ((protectItems.isEmpty() && ItemHandler.containsNBTData(inventory.getItemInOffHand())) 
					|| (!protectItems.contains(item) && item.isSimilar(inventory.getItemInOffHand())))) {
				inventory.setItemInOffHand(new ItemStack(Material.AIR));
			} if (PlayerHandler.isCraftingInv(player.getOpenInventory())) {
				for (int k = 0; k < player.getOpenInventory().getTopInventory().getContents().length; k++) {
					if (craftView.getItem(k) != null && !isBlacklisted("CRAFTING[" + k + "]", player.getOpenInventory().getTopInventory().getItem(k)) 
						&& craftView.getItem(k).getType() != Material.AIR && ((protectItems.isEmpty() && ItemHandler.containsNBTData(craftView.getItem(k))) 
							|| (!protectItems.contains(item) && item.isSimilar(craftView.getItem(k))))) {
						craftView.setItem(k, new ItemStack(Material.AIR));
					}
				}
			} for (int f = 0; f <= 36; f++) {
				if (inventory.getItem(f) != null && !isBlacklisted(Integer.toString(f), inventory.getItem(f)) 
					&& ((protectItems.isEmpty() && ItemHandler.containsNBTData(inventory.getItem(f))) || (!protectItems.contains(item) && item.isSimilar(inventory.getItem(f))))) {
					inventory.setItem(f, new ItemStack(Material.AIR));
				}
			}
		}
	}
	
	public static List<ItemMap> getProtectItems() {
		List<ItemMap> protectItems = new ArrayList<ItemMap>();
		if (Utils.containsIgnoreCase(ConfigHandler.getConfig("config.yml").getString("Clear-Items.Options"), "PROTECT")) {
			for (ItemMap item: ItemUtilities.getItems()) {
				if (item.isOnlyFirstJoin() || item.isOnlyFirstWorld()) {
					protectItems.add(item);
				}
			}
		}
		return protectItems;
	}
	
	public static void saveReturnItems(Player player, String region, String type, Inventory craftView, PlayerInventory inventory, boolean clearAll) {
		boolean doReturn = Utils.containsIgnoreCase(ConfigHandler.getConfig("config.yml").getString("Clear-Items.Options"), "RETURN");
		if (region != null && !region.isEmpty() && type.equalsIgnoreCase("REGION-ENTER") && doReturn) {
			Inventory saveInventory = Bukkit.createInventory(null, 54);
			for (int i = 0; i <= 47; i++) {
				if (doReturn) {
					for (ItemMap itemMap: ItemUtilities.getItems()) {
						if (!itemMap.isOnlyFirstJoin() && !itemMap.isOnlyFirstWorld()) {
							if (inventory.getItem(i) != null && inventory.getItem(i).getType() != Material.AIR && itemMap.isSimilar(inventory.getItem(i)) && i <= 41) {
								saveInventory.setItem(i, inventory.getItem(i).clone());
							} else if (i >= 42 && craftView.getItem(i - 42) != null && craftView.getItem(i - 42).getType() != Material.AIR 
									&& itemMap.isSimilar(craftView.getItem(i - 42)) && PlayerHandler.isCraftingInv(player.getOpenInventory())) {
								saveInventory.setItem(i, craftView.getItem(i - 42).clone());
							}
						}
					}
				} else {
					if (inventory.getItem(i) != null && inventory.getItem(i).getType() != Material.AIR && ((!clearAll && ItemHandler.containsNBTData(inventory.getItem(i))) || clearAll) && i <= 41) {
						saveInventory.setItem(i, inventory.getItem(i).clone());
					} else if (i >= 42 && craftView.getItem(i - 42) != null && craftView.getItem(i - 42).getType() != Material.AIR 
							&& ((!clearAll && ItemHandler.containsNBTData(craftView.getItem(i - 42))) || clearAll) && PlayerHandler.isCraftingInv(player.getOpenInventory())) {
						saveInventory.setItem(i, craftView.getItem(i - 42).clone());
					}
				}
			}
			ConfigHandler.getSQLData().saveReturnItems(player, player.getWorld().getName(), region, saveInventory);
		}
	}
	
	public static void pasteReturnItems(Player player, String world, String region) {
		if (region != null && !region.isEmpty() && Utils.containsIgnoreCase(ConfigHandler.getConfig("config.yml").getString("Clear-Items.Options"), "RETURN")) {
			Inventory inventory = ConfigHandler.getSQLData().getReturnItems(player, world, region);
			for (int i = 47; i >= 0; i--) {
				if (inventory != null && inventory.getItem(i) != null && inventory.getItem(i).getType() != Material.AIR) {
					if (i <= 41) {
						player.getInventory().setItem(i, inventory.getItem(i).clone());
					} else if (i >= 42 && PlayerHandler.isCraftingInv(player.getOpenInventory())) {
						player.getOpenInventory().getTopInventory().setItem(i - 42, inventory.getItem(i).clone());
						PlayerHandler.updateInventory(player);
					}
				}
				ConfigHandler.getSQLData().removeReturnItems(player, world, region);
			}
		}
	}

	public static boolean isObtainable(Player player, ItemMap itemMap, int session) {
		if (!itemMap.hasItem(player) || itemMap.isAlwaysGive() || !itemMap.isLimitMode(player.getGameMode())) {
			boolean firstJoin = ConfigHandler.getSQLData().hasFirstJoined(player, itemMap);
			boolean firstWorld = ConfigHandler.getSQLData().hasFirstWorld(player, itemMap);
			boolean ipLimit = ConfigHandler.getSQLData().isIPLimited(player, itemMap);
			if (itemMap.isLimitMode(player.getGameMode())) {
				if (!firstJoin && !firstWorld && !ipLimit && canOverwrite(player, itemMap)) {
					return true;
				} else if (!firstJoin && !firstWorld && !ipLimit) {
					if (session != 0 && failCount.get(session) != null) {
						failCount.put(session, failCount.get(session) + 1);
					} else if (session != 0) { failCount.put(session, 1); }
					ServerHandler.logDebug("{ItemMap} " + player.getName() + " has failed to receive item: " + itemMap.getConfigName() + ".");
				} else { 
					if (firstJoin) { 
						ServerHandler.logDebug("{ItemMap} " + player.getName() + " has already received first-join " + itemMap.getConfigName() + ", they can no longer recieve this."); 
					} else if (firstWorld) { 
						ServerHandler.logDebug("{ItemMap} " + player.getName() + " has already received first-world " + itemMap.getConfigName() + ", they can no longer recieve this in " + player.getWorld().getName() + "."); 
					} else if (ipLimit) { 
						ServerHandler.logDebug("{ItemMap} " + player.getName() + " has already received ip-limited " + itemMap.getConfigName() + ", they will only recieve this on their dedicated ip.");  
					}
				}
				return false;
			} else { return false; }
		}
		ServerHandler.logDebug("{ItemMap} " + player.getName() + " already has item: " + itemMap.getConfigName() + ".");
		return false;
	}
	
	public static boolean canOverwrite(Player player, ItemMap itemMap) {
		try {
			if (isOverwrite(player, itemMap) || (itemMap.isDropFull() || ((itemMap.isGiveNext() || itemMap.isMoveNext()) && player.getInventory().firstEmpty() != -1))) { return true; }
		} catch (Exception e) { ServerHandler.sendDebugTrace(e); }
		return false;
	}
	
	public static boolean isOverwrite(Player player, ItemMap itemMap) {
		try {
			if (itemMap.isOverwritable() || (ConfigHandler.getConfig("items.yml").getString("items-Overwrite") != null && isOverwriteWorld(player.getWorld().getName()) 
					|| ConfigHandler.getConfig("items.yml").getString("items-Overwrite") != null && ConfigHandler.getConfig("items.yml").getBoolean("items-Overwrite"))) {
				return true; 
			} else if (itemMap.getSlot().equalsIgnoreCase("ARBITRARY") && player.getInventory().firstEmpty() == -1) {
				return false;
			} else if (itemMap.getSlot().equalsIgnoreCase("HELMET") && player.getInventory().getHelmet() != null) {
				return false;
			} else if (itemMap.getSlot().equalsIgnoreCase("CHESTPLATE") && player.getInventory().getChestplate() != null) {
				return false;
			} else if (itemMap.getSlot().equalsIgnoreCase("LEGGINGS") && player.getInventory().getLeggings() != null) {
				return false;
			} else if (itemMap.getSlot().equalsIgnoreCase("BOOTS") && player.getInventory().getBoots() != null) {
				return false;
			} else if (Utils.isInt(itemMap.getSlot()) && player.getInventory().getItem(Integer.parseInt(itemMap.getSlot())) != null) {
				return false;
			} else if (ServerHandler.hasSpecificUpdate("1_9") && itemMap.getSlot().equalsIgnoreCase("OFFHAND")) {
				if (player.getInventory().getItemInOffHand().getType() != Material.AIR) {
					return false;
				}
			}
		} catch (Exception e) { ServerHandler.sendDebugTrace(e); }
		return true;
	}
	
	public static boolean isOverwriteWorld(String world) {
		if (ConfigHandler.getConfig("items.yml").getString("items-Overwrite") != null) {
			String worldlist = ConfigHandler.getConfig("items.yml").getString("items-Overwrite").replace(" ", "");
			String[] compareWorlds = worldlist.split(",");
			for (String compareWorld: compareWorlds) {
				if (compareWorld.equalsIgnoreCase(world) || compareWorld.equalsIgnoreCase("all") || compareWorld.equalsIgnoreCase("global")) {
					return true;
				}
			}
		} else if (ConfigHandler.getConfig("items.yml").getString("items-Overwrite") == null) {
			return true;
		}
		return false;
	}
	
	public static void sendFailCount(Player player, int session) {
		if (failCount.get(session) != null && failCount.get(session) != 0) {
			if (ConfigHandler.getConfig("items.yml").getString("items-Overwrite") != null && isOverwriteWorld(player.getWorld().getName()) 
					|| ConfigHandler.getConfig("items.yml").getString("items-Overwrite") != null && ConfigHandler.getConfig("items.yml").getBoolean("items-Overwrite")) {
				String[] placeHolders = Language.newString(); placeHolders[7] = failCount.get(session).toString();
				Language.sendLangMessage("General.failedInventory", player, placeHolders);
			} else {
				String[] placeHolders = Language.newString(); placeHolders[7] = failCount.get(session).toString();
				Language.sendLangMessage("General.failedOverwrite", player, placeHolders);
			}
			failCount.remove(session);
		}
	}
	
	public static void setInvSlots(Player player, ItemMap itemMap, ItemStack item, boolean command, int amount) {
		boolean givenItem = false;
		ItemStack getItem = player.getInventory().getItem(Integer.parseInt(itemMap.getSlot()));
		if (amount != 0 && command) { item.setAmount(amount); }
		if (amount != 0 || itemMap.isAlwaysGive()) { givenItem = true; player.getInventory().addItem(item); }
		else if ((((itemMap.isGiveNext() || itemMap.isMoveNext()) && player.getInventory().firstEmpty() != -1) || (itemMap.isDropFull() && player.getInventory().firstEmpty() != -1)) && getItem != null) {
			if (itemMap.isMoveNext()) { player.getInventory().setItem(Integer.parseInt(itemMap.getSlot()), item); }
			for (int i = Integer.parseInt(itemMap.getSlot()); i <= 35; i++) {
				if (itemMap.isMoveNext() && (player.getInventory().getItem(i) == null || player.getInventory().getItem(i).getType() == Material.AIR)) { givenItem = true; player.getInventory().setItem(i, getItem); break; }
				else if (player.getInventory().getItem(i) == null || player.getInventory().getItem(i).getType() == Material.AIR) { givenItem = true; player.getInventory().setItem(i, item); break; }
				else if (i == 35) {
					for (int k = Integer.parseInt(itemMap.getSlot()); k >= 0; k--) {
						if (itemMap.isMoveNext() && (player.getInventory().getItem(k) == null || player.getInventory().getItem(k).getType() == Material.AIR)) {
							givenItem = true; player.getInventory().setItem(k, getItem); break; 
						}
						else if (player.getInventory().getItem(k) == null || player.getInventory().getItem(k).getType() == Material.AIR) {
							givenItem = true; player.getInventory().setItem(k, item); break; 
						}
					}
				}
			}
		} else if (!givenItem && !itemMap.isDropFull()) { givenItem = true; player.getInventory().setItem(Integer.parseInt(itemMap.getSlot()), item); }
		if (!givenItem && itemMap.isDropFull()) { player.getWorld().dropItem(player.getLocation(), item); } 
		ConfigHandler.getSQLData().saveItemData(player, itemMap);
		ServerHandler.logDebug("{ItemMap} Given the Item: " + itemMap.getConfigName() + ".");
	}
	
	public static void setCustomSlots(Player player, ItemMap itemMap, ItemStack item, boolean command, int amount) {
		EntityEquipment Equip = player.getEquipment();
		Inventory topInventory = player.getOpenInventory().getTopInventory();
		int craftSlot = Utils.getSlotConversion(itemMap.getSlot());
		boolean givenItem = false;
		if (amount != 0 && command) { item.setAmount(amount); }
		if ((amount != 0 || itemMap.isAlwaysGive()) && itemMap.hasItem(player)) {
			player.getInventory().addItem(item); givenItem = true;
		} else if (itemMap.getSlot().equalsIgnoreCase("Arbitrary")) {
			if (player.getInventory().firstEmpty() != -1) {
				player.getInventory().addItem(item); givenItem = true;
			}
		} else if (itemMap.getSlot().equalsIgnoreCase("Helmet")) {
			if (!itemMap.isDropFull()) {
				Equip.setHelmet(item); givenItem = true;
			}
		} else if (itemMap.getSlot().equalsIgnoreCase("Chestplate")) {
			if (!itemMap.isDropFull()) {
				Equip.setChestplate(item); givenItem = true;
			}
		} else if (itemMap.getSlot().equalsIgnoreCase("Leggings")) {
			if (!itemMap.isDropFull()) {
				Equip.setLeggings(item); givenItem = true;
			}
		} else if (itemMap.getSlot().equalsIgnoreCase("Boots")) {
			if (!itemMap.isDropFull()) {
				Equip.setBoots(item); givenItem = true;
			}
		} else if (ServerHandler.hasSpecificUpdate("1_9") && itemMap.getSlot().equalsIgnoreCase("Offhand")) {
			if (!itemMap.isDropFull()) {
				PlayerHandler.setOffhandItem(player, item); givenItem = true;
			}
		} else if (craftSlot != -1 && !itemMap.isDropFull()) {
			if (craftSlot == 0) {
			    Bukkit.getScheduler().scheduleSyncDelayedTask(ItemJoin.getInstance(), new Runnable() {
			    	@Override
			    	public void run() {
			    		if (PlayerHandler.isCraftingInv(player.getOpenInventory())) {
			    			topInventory.setItem(craftSlot, item);
			    			PlayerHandler.updateInventory(player);
			    		}
			    	}
			    }, 4L);
			} else {
				topInventory.setItem(craftSlot, item);
			}
			givenItem = true;
		}
		if (!givenItem && itemMap.isDropFull()) { player.getWorld().dropItem(player.getLocation(), item); }
		ServerHandler.logDebug("{ItemMap} Given the Item: " + itemMap.getConfigName() + ".");
		ConfigHandler.getSQLData().saveItemData(player, itemMap);
	}
	
	public static void addItem(ItemMap itemMap) {
		items.add(itemMap);
	}
	
	public static List < ItemMap > getItems() {
		return items;
	}
	
	public static List < ItemMap > copyItems() {
		List < ItemMap > itemsCopy = new ArrayList < ItemMap > ();
		for (ItemMap itemMap : items) { 
			itemsCopy.add(itemMap.clone());
		}
		return itemsCopy;
	}
	
	public static void clearItems() {
		items = new ArrayList < ItemMap >();
	}
}