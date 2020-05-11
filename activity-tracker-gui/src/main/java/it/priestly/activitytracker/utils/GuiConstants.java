package it.priestly.activitytracker.utils;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice.WindowTranslucency;

public abstract class GuiConstants {
	public static final boolean transparencySupported;
	
	static {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		transparencySupported = gd.isWindowTranslucencySupported(WindowTranslucency.TRANSLUCENT);
	}
}
