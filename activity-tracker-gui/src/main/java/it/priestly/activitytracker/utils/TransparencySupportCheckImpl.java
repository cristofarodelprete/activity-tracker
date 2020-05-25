package it.priestly.activitytracker.utils;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.priestly.activitytracker.enums.ConfigKey;
import it.priestly.activitytracker.support.SupportCheck;

@Component
public class TransparencySupportCheckImpl implements SupportCheck {

	@Autowired
	GuiUtils guiUtils;
	
	@Override
	public boolean isSupported(ConfigKey key) {
		switch (key) {
			case enableTransparency:
				return guiUtils.isTransparencySupported();
			default:
				return true;
		}
	}
	
	@Override
	public Set<ConfigKey> unsupportedKeys() {
		Set<ConfigKey> keys = new HashSet<ConfigKey>();
		if (!guiUtils.isTransparencySupported()) {
			keys.add(ConfigKey.enableTransparency);
		}
		return keys;
	}

}
