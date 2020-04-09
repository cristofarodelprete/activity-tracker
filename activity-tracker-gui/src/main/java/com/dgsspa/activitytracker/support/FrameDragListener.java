package com.dgsspa.activitytracker.support;

import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FrameDragListener extends MouseAdapter {

    private Point mouseDownCoords = null;
    
    private Window window;
    
    public FrameDragListener(Window window) {
    	this.window = window;
    }

    public void mouseReleased(MouseEvent e) {
        mouseDownCoords = null;
    }

    public void mousePressed(MouseEvent e) {
        mouseDownCoords = e.getPoint();
    }

    public void mouseDragged(MouseEvent e) {
        Point currCoords = e.getLocationOnScreen();
        window.setLocation(
        		currCoords.x - mouseDownCoords.x,
        		currCoords.y - mouseDownCoords.y);
    }
}
