package it.priestly.activitytracker.utils;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@FunctionalInterface
public interface SimpleChangeListener extends ChangeListener {

    void update(ChangeEvent e);

    @Override
    default void stateChanged(ChangeEvent e) {
    	update(e);
    }
}