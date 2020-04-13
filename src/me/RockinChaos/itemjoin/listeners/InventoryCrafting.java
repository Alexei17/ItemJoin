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
package me.RockinChaos.itemjoin.listeners;

import java.util.Collection;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.RockinChaos.itemjoin.ItemJoin;
import me.RockinChaos.itemjoin.giveitems.utils.ItemMap;
import me.RockinChaos.itemjoin.giveitems.utils.ItemUtilities;
import me.RockinChaos.itemjoin.handlers.ItemHandler;
import me.RockinChaos.itemjoin.handlers.PlayerHandler;
import me.RockinChaos.itemjoin.handlers.ServerHandler;
import me.RockinChaos.itemjoin.handlers.events.PlayerAutoCraftEvent;

public class InventoryCrafting implements Listener {
	private static HashMap<String, ItemStack[]> craftingItems = new HashMap<String, ItemStack[]>();
	private static HashMap<String, ItemStack[]> craftingOpenItems = new HashMap<String, ItemStack[]>();
	private static HashMap<String, ItemStack[]> creativeCraftingItems = new HashMap<String, ItemStack[]>();
	private HashMap<String, Boolean> worldSwitch = new HashMap<String, Boolean>();
	
	@EventHandler(priority = EventPriority.HIGHEST)
	private void onAutoCraft(PlayerAutoCraftEvent event) {
		for (int i = 0; i <= 4; i++) {
  			final ItemStack[] craftingContents = event.getContents().clone();
  			if (event.isCancelled()) { return; }
  			for (ItemMap itemMap: ItemUtilities.getItems()) {
  				if (!event.isCancelled() && itemMap.isCraftingItem() && itemMap.isSimilar(craftingContents[i])) {
  					event.setCancelled(true);
  				} else if (event.isCancelled()) { return; }
  			}
  		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
    private void onCraftingOpen(InventoryOpenEvent event) {
    	final Player player = (Player) event.getPlayer();
    	if (!craftingOpenItems.containsKey(PlayerHandler.getPlayerID(player))) {
	    	craftingOpenItems.put(PlayerHandler.getPlayerID(player), this.getContents(player));
			ItemHandler.removeCraftItems(player);
	    	PlayerHandler.updateInventory(player);
    	}
    }
	
	@EventHandler(priority = EventPriority.HIGHEST)
    private void onCraftingClose(InventoryCloseEvent event) {
    	final InventoryView view = event.getView();
    	final Player player = (Player) event.getPlayer();
    	final ItemStack[] craftingContents = view.getTopInventory().getContents().clone();
    	if (PlayerHandler.isCraftingInv(view)) {
    		for (int i = 0; i <= 4; i++) {
    			boolean craftingItem = false;
    			for (ItemMap itemMap: ItemUtilities.getItems()) {
    				if (itemMap.isCraftingItem() && itemMap.isSimilar(craftingContents[i])) {
    					craftingItem = true;
    					break;
    				}
    			}
    			if (!craftingItem && craftingContents[i] != null && craftingContents[i].getType() != Material.AIR) {
    				if (player.getInventory().firstEmpty() != -1) {
    					player.getInventory().addItem(craftingContents[i].clone());
    				} else {
    					this.dropItem(player, craftingContents[i].clone());
    				}
    				craftingContents[i] = new ItemStack(Material.AIR);
    			}
    		}
    		for (int i = 0; i <= 4; i++) { view.setItem(i, new ItemStack(Material.AIR)); }
    		for (int i = 0; i <= 4; i++) { this.delayReturnItem(player, i, craftingContents[i], 1L); }
    	}else {
    		new BukkitRunnable() {
                @Override
                public void run() {
            		if (PlayerHandler.isCraftingInv(player.getOpenInventory()) && craftingOpenItems.containsKey(PlayerHandler.getPlayerID(player))) {
                    	ItemStack[] openCraftContents = craftingOpenItems.get(PlayerHandler.getPlayerID(player));
            			if (openCraftContents != null && openCraftContents.length != 0) { 
                    	for (int i = 4; i >= 0; i--) { delayReturnItem(player, i, openCraftContents[i], 1L); }
                    	craftingItems.put(PlayerHandler.getPlayerID(player), craftingOpenItems.get(PlayerHandler.getPlayerID(player)));
            			craftingOpenItems.remove(PlayerHandler.getPlayerID(player));
            			}
            		}
                }
            }.runTaskAsynchronously(ItemJoin.getInstance());
    	}
    }
    
	@EventHandler(priority = EventPriority.HIGHEST)
    private void onCraftingClick(InventoryClickEvent event) {
    	final InventoryView view = event.getView();
    	final Player player = (Player) event.getWhoClicked();
    	final ItemStack[] craftingContents = view.getTopInventory().getContents().clone();
    	if (PlayerHandler.isCraftingInv(view) && event.getSlot() <= 4) {
    		if (event.getSlot() != 0 && event.getSlotType() == SlotType.CRAFTING) {
    			if (craftingContents[0] != null && craftingContents[0].getType() != Material.AIR) {
    				this.delayReturnItem(player, 0, craftingContents[0], 1L);
    			}
    		} else if (event.getSlot() == 0 && event.getSlotType() == SlotType.RESULT) {
    			if (craftingContents[0] != null && craftingContents[0].getType() != Material.AIR) {
    				for (ItemMap itemMap: ItemUtilities.getItems()) {
    					if (itemMap.isCraftingItem() && !itemMap.isMovement() && itemMap.isSimilar(craftingContents[0])) {
    						for (int i = 1; i <= 4; i++) { this.delayReturnItem(player, i, craftingContents[i].clone(), 1L); }
    						break;
    					}
    				}
    			} else if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
    				for (ItemMap itemMap: ItemUtilities.getItems()) {
    					if (itemMap.isCraftingItem() && !itemMap.isMovement() && itemMap.isSimilar(event.getCursor())) {
    						ItemStack cursor = event.getCursor().clone();
    						player.setItemOnCursor(new ItemStack(Material.AIR));
    						this.delayReturnItem(player, 0, cursor, 1L);
    						break;
    					}
    				}
    			}
    		}
    	}
    }
    
    @EventHandler
    private void onCraftingDrop(PlayerDropItemEvent event) {
    	final Player player = (Player) event.getPlayer();
    	final ItemStack item = event.getItemDrop().getItemStack().clone();
    	if (!player.isDead() && !event.isCancelled()) {
	    	event.getItemDrop().getItemStack().setItemMeta(null);
	    	event.getItemDrop().remove();
	    	Bukkit.getScheduler().scheduleSyncDelayedTask(ItemJoin.getInstance(), new Runnable() {
	    		@Override
	    		public void run() {
	    			ItemMap itemMap = ItemUtilities.getItemMap(item, null, player.getWorld());
	    			if (itemMap != null && !event.isCancelled() && worldSwitch.containsKey(PlayerHandler.getPlayerID(player)) && worldSwitch.get(PlayerHandler.getPlayerID(player))) {
		    			boolean isCrafting = itemMap.isCraftingItem();
	    				if (!isCrafting) {
	    					dropItem(player, item);
	    				}
	    			} else {
	    				dropItem(player, item);
	    			}
	    		}
	    	}, 2L);
    	}
    }
    
    @EventHandler
    private void onSwitchTrigger(PlayerChangedWorldEvent event) {
    	final Player player = (Player) event.getPlayer();
    	worldSwitch.put(PlayerHandler.getPlayerID(player), true);
    	Bukkit.getScheduler().scheduleSyncDelayedTask(ItemJoin.getInstance(), new Runnable() {
    		@Override
    		public void run() {
    			if (craftingItems.containsKey(PlayerHandler.getPlayerID(player))) {
    	    		for (int i = 0; i <= 4; i++) {
    					delayReturnItem(player, i, craftingItems.get(PlayerHandler.getPlayerID(player))[i], 1L);
    				}
    			}
    			worldSwitch.remove(PlayerHandler.getPlayerID(player));
    		}
    	}, 4L);
    }
    
    @EventHandler
    private void onSwitchGamemode(PlayerGameModeChangeEvent event) {
    	final Player player = (Player) event.getPlayer();
    	if (event.getNewGameMode() == GameMode.CREATIVE) {
    		creativeCraftingItems.put(PlayerHandler.getPlayerID(player), craftingItems.get(PlayerHandler.getPlayerID(player)));
    		ItemHandler.removeCraftItems(player);
    	} else if (event.getNewGameMode() != GameMode.CREATIVE && creativeCraftingItems.containsKey(PlayerHandler.getPlayerID(player))) {
    		ItemStack[] craftingContents = creativeCraftingItems.get(PlayerHandler.getPlayerID(player));
    		for (int i = 4; i >= 0; i--) { this.delayReturnItem(player, i, craftingContents[i], 1L); }
    		craftingItems.put(PlayerHandler.getPlayerID(player), creativeCraftingItems.get(PlayerHandler.getPlayerID(player)));
    		creativeCraftingItems.remove(PlayerHandler.getPlayerID(player));
    	}
    	PlayerHandler.updateInventory(player);
    }
    
    public static void cycleTask() {
    	Bukkit.getScheduler().runTaskTimerAsynchronously(ItemJoin.getInstance(), new Runnable() {
    		public void run() {
    			Collection < ? > playersOnlineNew = null;
    			Player[] playersOnlineOld;
    			try {
    				if (Bukkit.class.getMethod("getOnlinePlayers", new Class < ? > [0]).getReturnType() == Collection.class) {
    					if (Bukkit.class.getMethod("getOnlinePlayers", new Class < ? > [0]).getReturnType() == Collection.class) {
    						playersOnlineNew = ((Collection < ? > ) Bukkit.class.getMethod("getOnlinePlayers", new Class < ? > [0]).invoke(null, new Object[0]));
    						for (Object objPlayer: playersOnlineNew) {
    							if (((Player) objPlayer).isOnline() && PlayerHandler.isCraftingInv(((Player) objPlayer).getOpenInventory())) {
    								ItemStack[] tempContents = ((Player) objPlayer).getOpenInventory().getTopInventory().getContents();
    								ItemStack[] contents = new ItemStack[5];
    								if (contents != null && tempContents != null) {
	    								for (int i = 0; i <= 4; i++) {
	    									contents[i] = tempContents[i].clone();
	    								}
    								}
    								craftingItems.put(PlayerHandler.getPlayerID(((Player) objPlayer)), contents);
    							} else {
    								craftingItems.remove(PlayerHandler.getPlayerID((Player) objPlayer));
    							}
    						}
    					}
    				} else {
    					playersOnlineOld = ((Player[]) Bukkit.class.getMethod("getOnlinePlayers", new Class < ? > [0]).invoke(null, new Object[0]));
    					for (Player player: playersOnlineOld) {
    						if (player.isOnline() && PlayerHandler.isCraftingInv(player.getOpenInventory())) {
    							ItemStack[] tempContents = player.getOpenInventory().getTopInventory().getContents();
    							ItemStack[] contents = new ItemStack[5];
    							if (contents != null && tempContents != null) {
    								for (int i = 0; i <= 4; i++) {
    									contents[i] = tempContents[i].clone();
    								}
    							}
    							craftingItems.put(PlayerHandler.getPlayerID(player), contents);
    						} else {
    							craftingItems.remove(PlayerHandler.getPlayerID(player));
    						}
    					}
    				}
    			} catch (Exception e) {
    				ServerHandler.sendDebugTrace(e);
    			}
    		}
    	}, 0L, 40L);
    }
    
    private void delayReturnItem(final Player player, final int slot, final ItemStack item, long delay) {
    	if (item == null) { return; } if (slot == 0) { delay = 3L; }
    	Bukkit.getScheduler().scheduleSyncDelayedTask(ItemJoin.getInstance(), new Runnable() {
    		@Override
    		public void run() {
    			if (PlayerHandler.isCraftingInv(player.getOpenInventory())) {
    	    		player.getOpenInventory().getTopInventory().setItem(slot, item);	
    	    		PlayerHandler.updateInventory(player);
    			} else {
    				delayReturnItem(player, slot, item, 10L);
    			}
    		}
    	}, delay);
    }
    
    private void dropItem(final Player player, final ItemStack item) { 
    	Location location = player.getLocation();
    	location.setY(location.getY() + 1);
    	Item dropped = player.getWorld().dropItem(location, item);
		dropped.setVelocity(location.getDirection().multiply(.30));
    }
    
    private ItemStack[] getContents(Player player) {
		ItemStack[] tempContents = player.getOpenInventory().getTopInventory().getContents();
		ItemStack[] contents = new ItemStack[5];
		if (contents != null && tempContents != null) { 
			for (int i = 0; i <= 4; i++) { 
				contents[i] = tempContents[i].clone(); 
			} 
			return contents;
		}
		return tempContents;
    }
    
    public static HashMap<String, ItemStack[]> getCraftItems() {
    	return craftingItems;
    }
    
    public static HashMap<String, ItemStack[]> getCreativeCraftItems() {
    	return creativeCraftingItems;
    }
    
    public static HashMap<String, ItemStack[]> getOpenCraftItems() {
    	return craftingOpenItems;
    }
}