package it.priestly.activitytracker.services;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import it.priestly.activitytracker.enums.ConfigKey;
import it.priestly.activitytracker.models.Config;
import it.priestly.activitytracker.repositories.ConfigRepository;
import it.priestly.activitytracker.services.ConfigurationService;

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
	public Map<ConfigKey, Object> getConfig() {
		Map<ConfigKey, Object> map = new LinkedHashMap<>();
		for (Config config : configRepository.findAll()) {
			ConfigKey key = Enum.valueOf(ConfigKey.class, config.getKey());
			map.put(key, conversionService.convert(config.getValue(), key.type()));
		}
		return map;
	}

	@Override
	public void setConfig(Map<ConfigKey, Object> map) {
		Map<String,Config> configs = configRepository.findAll().stream().collect(Collectors.toMap(
				c -> c.getKey(),
				c -> c
		));
		List<Config> toUpdate = new LinkedList<Config>();
		for (Map.Entry<ConfigKey, Object> entry : map.entrySet()) {
			Config config = null;
			if (configs.containsKey(entry.getKey().name())) {
				config = configs.get(entry.getKey().name());
			} else {
				config = new Config();
				config.setKey(entry.getKey().name());
			}
			config.setValue(conversionService.convert(entry.getValue(), String.class));
			toUpdate.add(config);
		}
		configRepository.saveAll(toUpdate);
	}
}
