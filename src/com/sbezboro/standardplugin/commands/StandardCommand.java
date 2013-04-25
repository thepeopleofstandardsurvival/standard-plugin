package com.sbezboro.standardplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.sbezboro.standardplugin.StandardPlugin;

public class StandardCommand extends BaseCommand {

	public StandardCommand(StandardPlugin plugin) {
		super(plugin);
	}

	@Override
	public boolean handle(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			showUsageInfo(sender);
			return false;
		}
		
		if (args[0].equalsIgnoreCase("reload")) {
			plugin.reloadPlugin();
			sender.sendMessage("Plugin reloaded");
			return true;
		}
		
		showUsageInfo(sender);
		return false;
	}

	@Override
	public void showUsageInfo(CommandSender sender) {
		sender.sendMessage("Usage: /" + getName() + " reload");
	}

	@Override
	public String getName() {
		return "standard";
	}

	@Override
	public boolean isPlayerOnly(int numArgs) {
		return false;
	}

}
