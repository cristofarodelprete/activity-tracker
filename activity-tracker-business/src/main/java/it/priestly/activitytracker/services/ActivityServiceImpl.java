package it.priestly.activitytracker.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.priestly.activitytracker.converters.ActivityConverter;
import it.priestly.activitytracker.converters.AllocationConverter;
import it.priestly.activitytracker.objects.Activity;
import it.priestly.activitytracker.objects.Allocation;
import it.priestly.activitytracker.repositories.ActivityRepository;
import it.priestly.activitytracker.repositories.AllocationRepository;
import it.priestly.activitytracker.services.ActivityService;
import it.priestly.activitytracker.utils.AllocationPrinter;

@Service
public class ActivityServiceImpl implements ActivityService {

	@Autowired
	private ActivityRepository activityRepository;
	
	@Autowired
	private AllocationRepository allocationRepository;
	
	@Autowired
	private ActivityConverter activityConverter;

	@Autowired
	private AllocationConverter allocationConverter;
	
	@Autowired
	private AllocationPrinter allocationPrinter;
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean activityExists(String name) {
		return activityRepository.countByName(name).compareTo(0L) > 0;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<Activity> getActivities() {
		return activityConverter.from(activityRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Optional<Activity> getActivity(Long id) {
		return activityConverter.from(activityRepository.findById(id));
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Activity createActivity(Activity activity) {
		return activityConverter.from(activityRepository.saveAndFlush(activityConverter.to(activity)));
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean switchToActivity(Long id) {
		Optional<Activity> activity = activityConverter.from(activityRepository.findById(id));
		if (activity.isPresent()) {
			boolean active = activity.get().isActive();
			switchOff();
			if (!active) {
				Allocation allocation = new Allocation();
				allocation.setStart(LocalDateTime.now());
				allocation.setActivityId(id);
				allocationRepository.saveAndFlush(allocationConverter.to(allocation));
				return true;
			}	
		}
		return false;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void switchOff() {
		List<Allocation> toSwitchOff = allocationConverter.from(allocationRepository.findByEndIsNull());
		LocalDateTime now = LocalDateTime.now();
		for (Allocation allocation : toSwitchOff) {
			allocation.setEnd(now);
		}
		allocationRepository.saveAll(allocationConverter.to(toSwitchOff));
		allocationRepository.flush();
	}

	@Override
	public void deleteActivity(Long id) {
		activityRepository.deleteById(id);
	}

	@Override
	public void clearAll() {
		allocationRepository.deleteAll();
	}

	@Override
	public byte[] exportAll() {
		return allocationPrinter.print(allocationConverter.from(allocationRepository.findByEndIsNotNullOrderByStartDesc()));
	}
}
