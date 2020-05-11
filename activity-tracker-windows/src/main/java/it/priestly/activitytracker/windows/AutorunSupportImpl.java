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
		return Constants.debugMode || Constants.isWindows && Constants.executablePath != null;
	}

	@Override
	public PlatformKey getKey() {
		return PlatformKey.autorun;
	}

	@Override
	public Boolean get() {
		String executablePath = Constants.debugMode ? "D:\\Desktop\\ActivityTracker.exe" : Constants.executablePath;
		try {
			String currentPath = registryHelper.read(
					"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
			        "ActivityTracker");
			return currentPath != null && !currentPath.isEmpty() && currentPath.equals(executablePath);
		} catch (Exception ex) {
			return null;
		}
	}

	@Override
	public void set(Boolean value) {
		String executablePath = Constants.debugMode ? "D:\\Desktop\\ActivityTracker.exe" : Constants.executablePath;
		try {
			if (value) {
				registryHelper.write(
						"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
						"ActivityTracker",
						executablePath);
			} else {
				String currentPath = registryHelper.read(
						"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run",
				        "ActivityTracker");
				if (currentPath != null && !currentPath.isEmpty() && currentPath.equals(executablePath)) {
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
