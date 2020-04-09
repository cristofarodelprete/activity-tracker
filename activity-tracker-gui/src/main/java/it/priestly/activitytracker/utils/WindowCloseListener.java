package it.priestly.activitytracker.utils;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

public class WindowCloseListener extends WindowAdapter {
	
	private Consumer<WindowEvent> delegate;
	
	public WindowCloseListener(Consumer<WindowEvent> delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public void windowClosing(WindowEvent e) {
		delegate.accept(e);
	}
}
