package it.priestly.activitytracker.windows;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.priestly.activitytracker.enums.ConfigKey;
import it.priestly.activitytracker.services.ConfigurationService;
import it.priestly.activitytracker.support.SupportCheck;
import it.priestly.activitytracker.utils.Constants;

@Service
public class AutorunSupportImpl implements ConfigurationService, SupportCheck {

	private static final ConfigKey[] supportedKeys = new ConfigKey[] { ConfigKey.autorun };
	
	@Autowired
	private RegistryHelper registryHelper;
	
	private Boolean getAutorun() {
		try {
			String currentPath = registryHelper.read(
					"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
			        "ActivityTracker");
			return currentPath != null && !currentPath.isEmpty() && currentPath.equals(Constants.executablePath);
		} catch (Exception ex) {
			return null;
		}
	}

	private void setAutorun(Boolean value) {
		try {
			if (value) {
				registryHelper.write(
						"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
						"ActivityTracker",
						Constants.executablePath);
			} else {
				String currentPath = registryHelper.read(
						"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
				        "ActivityTracker");
				if (currentPath != null && !currentPath.isEmpty() && currentPath.equals(Constants.executablePath)) {
					registryHelper.write(
							"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
							"ActivityTracker",
							null);
				}
			}
		} catch (Exception ex) {
			
		}
	}
	
	@Override
	public boolean isSupported(ConfigKey key) {
		switch (key) {
			case autorun:
				return Constants.isWindows;
			default:
				return true;
		}
	}
	
	@Override
	public Set<ConfigKey> unsupportedKeys() {
		Set<ConfigKey> keys = new HashSet<ConfigKey>();
		if (!Constants.isWindows) {
			keys.add(ConfigKey.autorun);
		}
		return keys;
	}

	@Override
	public ConfigKey[] getSupportedKeys() {
		return supportedKeys;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getConfig(ConfigKey key, T defaultValue) {
		switch (key) {
			case autorun:
				return (T)getAutorun();
			default:
				return defaultValue;
		}
	}

	@Override
	public <T> void setConfig(ConfigKey key, T value) {
		switch (key) {
			case autorun:
				setAutorun((Boolean)value);
				break;
			default:
				break;
		}
	}
}
