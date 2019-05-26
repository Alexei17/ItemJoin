package me.RockinChaos.itemjoin.handlers;

import java.io.File;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.RockinChaos.itemjoin.Commands;
import me.RockinChaos.itemjoin.ItemJoin;
import me.RockinChaos.itemjoin.giveitems.listeners.LimitSwitch;
import me.RockinChaos.itemjoin.giveitems.listeners.PlayerJoin;
import me.RockinChaos.itemjoin.giveitems.listeners.PlayerQuit;
import me.RockinChaos.itemjoin.giveitems.listeners.RegionEnter;
import me.RockinChaos.itemjoin.giveitems.listeners.Respawn;
import me.RockinChaos.itemjoin.giveitems.listeners.WorldSwitch;
import me.RockinChaos.itemjoin.giveitems.utils.ItemDesigner;
import me.RockinChaos.itemjoin.listeners.Consumes;
import me.RockinChaos.itemjoin.listeners.Drops;
import me.RockinChaos.itemjoin.listeners.Interact;
import me.RockinChaos.itemjoin.listeners.InventoryClick;
import me.RockinChaos.itemjoin.listeners.InventoryClose;
import me.RockinChaos.itemjoin.listeners.Legacy_Pickups;
import me.RockinChaos.itemjoin.listeners.Legacy_Storable;
import me.RockinChaos.itemjoin.listeners.Pickups;
import me.RockinChaos.itemjoin.listeners.Placement;
import me.RockinChaos.itemjoin.listeners.Recipes;
import me.RockinChaos.itemjoin.listeners.Storable;
import me.RockinChaos.itemjoin.listeners.SwitchHands;
import me.RockinChaos.itemjoin.utils.DependAPI;
import me.RockinChaos.itemjoin.utils.ItemCreator;
import me.RockinChaos.itemjoin.utils.Metrics;
import me.RockinChaos.itemjoin.utils.Reflection;
import me.RockinChaos.itemjoin.utils.Utils;
import me.RockinChaos.itemjoin.utils.YAMLGenerator;
import me.RockinChaos.itemjoin.utils.sqlite.SQLData;

public class ConfigHandler {
	
	private static YamlConfiguration itemsYAML;
	private static YamlConfiguration configYAML;
	private static YamlConfiguration enLangYAML;
	private static boolean yamlGenerating = false;
	
	private static SQLData sqlData;
	private static UpdateHandler updater;
	private static ItemDesigner itemDesigner;
	private static Metrics metrics;
	private static DependAPI depends;
	
	public static void getConfigs() {
		configFile();
		itemsFile();
		enLangFile();
	}
	
	public static void generateData() {
		ItemHandler.initializeItemID();
		setDepends(new DependAPI());
		setSQLData(new SQLData());
		setItemDesigner(new ItemDesigner());
		setMetrics(new Metrics());
		sendUtilityDepends();
	}
	
