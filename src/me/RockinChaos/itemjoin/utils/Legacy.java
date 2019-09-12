package me.RockinChaos.itemjoin.utils;

import java.util.EnumSet;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.map.MapView;
import org.bukkit.material.MaterialData;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import me.RockinChaos.itemjoin.ItemJoin;
import me.RockinChaos.itemjoin.giveitems.utils.ItemUtilities;
import me.RockinChaos.itemjoin.handlers.ServerHandler;

@SuppressWarnings("deprecation")
public class Legacy {
	
	// WELCOME TO THE LAND OF MAKE-BELIEVE! //
    
    public static void updateLegacyInventory(Player player) {
    	player.updateInventory();
    }
    
    public static void setLegacyInHandItem(Player player, ItemStack item) {
    	player.setItemInHand(item);
    }
    
    public static ItemStack getLegacyInHandItem(Player player) {
    	return player.getInventory().getItemInHand();
    }
    
    public static String getLegacySkullOwner(SkullMeta skullMeta) {
    	return skullMeta.getOwner();
    }
    
    public static org.bukkit.inventory.meta.MapMeta setMapID(org.bukkit.inventory.meta.MapMeta meta, int mapId) {
    	MapMeta mapmeta = meta;
    	mapmeta.setMapId(mapId);
    	return mapmeta; 
    }
    
    public static MapView getMapView(int id) {
    	if (!ItemUtilities.getMapViewMethod()) {
    		try { return ItemJoin.getInstance().getServer().getMap((short) id); } 
			catch (NoSuchMethodError e) { ItemUtilities.setMapViewMethod(true); return Reflection.getMapView(id); }
    	} else { return Reflection.getMapView(id); }
    }

    public static MapView createLegacyMapView() {
    	try {
    		return ItemJoin.getInstance().getServer().createMap(ItemJoin.getInstance().getServer().getWorlds().get(0));
    	} catch (Exception e) { }
    	return null;
    }
    
    public static ItemStack newLegacyItemStack(Material material, int count, short dataValue) {
    	return new ItemStack(material, count, dataValue);
    }
    
    public static org.bukkit.Material getLegacyMaterial(int typeID, byte dataValue) {
		return ItemJoin.getInstance().getServer().getUnsafe().fromLegacy(new MaterialData(findLegacyMaterial(typeID), dataValue));
    }
    
    public static org.bukkit.Material getLegacyMaterial(Material typeID, byte dataValue) {
  		return ItemJoin.getInstance().getServer().getUnsafe().fromLegacy(new MaterialData(typeID, dataValue));
    }
    
    public static org.bukkit.Material findLegacyMaterial(int typeID) {
        final Material[] foundMaterial = new Material[1];
        EnumSet.allOf(Material.class).forEach(material -> { try { if (Utils.containsIgnoreCase(material.toString(), "LEGACY_") && material.getId() == typeID || !ServerHandler.hasAquaticUpdate() && material.getId() == typeID) { foundMaterial[0] = material; } } catch (Exception e) { }});
        return foundMaterial[0];
    }

	public static int getLegacyMaterialID(Material mat) {
	    return mat.getId();
	}
	
	public static ItemStack setLegacyDurability(ItemStack item, short durability) {
		item.setDurability(durability);
		return item;
	}
	
	public static short getLegacyDurability(ItemStack item) {
		return item.getDurability();
	}
	
	public static String getLegacyEnchantName(Enchantment e) {
		return e.getName();
	}
	
	public static Enchantment getLegacyEnchantByName(String name) {
		return Enchantment.getByName(name.toUpperCase());
	}
	
	public static ItemMeta setLegacySkullOwner(SkullMeta skullMeta, String owner) {
		skullMeta.setOwner(owner);
		return skullMeta;
	}
	
	public static Player getLegacyPlayer(String playerName) {
		return Bukkit.getPlayer(playerName);
	}
	
	public static ApplicableRegionSet getLegacyRegionSet(World world, Location loc) {
        com.sk89q.worldguard.bukkit.WorldGuardPlugin wg = (com.sk89q.worldguard.bukkit.WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        com.sk89q.worldguard.bukkit.RegionContainer rm = wg.getRegionContainer();
        if (hasLegacyWorldEdit()) {
        	com.sk89q.worldedit.Vector wgVector = new com.sk89q.worldedit.Vector(loc.getX(), loc.getY(), loc.getZ());
        	return rm.get(world).getApplicableRegions(wgVector);
        } else { return rm.get(world).getApplicableRegions(loc); }
	}
	
	public static Map<String, ProtectedRegion> getLegacyRegions(World world) {
        com.sk89q.worldguard.bukkit.WorldGuardPlugin wg = (com.sk89q.worldguard.bukkit.WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
        com.sk89q.worldguard.bukkit.RegionContainer rm = wg.getRegionContainer();
        if (hasLegacyWorldEdit()) {
        	return rm.get(world).getRegions();
        } else { return rm.get(world).getRegions(); }
	}
	
	public static boolean hasLegacyWorldEdit() {
		try {
			Class<?> wEdit = Class.forName("com.sk89q.worldedit.Vector");
			if (wEdit != null) { return true; }
			return false;
		} catch (Exception e) { return false; }
	}
	
    public static com.sk89q.worldedit.math.BlockVector3 asBlockVector(org.bukkit.Location location) {
        return com.sk89q.worldedit.math.BlockVector3.at(location.getX(), location.getY(), location.getZ());
    }
}