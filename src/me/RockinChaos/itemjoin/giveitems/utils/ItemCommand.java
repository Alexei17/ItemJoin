package me.RockinChaos.itemjoin.giveitems.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import me.RockinChaos.itemjoin.ItemJoin;
import me.RockinChaos.itemjoin.handlers.ConfigHandler;
import me.RockinChaos.itemjoin.handlers.ServerHandler;
import me.RockinChaos.itemjoin.utils.BungeeCord;
import me.RockinChaos.itemjoin.utils.CustomFilter;
import me.RockinChaos.itemjoin.utils.Hooks;
import me.RockinChaos.itemjoin.utils.Utils;

public class ItemCommand {
	private String command;
	private Type type;
	private ActionType action;
	private long delay = 0L;
	
	private ItemCommand(final String command, final ActionType action, final Type type, final long delay) {
		this.command = command;
		this.type = type;
		this.action = action;
		this.delay = delay;
	}
	
	public void execute(final Player player, final String action) {
		if (this.command == null || this.command.length() == 0 || !CommandType.INVENTORY.hasAction(action) && !CommandType.INTERACT.hasAction(action) && !this.action.hasAction(action)) { return; }
		sendDispatch(player, this.type);
	}
	
	private void sendDispatch(final Player player, final Type cmdtype) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(ItemJoin.getInstance(), (Runnable) new Runnable() {
			public void run() {
				switch (cmdtype) {
					case CONSOLE: dispatchConsoleCommands(player); break;
					case OP: dispatchOpCommands(player); break;
					case PLAYER: dispatchPlayerCommands(player); break;
					case MESSAGE: dispatchMessageCommands(player); break;
					case SERVERSWITCH: dispatchServerSwitchCommands(player); break;
					case BUNGEE: dispatchBungeeCordCommands(player); break;
					case DEFAULT: dispatchPlayerCommands(player); break;
					case DELAY: break;
					default: dispatchPlayerCommands(player); break;
				}
			}
		}, this.delay);
	}
	
	private void dispatchConsoleCommands(Player player) {
		try {
			setLoggable(player, "/" + Utils.translateLayout(this.command, player));
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), Utils.translateLayout(this.command, player));
		} catch (Exception e) {
			ServerHandler.sendErrorMessage("&cThere was an issue executing an item's command as console, if this continues please report it to the developer!");
			ServerHandler.sendDebugTrace(e);
		}
	}
	
	private void dispatchOpCommands(Player player) {
		try {
			boolean isOp = player.isOp();
			try {
				player.setOp(true);
				setLoggable(player, "/" + Utils.translateLayout(this.command, player));
				player.chat("/" + Utils.translateLayout(this.command, player));
			} catch (Exception e) {
				ServerHandler.sendDebugTrace(e);
				player.setOp(isOp);
				ServerHandler.sendErrorMessage("&cAn error has occurred while setting " + player.getName() + " status on the OP list, to ensure server security they have been removed as an OP.");
			} finally { player.setOp(isOp); }
		} catch (Exception e) {
			ServerHandler.sendErrorMessage("&cThere was an issue executing an item's command as an op, if this continues please report it to the developer!");
			ServerHandler.sendDebugTrace(e);
		}
	}
	
	private void dispatchPlayerCommands(Player player) {
		try {
			setLoggable(player, "/" + Utils.translateLayout(this.command, player));
			player.chat("/" + Utils.translateLayout(this.command, player));
		} catch (Exception e) {
			ServerHandler.sendErrorMessage("&cThere was an issue executing an item's command as a player, if this continues please report it to the developer!");
			ServerHandler.sendDebugTrace(e);
		}
	}
	
	private void dispatchMessageCommands(Player player) {
		try { player.sendMessage(Utils.translateLayout(this.command, player)); } 
		catch (Exception e) {
			ServerHandler.sendErrorMessage("&cThere was an issue executing an item's command to send a message, if this continues please report it to the developer!");
			ServerHandler.sendDebugTrace(e);
		}
	}
	
	private void dispatchServerSwitchCommands(Player player) {
		try { BungeeCord.SwitchServers(player, Utils.translateLayout(this.command, player)); } 
		catch (Exception e) {
			ServerHandler.sendErrorMessage("&cThere was an issue executing an item's command to switch servers, if this continues please report it to the developer!");
			ServerHandler.sendDebugTrace(e);
		}
	}
	
	private void dispatchBungeeCordCommands(Player player) {
		try { BungeeCord.ExecuteCommand(player, Utils.translateLayout(this.command, player)); } 
		catch (Exception e) {
			ServerHandler.sendErrorMessage("&cThere was an issue executing an item's command to BungeeCord, if this continues please report it to the developer!");
			ServerHandler.sendDebugTrace(e);
		}
	}
	
	private void setLoggable(Player player, String logCommand) {
		if (!Hooks.isLoggable()) {
			ArrayList < String > templist = new ArrayList < String > ();
			if (CustomFilter.clearLoggables.get("commands-list") != null && !CustomFilter.clearLoggables.get("commands-list").contains(logCommand)) {
				templist = CustomFilter.clearLoggables.get("commands-list");
			}
			templist.add(logCommand);
			CustomFilter.clearLoggables.put("commands-list", templist);
			((Logger) LogManager.getRootLogger()).addFilter(new CustomFilter());
		}
	}
	
	private static ActionType getExactActionType(ItemMap itemMap, String definition) {
		String invExists = itemMap.getNodeLocation().getString(".commands" + ActionType.INVENTORY.definition);
				if (Utils.containsIgnoreCase(itemMap.getCommandType().toString(), "inventory") || invExists != null) {
					if (ActionType.INVENTORY.hasDefine(definition)) {
						return ActionType.INVENTORY;
					} else if (ActionType.MULTI_CLICK_INVENTORY.hasDefine(definition)) {
						return ActionType.MULTI_CLICK_INVENTORY;
					}
				} else if (Utils.containsIgnoreCase(itemMap.getCommandType().toString(), "interact")) {
					if (ActionType.LEFT_CLICK_ALL.hasDefine(definition)) {
						return ActionType.LEFT_CLICK_ALL;
					} else if (ActionType.LEFT_CLICK_AIR.hasDefine(definition)) {
						return ActionType.LEFT_CLICK_AIR;
					} else if (ActionType.LEFT_CLICK_BLOCK.hasDefine(definition)) {
						return ActionType.LEFT_CLICK_BLOCK;
					} else if (ActionType.RIGHT_CLICK_ALL.hasDefine(definition)) {
						return ActionType.RIGHT_CLICK_ALL;
					} else if (ActionType.RIGHT_CLICK_AIR.hasDefine(definition)) {
						return ActionType.RIGHT_CLICK_AIR;
					} else if (ActionType.RIGHT_CLICK_BLOCK.hasDefine(definition)) {
						return ActionType.RIGHT_CLICK_BLOCK;
					} else if (ActionType.MULTI_CLICK_ALL.hasDefine(definition)) {
						return ActionType.MULTI_CLICK_ALL;
					} else if (ActionType.MULTI_CLICK_AIR.hasDefine(definition)) {
						return ActionType.MULTI_CLICK_AIR;
					} else if (ActionType.MULTI_CLICK_BLOCK.hasDefine(definition)) {
						return ActionType.MULTI_CLICK_BLOCK;
					} else if (ActionType.PHYSICAL.hasDefine(definition)) {
						return ActionType.PHYSICAL;
					}
				}
		return ActionType.DEFAULT;
	}
	
	private static ActionType getActionType(ItemMap itemMap, String action) {
		String invExists = itemMap.getNodeLocation().getString(".commands" + ActionType.INVENTORY.definition);
		if (ConfigHandler.getCommandsSection(itemMap.getNodeLocation()) != null) {
			Iterator < String > it = ConfigHandler.getCommandsSection(itemMap.getNodeLocation()).getKeys(false).iterator();
			while (it.hasNext()) {
				String definition = it.next();
				if (Utils.containsIgnoreCase(itemMap.getCommandType().toString(), "INVENTORY") 
						&& CommandType.INVENTORY.hasAction(action) || Utils.containsIgnoreCase(itemMap.getCommandType().toString(), "BOTH") 
						&& CommandType.INVENTORY.hasAction(action) || invExists != null && CommandType.INVENTORY.hasAction(action)) {
					if (ActionType.INVENTORY.hasAction(action) && ActionType.INVENTORY.hasDefine(definition)) {
						return ActionType.INVENTORY;
					} else if (ActionType.MULTI_CLICK_INVENTORY.hasAction(action) && ActionType.MULTI_CLICK_INVENTORY.hasDefine(definition)) {
						return ActionType.MULTI_CLICK_INVENTORY;
					} else if (ActionType.LEFT_CLICK_INVENTORY.hasAction(action) && ActionType.LEFT_CLICK_INVENTORY.hasDefine(definition)) {
						return ActionType.LEFT_CLICK_INVENTORY;
					} else if (ActionType.RIGHT_CLICK_INVENTORY.hasAction(action) && ActionType.RIGHT_CLICK_INVENTORY.hasDefine(definition)) {
						return ActionType.RIGHT_CLICK_INVENTORY;
					}
				} else if (Utils.containsIgnoreCase(itemMap.getCommandType().toString(), "INTERACT") && CommandType.INTERACT.hasAction(action) 
						|| Utils.containsIgnoreCase(itemMap.getCommandType().toString(), "BOTH") && CommandType.INTERACT.hasAction(action)) {
					if (ActionType.LEFT_CLICK_ALL.hasAction(action) && ActionType.LEFT_CLICK_ALL.hasDefine(definition)) {
						return ActionType.LEFT_CLICK_ALL;
					} else if (ActionType.LEFT_CLICK_AIR.hasAction(action) && ActionType.LEFT_CLICK_AIR.hasDefine(definition)) {
						return ActionType.LEFT_CLICK_AIR;
					} else if (ActionType.LEFT_CLICK_BLOCK.hasAction(action) && ActionType.LEFT_CLICK_BLOCK.hasDefine(definition)) {
						return ActionType.LEFT_CLICK_BLOCK;
					} else if (ActionType.RIGHT_CLICK_ALL.hasAction(action) && ActionType.RIGHT_CLICK_ALL.hasDefine(definition)) {
						return ActionType.RIGHT_CLICK_ALL;
					} else if (ActionType.RIGHT_CLICK_AIR.hasAction(action) && ActionType.RIGHT_CLICK_AIR.hasDefine(definition)) {
						return ActionType.RIGHT_CLICK_AIR;
					} else if (ActionType.RIGHT_CLICK_BLOCK.hasAction(action) && ActionType.RIGHT_CLICK_BLOCK.hasDefine(definition)) {
						return ActionType.RIGHT_CLICK_BLOCK;
					} else if (ActionType.MULTI_CLICK_ALL.hasAction(action) && ActionType.MULTI_CLICK_ALL.hasDefine(definition)) {
						return ActionType.MULTI_CLICK_ALL;
					} else if (ActionType.MULTI_CLICK_AIR.hasAction(action) && ActionType.MULTI_CLICK_AIR.hasDefine(definition)) {
						return ActionType.MULTI_CLICK_AIR;
					} else if (ActionType.MULTI_CLICK_BLOCK.hasAction(action) && ActionType.MULTI_CLICK_BLOCK.hasDefine(definition)) {
						return ActionType.MULTI_CLICK_BLOCK;
					} else if (ActionType.PHYSICAL.hasAction(action) && ActionType.PHYSICAL.hasDefine(definition)) {
						return ActionType.PHYSICAL;
					}
				}
			}
		}
		return ActionType.DEFAULT;
	}
	
	public static boolean isCommandable(ItemMap itemMap, String action) {
		List < String > actionCommandList = itemMap.getNodeLocation().getStringList(".commands" + getActionType(itemMap, action).definition);
		if (actionCommandList != null && !actionCommandList.isEmpty()) {
			return true;
		}
		return false;
	}
	
	public static ItemCommand[] arrayFromString(ItemMap itemMap) {
		if (ConfigHandler.getCommandsSection(itemMap.getNodeLocation()) == null) {
			return new ItemCommand[] {
				new ItemCommand("", ActionType.DEFAULT, Type.DEFAULT, 0L)
			};
		}
		return fromConfigList(itemMap);
	}
	
	private static ItemCommand[] fromConfigList(ItemMap itemMap) {
		if (ConfigHandler.getCommandsSection(itemMap.getNodeLocation()) != null) {
			final List < ItemCommand > arrayCommands = new ArrayList < ItemCommand > ();
			Iterator < String > it = ConfigHandler.getCommandsSection(itemMap.getNodeLocation()).getKeys(false).iterator();
			long delay = 0L;
			while (it.hasNext()) {
				String definition = it.next();
				List < String > commandsList = itemMap.getNodeLocation().getStringList("commands." + definition);
				for (int i = 0; i < commandsList.size(); ++i) {
					if (commandsList.get(i).trim().startsWith("delay:")) { delay = delay + getDelay(commandsList.get(i).trim());}
					arrayCommands.add(fromString(commandsList.get(i).trim(), getExactActionType(itemMap, definition), itemMap, delay));
				}
			}
			final ItemCommand[] commands = new ItemCommand[arrayCommands.size()];
			for (int i = 0; i < arrayCommands.size(); ++i) { commands[i] = arrayCommands.get(i);}
			return commands;
		}
		return null;
	}
	
	private static ItemCommand fromString(String input, ActionType action, ItemMap itemMap, long delay) {
		if (input == null || input.length() == 0) { return new ItemCommand("", ActionType.DEFAULT, Type.DEFAULT, 0L); }
		input = input.trim();
		Type type = Type.DEFAULT;
		
		if (input.startsWith("console:")) { input = input.substring(8); type = Type.CONSOLE; } 
		else if (input.startsWith("op:")) { input = input.substring(3); type = Type.OP; } 
		else if (input.startsWith("player:")) { input = input.substring(7); type = Type.PLAYER; } 
		else if (input.startsWith("server:")) { input = input.substring(7); type = Type.SERVERSWITCH; } 
		else if (input.startsWith("bungee:")) { input = input.substring(7); type = Type.BUNGEE; } 
		else if (input.startsWith("message:")) { input = input.substring(8); type = Type.MESSAGE; } 
		else if (input.startsWith("delay:")) { input = input.substring(6); type = Type.DELAY; }
		
		input = input.trim();
		input = Utils.colorFormat(input);
		return new ItemCommand(input, action, type, delay);
	}
	
	private static int getDelay(String lDelay) {
		try { if (Utils.returnInteger(lDelay) != null) { return Utils.returnInteger(lDelay); } } 
		catch (Exception e) { ServerHandler.sendDebugTrace(e); }
		return 0;
	}
	
	private enum Type {
		DEFAULT("DEFAULT", 0), CONSOLE("CONSOLE", 1), OP("OP", 2), PLAYER("PLAYER", 3), 
		SERVERSWITCH("SERVER", 4), MESSAGE("MESSAGE", 5), BUNGEE("BUNGEE", 6), DELAY("DELAY", 7);
		private Type(final String t, final int n) { }
	}
	
	private enum ActionType {
		DEFAULT("", ""),
		PHYSICAL("PHYSICAL", ".physical"),
		INVENTORY("PICKUP_ALL, PICKUP_HALF, PLACE_ALL", ".inventory"),
		MULTI_CLICK_INVENTORY("PICKUP_ALL, PICKUP_HALF, PLACE_ALL", ".multi-click"),
		LEFT_CLICK_INVENTORY("PICKUP_ALL", ".left-click"),
		RIGHT_CLICK_INVENTORY("PICKUP_HALF", ".right-click"),
		MULTI_CLICK_ALL("LEFT_CLICK_BLOCK, LEFT_CLICK_AIR, RIGHT_CLICK_BLOCK, RIGHT_CLICK_AIR", ".multi-click"),
		MULTI_CLICK_AIR("LEFT_CLICK_AIR, RIGHT_CLICK_AIR", ".multi-click-air"),
		MULTI_CLICK_BLOCK("LEFT_CLICK_BLOCK, RIGHT_CLICK_BLOCK", ".multi-click-block"),
		LEFT_CLICK_ALL("LEFT_CLICK_AIR, LEFT_CLICK_BLOCK", ".left-click"),
		LEFT_CLICK_AIR("LEFT_CLICK_AIR", ".left-click-air"),
		LEFT_CLICK_BLOCK("LEFT_CLICK_BLOCK", ".left-click-block"),
		RIGHT_CLICK_ALL("RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK", ".right-click"),
		RIGHT_CLICK_AIR("RIGHT_CLICK_AIR", ".right-click-air"),
		RIGHT_CLICK_BLOCK("RIGHT_CLICK_BLOCK", ".right-click-block");
		
		private final String name;
		private final String definition;
		private ActionType(String Action, String Definition) { name = Action; definition = Definition; }
		public boolean hasAction(String Action) { return name.contains(Action); }
		public boolean hasDefine(String Define) { return definition.contains(Define); }
	}
	
	private enum CommandType {
		INTERACT("PHYSICAL, LEFT_CLICK_BLOCK, LEFT_CLICK_AIR, RIGHT_CLICK_BLOCK, RIGHT_CLICK_AIR"),
		INVENTORY("PICKUP_ALL, PICKUP_HALF, PLACE_ALL");
		private final String name;
		private CommandType(String Action) { name = Action; }
		public boolean hasAction(String Action) { return name.contains(Action); }
	}	
}