	public static void registerEvents() {
	    ItemJoin.getInstance().getCommand("itemjoin").setExecutor(new Commands());
		ItemJoin.getInstance().getCommand("ij").setExecutor(new Commands());
		ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new PlayerJoin(), ItemJoin.getInstance());
		ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new PlayerQuit(), ItemJoin.getInstance());
		ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new InventoryClose(), ItemJoin.getInstance());
		ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new WorldSwitch(), ItemJoin.getInstance());
		ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new LimitSwitch(), ItemJoin.getInstance());
		ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Respawn(), ItemJoin.getInstance());
		ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new InventoryClick(), ItemJoin.getInstance());
		ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Drops(), ItemJoin.getInstance());
		ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Interact(), ItemJoin.getInstance());
		ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Placement(), ItemJoin.getInstance());
		ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Consumes(), ItemJoin.getInstance());
		ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new ItemCreator(), ItemJoin.getInstance());
		ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Recipes(), ItemJoin.getInstance());
		if (!ServerHandler.hasSpecificUpdate("1_8")) { ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Legacy_Storable(), ItemJoin.getInstance());} 
		else { ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Storable(), ItemJoin.getInstance()); }
		if (ServerHandler.hasSpecificUpdate("1_12") && Reflection.getEventClass("entity.EntityPickupItemEvent") != null) { ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Pickups(), ItemJoin.getInstance()); } 
		else { ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new Legacy_Pickups(), ItemJoin.getInstance()); }
		if (ServerHandler.hasCombatUpdate() && Reflection.getEventClass("player.PlayerSwapHandItemsEvent") != null) { ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new SwitchHands(), ItemJoin.getInstance()); }
		if (getDepends().getGuard().guardEnabled()) { ItemJoin.getInstance().getServer().getPluginManager().registerEvents(new RegionEnter(), ItemJoin.getInstance()); }
	}
	
	public static Boolean isConfigurable() {
		if (ConfigHandler.getConfigurationSection() != null) {
			return true;
		} else if (ConfigHandler.getConfigurationSection() == null) {
			ServerHandler.sendConsoleMessage("&4There are no items detected in the items.yml.");
			ServerHandler.sendConsoleMessage("&4Try adding an item to the items section in the items.yml.");
			ServerHandler.sendConsoleMessage("&eIf you continue to see this message contact the plugin developer!");
			return false;
		}
		return false;
	}

	public static FileConfiguration getConfig(String path) {
		File file = new File(ItemJoin.getInstance().getDataFolder(), path);
		if (configYAML == null) { getConfigData(path); }
		return getPath(path, file, false);
	}
	
	public static FileConfiguration getConfigData(String path) {
		File file = new File(ItemJoin.getInstance().getDataFolder(), path);
		if (!(file).exists()) {
			try {
				ItemJoin.getInstance().saveResource(path, false);
				if (path.contains("items.yml")) { setGenerating(true); }
			} catch (Exception e) {
				ServerHandler.sendDebugTrace(e);
				ItemJoin.getInstance().getLogger().warning("Cannot save " + path + " to disk!");
				return null;
			}
		}
		return getPath(path, file, true);
	}

	public static YamlConfiguration getPath(String path, File file, boolean saveData) {
		if (path.contains("items.yml")) {
			if (saveData) { itemsYAML = YamlConfiguration.loadConfiguration(file); }
			return itemsYAML;
		} else if (path.contains("config.yml")) {
			if (saveData) { configYAML = YamlConfiguration.loadConfiguration(file); }
			return configYAML;
		} else if (path.contains("en-lang.yml")) {
			if (saveData) { enLangYAML = YamlConfiguration.loadConfiguration(file); }
			return enLangYAML;
		}
		return null;
	}

	public static void configFile() {
		getConfigData("config.yml");
		File File = new File(ItemJoin.getInstance().getDataFolder(), "config.yml");
		if (File.exists() && getConfig("config.yml").getInt("config-Version") != 7) {
			if (ItemJoin.getInstance().getResource("config.yml") != null) {
				String newGen = "config" + Utils.getRandom(1, 50000) + ".yml";
				File newFile = new File(ItemJoin.getInstance().getDataFolder(), newGen);
				if (!newFile.exists()) {
					File.renameTo(newFile);
					File configFile = new File(ItemJoin.getInstance().getDataFolder(), "config.yml");
					configFile.delete();
					getConfigData("config.yml");
					ServerHandler.sendConsoleMessage("&aYour config.yml is out of date and new options are available, generating a new one!");
				}
			}
		}
		getConfig("config.yml").options().copyDefaults(false);
	}

	public static void itemsFile() {
		getConfigData("items.yml");
		File itemsFile = new File(ItemJoin.getInstance().getDataFolder(), "items.yml");
		if (itemsFile.exists() && getConfig("items.yml").getInt("items-Version") != 6) {
			if (ItemJoin.getInstance().getResource("items.yml") != null) {
				String newGen = "items" + Utils.getRandom(1, 50000) + ".yml";
				File newFile = new File(ItemJoin.getInstance().getDataFolder(), newGen);
				if (!newFile.exists()) {
					itemsFile.renameTo(newFile);
					File configFile = new File(ItemJoin.getInstance().getDataFolder(), "items.yml");
					configFile.delete();
					getConfigData("items.yml");
					ServerHandler.sendConsoleMessage("&4Your items.yml is out of date and new options are available, generating a new one!");
				}
			}
		}
		YAMLSetup();
		getConfig("items.yml").options().copyDefaults(false);
	}
	
	private static void YAMLSetup() {
		if (isGenerating()) { 
			YAMLGenerator.generateItemsFile();
			getConfigData("items.yml");
			setGenerating(false);
		}
	}
	
	public static void enLangFile() {
		getConfigData("en-lang.yml");
	      File enLang = new File(ItemJoin.getInstance().getDataFolder(), "en-lang.yml");
	      if (enLang.exists() && ItemJoin.getInstance().getConfig().getString("Language").equalsIgnoreCase("English") && getConfig("en-lang.yml").getInt("en-Version") != 7) {
	      if (ItemJoin.getInstance().getResource("en-lang.yml") != null) {
	        String newGen = "en-lang" + Utils.getRandom(1, 50000) + ".yml";
	        File newFile = new File(ItemJoin.getInstance().getDataFolder(), newGen);
	           if (!newFile.exists()) {
	    	      enLang.renameTo(newFile);
	              File configFile = new File(ItemJoin.getInstance().getDataFolder(), "en-lang.yml");
	              configFile.delete();
	              getConfigData("en-lang.yml");
				  ServerHandler.sendConsoleMessage("&4Your en-lang.yml is out of date and new options are available, generating a new one!");
	           }
	        }
	      }
		  if (ItemJoin.getInstance().getConfig().getString("Language").equalsIgnoreCase("English")) {
			  getConfig("en-lang.yml").options().copyDefaults(false);
		  }
	}

	public static String encodeSecretData(String str) {
		try {
			String hiddenData = "";
			for (char c: str.toCharArray()) {
				hiddenData += "�" + c;
			}
			return hiddenData;
		} catch (Exception e) {
			ServerHandler.sendDebugTrace(e);
			return null;
		}
	}

	public static String decodeSecretData(String str) {
		try {
			String[] hiddenData = str.split("(?:\\w{2,}|\\d[0-9A-Fa-f])+");
			String returnData = "";
			if (hiddenData == null) {
				hiddenData = str.split("�");
				for (int i = 0; i < hiddenData.length; i++) {
					returnData += hiddenData[i];
				}
				return returnData;
			} else {
				String[] d = hiddenData[hiddenData.length - 1].split("�");
				for (int i = 1; i < d.length; i++) {
					returnData += d[i];
				}
				return returnData;
			}
		} catch (Exception e) {
			ServerHandler.sendDebugTrace(e);
			return null;
		}
	}
	
	private static void sendUtilityDepends() {
		ServerHandler.sendConsoleMessage("&aUtilizing [ &e" + (getDepends().authMeEnabled() ? "AuthMe, " : "") + (getDepends().nickEnabled() ? "BetterNick, " : "") 
		+ (getDepends().mCoreEnabled() ? "Multiverse-Core, " : "") + (getDepends().mInventoryEnabled() ? "Multiverse-Inventories, " : "") 
		+ (getDepends().myWorldsEnabled() ? "My Worlds, " : "") + (getDepends().perInventoryEnabled() ? "PerWorldInventory, " : "") 
		+ (getDepends().perPluginsEnabled() ? "PerWorldPlugins, " : "") + (getDepends().tokenEnchantEnabled() ? "TokenEnchant, " : "") 
		+ (getDepends().getGuard().guardEnabled() ? "WorldGuard, " : "") + (getDepends().databaseEnabled() ? "HeadDatabase, " : "") 
		+ (getDepends().xInventoryEnabled() ? "xInventories, " : "") + (getDepends().placeHolderEnabled() ? "PlaceholderAPI, " : "") 
		+ (getDepends().getVault().vaultEnabled() ? "Vault " : "") + "&a]");
	}
	
	private static boolean isGenerating() {
		return yamlGenerating;
	}
	
	public static boolean isDebugging() {
		return ConfigHandler.getConfig("config.yml").getBoolean("General.Debugging");
	}
	
	public static boolean isLoggable() {
		return ConfigHandler.getConfig("config.yml").getBoolean("General.Log-Commands");
	}
	
	public static boolean isLogColor() {
		return ConfigHandler.getConfig("config.yml").getBoolean("General.Log-Coloration");
	}

	public static String isPreventPickups() {
		return ConfigHandler.getConfig("config.yml").getString("Prevent.Pickups");
	}
	
	public static String isPreventModify() {
		return ConfigHandler.getConfig("config.yml").getString("Prevent.itemMovement");
	}
	
	public static boolean isPreventOBypass() {
		return Utils.containsIgnoreCase(ConfigHandler.getConfig("config.yml").getString("Prevent.Bypass"), "OP");
	}
	
	public static boolean isPreventCBypass() {
		return Utils.containsIgnoreCase(ConfigHandler.getConfig("config.yml").getString("Prevent.Bypass"), "CREATIVE");
	}
	
	public static UpdateHandler getUpdater() {
		return updater;
	}
	
	public static void setUpdater(UpdateHandler update) {
		updater = update;
	}
	
	public static SQLData getSQLData() {
		return sqlData;
	}
	
	public static void setSQLData(SQLData sql) {
		sqlData = sql;
	}
	
	public static ItemDesigner getItemDesigner() {
		return itemDesigner;
	}
	
	private static void setItemDesigner(ItemDesigner designer) {
		itemDesigner = designer;
	}
	
	public static DependAPI getDepends() {
		return depends;
	}
	
	private static void setDepends(DependAPI depend) {
		depends = depend;
	}
	
	public static Metrics getMetrics() {
		return metrics;
	}
	
	private static void setMetrics(Metrics metric) {
		metrics = metric;
	}
	
	private static void setGenerating(boolean isGenerating) {
		yamlGenerating = isGenerating;
	}
	
	public static long getClearDelay() {
		if (!Utils.containsIgnoreCase(ConfigHandler.getConfig("config.yml").getString("Clear-Items.Join"), "DISABLED") 
				&& !Utils.containsIgnoreCase(ConfigHandler.getConfig("config.yml").getString("Clear-Items.Join"), "FALSE")
				|| !Utils.containsIgnoreCase(ConfigHandler.getConfig("config.yml").getString("Clear-Items.World-Switch"), "DISABLED")
				&& !Utils.containsIgnoreCase(ConfigHandler.getConfig("config.yml").getString("Clear-Items.World-Switch"), "FALSE")) {
				return ConfigHandler.getConfig("config.yml").getInt("Clear-Items.Delay-Tick") * 10L;
		}
		return 0;
	}
	
	public static long getItemDelay() {
		if (getClearDelay() >= ConfigHandler.getConfig("items.yml").getInt("items-Delay") * 10L) { return getClearDelay() + 1; }
		return ConfigHandler.getConfig("items.yml").getInt("items-Delay") * 10L;
	}
	
	public static boolean getItemPermissions() {
		return ConfigHandler.getConfig("config.yml").getBoolean("Permissions.Commands-Get");
	}
	
	public static boolean getCommandPermissions() {
		return ConfigHandler.getConfig("config.yml").getBoolean("Permissions.Commands-OP");
	}
	
	public static ConfigurationSection getConfigurationSection() {
		return getConfig("items.yml").getConfigurationSection("items");
	}
	
	public static ConfigurationSection getItemSection(String item) {
		return getConfigurationSection().getConfigurationSection(item);
	}
	
	public static ConfigurationSection getNameSection(ConfigurationSection items) {
		return getConfig("items.yml").getConfigurationSection(items.getCurrentPath() + ".name");
	}
	
	public static ConfigurationSection getLoreSection(ConfigurationSection items) {
		return getConfig("items.yml").getConfigurationSection(items.getCurrentPath() + ".lore");
	}
	
	public static ConfigurationSection getMaterialSection(ConfigurationSection items) {
		return getConfig("items.yml").getConfigurationSection(items.getCurrentPath() + ".id");
	}
	
	public static ConfigurationSection getOwnerSection(ConfigurationSection items) {
		return getConfig("items.yml").getConfigurationSection(items.getCurrentPath() + ".skull-owner");
	}
	
	public static ConfigurationSection getTextureSection(ConfigurationSection items) {
		return getConfig("items.yml").getConfigurationSection(items.getCurrentPath() + ".skull-texture");
	}
	
	public static ConfigurationSection getPagesSection(ConfigurationSection items) {
		return getConfig("items.yml").getConfigurationSection(items.getCurrentPath() + ".pages");
	}
	
	public static ConfigurationSection getCommandsSection(ConfigurationSection items) {
		return getConfig("items.yml").getConfigurationSection(items.getCurrentPath() + ".commands");
	}	
}