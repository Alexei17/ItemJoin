package me.RockinChaos.itemjoin.api;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.RockinChaos.itemjoin.giveitems.utils.ItemCommand;
import me.RockinChaos.itemjoin.giveitems.utils.ItemMap;
import me.RockinChaos.itemjoin.giveitems.utils.ItemUtilities;
import me.RockinChaos.itemjoin.handlers.ConfigHandler;
import me.RockinChaos.itemjoin.handlers.PlayerHandler;
import me.RockinChaos.itemjoin.utils.Chances;
import me.RockinChaos.itemjoin.utils.Utils;

public class APIUtils {
	
    /**
     * Gives all ItemJoin items to the specified player.
     * 
     * @param player that will recieve the items.
     */
	 public void setItems(Player player) {
		final Chances probability = new Chances();
		final ItemMap probable = probability.getRandom(player);
		final int session = Utils.getRandom(1, 80000);
		for (ItemMap item : ItemUtilities.getItems()) {
			if (item.inWorld(player.getWorld()) && probability.isProbability(item, probable) && ConfigHandler.getSQLData().isEnabled(player)
					&& item.hasPermission(player) && ItemUtilities.isObtainable(player, item, session)) {
					item.giveTo(player, false, 0);
			}
			item.setAnimations(player);
		}
		ItemUtilities.sendFailCount(player, session);
		PlayerHandler.delayUpdateInventory(player, 15L);
	 }
	 
	/**
	 * Checks if the itemstack in the said world is a custom item.
	 * 
	 * @param player that will recieve the items.
	 * @param world that the item is said to be in.
	 * @return Boolean is a custom item.
	 */
	 public boolean isCustom(ItemStack item, World world) {
		 ItemMap itemMap = this.getMap(item, world, null);
		 if (itemMap != null) {
			 return true;
		 }
		 return false;
	 }
	 
	/**
	 * Fetches the ItemStack defined for the provided itemNode.
	 * 
     * @param itemNode that is the custom items config node.
	 * @return ItemStack found custom item.
	 */
	public ItemStack getItemStack(Player player, String itemNode) {
		ItemMap itemMap = this.getMap(null, null, itemNode);
	    if (itemMap != null) {
	    	return itemMap.getItemStack(player);
		}
	    return null;
	}

	/**
	 * Fetches the config node name of the custom item.
     * 
     * @param item that will be checked.
	 * @param world that the item is said to be in.
	 * @return String node of the custom item.
	 */
	 public String getNode(ItemStack item, World world) {
		 ItemMap itemMap = this.getMap(item, world, null);
		 if (itemMap != null) {
			 return itemMap.getConfigName();
		 }
		 return null;
	 }
	 
	/**
	 * Fetches the itemflags that are defined for the custom item. 
	 * 
	 * @param itemNode that is the custom items config node.
	 * @return List of itemflags for the custom item.
	 */
	 public List <String> getItemflags(String itemNode) {
		 ItemMap itemMap = this.getMap(null, null, itemNode);
		 List <String> itemflags = new ArrayList<String>();
		 if (itemMap != null && itemMap.getItemFlags() != null && !itemMap.getItemFlags().isEmpty()) {
			for (String itemflag : itemMap.getItemFlags().replace(" ", "").split(",")) {
				itemflags.add(itemflag);
			}
			return itemflags;
		 }
		 return null;
	 }
	 
	/**
	 * Fetches commands that are defined for the custom item.
     * 
	 * @param itemNode that is the custom items config node.
     * @return List of commands for the custom item.
	 */
	 public List <String> getCommands(String itemNode) {
		 ItemMap itemMap = this.getMap(null, null, itemNode);
		 List <String> commands = new ArrayList<String>();
		 if (itemMap != null && itemMap.getCommands() != null && itemMap.getCommands().length > 0) {
			for (ItemCommand command : itemMap.getCommands()) {
				commands.add(command.getCommand());
			}
			return commands;
		 }
		 return null;
	 }
	 
   /**
	 * Fetches triggers that are defined for the custom item.
     * 
	 * @param itemNode that is the custom items config node.
	 * @return List of triggers for the custom item.
	 */
	 public List <String> getTriggers(String itemNode) {
		 ItemMap itemMap = this.getMap(null, null, itemNode);
		 List <String> triggers = new ArrayList<String>();
		 if (itemMap != null && itemMap.getTriggers() != null && !itemMap.getTriggers().isEmpty()) {
			for (String trigger : itemMap.getTriggers().replace(" ", "").split(",")) {
				triggers.add(trigger);
			}
			return triggers;
		 }
		 return null;
	 }
	 
	/**
	 * Fetches the slot that the custom item is defined to be set to.
	 * 
	 * @param itemNode that is the custom items config node.
	 * @return String of integer or custom slot for the custom item.
	 */
	 public String getSlot(String itemNode) {
		 ItemMap itemMap = this.getMap(null, null, itemNode);
		 if (itemMap != null) {
			 return itemMap.getSlot();
		 }
		 return null;
	 }
	 
	/**
	 * Fetches all slots that the custom item is defined to be set to.
	 * In the instance that the custom item is a MultiSlot item.
	 * 
	 * @param itemNode that is the custom items config node.
  	 * @return List of slots for the custom item.
     */
	 public List<String> getMultipleSlots(String itemNode) {
		 ItemMap itemMap = this.getMap(null, null, itemNode);
		 if (itemMap != null) {
			 return itemMap.getMultipleSlots();
		 }
		 return null;
	 }
	 
	/**
	 * Fetches the mapping of the custom item.
     * 
	 * @param item that will be checked.
	 * @param world that the custom item is said to be in.
	 * @param itemNode that is the custom items config node.
	 * @return ItemMap that is the located custom item.
	 */
	 private ItemMap getMap(ItemStack item, World world, String itemNode) {
		for (ItemMap itemMap: ItemUtilities.getItems()) {
			if (world != null && itemMap.inWorld(world) && itemMap.isSimilar(item)) {
			    return itemMap;
			} else if (world == null && itemMap.isSimilar(item)) {
			 	return itemMap;
			} else if (itemNode != null && world == null && item == null && itemMap.getConfigName().equalsIgnoreCase(itemNode)) {
			 	return itemMap;
			}
		}
	    return null;
	 }
}