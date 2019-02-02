package me.RockinChaos.itemjoin.handlers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.RockinChaos.itemjoin.ItemJoin;

public class ServerHandler {
	private static boolean Debugging = false;
	
	public static boolean hasCombatUpdate() {
		String pkgname = ItemJoin.getInstance().getServer().getClass().getPackage().getName();
		String combatVersion = "v1_9_R0".replace("_", "").replace("R0", "").replace("R1", "").replace("R2", "").replace("R3", "").replace("R4", "").replace("R5", "").replaceAll("[a-z]", "");
		String version = pkgname.substring(pkgname.lastIndexOf('.') + 1).replace("_", "").replace("R0", "").replace("R1", "").replace("R2", "").replace("R3", "").replace("R4", "").replace("R5", "").replaceAll("[a-z]", "");
		if (Integer.parseInt(version) >= Integer.parseInt(combatVersion)) {
			return true;
		}
		return false;
	}
	
	public static boolean hasAquaticUpdate() {
		String pkgname = ItemJoin.getInstance().getServer().getClass().getPackage().getName();
		String combatVersion = "v1_13_R0".replace("_", "").replace("R0", "").replace("R1", "").replace("R2", "").replace("R3", "").replace("R4", "").replace("R5", "").replaceAll("[a-z]", "");
		String version = pkgname.substring(pkgname.lastIndexOf('.') + 1).replace("_", "").replace("R0", "").replace("R1", "").replace("R2", "").replace("R3", "").replace("R4", "").replace("R5", "").replaceAll("[a-z]", "");
		if (Integer.parseInt(version) >= Integer.parseInt(combatVersion)) {
			return true;
		}
		return false;
	}
	
	public static boolean hasSpecificUpdate(String versionString) {
		String pkgname = ItemJoin.getInstance().getServer().getClass().getPackage().getName();
		String localeVersion = "v" + versionString + "_R0";
	    localeVersion = localeVersion.replace("_", "").replace("R0", "").replace("R1", "").replace("R2", "").replace("R3", "").replace("R4", "").replace("R5", "").replaceAll("[a-z]", "");
		String version = pkgname.substring(pkgname.lastIndexOf('.') + 1).replace("_", "").replace("R0", "").replace("R1", "").replace("R2", "").replace("R3", "").replace("R4", "").replace("R5", "").replaceAll("[a-z]", "");
		if (Integer.parseInt(version) >= Integer.parseInt(localeVersion)) {
			return true;
		}
		return false;
	}

	public static void sendConsoleMessage(String message) {
		String prefix = "&7[&eItemJoin&7] ";
		message = prefix + message;
		message = ChatColor.translateAlternateColorCodes('&', message).toString();
		if (ConfigHandler.getConfig("config.yml") != null) {
			if(ConfigHandler.getConfig("config.yml").getBoolean("Log-Coloration") != true) {
			message = ChatColor.stripColor(message);
			}
		}
		if (message.equalsIgnoreCase("") || message.isEmpty()) {
			message = "";
		}
		ItemJoin.getInstance().getServer().getConsoleSender().sendMessage(message);
	}
	
	public static void sendErrorMessage(String message) {
		String prefix = "&e[&4ITEMJOIN_ERROR&e]&c ";
		message = prefix + message;
		message = ChatColor.translateAlternateColorCodes('&', message).toString();
		if (ConfigHandler.getConfig("config.yml") != null) {
			if(ConfigHandler.getConfig("config.yml").getBoolean("Log-Coloration") != true) {
			message = ChatColor.stripColor(message);
			}
		}
		if (message.equalsIgnoreCase("") || message.isEmpty()) {
			message = "";
	}
		ItemJoin.getInstance().getServer().getConsoleSender().sendMessage(message);
	}
	
	public static void sendPlayerMessage(Player player, String message) {
		String prefix = "&7[&eItemJoin&7] ";
		message = prefix + message;
		message = ChatColor.translateAlternateColorCodes('&', message).toString();
			if (message.contains("blankmessage")) {
				message = "";
		}
		player.sendMessage(message);
	}
	
	public static void sendDebugTrace(Exception e) {
		if (ServerHandler.hasDebuggingMode()) { e.printStackTrace(); }
	}

	public static void sendDebugMessage(String message) {
		if (ServerHandler.hasDebuggingMode()) {
		String prefix = "[ITEMJOIN_DEBUG] &c";
		message = ChatColor.translateAlternateColorCodes('&', message).toString();
		message = ChatColor.stripColor(message);
		message = prefix + message;
		message = ChatColor.translateAlternateColorCodes('&', message).toString();
		ItemJoin.getInstance().getServer().getConsoleSender().sendMessage(message);
		}
	}
	
	public static boolean hasDebuggingMode() {
		return Debugging;
	}
	
	public static void loadDebuggingMode() {
		Debugging = ConfigHandler.getConfig("config.yml").getBoolean("Debugging-Mode");
	}
}
