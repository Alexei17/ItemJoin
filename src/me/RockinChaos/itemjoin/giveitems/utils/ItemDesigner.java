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
package me.RockinChaos.itemjoin.giveitems.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.map.MapView;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import me.RockinChaos.itemjoin.ItemJoin;
import me.RockinChaos.itemjoin.handlers.ConfigHandler;
import me.RockinChaos.itemjoin.handlers.ItemHandler;
import me.RockinChaos.itemjoin.handlers.ServerHandler;
import me.RockinChaos.itemjoin.utils.Legacy;
import me.RockinChaos.itemjoin.utils.Reflection;
import me.RockinChaos.itemjoin.utils.Chances;
import me.RockinChaos.itemjoin.utils.ImageMap;
import me.RockinChaos.itemjoin.utils.Utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;

public class ItemDesigner {

   /**
	* Creates a new ItemDesigner instance.
	* 
	*/
	public ItemDesigner() {
		if (ConfigHandler.isConfigurable()) {
			ItemHandler.initializeItemID();
			for (String internalName: ConfigHandler.getConfigurationSection().getKeys(false)) {
				ConfigurationSection itemNode = ConfigHandler.getItemSection(internalName);
				if (isConfigurable(internalName, itemNode)) {
					String[] slots = itemNode.getString(".slot").replace(" ", "").split(",");
					for (String slot: slots) {
						if (isDefinable(internalName, slot)) {
							ItemMap itemMap = new ItemMap(internalName, slot);
							
							this.setMaterial(itemMap);
							this.setSkullDatabase(itemMap);
							this.setUnbreaking(itemMap);
							this.durabilityBar(itemMap);
							this.setEnchantments(itemMap);
							this.setMapImage(itemMap);
							this.setJSONBookPages(itemMap);
							this.setNBTData(itemMap);
							this.setName(itemMap);
							this.setLore(itemMap);
							this.setDurability(itemMap);
							this.setData(itemMap);
							this.setModelData(itemMap);
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
					    	ConfigHandler.setListenerRestrictions(itemMap);
						}
					}
				}
			}
			ItemUtilities.updateItems();
		}
	}
	
//  =========================================================================================================================== //
//      Determines if the specific item node has a valid Material ID defined as well as if the item node has a slot defined.    //
//  =========================================================================================================================== //
	private boolean isConfigurable(String internalName, ConfigurationSection itemNode) {
		String id = ItemHandler.getMaterialPath(itemNode);
		String dataValue = null;
		if (id != null) {
			if (id.contains(":")) {
				String[] parts = id.split(":"); id = parts[0]; dataValue = parts[1];
				if (ServerHandler.hasSpecificUpdate("1_13")) {
					ServerHandler.logWarn("{ItemMap} The item " + internalName + " is using a Legacy Material which is no longer supported as of Minecraft 1.13.");
					ServerHandler.logWarn("{ItemMap} This will cause issues, please see: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html for a list of material names.");
				}
			}
			if (!ServerHandler.hasSpecificUpdate("1_9") && id.equalsIgnoreCase("TIPPED_ARROW") || id.equalsIgnoreCase("440") || id.equalsIgnoreCase("0440")) {
				ServerHandler.logWarn("{ItemMap} Your server is running MC " + Reflection.getServerVersion() + " and this version of Minecraft does not have the item TIPPED_ARROW.");
				ServerHandler.logWarn("{ItemMap} You are receiving this notice because the item(s) exists in your items.yml and will not be set, please remove the item(s) or update your server.");
				return false;
			} else if (!ServerHandler.hasSpecificUpdate("1_9") && id.equalsIgnoreCase("LINGERING_POTION") || id.equalsIgnoreCase("441") || id.equalsIgnoreCase("0441")) {
				ServerHandler.logWarn("{ItemMap} Your server is running MC " + Reflection.getServerVersion() + " and this version of Minecraft does not have the item LINGERING_POTION.");
				ServerHandler.logWarn("{ItemMap} You are receiving this notice because the item(s) exists in your items.yml and will not be set, please remove the item(s) or update your server.");
				return false;
			} else if (ItemHandler.getMaterial(id, dataValue) == null) {
				ServerHandler.logSevere("{ItemMap} The Item " + internalName + "'s Material 'ID' is invalid or does not exist.");
				ServerHandler.logWarn("{ItemMap} The Item " + internalName + " will not be set!");
				if (Utils.isInt(id)) {
					ServerHandler.logSevere("{ItemMap} If you are using a numerical id and a numerical dataValue.");
					ServerHandler.logSevere("{ItemMap} Include quotations or apostrophes at the beginning and the end or this error will persist, the id should look like '160:15' or \"160:15\".");
				}
				return false;
			} else if (itemNode.getString(".slot") == null) {
				ServerHandler.logSevere("{ItemMap} The Item " + internalName + "'s SLOT is invalid.");
				ServerHandler.logWarn("{ItemMap} Please refresh your items.yml and fix the undefined slot.");
				return false;
			}
		} else { 
			ServerHandler.logSevere("{ItemMap} The Item" + internalName + " does not have a Material ID defined."); 
			ServerHandler.logWarn("{ItemMap} The Item " + internalName + " will not be set!"); 
			return false;
		}
		return true;
	}
//  =================================================================================================================================================================================================================== //


//  =========================================================================================================================== //
//    Determines if the specific item node has an actual ItemJoin definable slot, being a custom slot or a true integer slot.   //
//  =========================================================================================================================== //
	private boolean isDefinable(String internalName, String slot) {
		if (!Utils.isInt(slot) && !ItemHandler.isCustomSlot(slot)) {
			ServerHandler.logSevere("{ItemMap} The Item " + internalName + "'s slot is invalid or does not exist.");
			ServerHandler.logWarn("{ItemMap} The Item " + internalName + " will not be set!");
			return false;
		} else if (Utils.isInt(slot)) {
			int parseSlot = Integer.parseInt(slot);
			if (!(parseSlot >= 0 && parseSlot <= 35)) {
				ServerHandler.logSevere("{ItemMap} The Item " + internalName + "'s slot must be between 0 and 35.");
				ServerHandler.logWarn("{ItemMap} The Item " + internalName + " will not be set!");
				return false;
			}
		} else if (!ServerHandler.hasSpecificUpdate("1_9") && slot.equalsIgnoreCase("Offhand")) {
			ServerHandler.logWarn("{ItemMap} Your server is running MC " + Reflection.getServerVersion() + " and this version of Minecraft does not have OFFHAND support!");
			return false;
		}
		return true;
	}
//  ============================================================================================================================================================================ //
//  ===================================================================================================================================================================================================================== //

	
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
			return ItemHandler.getMaterial(material, null);
		}
		if (material.contains(":")) { String[] parts = material.split(":"); itemMap.setDataValue((short) Integer.parseInt(parts[1])); }
		return ItemHandler.getMaterial(material, null);
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
	private String getActualTexture(ItemMap itemMap) {
		String texture = ItemHandler.purgeDelay(itemMap.getNodeLocation().getString(".skull-texture"));
		if (ConfigHandler.getTextureSection(itemMap.getNodeLocation()) != null) {
			List<String> textures = new ArrayList<String>();
			for (String textureKey : ConfigHandler.getTextureSection(itemMap.getNodeLocation()).getKeys(false)) {
				String textureList = itemMap.getNodeLocation().getString(".skull-texture." + textureKey);
				if (textureList != null) {
					textures.add(textureList);
				}
			}
			itemMap.setDynamicTextures(textures);
			return ItemHandler.purgeDelay(itemMap.getNodeLocation().getString(".skull-texture." + ConfigHandler.getTextureSection(itemMap.getNodeLocation()).getKeys(false).iterator().next()));
		}
		if (texture != null && !texture.isEmpty()) {
			if (itemMap.isDynamic() || itemMap.isAnimated()) {
				List<String> textures = new ArrayList<String>(); textures.add(texture);
				itemMap.setDynamicTextures(textures);
			}
		}
		return texture;
	}
	
	private void setSkullDatabase(ItemMap itemMap) {
		if (ConfigHandler.getDepends().databaseEnabled() && itemMap.getNodeLocation().getString(".skull-texture") != null) {
			if (itemMap.getMaterial().toString().equalsIgnoreCase("SKULL_ITEM") || itemMap.getMaterial().toString().equalsIgnoreCase("PLAYER_HEAD")) {
				if (itemMap.getNodeLocation().getString(".skull-owner") != null) {  ServerHandler.logWarn("{ItemMap} You cannot define a skull owner and a skull texture at the same time, remove one from the item."); return;  }
				String skullTexture = getActualTexture(itemMap);
				if (skullTexture.contains("hdb-")) {
					try {
						itemMap.setSkullTexture(skullTexture.replace("hdb-", ""));
						itemMap.setHeadDatabase(true);
					} catch (NullPointerException e) {
						ServerHandler.logSevere("{ItemMap} HeadDatabaseAPI could not find #" + skullTexture + ", this head does not exist.");
						ServerHandler.sendDebugTrace(e);
					}
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

	private void durabilityBar(ItemMap itemMap) {
		if (Utils.containsIgnoreCase(itemMap.getItemFlags(), "hide-durability")) {
			try {
				itemMap.setDurabilityBar(true);
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
						ServerHandler.logSevere("{ItemMap} An error occurred in the config, " + parts[1] + " is not a number and a number was expected!");
						ServerHandler.logWarn("{ItemMap} Enchantment: " + parts[0] + " will now be enchanted by level 1.");
						ServerHandler.sendDebugTrace(e);
					}
				}
				if (enchantName != null) {
					listEnchants.put(name, level);
				} else if (enchantName == null && ConfigHandler.getDepends().tokenEnchantEnabled() && TokenEnchantAPI.getInstance().getEnchant(name) != null) {
					listEnchants.put(name, level);
				} else if (enchantName == null && !ConfigHandler.getDepends().tokenEnchantEnabled()) {
					ServerHandler.logSevere("{ItemMap} An error occurred in the config, " + name + " is not a proper enchant name!");
					ServerHandler.logWarn("{ItemMap} Please see: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/enchantments/Enchantment.html for a list of correct enchantment names.");
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
			if (itemMap.getMapImage().equalsIgnoreCase("default.jpg") || new File(ItemJoin.getInstance().getDataFolder(), itemMap.getMapImage()).exists()) {
				if (ConfigHandler.getSQLData().imageNumberExists(itemMap.getMapImage())) {
					int mapID = ConfigHandler.getSQLData().getImageNumber(itemMap.getMapImage());
					ImageMap imgPlatform = new ImageMap(itemMap.getMapImage(), mapID);
					MapView view = imgPlatform.FetchExistingView(mapID);
					itemMap.setMapID(mapID);
					itemMap.setMapView(view);
					try { view.removeRenderer(view.getRenderers().get(0)); } catch (NullPointerException e) { ServerHandler.sendDebugTrace(e); }
					try { view.addRenderer(imgPlatform); } catch (NullPointerException e) { ServerHandler.sendDebugTrace(e); }
				} else {
					MapView view = Legacy.createLegacyMapView();
					try { view.removeRenderer(view.getRenderers().get(0)); } catch (NullPointerException e) { ServerHandler.sendDebugTrace(e); }
					int mapID = Legacy.getMapID(view);
					ImageMap imgPlatform = new ImageMap(itemMap.getMapImage(), mapID);
					itemMap.setMapID(mapID);
					itemMap.setMapView(view);
					try { view.addRenderer(imgPlatform); } catch (NullPointerException e) { ServerHandler.sendDebugTrace(e); }
					ConfigHandler.getSQLData().saveMapImage(itemMap);
				}
			}
		}
	}
//  =========================================================================================================================================================================================================================== //
	
//  =============================================== //
//  ~ Sets the NBTData to the Custom Item ~     //
//  This designs the item to be unique to ItemJoin. //
//  =============================================== //
	private void setNBTData(ItemMap itemMap) {
		if (ConfigHandler.dataTagsEnabled() && !itemMap.isVanilla() && !itemMap.isVanillaControl() && !itemMap.isVanillaStatus()) {
			try {
				Object tag = Reflection.getMinecraftClass("NBTTagCompound").getConstructor().newInstance();
				tag.getClass().getMethod("setString", String.class, String.class).invoke(tag, "ItemJoin Name", itemMap.getConfigName());
				tag.getClass().getMethod("setString", String.class, String.class).invoke(tag, "ItemJoin Slot", itemMap.getItemValue());
				itemMap.setNewNBTData(itemMap.getConfigName() + " " + itemMap.getItemValue(), tag);
			} catch (Exception e) {
				ServerHandler.logSevere("{ItemMap} An error has occured when setting NBTData to an item.");
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
		if (itemMap.getMaterial().toString().equalsIgnoreCase("WRITTEN_BOOK") && itemMap.getNodeLocation().getString(".pages") != null && ConfigHandler.getPagesSection(itemMap.getNodeLocation()) != null && ServerHandler.hasSpecificUpdate("1_8")) {
			List < String > JSONPages = new ArrayList < String > ();
			List < List < String > > rawPages = new ArrayList < List < String > > ();
			for (String pageString: ConfigHandler.getPagesSection(itemMap.getNodeLocation()).getKeys(false)) {
				List < String > pageList = itemMap.getNodeLocation().getStringList(".pages." + pageString);
				rawPages.add(pageList);
				String textBuilder = "[\"\"";
				for (int k = 0; k < pageList.size(); k++) {
					Map < Integer, String > JSONBuilder = new HashMap < Integer, String > ();
					String formatLine = pageList.get(k);
					if (this.containsJSONEvent(formatLine)) {
						while (this.containsJSONEvent(formatLine)) {
							for (JSONEvent jsonType: JSONEvent.values()) {
								Matcher matchPattern = java.util.regex.Pattern.compile(jsonType.matchType + "(.*?)>").matcher(formatLine);
								if (matchPattern.find()) {
									String inputResult = matchPattern.group(1);
									JSONBuilder.put(JSONBuilder.size(), ((jsonType != JSONEvent.TEXT) 
										? (",\"" + jsonType.event + "\":{\"action\":\"" + jsonType.action + "\",\"value\":\"" + inputResult + "\"}") 
										: ("," + "{\"" + jsonType.action + "\":\"" + inputResult + "\"")));
									formatLine = formatLine.replace(jsonType.matchType + inputResult + ">", "<JSONEvent>");
									this.safteyCheckURL(itemMap, jsonType, inputResult);
								}
							}
						}
						if (!formatLine.isEmpty() && formatLine.length() != 0 && !formatLine.trim().isEmpty()) {
							boolean definingText = false;
							String[] JSONEvents = formatLine.split("<JSONEvent>");
							if (!(StringUtils.countMatches(formatLine,"<JSONEvent>") <= JSONEvents.length)) { 
								String adjustLine = new String(); 
								for (String s : formatLine.split("JSONEvent>"))  { adjustLine += s + "JSONEvent> "; } 
								JSONEvents = adjustLine.split("<JSONEvent>"); 
							}
							for (int i = 0; i < JSONEvents.length; i++) {
								if (!JSONEvents[i].isEmpty() && JSONEvents[i].length() != 0 && !JSONEvents[i].trim().isEmpty()) {
									textBuilder += ((i == 0) ? "," : "},") + "{\"" + "text" + "\":\"" + JSONEvents[i] + ((JSONBuilder.get(i) != null && JSONBuilder.get(i).contains("\"text\"")) 
										? "\"}" : "\"") + (JSONBuilder.get(i) != null ? JSONBuilder.get(i) : "");
								} else if (JSONBuilder.get(i) != null) {
									if (JSONBuilder.get(i).contains("\"text\"") && !definingText) {
										textBuilder += JSONBuilder.get(i); definingText = true;
									} else if (JSONBuilder.get(i).contains("\"text\"") && definingText) {
										textBuilder += "}" + JSONBuilder.get(i); definingText = false;
									} else {
									textBuilder += JSONBuilder.get(i);
									}
								}
							}
							textBuilder += "}," + "{\"text\":\"\\n\",\"color\":\"reset\"}";
						}
					} else if (formatLine.contains("raw:")) {
						textBuilder += formatLine.replace("raw: ", "").replace("raw:", "").replace("[\"\"", "").replace("\"bold\":false}]", "\"bold\":false}").replace("\"bold\":true}]", "\"bold\":true}") + "," + "{\"text\":\"\\n\",\"color\":\"reset\"}";
					} else { textBuilder += "," + "{\"text\":\"" + formatLine + "\"}" + "," + "{\"text\":\"\\n\",\"color\":\"reset\"}"; }
				}
				JSONPages.add(textBuilder + "]");
			}
			itemMap.setPages(JSONPages);
			itemMap.setListPages(rawPages);
		}
	}
	
	private boolean containsJSONEvent(String formatPage) {
		if (formatPage.contains(JSONEvent.TEXT.matchType) || formatPage.contains(JSONEvent.SHOW_TEXT.matchType) || formatPage.contains(JSONEvent.OPEN_URL.matchType) || formatPage.contains(JSONEvent.RUN_COMMAND.matchType)) {
			return true;
		}
		return false;
	}
	
	private void safteyCheckURL(ItemMap itemMap, JSONEvent type, String inputResult) {
		if (type.equals(JSONEvent.OPEN_URL)) {
			if (!Utils.containsIgnoreCase(inputResult, "https") && !Utils.containsIgnoreCase(inputResult, "http")) {
				ServerHandler.logSevere("{ItemMap} The URL Specified for the clickable link in the book " + itemMap.getConfigName() + " is missing http or https and will not be clickable.");
				ServerHandler.logWarn("{ItemMap} A URL designed for a clickable link should resemble this link structure: https://www.google.com/");
			}
		}
	}
	
	private enum JSONEvent {
		TEXT("nullEvent", "text", "<text:"),
		SHOW_TEXT("hoverEvent", "show_text", "<show_text:"),
		OPEN_URL("clickEvent", "open_url", "<open_url:"),
		RUN_COMMAND("clickEvent", "run_command", "<run_command:"),
		CHANGE_PAGE("clickEvent", "change_page", "<change_page:");
		private final String event;
		private final String action;
		private final String matchType;
		private JSONEvent(String Event, String Action, String MatchType) {
			this.event = Event;
			this.action = Action;
			this.matchType = MatchType;
		}
	}
//  =========================================================================================================================================================================================================================== //
	
//  =============================================== //1
//    ~ Sets the Custom Name to the Custom Item ~   //
//  Adds the custom name to the items display name. //
//  =============================================== //
	private String encodeName(ItemMap itemMap, String text) {
		return ("&f" + text + itemMap.getLegacySecret());
	}
	
	private String getActualName(ItemMap itemMap) {
		String name = itemMap.getNodeLocation().getString(".name");
		try { ItemHandler.purgeDelay(itemMap.getNodeLocation().getString(".name")); } catch (Exception e) { }
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
		if (name != null && !name.isEmpty()) {
			if (itemMap.isDynamic() || itemMap.isAnimated()) {
				List<String> names = new ArrayList<String>(); names.add(name);
				itemMap.setDynamicNames(names);
			}
		}
		return ItemHandler.purgeDelay(name);
	}
	
	private void setName(ItemMap itemMap) {
		String name = getActualName(itemMap);
		if (ConfigHandler.dataTagsEnabled() && ServerHandler.hasSpecificUpdate("1_8") || itemMap.isVanilla() && ServerHandler.hasSpecificUpdate("1_8")) {
			itemMap.setCustomName(name);
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
		if (lore != null && !lore.isEmpty()) {
			if (itemMap.isDynamic() || itemMap.isAnimated()) {
				List<List<String>> lores = new ArrayList<List<String>>(); lores.add(lore);
				itemMap.setDynamicLores(lores);
			}
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
		if (itemMap.getNodeLocation().getString(".data") == null || itemMap.getNodeLocation().getInt(".data") == 0) {
			if (itemMap.getNodeLocation().getString(".skull-owner") != null) {
				itemMap.setDurability((short) 3);
			} else if (itemMap.getNodeLocation().getString(".durability") != null) {
				int durability = itemMap.getNodeLocation().getInt(".durability");
				itemMap.setDurability((short) durability);
			}
		}
	}
	
	private void setData(ItemMap itemMap) {
		if (itemMap.getNodeLocation().getString(".data") != null) {
			itemMap.setData(itemMap.getNodeLocation().getInt(".data"));
			itemMap.setAttributesInfo(true);
			itemMap.setUnbreakable(true);
		}
	}
//  ================================================================================================================== //
	
//  ========================================================================================== //
//                         ~ Sets the Model Data for the Custom Item ~                         //
//    Adds an NBTTag to the item containing the numerical value for the Custom Model Data.     //
//  ========================================================================================== //
	
	private void setModelData(ItemMap itemMap) {
		if (ServerHandler.hasSpecificUpdate("1_14") && itemMap.getNodeLocation().getString(".model-data") != null) {
			itemMap.setModelData(itemMap.getNodeLocation().getInt(".model-data"));
		}
	}
	
//  ================================================================================================================ //
	
//  ================================================ //
//   ~ Sets the Probability of the Custom Item ~     //
//  Defines the probability percentage of the item.  //
//  ================================================ //
	private void setProbability(ItemMap itemMap) {
		if (itemMap.getNodeLocation().getString(".probability") != null) {
			String percentageString = itemMap.getNodeLocation().getString(".probability").replace("%", "").replace("-", "").replace(" ", "");
			int percentage = Integer.parseInt(percentageString);
			if (!Chances.probabilityItems.containsKey(itemMap)) { Chances.probabilityItems.put(itemMap, percentage); }
			itemMap.setProbability(percentage);
		}
	}
//  =================================================================================================================================================== //
	
//  ================================================= //
//  ~ Sets the Skull Owner of the Custom Skull Item ~ //
//   Adds the Texture of the Skull Owner to the Item. //
//  ================================================= //
	private String getActualOwner(ItemMap itemMap) {
		String owner = ItemHandler.purgeDelay(itemMap.getNodeLocation().getString(".skull-owner"));
		if (ConfigHandler.getOwnerSection(itemMap.getNodeLocation()) != null) {
			List<String> owners = new ArrayList<String>();
			for (String ownerKey : ConfigHandler.getOwnerSection(itemMap.getNodeLocation()).getKeys(false)) {
				String ownerList = itemMap.getNodeLocation().getString(".skull-owner." + ownerKey);
				if (ownerList != null) {
					owners.add(ownerList);
				}
			}
			itemMap.setDynamicOwners(owners);
			return ItemHandler.purgeDelay(itemMap.getNodeLocation().getString(".skull-owner." + ConfigHandler.getOwnerSection(itemMap.getNodeLocation()).getKeys(false).iterator().next()));
		}
		if (owner != null && !owner.isEmpty()) {
			if (itemMap.isDynamic() || itemMap.isAnimated()) {
				List<String> owners = new ArrayList<String>(); owners.add(owner);
				itemMap.setDynamicOwners(owners);
			}
		}
		return owner;
	}
	
	private void setSkull(ItemMap itemMap) {
		if (itemMap.getNodeLocation().getString(".skull-owner") != null) {
			if (itemMap.getMaterial().toString().equalsIgnoreCase("SKULL_ITEM") || itemMap.getMaterial().toString().equalsIgnoreCase("PLAYER_HEAD")) {
				if (itemMap.getNodeLocation().getString(".skull-texture") != null) { ServerHandler.logWarn("{ItemMap} You cannot define a skull owner and a skull texture at the same time, remove one from the item."); return;  }
				String owner = getActualOwner(itemMap);
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
    	if (ServerHandler.hasSpecificUpdate("1_8") && itemMap.getNodeLocation().getString(".skull-texture") != null) {
    		if (itemMap.getMaterial().toString().equalsIgnoreCase("SKULL_ITEM") || itemMap.getMaterial().toString().equalsIgnoreCase("PLAYER_HEAD")) {
				if (itemMap.getNodeLocation().getString(".skull-owner") != null) { ServerHandler.logWarn("{ItemMap} You cannot define a skull owner and a skull texture at the same time, remove one from the item."); return;  }
    			String texture = getActualTexture(itemMap);
    			if (!texture.contains("hdb-")) {
    				GameProfile gameProfile = new GameProfile(UUID.randomUUID(), null);
    				gameProfile.getProperties().put("textures", new Property("textures", new String(texture)));
    				try {
    					itemMap.setSkullTexture(texture);
    				} catch (Exception e) { ServerHandler.sendDebugTrace(e); }
    			}
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
						ServerHandler.logSevere("{ItemMap} An error occurred in the config, " + potionSection[1] + " is not a number and a number was expected.");
						ServerHandler.logWarn("{ItemMap} Consumable Potion: " + potionSection[0] + " will now be set to level 1.");
						ServerHandler.sendDebugTrace(e);
					}
				} else {
					ServerHandler.logSevere("{ItemMap} An error occurred in the config, " + potionSection[0] + " is an incorrect potion effect for the consumable.");
					ServerHandler.logWarn("{ItemMap} Please see: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html for a list of correct potion effects.");
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
				|| ServerHandler.hasSpecificUpdate("1_9") && itemMap.getMaterial().toString().equalsIgnoreCase("LINGERING_POTION")) {
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
							ServerHandler.logSevere("{ItemMap} An error occurred in the config, " + potionSection[1] + " is not a number and a number was expected.");
							ServerHandler.logWarn("{ItemMap} Custom Potion: " + potionSection[0] + " will now be set to level 1.");
							ServerHandler.sendDebugTrace(e);
						}
					} else {
						ServerHandler.logSevere("{ItemMap} An error occurred in the config, " + potionSection[0] + " is an incorrect potion effect for the custom potion.");
						ServerHandler.logWarn("{ItemMap} Please see: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html for a list of correct potion effects.");
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
			if (ServerHandler.hasSpecificUpdate("1_9") && !ItemJoin.getInstance().getServer().getVersion().contains("(MC: 1.9)") && itemMap.getMaterial().toString().equalsIgnoreCase("TIPPED_ARROW")) {
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
							ServerHandler.logSevere("{ItemMap} An error occurred in the config, " + tippedSection[1] + " is not a number and a number was expected.");
							ServerHandler.logWarn("{ItemMap} Tipped Effect: " + tippedSection[0] + " will now be set to level 1.");
							ServerHandler.sendDebugTrace(e);
						}
					} else {
						ServerHandler.logSevere("{ItemMap} An error occurred in the config, " + tippedSection[0] + " is an incorrect potion effect for the tipped arrow.");
						ServerHandler.logWarn("{ItemMap} Please see: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html for a list of correct potion effects.");
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
					ServerHandler.logSevere("{ItemMap} An error occurred in the config, " + bannerSection[0] + " is an incorrect dye color.");
					ServerHandler.logWarn("{ItemMap} Please see: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/DyeColor.html for a list of correct dye colors.");
				} else if (Pattern == null) {
					ServerHandler.logSevere("{ItemMap} An error occurred in the config, " + bannerSection[1] + " is an incorrect pattern type.");
					ServerHandler.logWarn("{ItemMap} Please see: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/banner/PatternType.html for a list of correct pattern types.");
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
		if (itemMap.getNodeLocation().getString(".firework") != null) {
			if (itemMap.getMaterial().toString().equalsIgnoreCase("FIREWORK") || itemMap.getMaterial().toString().equalsIgnoreCase("FIREWORK_ROCKET")) {
				if (itemMap.getNodeLocation().getString(".firework.type") != null) {
					String stringType = itemMap.getNodeLocation().getString(".firework.type").toUpperCase();
					boolean flicker = itemMap.getNodeLocation().getBoolean(".firework.flicker");
					boolean trail = itemMap.getNodeLocation().getBoolean(".firework.trail");
					Type buildType = Type.valueOf(stringType);
					List <Color> colors = new ArrayList <Color> (); List <DyeColor> saveColors = new ArrayList <DyeColor> ();
					if (itemMap.getNodeLocation().getString(".firework.colors") != null) {
						String colorlist = itemMap.getNodeLocation().getString(".firework.colors").replace(" ", "");
						for (String color: colorlist.split(",")) {
							try { colors.add(DyeColor.valueOf(color.toUpperCase()).getFireworkColor()); saveColors.add(DyeColor.valueOf(color.toUpperCase())); } 
							catch (Exception e) {
								ServerHandler.logSevere("{ItemMap} The item " + itemMap.getConfigName() + " has the incorrect dye color " + color.toUpperCase() + " and does not exist.");
								ServerHandler.logWarn("{ItemMap} Please see: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/DyeColor.html for a list of correct dye color names.");
							}
						}
					} else if (itemMap.getNodeLocation().getString(".firework.colors") == null) {
						colors.add(DyeColor.valueOf("WHITE").getFireworkColor());
						saveColors.add(DyeColor.valueOf("WHITE"));
					}
					FireworkEffect effect = FireworkEffect.builder().trail(trail).flicker(flicker).withColor(colors).withFade(colors).with(buildType).build();
					itemMap.setFirework(effect); itemMap.setFireworkType(buildType); itemMap.setFireworkColor(saveColors); itemMap.setFireworkTrail(trail); itemMap.setFireworkFlicker(flicker);
				}
				int power = itemMap.getNodeLocation().getInt(".firework.power"); if (power == 0) { power = 1; }
				itemMap.setFireworkPower(power);
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
				try { 
					if (leatherColor.startsWith("#")) { 
						itemMap.setLeatherHex(leatherColor); 
					} else { 
						boolean hexValue = true;
						for (DyeColor color: DyeColor.values()) {
							if (color.name().replace(" ", "").equalsIgnoreCase(leatherColor)) {
								itemMap.setLeatherColor(leatherColor); 
								hexValue = false;
								break;
							}
						}
						if (hexValue) { itemMap.setLeatherHex(leatherColor); }
					} 
				} catch (Exception ex) { 
					ServerHandler.logSevere("{ItemMap} The leather-color: " + leatherColor + " is not a valid color for the item " + itemMap.getConfigName() + "."); 
					ServerHandler.logWarn("{ItemMap} Use hexcolor or see: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/DyeColor.html for valid bukkit colors."); 
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
				itemMap.setGeneration(org.bukkit.inventory.meta.BookMeta.Generation.valueOf(itemMap.getNodeLocation().getString(".generation")));
			} else {
				itemMap.setGeneration(org.bukkit.inventory.meta.BookMeta.Generation.ORIGINAL);
			}
		}
	}
//  ========================================================================================================================== //
	
//  =================================================================== //
//        ~ Sets the Legacy Book Pages of the Custom Book Item ~        //
//  Adds the custom book pages to the item without any JSON Formatting. //
//  =================================================================== //
	private void setLegacyBookPages(ItemMap itemMap) {
		if (!ServerHandler.hasSpecificUpdate("1_8") && itemMap.getMaterial().toString().equalsIgnoreCase("WRITTEN_BOOK") 
			&& itemMap.getNodeLocation().getString(".pages") != null && ConfigHandler.getPagesSection(itemMap.getNodeLocation()) != null) {
			List < String > formattedPages = new ArrayList < String > ();
			List<List <String> > rawPages = new ArrayList<List <String> >();
			for (String pageString: ConfigHandler.getPagesSection(itemMap.getNodeLocation()).getKeys(false)) {
				List < String > pageList = itemMap.getNodeLocation().getStringList(".pages." + pageString);
				rawPages.add(pageList);
				String saveList = "";
				for (int k = 0; k < pageList.size(); k++) {
					String formatLine = pageList.get(k);
					if (this.containsJSONEvent(formatLine)) {
						for (JSONEvent jsonType: JSONEvent.values()) {
							Matcher matchPattern = java.util.regex.Pattern.compile(jsonType.matchType + "(.*?)>").matcher(pageList.get(k));
							while (matchPattern.find()) {
								String inputResult = matchPattern.group(1);
								formatLine = formatLine.replace(jsonType.matchType + inputResult + ">", ((jsonType == JSONEvent.TEXT) ? inputResult : ""));
							}
						}
					} else if (formatLine.contains("raw:")) { formatLine = new String(); }
					saveList = saveList + formatLine + "\n";
				}
				formattedPages.add(saveList);
			}
			itemMap.setPages(formattedPages);
			itemMap.setListPages(rawPages);
		}
	}
//  =========================================================================================================================================================================================================================================================================================================================================================== //
	
//  =============================================== //
//  ~ Sets the Attributes of the Custom Book Item ~ //
//     Shows or Hides the Attributes of the item.   //
//  =============================================== //
	private void setAttributes(ItemMap itemMap) {
		if (ServerHandler.hasSpecificUpdate("1_8") && Utils.containsIgnoreCase(itemMap.getItemFlags(), "hide-attributes")) {
			itemMap.setAttributesInfo(true);
		}
	}
//  ===================================================================================================================================== //
}