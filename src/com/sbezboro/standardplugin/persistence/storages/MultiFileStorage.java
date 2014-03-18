package com.sbezboro.standardplugin.persistence.storages;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.sbezboro.standardplugin.StandardPlugin;
import com.sbezboro.standardplugin.persistence.PersistedObject;

public abstract class MultiFileStorage<T extends PersistedObject> implements FileStorage {
	protected StandardPlugin plugin;
	private String dataFolder;

	private HashMap<String, File> idToFile;
	private HashMap<String, FileConfiguration> idToConfig;

	private HashMap<String, T> idToObject;

	public MultiFileStorage(StandardPlugin plugin, String dataFolder) {
		this.plugin = plugin;
		this.dataFolder = dataFolder;
		
		new File(plugin.getDataFolder(), dataFolder).mkdirs();

		idToFile = new HashMap<String, File>();
		idToConfig = new HashMap<String, FileConfiguration>();

		idToObject = new HashMap<String, T>();
	}

	@Override
	public void reload() {
		for (String identifier : idToFile.keySet()) {
			load(identifier);
			idToObject.get(identifier).loadProperties();
		}
	}

	@Override
	public void unload() {
		for (T object : idToObject.values()) {
			if (object.toCommit()) {
				object.save();
			}
		}
	}

	@Override
	public ConfigurationSection load(String identifier) {
		File file = idToFile.get(identifier);
		if (file == null) {
			file = new File(plugin.getDataFolder(), dataFolder + "/" + identifier + ".yml");
			idToFile.put(identifier, file);
		}

		FileConfiguration config = YamlConfiguration.loadConfiguration(file);

		idToConfig.put(identifier, config);

		return config;
	}
	
	public void loadObjects() {
		File directory = new File(plugin.getDataFolder(), dataFolder);
		for (File file : directory.listFiles()) {
			if (file.isFile()) {
				String identifier = file.getName().split("\\.(?=[^\\.]+$)")[0];
				if (identifier.length() > 0) {
					T object = createObject(identifier);
					cacheObject(identifier, object);
				}
			}
		}
	}

	@Override
	public void save(String identifier) {
		File file = idToFile.get(identifier);
		FileConfiguration config = idToConfig.get(identifier);
		if (config == null || file == null) {
			return;
		}

		try {
			config.save(file);
		} catch (IOException e) {
			plugin.getLogger().severe("Error saving object to file!");
		}
	}
	
	public void remove(String identifier) {
		File file = idToFile.get(identifier);
		file.delete();
		
		idToFile.remove(identifier);
		idToConfig.remove(identifier);
		idToObject.remove(identifier);
	}

	public final Object loadProperty(String identifier, String key) {
		FileConfiguration config = idToConfig.get(identifier);
		return config.get(key);
	}

	public final void saveProperty(String identifier, String key, Object value) {
		idToConfig.get(identifier).set(key, value);
	}
	
	protected Collection<T> getObjects() {
		return idToObject.values();
	}

	protected T getObject(String identifier) {
		return idToObject.get(identifier);
	}
	
	public T createObject(String identifier) {
		return null;
	}

	protected void cacheObject(String identifier, T object) {
		idToObject.put(identifier, object);

		object.loadProperties();
	}

	public void uncacheObject(String identifer) {
		idToObject.remove(identifer);
	}
}
