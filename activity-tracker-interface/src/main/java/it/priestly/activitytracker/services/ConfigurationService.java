package it.priestly.activitytracker.services;

import java.util.EnumMap;
import java.util.Map;

import it.priestly.activitytracker.enums.ConfigKey;

public interface ConfigurationService {

	ConfigKey[] getSupportedKeys();
	
	default <T> T getConfig(ConfigKey key) {
		return getConfig(key, null);
	}
	
	<T> T getConfig(ConfigKey key, T defaultValue);
	
	<T> void setConfig(ConfigKey key, T value);
	
	default Map<ConfigKey, Object> getConfig() {
		Map<ConfigKey, Object> map = new EnumMap<ConfigKey, Object>(ConfigKey.class);
		for (ConfigKey key : getSupportedKeys()) {
			map.put(key, getConfig(key));
		}
		return map;
	}
	
	default void setConfig(Map<ConfigKey, Object> map) {
		for (Map.Entry<ConfigKey, Object> entry : map.entrySet()) {
			setConfig(entry.getKey(), entry.getValue());
		}
	}
	
}
