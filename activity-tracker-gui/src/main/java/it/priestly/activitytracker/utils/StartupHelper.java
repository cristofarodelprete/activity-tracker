package it.priestly.activitytracker.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.priestly.activitytracker.enums.ConfigKey;
import it.priestly.activitytracker.windows.MainWindow;

@Component
public class StartupHelper {

	@Autowired
	private UpdateHelper updateHelper;
	
	@Autowired
	private ConfigurationHelper configurationHelper;
	
	@Autowired
	private MainWindow mainWindow;
	
	public void start() {
		boolean checkUpdates = configurationHelper.get(ConfigKey.checkUpdates);
		if (checkUpdates) {
			updateHelper.checkUpdates(mainWindow::run);
		} else {
			mainWindow.run();
		}
	}
}
