package com.dgsspa.activitytracker.services;

import java.util.Map;

import com.dgsspa.activitytracker.enums.ConfigKey;

public interface ConfigurationService {

	<T> T getConfig(ConfigKey key);
	
	<T> T getConfig(ConfigKey key, T defaultValue);
	
	<T> void setConfig(ConfigKey key, T value);
	
	Map<ConfigKey, String> getRawConfig();
	
	void setRawConfig(Map<ConfigKey, String> map);
	
}
