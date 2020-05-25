package it.priestly.activitytracker.support;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import it.priestly.activitytracker.enums.ConfigKey;

public interface SupportCheck {

	boolean isSupported(ConfigKey key);
	
	default Set<ConfigKey> unsupportedKeys() {
		return Arrays.stream(ConfigKey.values())
				.filter(key -> !isSupported(key))
				.collect(Collectors.toSet());
	}
}
