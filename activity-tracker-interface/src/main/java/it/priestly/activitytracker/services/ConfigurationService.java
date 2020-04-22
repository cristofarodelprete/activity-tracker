package it.priestly.activitytracker.services;

import java.util.Map;

import it.priestly.activitytracker.enums.ConfigKey;

public interface ConfigurationService {

	<T> T getConfig(ConfigKey key);
	
	<T> T getConfig(ConfigKey key, T defaultValue);
	
	<T> void setConfig(ConfigKey key, T value);
	
	Map<ConfigKey, Object> getConfig();
	
	void setConfig(Map<ConfigKey, Object> map);
	
}
