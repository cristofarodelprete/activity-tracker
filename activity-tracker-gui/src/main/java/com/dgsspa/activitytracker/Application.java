package com.dgsspa.activitytracker;

import java.awt.EventQueue;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import com.dgsspa.activitytracker.windows.MainWindow;

@SpringBootApplication
public class Application {
	
	public static void main(String[] args) {
		ApplicationContext context = new SpringApplicationBuilder(Application.class)
				.headless(false).run(args);
		EventQueue.invokeLater(() -> {
			context.getBean(MainWindow.class);
		});
	}
}
