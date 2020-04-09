package com.dgsspa.activitytracker.animations;

import java.util.Optional;
import java.util.function.Function;

import javax.swing.JButton;

import com.dgsspa.activitytracker.objects.Activity;

public class UpdateButtonAnimation extends Animation<String> {
	
	private static final long updateTime = 1000;
	
	public UpdateButtonAnimation(JButton button, Activity activity, Function<Activity,String> buttonPrinter) {
		super(updateTime,
				() -> button.getText(),
				value -> button.setText(value),
				current -> Optional.of(buttonPrinter.apply(activity))
		);
	}

}
