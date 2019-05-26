package me.RockinChaos.itemjoin.utils;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import me.RockinChaos.itemjoin.ItemJoin;
import me.RockinChaos.itemjoin.handlers.ServerHandler;

public class EffectAPI {
	
	public static void spawnParticle(Player player, String commandParticle) {
		if (Utils.containsIgnoreCase(commandParticle, "FIREWORK")) {
			particleFirework(player, commandParticle);
		} else {
			try {
				String[] particleParts = commandParticle.split(":");
				Particle particle;
				int particleLife = 1;
				particle = Particle.valueOf(particleParts[0]);
				if (particleParts[1] != null && !particleParts[1].isEmpty() && Utils.isInt(particleParts[1])) { particleLife = Integer.parseInt(particleParts[1]); }
				player.getWorld().spawnParticle(particle, player.getLocation(), particleLife);
			} catch (Exception e) {
				ServerHandler.sendErrorMessage("&cThere was an issue executing the commands-particle you defined.");
				ServerHandler.sendErrorMessage("&c" + commandParticle + "&c is not a particle in " + Reflection.getServerVersion() + ".");
				ServerHandler.sendDebugTrace(e);
			}
		}
	}

	public static void particleFirework(Player player, String commandParticle) {
		String[] projectileParts = commandParticle.split(":");
		Color startColor = Color.PURPLE;
		Color endColor = Color.GREEN;
		Type effectType = FireworkEffect.Type.STAR;
		int detonationDelay = 0;
		if (projectileParts[1] != null && !projectileParts[1].isEmpty()) { startColor = DyeColor.valueOf(projectileParts[1].toUpperCase()).getFireworkColor(); }
		if (projectileParts[2] != null && !projectileParts[2].isEmpty()) { endColor = DyeColor.valueOf(projectileParts[2].toUpperCase()).getFireworkColor(); }
		if (projectileParts[3] != null && !projectileParts[3].isEmpty()) { effectType = FireworkEffect.Type.valueOf(projectileParts[3]); }
		if (projectileParts[4] != null && !projectileParts[4].isEmpty() && Utils.isInt(projectileParts[4])) { detonationDelay = Integer.parseInt(projectileParts[4]); }
		FireworkEffect effect = FireworkEffect.builder().withColor(startColor).withFade(endColor).with(effectType).trail(false).flicker(true).build();
        Firework fw = (Firework)player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(effect);
        meta.setPower(1);
        fw.setFireworkMeta(meta);
		Bukkit.getScheduler().scheduleSyncDelayedTask(ItemJoin.getInstance(), new Runnable() {
			@Override
			public void run() {
				fw.detonate();
			}
		}, (detonationDelay * 20));
	}
}
