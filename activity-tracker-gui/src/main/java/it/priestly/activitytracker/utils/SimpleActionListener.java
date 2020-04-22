package it.priestly.activitytracker.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@FunctionalInterface
public interface SimpleActionListener extends ActionListener {

    void update(ActionEvent e);

    @Override
    default void actionPerformed(ActionEvent e) {
    	update(e);
    }
}