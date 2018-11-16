package de.timmyrs;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class CommandOverride extends JavaPlugin implements Listener, CommandExecutor
{

	private final HashMap<String, String> commands = new HashMap<>();

	@Override
	public void onEnable()
	{
		final ArrayList<HashMap<String, Object>> defaultCommands = new ArrayList<>();
		final HashMap<String, Object> defaultCommandHelp = new HashMap<>();
		defaultCommandHelp.put("names", new String[]{"help", "?"});
		defaultCommandHelp.put("response", "Here's some help!\n...but it's multiline! :O");
		defaultCommands.add(defaultCommandHelp);
		final HashMap<String, Object> defaultCommandPlugins = new HashMap<>();
		defaultCommandPlugins.put("names", new String[]{"plugins", "pl"});
		defaultCommandPlugins.put("response", "Plugin (1): &aWhatDoYouCare");
		defaultCommands.add(defaultCommandPlugins);
		getConfig().addDefault("commands", defaultCommands);
		getConfig().options().copyDefaults(true);
		saveConfig();
		reloadCommands();
		getServer().getPluginManager().registerEvents(this, this);
		getCommand("reloadcommands").setExecutor(this);
	}

	private void reloadCommands()
	{
		//noinspection unchecked
		final List<Map<String, Object>> commands = (List<Map<String, Object>>) getConfig().getList("commands");
		synchronized(this.commands)
		{
			this.commands.clear();
			for(Map<String, Object> command : commands)
			{
				if(command.containsKey("names") && command.containsKey("response"))
				{
					final String response = ((String) command.get("response")).replace("&", "§");
					//noinspection unchecked
					for(String alias : (List<String>) command.get("names"))
					{
						this.commands.put(alias.toLowerCase(), response);
					}
				}
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender s, Command c, String l, String[] a)
	{
		if(s.hasPermission("commandoverride.reload"))
		{
			reloadConfig();
			reloadCommands();
			s.sendMessage("§aCommandOverride commands have been reloaded!");
		}
		else
		{
			s.sendMessage("§cYou're not authorized to access this command.");
		}
		return true;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e)
	{
		String command = e.getMessage().substring(1).split(" ")[0];
		if(command.contains(":"))
		{
			final String[] arr = command.split(":");
			if(arr.length != 2)
			{
				return;
			}
			command = arr[1];
		}
		command = command.toLowerCase();
		synchronized(this.commands)
		{
			for(Map.Entry<String, String> entry : this.commands.entrySet())
			{
				if(entry.getKey().equals(command))
				{
					e.getPlayer().sendMessage(entry.getValue());
					e.setCancelled(true);
					return;
				}
			}
		}
	}
}
