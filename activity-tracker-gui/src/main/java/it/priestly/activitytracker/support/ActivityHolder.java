package it.priestly.activitytracker.support;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.priestly.activitytracker.objects.Activity;

public class ActivityHolder {
	private List<Activity> activities;
	
	private Activity activity;

	public ActivityHolder(List<Activity> activities, Activity activity) {
		this.activities = activities;
		this.activity = activity;
	}

	public boolean isActive() {
		return activity.isActive();
	}
	
	public String getName() {
		return activity.getName();
	}
	
	public int[] getTime() {
		Duration duration = activity.getTotalAllocatedTime();
		List<Integer> time = new ArrayList<Integer>();
		if (!duration.isZero() && !duration.isNegative()) {
			long tmp = duration.getSeconds();
			time.add((int)(tmp % 60));
			tmp /= 60;
			if (tmp > 0) {
				time.add((int)(tmp % 60));
				tmp /= 60;
				if (tmp > 0) {
					time.add((int)tmp);
				}
			}
		}
		Collections.reverse(time);
		return time.stream().mapToInt(i->i).toArray();
	}
	
	public float getPercentage() {
		Duration duration = activity.getTotalAllocatedTime();
		Duration others = activities.stream()
				.filter(a -> a != activity).map(a -> a.getTotalAllocatedTime())
				.reduce(Duration.ZERO, (a, d) -> a.plus(d));
		return (float)duration.getSeconds() / (float)(duration.getSeconds() + others.getSeconds());
	}
}
