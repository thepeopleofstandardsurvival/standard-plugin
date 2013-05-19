package com.sbezboro.standardplugin.persistence;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.model.StandardPlayer;

public class PlayerStorage extends ObjectStorage<StandardPlayer> {
	public PlayerStorage(StandardPlugin plugin) {
		super(plugin, "players");
	}
	
	public StandardPlayer getPlayer(String username) {
		StandardPlayer standardPlayer = getObject(username);
		
		if (standardPlayer == null) {
			Player player = Bukkit.getPlayer(username);
			if (player == null) {
				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(username);
				standardPlayer = new StandardPlayer(offlinePlayer, this);
			} else {
				standardPlayer = new StandardPlayer(player, this);
			}
			
			cacheObject(username, standardPlayer);
		} else if (standardPlayer.isOnline() && standardPlayer.getBasePlayer() == null) {
			standardPlayer.setPlayer(Bukkit.getPlayer(username));
		}
		
		return standardPlayer;
	}
}