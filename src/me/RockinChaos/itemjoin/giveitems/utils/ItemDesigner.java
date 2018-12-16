package me.RockinChaos.itemjoin.giveitems.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.BookMeta.Generation;
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import me.RockinChaos.itemjoin.ItemJoin;
import me.RockinChaos.itemjoin.handlers.ConfigHandler;
import me.RockinChaos.itemjoin.handlers.ItemHandler;
import me.RockinChaos.itemjoin.handlers.ServerHandler;
import me.RockinChaos.itemjoin.utils.Hooks;
import me.RockinChaos.itemjoin.utils.ImageRenderer;
import me.RockinChaos.itemjoin.utils.Legacy;
import me.RockinChaos.itemjoin.utils.Reflection;
import me.RockinChaos.itemjoin.utils.Utils;
import me.RockinChaos.itemjoin.utils.sqlite.SQLData;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;

public class ItemDesigner {

	public void generateItems() {
		if (ConfigHandler.isConfigurable()) {
			for (String internalName: ConfigHandler.getConfigurationSection().getKeys(false)) {
				ConfigurationSection itemNode = ConfigHandler.getItemSection(internalName);
				String[] slots = itemNode.getString(".slot").replace(" ", "").split(",");
				for (String slot: slots) {
					if (isCreatable(internalName, slot)) {
						ItemMap itemMap = new ItemMap(internalName, slot);
						this.setMaterial(itemMap);
						
						this.setSkullDatabase(itemMap);
						this.setUnbreaking(itemMap);
						this.showDurability(itemMap);
						this.setEnchantments(itemMap);
						this.setMapImage(itemMap);
						this.setJSONBookPages(itemMap);
						this.setNBTData(itemMap);
						this.setName(itemMap);
						this.setLore(itemMap);
						this.setDurability(itemMap);
						this.setSkull(itemMap);
						this.setSkullTexture(itemMap);
						this.setConsumableEffects(itemMap);
						this.setPotionEffects(itemMap);
						this.setTippedArrows(itemMap);
						this.setBanners(itemMap);
						this.setFireworks(itemMap);
						this.setFireChargeColor(itemMap);
						this.setDye(itemMap);
						this.setBookAuthor(itemMap);
						this.setBookTitle(itemMap);
						this.setBookGeneration(itemMap);
						this.setLegacyBookPages(itemMap);
						this.setAttributes(itemMap);
						this.setProbability(itemMap);
						
						ItemUtilities.addItem(itemMap);
					}
				}
			}
		}
	}
	
	private boolean isCreatable(String internalName, String slot) {
		ConfigurationSection itemNode = ConfigHandler.getItemSection(internalName);
		String id = ItemHandler.getMaterialPath(itemNode);
		String originalID = id;
		if (id.contains(":")) { 
			String[] parts = id.split(":"); id = parts[0]; 
			ServerHandler.sendConsoleMessage("&4[WARNING] The item " + internalName + " is using an ItemID (Numerical Value) which is no longer supported as of Minecraft 1.13, instead use its material name.");
			ServerHandler.sendConsoleMessage("&4This will cause issues, please see: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html for a list of material names.");
		}
		if (slot != null) {
			if (!Utils.isInt(slot) && !ItemHandler.isCustomSlot(slot)) {
				ServerHandler.sendConsoleMessage("&eThe Item " + internalName + "'s slot is invalid or does not exist!");
				ServerHandler.sendConsoleMessage("&eThe Item " + internalName + " &ewill not be set!");
				return false;
			} else if (Utils.isInt(slot)) {
				int iSlot = Integer.parseInt(slot);
				if (!(iSlot >= 0 && iSlot <= 35)) {
					ServerHandler.sendConsoleMessage("&eThe Item " + internalName + "'s slot must be between 1 and 36!");
					ServerHandler.sendConsoleMessage("&eThe Item " + internalName + " &ewill not be set!");
					return false;
				}
			} else if (!ServerHandler.hasCombatUpdate() && slot.equalsIgnoreCase("Offhand")) {
				ServerHandler.sendConsoleMessage("&4Your server is running &eMC " + Reflection.getServerVersion() + " and this version of Minecraft does not have Offhand support!");
				return false;
			}
		}
		if (!ServerHandler.hasCombatUpdate() && id != null) {
			if (id.equalsIgnoreCase("TIPPED_ARROW") || id.equalsIgnoreCase("440") || id.equalsIgnoreCase("0440")) {
				ServerHandler.sendConsoleMessage("&4Your server is running &eMC " + Reflection.getServerVersion() + " and this version of Minecraft does not have the item TIPPED_ARROW!");
				ServerHandler.sendConsoleMessage("&4You are receiving this notice because the item(s) exists in your items.yml and will not be set, please remove the item(s) or update your server!");
				return false;
			} else if (id.equalsIgnoreCase("LINGERING_POTION") || id.equalsIgnoreCase("441") || id.equalsIgnoreCase("0441")) {
				ServerHandler.sendConsoleMessage("&4Your server is running &eMC " + Reflection.getServerVersion() + " and this version of Minecraft does not have the item LINGERING_POTION!");
				ServerHandler.sendConsoleMessage("&4You are receiving this notice because the item(s) exists in your items.yml and will not be set, please remove the item(s) or update your server!");
				return false;
			} else if (ItemHandler.getMaterial(originalID, null, itemNode.getName()) == null) {
				ServerHandler.sendConsoleMessage("&4Your server is running &eMC " + Reflection.getServerVersion() + " and this version of Minecraft does not have the item " + id);
				ServerHandler.sendConsoleMessage("&4You are receiving this notice because the item(s) exists in your items.yml and will not be set, please remove the item(s) or update your server!");
				return false;
			}
		} else {
			if (ItemHandler.getMaterial(originalID, null, itemNode.getName()) == null) {
				ServerHandler.sendConsoleMessage("&eThe Item " + internalName + "'s Material 'ID' is invalid or does not exist!");
				ServerHandler.sendConsoleMessage("&eThe Item " + internalName + " &ewill not be set!");
				return false;
			}
		}
		return true;
	}
	
//  =============================================== //
//  ~ Sets the Custom Material to the Custom Item ~ //
//       Adds the custom material to the item.      //
//  =============================================== //
	
