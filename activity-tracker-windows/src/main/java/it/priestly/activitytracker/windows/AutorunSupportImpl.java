package it.priestly.activitytracker.windows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.priestly.activitytracker.enums.PlatformKey;
import it.priestly.activitytracker.support.PlatformSupport;
import it.priestly.activitytracker.utils.Constants;

@Component
public class AutorunSupportImpl implements PlatformSupport<Boolean> {
	
	@Autowired
	private RegistryHelper registryHelper;
	
	@Override
	public Class<Boolean> getType() {
		return Boolean.class;
	}
	
	@Override
	public boolean isSupported() {
		return Constants.isWindows && Constants.executablePath != null;
	}

	@Override
	public PlatformKey getKey() {
		return PlatformKey.autorun;
	}

	@Override
	public Boolean get() {
		try {
			String currentPath = registryHelper.read(
					"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
			        "ActivityTracker");
			return currentPath != null && !currentPath.isEmpty() && currentPath.equals(Constants.executablePath);
		} catch (Exception ex) {
			return null;
		}
	}

	@Override
	public void set(Boolean value) {
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
}
