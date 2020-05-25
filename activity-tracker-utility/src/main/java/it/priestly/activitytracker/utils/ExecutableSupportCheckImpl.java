package it.priestly.activitytracker.utils;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import it.priestly.activitytracker.enums.ConfigKey;
import it.priestly.activitytracker.support.SupportCheck;

@Component
public class ExecutableSupportCheckImpl implements SupportCheck {

	private boolean isUpdateSupported() {
		return !Constants.debugMode && Constants.installationPath != null;
	}
	
	private boolean isAutorunSupported() {
		return !Constants.debugMode && Constants.executablePath != null;
	}
	
	@Override
	public boolean isSupported(ConfigKey key) {
		switch (key) {
			case checkUpdates:
				return isUpdateSupported();
			case autorun:
				return isAutorunSupported();
			default:
				return true;
		}
	}
	
	@Override
	public Set<ConfigKey> unsupportedKeys() {
		Set<ConfigKey> keys = new HashSet<ConfigKey>();
		if (!isUpdateSupported()) {
			keys.add(ConfigKey.checkUpdates);
		}
		if (!isAutorunSupported()) {
			keys.add(ConfigKey.autorun);
		}
		return keys;
	}

}