	private Material getActualMaterial(ItemMap itemMap) {
		String material = ItemHandler.purgeDelay(itemMap.getNodeLocation().getString(".id"));
		if (ConfigHandler.getMaterialSection(itemMap.getNodeLocation()) != null) {
			List<String> materials = new ArrayList<String>();
			for (String materialKey : ConfigHandler.getMaterialSection(itemMap.getNodeLocation()).getKeys(false)) {
				String materialList = itemMap.getNodeLocation().getString(".id." + materialKey);
				if (materialList != null) {
					materials.add(materialList);
				}
			}
			itemMap.setDynamicMaterials(materials);
			material = ItemHandler.purgeDelay(itemMap.getNodeLocation().getString(".id." + ConfigHandler.getMaterialSection(itemMap.getNodeLocation()).getKeys(false).iterator().next()));
			if (material.contains(":")) { String[] parts = material.split(":"); itemMap.setDataValue((short) Integer.parseInt(parts[1])); }
			return ItemHandler.getMaterial(material, null, itemMap.getConfigName());
		}
		if (material.contains(":")) { String[] parts = material.split(":"); itemMap.setDataValue((short) Integer.parseInt(parts[1])); }
		return ItemHandler.getMaterial(material, null, itemMap.getConfigName());
	}
	
	private void setMaterial(ItemMap itemMap) {
		Material material = getActualMaterial(itemMap);
		itemMap.setMaterial(material);
		itemMap.renderItemStack();
	}
//====================================================================================================================================================================================================== //

	
// =============================================================== //
//  ~ Sets the HeadDatabase Texture to the Custom Skull Item ~     //
// Gives the item the skull texture of the exact HeadDatabase ID.  //
// =============================================================== //
	private void setSkullDatabase(ItemMap itemMap) {
		if (ServerHandler.hasSpecificUpdate("1_8") && Hooks.hasHeadDatabase() && itemMap.getNodeLocation().getString(".skull-texture") != null  && itemMap.getNodeLocation().getString(".skull-texture").contains("hdb-")) {
			if (itemMap.getMaterial().toString().equalsIgnoreCase("SKULL_ITEM") || itemMap.getMaterial().toString().equalsIgnoreCase("PLAYER_HEAD")) {
				if (itemMap.getNodeLocation().getString(".skull-owner") != null) {  ServerHandler.sendErrorMessage("&4You cannot define a skull owner and a skull texture at the same time, please remove one from the item."); return;  }
				String skullTexture = itemMap.getNodeLocation().getString(".skull-texture").replace("hdb-", "");
				try {
					itemMap.setSkullTexture(skullTexture);
					itemMap.setHeadDatabase(true);
				} catch (NullPointerException e) {
					ServerHandler.sendErrorMessage("&4HeadDatabase could not find &c#" + skullTexture + "&4, this head does not exist.");
					ServerHandler.sendDebugTrace(e);
				}
			}
		}
	}
//  ================================================================================================================================================================================================================================================= //
	
//  ======================================================== //
//   ~ Sets the Unbreakable Boolean to the Custom Item ~     //
//  Prevents any item with a durability from being damaged.  //
//  ======================================================== //
	private void setUnbreaking(ItemMap itemMap) {
		if (Utils.containsIgnoreCase(itemMap.getItemFlags(), "unbreakable")) {
			try {
				itemMap.setUnbreakable(true);
			} catch (Exception e) { ServerHandler.sendDebugTrace(e); } }
	}
//  ===================================================================================== //

