package it.priestly.activitytracker.animations;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.JButton;

import it.priestly.activitytracker.objects.Activity;

public class UpdateButtonAnimation extends Animation<Map<JButton,Activity>> {
	
	private static final long updateTime = 1000;
	
	private Map<JButton,Activity> activityMap;
	
	public UpdateButtonAnimation(Map<JButton,Activity> activityMap, Consumer<Map<JButton,Activity>> buttonPrinter) {
		super(updateTime,
				() -> activityMap,
				value -> { buttonPrinter.accept(activityMap); },
				current -> Optional.of(current)
		);
		this.activityMap = activityMap;
	}

	public long countActive() {
		return activityMap.values().stream().filter(a -> a.isActive()).count();
	}
}
