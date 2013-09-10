package com.sbezboro.standardplugin.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.model.StandardPlayer;
import com.sbezboro.standardplugin.model.Title;
import com.sbezboro.standardplugin.util.MiscUtil;

public class EntityDamageListener extends EventListener implements Listener {

	public EntityDamageListener(StandardPlugin plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (!plugin.isPvpProtectionEnabled()) {
			return;
		}

		Entity damagerEntity = event.getDamager();
		StandardPlayer damager = plugin.getStandardPlayer(damagerEntity);
		if (damager == null && damagerEntity instanceof Arrow) {
			damager = plugin.getStandardPlayer(((Arrow) damagerEntity).getShooter());
		}

		// Player attacking
		if (damager != null) {
			StandardPlayer victim = plugin.getStandardPlayer(event.getEntity());
			
			if (damager == victim) {
				return;
			}

			if (victim != null) {
				// Victim protected
				if (victim.isPvpProtected()) {
					int remainingTime = victim.getPvpProtectionTimeRemaining();
					damager.sendMessage(ChatColor.RED + "This player is protected from PVP!");
					victim.sendMessage(ChatColor.RED + "You are immune to PVP damage for " + ChatColor.AQUA + remainingTime + ChatColor.RED + " more "
							+ MiscUtil.pluralize("minute", remainingTime) + "!");

					if (!damager.isPvpProtected() && !damager.isNewbieStalker() && damager.incrementNewbieAttacks() >= plugin.getNewbieStalkerThreshold()) {
						Title title = damager.addTitle(Title.NEWBIE_STALKER);
						StandardPlugin.broadcast("" + ChatColor.AQUA + ChatColor.BOLD + damager.getDisplayName(false) + ChatColor.AQUA
								+ " has been designated as a " + ChatColor.BOLD + title.getDisplayName() + ChatColor.AQUA + "! FOR SHAME");
					}

					event.setCancelled(true);
					return;
				}

				// Attacker protected but victim isn't so turn off
				// attacker's protection
				if (damager.isPvpProtected()) {
					damager.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "Your PVP protection has been disabled!");
					damager.setPvpProtection(false);

					plugin.getLogger().info("Disabling PVP protection for " + damager.getName() + " due to an attack on " + victim.getName());
				}

				damager.setInPvp(victim);
				victim.setLastAttacker(damager);
			}
		}
	}

}