	private void showDurability(ItemMap itemMap) {
		if (Utils.containsIgnoreCase(itemMap.getItemFlags(), "hide-durability")) {
			try {
				itemMap.setHideDurability(true);
			} catch (Exception e) { ServerHandler.sendDebugTrace(e); } }
	}
	
//  =================================================== //
//  ~ Sets the Custom Enchants to the Custom Item ~     //
//    Adds the specified enchantments to the item.      //
//  =================================================== //
	private void setEnchantments(ItemMap itemMap) {
		if (itemMap.getNodeLocation().getString(".enchantment") != null) {
			String enchantlist = itemMap.getNodeLocation().getString(".enchantment").replace(" ", "");
			String[] enchantments = enchantlist.split(",");
			Map < String, Integer > listEnchants = new HashMap < String, Integer > ();
			for (String enchantment: enchantments) {
				String[] parts = enchantment.split(":");
				String name = parts[0].toUpperCase();
				int level = 1;
				Enchantment enchantName = ItemHandler.getEnchantByName(name);
				if (Utils.containsIgnoreCase(enchantment, ":")) {
					try {
						level = Integer.parseInt(parts[1]);
					} catch (NumberFormatException e) {
						ServerHandler.sendErrorMessage("&4An error occurred in the config, &c" + parts[1] + "&4 is not a number and a number was expected!");
						ServerHandler.sendErrorMessage("&aEnchantment: " + parts[0] + " will now be enchanted by level 1.");
						ServerHandler.sendDebugTrace(e);
					}
				}
				if (enchantName != null) {
					listEnchants.put(name, level);
				} else if (enchantName == null && Hooks.hasTokenEnchant() == true && TokenEnchantAPI.getInstance().getEnchant(name) != null) {
					listEnchants.put(name, level);
				} else if (enchantName == null && Hooks.hasTokenEnchant() != true) {
					ServerHandler.sendErrorMessage("&4An error occurred in the config, &a" + name + "&4 is an incorrect enchantment name!");
					ServerHandler.sendErrorMessage("&4Please see: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/enchantments/Enchantment.html for a list of correct enchantment names!");
				}
			}
			itemMap.setEnchantments(listEnchants);
		}
	}
//  ============================================================================================================================================================================================== //

//  ======================================================= //
//     ~ Sets the Custom Map Image to the Custom Item ~     //
//   Displays the specified map image on the items canvas.  //
//  ======================================================= //
	private void setMapImage(ItemMap itemMap) {
		if (itemMap.getNodeLocation().getString(".custom-map-image") != null && Utils.containsIgnoreCase(itemMap.getMaterial().toString(), "MAP")) {
			itemMap.setMapImage(itemMap.getNodeLocation().getString(".custom-map-image"));
			if (itemMap.getMapImage().equalsIgnoreCase("default.png") || new File(ItemJoin.getInstance().getDataFolder(), itemMap.getMapImage()).exists()) {
				if (SQLData.hasImage(itemMap.getConfigName(), itemMap.getMapImage())) {
					int mapID = SQLData.getMapID(itemMap.getMapImage());
					itemMap.setMapID(mapID);
					if (ImageRenderer.hasRendered.get(itemMap.getMapImage()) == null || ImageRenderer.hasRendered.get(itemMap.getMapImage()) != null && !ImageRenderer.hasRendered.get(itemMap.getMapImage()).toString().contains(mapID + "")) {
						MapView view = ImageRenderer.FetchExistingView(mapID);
							try { view.removeRenderer(view.getRenderers().get(0)); } catch (NullPointerException e) { ServerHandler.sendDebugTrace(e); }
							try { view.addRenderer(new ImageRenderer(itemMap.getMapImage(), mapID)); } catch (NullPointerException e) { ServerHandler.sendDebugTrace(e); }
					}
				} else if (!SQLData.hasImage(itemMap.getConfigName(), itemMap.getMapImage())) {
					MapView view = ImageRenderer.NewMapView();
					try { view.removeRenderer(view.getRenderers().get(0)); } catch (NullPointerException e) { ServerHandler.sendDebugTrace(e); }
					int mapID = Legacy.getMapID(view);
					itemMap.setMapID(mapID);
					try { view.addRenderer(new ImageRenderer(itemMap.getMapImage(), mapID)); } catch (NullPointerException e) { ServerHandler.sendDebugTrace(e); }
					SQLData.saveMapImage(itemMap.getConfigName(), "map-id", itemMap.getMapImage(), mapID);
				}
			}
		}
	}
//  =========================================================================================================================================================================================================================== //
	
//  =============================================== //
//      ~ Sets the NBTData to the Custom Item ~     //
//  This designs the item to be unique to ItemJoin. //
//  =============================================== //
	private void setNBTData(ItemMap itemMap) {
		if (Hooks.hasNewNBTSystem() && !itemMap.isVanilla()) {
			try {
				Object tag = Reflection.getNMS("NBTTagCompound").getConstructor().newInstance();
				tag.getClass().getMethod("setString", String.class, String.class).invoke(tag, "ItemJoin Name", itemMap.getConfigName());
				tag.getClass().getMethod("setString", String.class, String.class).invoke(tag, "ItemJoin Slot", itemMap.getItemValue());
				itemMap.setNewNBTData(itemMap.getConfigName() + " " + itemMap.getItemValue(), tag);
			} catch (Exception e) {
				ServerHandler.sendDebugMessage("Error 133 has occured when setting NBTData to an item.");
				ServerHandler.sendDebugTrace(e);
			}
		} else { itemMap.setLegacySecret(ConfigHandler.encodeSecretData(ConfigHandler.getNBTData(itemMap))); }
	}
//  =========================================================================================================================================================== //
	
//  ========================================================== //
//         ~ Sets the Book Pages to the Custom Item ~          //
//  Adds the custom book pages to the item in JSON Formatting. //
//  ========================================================== //
	private void setJSONBookPages(ItemMap itemMap) { 
		if (itemMap.getMaterial().toString().equalsIgnoreCase("WRITTEN_BOOK") && itemMap.getNodeLocation().getString(".pages") != null 
				&& ConfigHandler.getPagesSection(itemMap.getNodeLocation()) != null && ServerHandler.hasSpecificUpdate("1_8")) {
			List<String> actualPageList = new ArrayList<String>();
			for (String pageString: ConfigHandler.getPagesSection(itemMap.getNodeLocation()).getKeys(false)) {
				 List<String> pageList = itemMap.getNodeLocation().getStringList(".pages." + pageString);
				 String textBuilder = "[\"\"";
				 for (int k = 0; k < pageList.size(); k++) {
						String formatPage = pageList.get(k);
						if (formatPage.contains("<hover type=\"text\"") || formatPage.contains("<hover type=\"open_url\"") || formatPage.contains("<hover type=\"run_command\"")) {
							HoverType type = getHoverType(formatPage);
							String result = null;
							String result2 = null;
							java.util.regex.Pattern pattern1 = java.util.regex.Pattern.compile(">\"(.*?)\"</hover>");
							Matcher matcher1 = pattern1.matcher(formatPage);
						    while (matcher1.find()) { result = matcher1.group(1); }
							
							java.util.regex.Pattern pattern2 = java.util.regex.Pattern.compile("value=\"(.*?)\">");
							Matcher matcher2 = pattern2.matcher(formatPage);
							while (matcher2.find()) { result2 = matcher2.group(1); }
							
							formatPage = formatPage.replace("<hover type=\"" + type.toString().toLowerCase() + "\" value=\"" + result2 + "\">\"" + result + "\"</hover>", "");
							
							String hoverBuilder = new String();
							String hoverString = result2.replace(" <n> ","<n>").replace("<n> ","<n>").replace(" <n>","<n>");
							String[] hovers = hoverString.split("<n>");
							for (String hover: hovers) { if (hoverString.contains("<n>")) { hoverBuilder = hoverBuilder + hover + "\n"; } else { hoverBuilder = hover; } }
							textBuilder += ", " + "{\"text\":\"" + result + "\",\"" + type.event + "\":{\"action\":\"" + type.action + "\",\"value\":\"" + hoverBuilder + "\"}}" 
							+ ", " + "{\"text\":\"" + formatPage + "\"}" + ", " +  "{\"text\":\"\\n\",\"color\":\"reset\"}";
							
							safteyCheckURL(type, hoverBuilder, itemMap);
						} else { textBuilder += ", " + "{\"text\":\"" + formatPage + "\"}" + ", " + "{\"text\":\"\\n\",\"color\":\"reset\"}"; }
				 }
				 actualPageList.add(textBuilder + "]");
			}
			itemMap.setPages(actualPageList);
		}
	}
	
