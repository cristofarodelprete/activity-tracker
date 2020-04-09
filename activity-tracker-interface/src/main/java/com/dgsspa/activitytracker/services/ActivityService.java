package com.dgsspa.activitytracker.services;

import java.util.List;
import java.util.Optional;

import com.dgsspa.activitytracker.objects.Activity;

public interface ActivityService {

	boolean activityExists(String name);
	
	List<Activity> getActivities();
	
	Optional<Activity> getActivity(Long id);

	Activity createActivity(Activity activity);
	
	boolean switchToActivity(Long id);
	
	void switchOff();

	void deleteActivity(Long id);

	void clearAll();

	byte[] exportAll();
}
