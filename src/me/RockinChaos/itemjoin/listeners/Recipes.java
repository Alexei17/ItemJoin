package me.RockinChaos.itemjoin.listeners;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import me.RockinChaos.itemjoin.handlers.ItemHandler;
import me.RockinChaos.itemjoin.handlers.PlayerHandler;
import me.RockinChaos.itemjoin.utils.Utils;

public class Recipes implements Listener {
	
    @EventHandler
    public void onPlayerCraft(PrepareItemCraftEvent event) {
        for (HumanEntity entity: event.getViewers()) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                for (int i = 0; i < player.getOpenInventory().getTopInventory().getSize(); i++) {
                    if (player.getOpenInventory().getTopInventory().getItem(i) != null && player.getOpenInventory().getTopInventory().getItem(i).getType() != Material.AIR) {
                        if (!ItemHandler.isAllowed(player, player.getOpenInventory().getTopInventory().getItem(i), "item-craftable")) {
                            ItemStack reAdd = player.getOpenInventory().getTopInventory().getItem(i).clone();
                            player.getOpenInventory().getTopInventory().setItem(i, null);
                            player.getInventory().addItem(reAdd);
                            PlayerHandler.updateInventory(player);
                            break;
                        }
                    }
                }
            }
        }
    }
  
	@EventHandler
	public void onRepairAnvil(InventoryClickEvent event) {
	    if (event.getInventory().getType().toString().contains("ANVIL")) {
	        Player player = (Player) event.getWhoClicked();
	        int rSlot = event.getSlot();
	        if (rSlot == 2 && event.getInventory().getItem(1) != null &&
	            event.getInventory().getItem(1).getType() != Material.AIR) {
	            ItemStack item = event.getInventory().getItem(2);
	            if (!Utils.containsIgnoreCase(event.getInventory().getItem(1).getType().toString(), "PAPER") && !Utils.containsIgnoreCase(event.getInventory().getItem(1).getType().toString(), "NAME_TAG") &&
	                !ItemHandler.isAllowed(player, item, "item-repairable") || !ItemHandler.isAllowed(player, event.getInventory().getItem(1), "item-repairable")) {
	                event.setCancelled(true);
	                PlayerHandler.updateExperienceLevels(player);
	                PlayerHandler.updateInventory(player);
	            }
	        }
	    }
	}
}