	private HoverType getHoverType(String formatPage) {
		if (formatPage.contains("<hover type=\"text\"")) { return HoverType.TEXT; } 
		else if (formatPage.contains("<hover type=\"open_url\"")) { return HoverType.OPEN_URL; }
		else if (formatPage.contains("<hover type=\"run_command\"")) { return HoverType.RUN_COMMAND; }
		return HoverType.EXEMPT;
	}
	
	private void safteyCheckURL(HoverType type, String hoverBuilder, ItemMap itemMap) {
		if (type.equals(HoverType.OPEN_URL)) {
			if (!Utils.containsIgnoreCase(hoverBuilder, "https") || !Utils.containsIgnoreCase(hoverBuilder, "http")) {
				ServerHandler.sendErrorMessage("&c[ERROR] The URL Specified for the clickable link in the book " + itemMap.getConfigName() + " is missing http or https and will not be clickable.");
				ServerHandler.sendErrorMessage("&c[ERROR] A URL designed for a clickable link should look as follows; https://www.google.com/");
			}
		}
	}
	
	private enum HoverType { 
		TEXT("hoverEvent","show_text"), 
		OPEN_URL("clickEvent","open_url"), 
		RUN_COMMAND("clickEvent","run_command"), 
		EXEMPT("",""); 
		
		private final String event;
		private final String action;
		private HoverType(String Event, String Action) { event = Event; action = Action; }
	}
//  =========================================================================================================================================================================================================================== //
	
//  =============================================== //
//    ~ Sets the Custom Name to the Custom Item ~   //
//  Adds the custom name to the items display name. //
//  =============================================== //
	private String encodeName(ItemMap itemMap, String text) {
		return (text + itemMap.getLegacySecret());
	}
	
	private String getActualName(ItemMap itemMap) {
		String name = ItemHandler.purgeDelay(itemMap.getNodeLocation().getString(".name"));
		if (ConfigHandler.getNameSection(itemMap.getNodeLocation()) != null) {
			List<String> names = new ArrayList<String>();
			for (String nameKey : ConfigHandler.getNameSection(itemMap.getNodeLocation()).getKeys(false)) {
				String nameList = itemMap.getNodeLocation().getString(".name." + nameKey);
				if (nameList != null) {
					names.add(nameList);
				}
			}
			itemMap.setDynamicNames(names);
			return ItemHandler.purgeDelay(itemMap.getNodeLocation().getString(".name." + ConfigHandler.getNameSection(itemMap.getNodeLocation()).getKeys(false).iterator().next()));
		} else if (name == null || name.isEmpty()) {
			return ItemHandler.getName(itemMap.getTempItem());
		}
		return name;
	}
	
