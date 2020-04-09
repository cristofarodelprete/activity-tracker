package com.dgsspa.activitytracker.services;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import com.dgsspa.activitytracker.enums.ConfigKey;
import com.dgsspa.activitytracker.models.Config;
import com.dgsspa.activitytracker.repositories.ConfigRepository;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {
	
	@Autowired
	private ConfigRepository configRepository;

	@Autowired
	private ConversionService conversionService;
	
	@Override
	public <T> T getConfig(ConfigKey key) {
		return getConfig(key, null);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getConfig(ConfigKey key, T defaultValue) {
		Optional<Config> config = configRepository.findByKey(key.name());
		if (config.isPresent()) {
			String stringValue = config.get().getValue();
			if (stringValue != null && !stringValue.isEmpty()) {
				return (T)conversionService.convert(config.get().getValue(), key.type());
			}
		}
		return defaultValue;
	}
	
	@Override
	public <T> void setConfig(ConfigKey key, T value) {
		String string = conversionService.convert(value, String.class);
		Config config = null;
		Optional<Config> configOpt = configRepository.findByKey(key.name());
		if (configOpt.isPresent()) {
			config = configOpt.get();
		} else {
			config = new Config();
			config.setKey(key.name());
		}
		config.setValue(string);
		configRepository.save(config);
	}

	@Override
	public Map<ConfigKey, String> getRawConfig() {
		Map<ConfigKey, String> map = new LinkedHashMap<>();
		for (Config config : configRepository.findAll()) {
			map.put(Enum.valueOf(ConfigKey.class, config.getKey()), config.getValue());
		}
		return map;
	}

	@Override
	public void setRawConfig(Map<ConfigKey, String> map) {
		List<Config> configs = new LinkedList<>();
		for (Map.Entry<ConfigKey, String> entry : map.entrySet()) {
			Config config = null;
			Optional<Config> configOpt = configRepository.findByKey(entry.getKey().name());
			if (configOpt.isPresent()) {
				config = configOpt.get();
			} else {
				config = new Config();
				config.setKey(entry.getKey().name());
			}
			config.setValue(entry.getValue());
			configs.add(config);
		}
		configRepository.saveAll(configs);
	}
}
