package it.priestly.activitytracker.utils;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import org.springframework.stereotype.Component;

import java.awt.GraphicsDevice.WindowTranslucency;

@Component
public class GuiUtils {
	public boolean isTransparencySupported() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		return gd.isWindowTranslucencySupported(WindowTranslucency.TRANSLUCENT);
	}
}
