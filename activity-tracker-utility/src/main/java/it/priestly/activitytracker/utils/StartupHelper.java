package it.priestly.activitytracker.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.priestly.activitytracker.enums.ConfigKey;
import it.priestly.activitytracker.support.UiHelper;

@Component
public class StartupHelper {

	@Autowired
	private ApplicationUpdater applicationUpdater;
	
	@Autowired
	private ConfigurationHelper configurationHelper;

	@Autowired
	private UiHelper uiHelper;
	
	public void start() {
		Boolean checkUpdates = configurationHelper.get(ConfigKey.checkUpdates);
		if (checkUpdates != null && checkUpdates) {
			applicationUpdater.checkUpdates(true);
		}
		uiHelper.run();
	}
}
