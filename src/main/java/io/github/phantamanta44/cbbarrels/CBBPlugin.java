package io.github.phantamanta44.cbbarrels;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class CBBPlugin extends JavaPlugin {

	private SignListener sl;

	@Override
	public void onEnable() {
		Bukkit.getServer().getPluginManager().registerEvents(sl = new SignListener(this), this);
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(sl);
		sl = null;
	}

}
