package it.priestly.activitytracker.utils;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import it.priestly.activitytracker.enums.ConfigKey;
import it.priestly.activitytracker.services.ConfigurationService;

@Component
public class ConfigurationHelper {

	@Autowired
	private ConversionService conversionService;
	
	@Autowired
	private ConfigurationService configurationService;

	private Properties defaultConfiguration;
	
	public ConfigurationHelper() {
		try {
			Resource resource = new ClassPathResource("config.properties");
			defaultConfiguration = PropertiesLoaderUtils.loadProperties(resource);
		} catch (IOException e) {
			defaultConfiguration = null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(ConfigKey key) {
		T value = configurationService.getConfig(key);
		if (value == null && defaultConfiguration != null) {
			String str = defaultConfiguration.getProperty("config." + key.name());
			if (str != null) {
				value = (T)conversionService.convert(str, key.type());
			}
		}
		return value;
	}
	
	public Map<ConfigKey, Object> get() {
		Map<ConfigKey, Object> config = configurationService.getConfig();
		for (ConfigKey key : ConfigKey.values()) {
			if (!config.containsKey(key)) {
				String str = defaultConfiguration.getProperty("config." + key.name());
				if (str != null) {
					config.put(key, conversionService.convert(str, key.type()));
				}
			}
		}
		return config;
	}
	
	public <T> void set(ConfigKey key, T value) {
		configurationService.setConfig(key, value);
	}
	
	public void set(Map<ConfigKey, Object> config) {
		configurationService.setConfig(config);
	}
}