	private void setName(ItemMap itemMap) {
		String name = getActualName(itemMap);
		if (ConfigHandler.getConfig("config.yml").getBoolean("NewNBT-System") == true && ServerHandler.hasSpecificUpdate("1_8") || itemMap.isVanilla() && ServerHandler.hasSpecificUpdate("1_8")) {
			if (!Utils.containsIgnoreCase(name, ItemHandler.getName(itemMap.getTempItem()))) {
				itemMap.setCustomName(name);
			}
		} else {
			itemMap.setCustomName(encodeName(itemMap, name));
		}
	}
//  ====================================================================================================================================================================================================== //

//  ============================================= //
//  ~ Sets the Custom Lore to the Custom Item ~   //
//    Adds the custom lore to the items lore.     //
//  ============================================= //
	private List < String > getActualLore(ItemMap itemMap) {
		List <String> lore = itemMap.getNodeLocation().getStringList(".lore");
		if (ConfigHandler.getLoreSection(itemMap.getNodeLocation()) != null) {
			List<List<String>> lores = new ArrayList<List<String>>();
			for (String loreKey : ConfigHandler.getLoreSection(itemMap.getNodeLocation()).getKeys(false)) {
				List<String> loreList = itemMap.getNodeLocation().getStringList(".lore." + loreKey);
				if (loreList != null) {
					lores.add(loreList);
				}
			}
			itemMap.setDynamicLores(lores);
			return itemMap.getNodeLocation().getStringList(".lore." + ConfigHandler.getLoreSection(itemMap.getNodeLocation()).getKeys(false).iterator().next());
		}
		return lore;
	}
	
