package it.priestly.activitytracker.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import it.priestly.activitytracker.enums.ConfigKey;
import it.priestly.activitytracker.services.ConfigurationService;
import it.priestly.activitytracker.support.SupportCheck;

@Component
public class ConfigurationHelper {

	@Autowired
	private ConversionService conversionService;
	
	@Autowired
	private List<ConfigurationService> configurationServices;

	@Autowired
	private List<SupportCheck> supportChecks;
	
	private Map<ConfigKey,List<ConfigurationService>> configurationServiceMap;
	
	private Properties defaultConfiguration;
	
	public ConfigurationHelper() {
		try {
			Resource resource = new ClassPathResource("config.properties");
			defaultConfiguration = PropertiesLoaderUtils.loadProperties(resource);
		} catch (IOException e) {
			defaultConfiguration = null;
		}
	}
	
	private Map<ConfigKey,List<ConfigurationService>> getConfigurationServiceMap() {
		if (configurationServiceMap == null) {
			configurationServiceMap = new EnumMap<ConfigKey,List<ConfigurationService>>(ConfigKey.class);
			if (configurationServices != null) {
				for (ConfigurationService service : configurationServices) {
					for (ConfigKey key : service.getSupportedKeys()) {
						if (!configurationServiceMap.containsKey(key)) {
							configurationServiceMap.put(key, new ArrayList<ConfigurationService>());
						}
						configurationServiceMap.get(key).add(service);
					}
				}
			}
		}
		return configurationServiceMap;
	}
	
	private ConfigurationService getService(ConfigKey key) {
		List<ConfigurationService> services = getConfigurationServiceMap().get(key);
		if (services != null && services.size() > 0) {
			return services.get(0);
		} else {
			return null;
		}
	}
	
	private boolean isKeySupported(ConfigKey key) {
		if (supportChecks != null) {
			for (SupportCheck check : supportChecks) {
				if (!check.isSupported(key)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private Set<ConfigKey> getUnsupportedKeys() {
		Set<ConfigKey> keys = new HashSet<ConfigKey>();
		if (supportChecks != null) {
			for (SupportCheck check : supportChecks) {
				keys.addAll(check.unsupportedKeys());
			}
		}
		return keys;
	}
	
	public Set<ConfigKey> getKeys() {
		Set<ConfigKey> keys = new HashSet<ConfigKey>(getConfigurationServiceMap().keySet());
		Set<ConfigKey> unsupported = getUnsupportedKeys();
		keys.removeAll(unsupported);
		return keys;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(ConfigKey key) {
		T value = null;
		if (isKeySupported(key)) {
			ConfigurationService service = getService(key);
			if (service != null) {
				value = service.getConfig(key);
				if (value == null && defaultConfiguration != null) {
					String str = defaultConfiguration.getProperty("config." + key.name());
					if (str != null) {
						value = (T)conversionService.convert(str, key.type());
					}
				}
			}
		}
		return value;
	}
	
	public Map<ConfigKey, Object> get() {
		Map<ConfigKey, Object> config = new HashMap<ConfigKey, Object>();
		for (ConfigurationService service : configurationServices) {
			config.putAll(service.getConfig());
		}
		Set<ConfigKey> keys = getKeys();
		for (ConfigKey key : keys) {
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
		if (isKeySupported(key)) {
			ConfigurationService service = getService(key);
			if (service != null) {
				service.setConfig(key, value);
			}
		}
	}
	
	public void set(Map<ConfigKey, Object> config) {
		Map<ConfigurationService,Map<ConfigKey, Object>> toSet = new HashMap<ConfigurationService,Map<ConfigKey, Object>>();
		for (ConfigurationService service : configurationServices) {
			toSet.put(service, new HashMap<ConfigKey, Object>());
		}
		Map<ConfigKey,List<ConfigurationService>> map = getConfigurationServiceMap();
		Set<ConfigKey> supportedKeys = getKeys();
		for (ConfigKey key : config.keySet()) {
			if (supportedKeys.contains(key)) {
				for (ConfigurationService service : map.get(key)) {
					toSet.get(service).put(key, config.get(key));
				}
			}
		}
		for (Map.Entry<ConfigurationService,Map<ConfigKey, Object>> entry : toSet.entrySet()) {
			entry.getKey().setConfig(entry.getValue());
		}
	}
}
