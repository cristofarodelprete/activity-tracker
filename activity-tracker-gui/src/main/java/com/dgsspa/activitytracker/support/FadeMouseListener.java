package com.dgsspa.activitytracker.support;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import com.dgsspa.activitytracker.animations.Animation;
import com.dgsspa.activitytracker.animations.FadeAnimation;

public class FadeMouseListener extends MouseAdapter {

	private Animation<?> animation = null;
	
	private Window window;

	private float hiddenOpacity;

	private float transitionDuration;

    public FadeMouseListener(Window window, float hiddenOpacity, float transitionDuration) {
    	this.window = window;
    	this.hiddenOpacity = hiddenOpacity;
    	this.transitionDuration = transitionDuration;
    }

	public void mouseExited(MouseEvent me) {
        Component c = SwingUtilities.getDeepestComponentAt(
           me.getComponent(), me.getX(), me.getY());
        if(c == null || !SwingUtilities.isDescendingFrom(c, window)) {
        	if (animation != null) animation.stop();
    		animation = new FadeAnimation(window, hiddenOpacity, transitionDuration, FadeAnimation.Direction.FadeOut);
    		new Thread(animation).start();
        }
	}

	public void mouseEntered(MouseEvent me) {
		if (animation != null) animation.stop();
		animation = new FadeAnimation(window, hiddenOpacity, transitionDuration, FadeAnimation.Direction.FadeIn);
		new Thread(animation).start();
	}
}