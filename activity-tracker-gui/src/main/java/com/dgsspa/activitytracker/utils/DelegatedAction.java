package com.dgsspa.activitytracker.utils;

import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import javax.swing.AbstractAction;

public class DelegatedAction extends AbstractAction {
	private static final long serialVersionUID = 3043152170090058649L;
	
	public Consumer<ActionEvent> delegate;
	
	public DelegatedAction(Consumer<ActionEvent> delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		delegate.accept(e);
	}
}
