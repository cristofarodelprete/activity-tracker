package it.priestly.activitytracker;

import java.awt.EventQueue;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import it.priestly.activitytracker.utils.UpdateHelper;
import it.priestly.activitytracker.windows.MainWindow;

@SpringBootApplication
public class Application {
	
	public static void main(String[] args) {
		ApplicationContext context = new SpringApplicationBuilder(Application.class)
				.headless(false).run(args);
		EventQueue.invokeLater(() -> {
			UpdateHelper updateHelper = context.getBean(UpdateHelper.class);
			MainWindow mainWindow = context.getBean(MainWindow.class);
			String version = Application.class.getPackage().getImplementationVersion();
			updateHelper.checkUpdates(version, () -> {
				mainWindow.run();
			});
		});
	}
}