	private void setLore(ItemMap itemMap) {
		if (itemMap.getNodeLocation().getString(".lore") != null) {
			List <String> lore = getActualLore(itemMap);
			itemMap.setCustomLore(lore);
		}
	}
//  ====================================================================================================================================================================================================================================================== //
	
//  =============================================================== //
//          ~ Sets the Durability to the Custom Item ~              //
//    Changes the items durability to the specified durability.     //
//  =============================================================== //
	private void setDurability(ItemMap itemMap) {
		if (itemMap.getNodeLocation().getString(".skull-owner") != null) {
			itemMap.setDurability((short) 3);
		} else if (itemMap.getNodeLocation().getString(".durability") != null) {
			int durability = itemMap.getNodeLocation().getInt(".durability");
			itemMap.setDurability((short) durability);
		}
	}
//  ================================================================================ //
	
//  ================================================ //
//   ~ Sets the Probability of the Custom Item ~     //
//  Defines the probability percentage of the item.  //
//  ================================================ //
	private void setProbability(ItemMap itemMap) {
		if (itemMap.getNodeLocation().getString(".probability") != null) {
			String percentageString = itemMap.getNodeLocation().getString(".probability").replace("%", "").replace("-", "").replace(" ", "");
			int percentage = Integer.parseInt(percentageString);
			if (!ItemUtilities.probability.containsKey(itemMap.getConfigName())) { ItemUtilities.probability.put(itemMap.getConfigName(), percentage); }
			itemMap.setProbability(percentage);
		}
	}
//  =================================================================================================================================================== //
	
//  ================================================= //
//  ~ Sets the Skull Owner of the Custom Skull Item ~ //
//   Adds the Texture of the Skull Owner to the Item. //
//  ================================================= //
	private void setSkull(ItemMap itemMap) {
		if (itemMap.getNodeLocation().getString(".skull-owner") != null) {
			if (itemMap.getMaterial().toString().equalsIgnoreCase("SKULL_ITEM") || itemMap.getMaterial().toString().equalsIgnoreCase("PLAYER_HEAD")) {
				if (itemMap.getNodeLocation().getString(".skull-texture") != null) { ServerHandler.sendErrorMessage("&4You cannot define a skull owner and a skull texture at the same time, please remove one from the item."); return;  }
				String owner = itemMap.getNodeLocation().getString(".skull-owner");
				itemMap.setSkull(owner);
			}
		}
	}
//  ============================================================================================================================================================================================================================================ //
	
//  ============================================= //
//  ~ Sets the Skull Texture of the Custom Item ~ //
//   Adds the Custom Skull Texture to the item.   //
//  ============================================= //
    private void setSkullTexture(ItemMap itemMap) {
    	if (ServerHandler.hasSpecificUpdate("1_8") && itemMap.getNodeLocation().getString(".skull-texture") != null && !itemMap.getNodeLocation().getString(".skull-texture").contains("hdb-")) {
    		if (itemMap.getMaterial().toString().equalsIgnoreCase("SKULL_ITEM") || itemMap.getMaterial().toString().equalsIgnoreCase("PLAYER_HEAD")) {
				if (itemMap.getNodeLocation().getString(".skull-owner") != null) { ServerHandler.sendErrorMessage("&4You cannot define a skull owner and a skull texture at the same time, please remove one from the item."); return;  }
    			String texture = itemMap.getNodeLocation().getString(".skull-texture");
    			GameProfile gameProfile = new GameProfile(UUID.randomUUID(), null);
    			gameProfile.getProperties().put("textures", new Property("textures", new String(texture)));
    			try {
    				itemMap.setSkullTexture(texture);
    			} catch (Exception e) { ServerHandler.sendDebugTrace(e); }
    		}
    	}
    }
//  ============================================================================================================================================================================================================================================ //
    
//  ============================================== //
//  ~ Sets the Potion Effects of the Custom Item ~ //
//    Adds the Custom Potion Effects to the item.  //
//  ============================================== //
	private void setConsumableEffects(ItemMap itemMap) {
		if (itemMap.getNodeLocation().getString(".potion-effect") != null && itemMap.getMaterial().toString().equalsIgnoreCase("GOLDEN_APPLE")) {
			String potionList = itemMap.getNodeLocation().getString(".potion-effect").replace(" ", "");
			List < PotionEffect > potionEffectList = new ArrayList < PotionEffect > ();
			for (String potion: potionList.split(",")) {
				String[] potionSection = potion.split(":");
				PotionEffectType type = PotionEffectType.getByName(potionSection[0].toUpperCase());
				if (PotionEffectType.getByName(potionSection[0].toUpperCase()) != null) {
					try {
						int duritation = 1;
						int amplifier = 1;
						if (Utils.containsIgnoreCase(potion, ":")) {
							if (Integer.parseInt(potionSection[1]) == 1 || Integer.parseInt(potionSection[1]) == 2 || Integer.parseInt(potionSection[1]) == 3) {
								amplifier = Integer.parseInt(potionSection[1]) - 1;
							} else { amplifier = Integer.parseInt(potionSection[1]); }
						}
						duritation = Integer.parseInt(potionSection[2]) * 20;
						potionEffectList.add(new PotionEffect(type, duritation, amplifier));
					} catch (NumberFormatException e) {
						ServerHandler.sendErrorMessage("&4An error occurred in the config, &c" + potionSection[1] + "&4 is not a number and a number was expected!");
						ServerHandler.sendErrorMessage("&4Potion: " + potionSection[0] + " will now be set to level 1.");
						ServerHandler.sendDebugTrace(e);
					}
				} else {
					ServerHandler.sendErrorMessage("&4[153] An error occurred in the config, &a" + potionSection[0] + "&4 is an incorrect potion effect!");
					ServerHandler.sendErrorMessage("&4Please see: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html for a list of correct potion effects!");
				}
			}
			itemMap.setCustomConsumable(true);
			itemMap.setPotionEffect(potionEffectList);
		}
	}
//  =========================================================================================================================================================================================================================== //
    
//  ============================================== //
//  ~ Sets the Potion Effects of the Custom Item ~ //
//    Adds the Custom Potion Effects to the item.  //
//  ============================================== //
	private void setPotionEffects(ItemMap itemMap) {
		if (itemMap.getNodeLocation().getString(".potion-effect") != null) {
			if (itemMap.getMaterial().toString().equalsIgnoreCase("POTION") || itemMap.getMaterial().toString().equalsIgnoreCase("SPLASH_POTION")
				|| ServerHandler.hasCombatUpdate() && itemMap.getMaterial().toString().equalsIgnoreCase("LINGERING_POTION")) {
				String potionList = itemMap.getNodeLocation().getString(".potion-effect").replace(" ", "");
				List <PotionEffect> potionEffectList = new ArrayList<PotionEffect>();
				for (String potion: potionList.split(",")) {
					String[] potionSection = potion.split(":");
					PotionEffectType type = PotionEffectType.getByName(potionSection[0].toUpperCase());
					if (PotionEffectType.getByName(potionSection[0].toUpperCase()) != null) {
						try {
							int duritation = 1; int amplifier = 1;
							if (Utils.containsIgnoreCase(potion, ":")) {
								if (Integer.parseInt(potionSection[1]) == 1 || Integer.parseInt(potionSection[1]) == 2 || Integer.parseInt(potionSection[1]) == 3) { amplifier = Integer.parseInt(potionSection[1]) - 1; } 
								else { amplifier = Integer.parseInt(potionSection[1]); }
							}
							duritation = Integer.parseInt(potionSection[2]) * 20;
							potionEffectList.add(new PotionEffect(type, duritation, amplifier));
						} catch (NumberFormatException e) {
							ServerHandler.sendErrorMessage("&4An error occurred in the config, &c" + potionSection[1] + "&4 is not a number and a number was expected!");
							ServerHandler.sendErrorMessage("&4Potion: " + potionSection[0] + " will now be set to level 1.");
							ServerHandler.sendDebugTrace(e);
						}
					} else {
						ServerHandler.sendErrorMessage("&4[152] An error occurred in the config, &a" + potionSection[0] + "&4 is an incorrect potion effect!");
						ServerHandler.sendErrorMessage("&4Please see: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html for a list of correct potion effects!");
					}
				}
				itemMap.setPotionEffect(potionEffectList);
			}
		}
	}
//  =========================================================================================================================================================================================================================== //
    
//  ==================================================== //
//  ~ Sets the Tipped Arrow Effects of the Custom Item ~ //
//    Adds the Custom Tipped Arrow Effects to the item.  //
//  ==================================================== //
	private void setTippedArrows(ItemMap itemMap) {
		if (itemMap.getNodeLocation().getString(".potion-effect") != null) {
			if (ServerHandler.hasCombatUpdate() && !ItemJoin.getInstance().getServer().getVersion().contains("(MC: 1.9)") && itemMap.getMaterial().toString().equalsIgnoreCase("TIPPED_ARROW")) {
				String effectList = itemMap.getNodeLocation().getString(".potion-effect").replace(" ", "");
				List <PotionEffect> potionEffectList = new ArrayList<PotionEffect>();
				for (String effect: effectList.split(",")) {
					String[] tippedSection = effect.split(":");
					PotionEffectType type = PotionEffectType.getByName(tippedSection[0].toUpperCase());
					if (PotionEffectType.getByName(tippedSection[0].toUpperCase()) != null) {
						try {
							int level = 1; int duration;
							if (Utils.containsIgnoreCase(effect, ":")) {
								if (Integer.parseInt(tippedSection[1]) == 1 || Integer.parseInt(tippedSection[1]) == 2 || Integer.parseInt(tippedSection[1]) == 3) { level = Integer.parseInt(tippedSection[1]) - 1; } 
								else { level = Integer.parseInt(tippedSection[1]); }
							}
							duration = Integer.parseInt(tippedSection[2]);
							potionEffectList.add(new PotionEffect(type, duration * 160, level));
						} catch (NumberFormatException e) {
							ServerHandler.sendErrorMessage("&4An error occurred in the config, &c" + tippedSection[1] + "&4 is not a number and a number was expected!");
							ServerHandler.sendErrorMessage("&4Effect: " + tippedSection[0] + " will now be set to level 1.");
							ServerHandler.sendDebugTrace(e);
						}
					} else {
						ServerHandler.sendErrorMessage("&4[151] An error occurred in the config, &a" + tippedSection[0] + "&4 is an incorrect potion effect!");
						ServerHandler.sendErrorMessage("&4Please see: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html for a list of correct potion effects!");
					}
				}
				itemMap.setPotionEffect(potionEffectList);
			}
		}
	}
//  =========================================================================================================================================================================================================================== //
    
//  =============================================== //
//  ~ Sets the Banner Patterns of the Custom Item ~ //
//    Adds the Custom Banner Patterns to the item.  //
//  =============================================== //
	private void setBanners(ItemMap itemMap) {
		if (itemMap.getNodeLocation().getString(".banner-meta") != null && ServerHandler.hasSpecificUpdate("1_8") && Utils.containsIgnoreCase(itemMap.getMaterial().toString(), "BANNER")) {
			String bannerList = itemMap.getNodeLocation().getString(".banner-meta").replace(" ", "");
			List <Pattern> patterns = new ArrayList <Pattern> ();
			for (String banner: bannerList.split(",")) {
				String[] bannerSection = banner.split(":");
				DyeColor Color = DyeColor.valueOf(bannerSection[0].toUpperCase());
				PatternType Pattern = PatternType.valueOf(bannerSection[1].toUpperCase());
				if (Color != null && Pattern != null) {
					patterns.add(new Pattern(Color, Pattern));
				} else if (Color == null) {
					ServerHandler.sendErrorMessage("&4An error occurred in the config, &a" + bannerSection[0] + "&4 is an incorrect dye color!");
					ServerHandler.sendErrorMessage("&4Please see: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/DyeColor.html for a list of correct dye colors!");
				} else if (Pattern == null) {
					ServerHandler.sendErrorMessage("&4An error occurred in the config, &a" + bannerSection[1] + "&4 is an incorrect pattern type!");
					ServerHandler.sendErrorMessage("&4Please see: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/banner/PatternType.html for a list of correct pattern types!");
				}
			}
			itemMap.setBannerPatterns(patterns);
		}
	}
//  ===================================================================================================================================================================================================== //
    
//  ================================================ //
//  ~ Sets the Firework Effects of the Custom Item ~ //
//    Adds the Custom Firework Effects to the item.  //
//  ================================================ //
	private void setFireworks(ItemMap itemMap) {
		if (itemMap.getNodeLocation().getString(".firework.type") != null) {
			if (itemMap.getMaterial().toString().equalsIgnoreCase("FIREWORK") || itemMap.getMaterial().toString().equalsIgnoreCase("FIREWORK_ROCKET")) {
				String stringType = itemMap.getNodeLocation().getString(".firework.type").toUpperCase();
				boolean flicker = itemMap.getNodeLocation().getBoolean(".firework.flicker");
				boolean trail = itemMap.getNodeLocation().getBoolean(".firework.trail");
				int power = itemMap.getNodeLocation().getInt(".firework.distance");
				Type buildType = Type.valueOf(stringType);
				List <Color> colors = new ArrayList <Color> ();
				if (itemMap.getNodeLocation().getString(".firework.colors") != null) {
					String colorlist = itemMap.getNodeLocation().getString(".firework.colors").replace(" ", "");
					for (String color: colorlist.split(",")) {
						try { colors.add(DyeColor.valueOf(color.toUpperCase()).getFireworkColor()); } 
						catch (Exception e) {
							ServerHandler.sendErrorMessage("&4The item " + itemMap.getConfigName() + " has the incorrect dye color " + color.toUpperCase() + " and does not exist!");
							ServerHandler.sendErrorMessage("&4Please see: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/DyeColor.html for a list of correct dye color names!");
						}
					}
				}
				FireworkEffect effect = FireworkEffect.builder().trail(trail).flicker(flicker).withColor(colors).withFade(colors).with(buildType).build();
				itemMap.setFirework(effect, power);
			}
		}
	}
//  ======================================================================================================================================================================================= //
    
//  ===================================================== //
//  ~ Sets the Firework Charge Color of the Custom Item ~ //
//    Adds the Custom Firework Charge Color to the item.  //
//  ===================================================== //
	private void setFireChargeColor(ItemMap itemMap) {
		if (itemMap.getNodeLocation().getString(".charge-color") != null) {
			if (Utils.containsIgnoreCase(itemMap.getMaterial().toString(), "CHARGE") || Utils.containsIgnoreCase(itemMap.getMaterial().toString(), "STAR")) {
				String color = itemMap.getNodeLocation().getString(".charge-color").toUpperCase();
				itemMap.setChargeColor(DyeColor.valueOf(color));
			}
		}
	}
//  ======================================================================================================================================================================== //
    
//  ========================================= //
//  ~ Sets the Dye Color of the Custom Item ~ //
//  Changes the Custom Dye Color of the item. //
//  ========================================= //
	private void setDye(ItemMap itemMap) {
		if (itemMap.getNodeLocation().getString(".leather-color") != null) {
			if (itemMap.getMaterial().toString().equalsIgnoreCase("LEATHER_HELMET") || itemMap.getMaterial().toString().equalsIgnoreCase("LEATHER_CHESTPLATE")
				|| itemMap.getMaterial().toString().equalsIgnoreCase("LEATHER_LEGGINGS") || itemMap.getMaterial().toString().equalsIgnoreCase("LEATHER_BOOTS")) {
				String leatherColor = itemMap.getNodeLocation().getString(".leather-color").toUpperCase();
				if (ItemHandler.getColorFromHexColor(leatherColor.replaceAll("#", "")) != null) {
					itemMap.setLeatherColor(ItemHandler.getColorFromHexColor(leatherColor.replaceAll("#", "")));
				} else {
					itemMap.setLeatherColor(DyeColor.valueOf(leatherColor).getFireworkColor());
				}
			}
		}
	}
//  =========================================================================================================================== //
    
//  =========================================== //
//  ~ Sets the Author of the Custom Book Item ~ //
//      Defines the author of the book item.    //
//  =========================================== //
	private void setBookAuthor(ItemMap itemMap) {
		if (itemMap.getMaterial().toString().equalsIgnoreCase("WRITTEN_BOOK")) {
			if (itemMap.getNodeLocation().getString(".author") != null) {
				itemMap.setAuthor(itemMap.getNodeLocation().getString(".author"));
			} else {
				itemMap.setAuthor("&f");
			}
		}
	}
//  ================================================================================= //
	
//  =========================================== //
//   ~ Sets the Title of the Custom Book Item ~ //
//   Defines the custom title of the book item. //
//  =========================================== //
	private void setBookTitle(ItemMap itemMap) {
		if (itemMap.getMaterial().toString().equalsIgnoreCase("WRITTEN_BOOK")) {
			if (itemMap.getNodeLocation().getString(".title") != null) {
				itemMap.setTitle(itemMap.getNodeLocation().getString(".title"));
			} else {
				itemMap.setTitle("&f");
			}
		}
	}
//  ============================================================================== //
	
//  =============================================== //
//  ~ Sets the Generation of the Custom Book Item ~ //
//  Defines the custom generation of the book item. //
//  =============================================== //
	private void setBookGeneration(ItemMap itemMap) {
		if (ServerHandler.hasSpecificUpdate("1_10") && itemMap.getMaterial().toString().equalsIgnoreCase("WRITTEN_BOOK")) {
			if (itemMap.getNodeLocation().getString(".generation") != null) {
				itemMap.setGeneration(Generation.valueOf(itemMap.getNodeLocation().getString(".generation")));
			} else {
				itemMap.setGeneration(Generation.ORIGINAL);
			}
		}
	}
//  ========================================================================================================================== //
	
//  =================================================================== //
//        ~ Sets the Legacy Book Pages of the Custom Book Item ~        //
//  Adds the custom book pages to the item without any JSON Formatting. //
//  =================================================================== //
	private void setLegacyBookPages(ItemMap itemMap) {
		if (!ServerHandler.hasSpecificUpdate("1_8") && itemMap.getMaterial().toString().equalsIgnoreCase("WRITTEN_BOOK") && itemMap.getNodeLocation().getString(".pages") != null && ConfigHandler.getPagesSection(itemMap.getNodeLocation()) != null) {
			List < String > pages = new ArrayList < String > ();
			for (String pageString: ConfigHandler.getPagesSection(itemMap.getNodeLocation()).getKeys(false)) {
				List < String > pageList = itemMap.getNodeLocation().getStringList(".pages." + pageString);
				String saveList = "";
				for (int k = 0; k < pageList.size(); k++) {
					String formatPage = pageList.get(k);
					if (formatPage.contains("<hover type=\"text\"") || formatPage.contains("<hover type=\"open_url\"") || formatPage.contains("<hover type=\"run_command\"")) {
						String result = "%failure%";
						String result2 = "%failure%";
						java.util.regex.Pattern pattern1 = java.util.regex.Pattern.compile(">\"(.*?)\"</hover>");
						Matcher matcher1 = pattern1.matcher(formatPage);
						while (matcher1.find()) { result = matcher1.group(1); }
						java.util.regex.Pattern pattern2 = java.util.regex.Pattern.compile("value=\"(.*?)\">");
						Matcher matcher2 = pattern2.matcher(formatPage);
						while (matcher2.find()) { result2 = matcher2.group(1); }
						formatPage = formatPage.replace("<hover type=\"text\" value=\"" + result2 + "\">\"" + result + "\"</hover>", result).replace("<hover type=\"open_url\" value=\"" + result2 + "\">\"" + result + "\"</hover>", result).replace("<hover type=\"run_command\" value=\"" + result2 + "\">\"" + result + "\"</hover>", result);
					}
					saveList = saveList + formatPage + "\n";
				}
				pages.add(saveList);
			}
			itemMap.setPages(pages);
		}
	}
//  =========================================================================================================================================================================================================================================================================================================================================================== //
	
//  =============================================== //
//  ~ Sets the Attributes of the Custom Book Item ~ //
//     Shows or Hides the Attributes of the item.   //
//  =============================================== //
	private void setAttributes(ItemMap itemMap) {
		if (ServerHandler.hasSpecificUpdate("1_8") && Utils.containsIgnoreCase(itemMap.getItemFlags(), "hide-attributes")) {
			itemMap.setAttributes(true);
		}
	}
//  ===================================================================================================================================== //
}