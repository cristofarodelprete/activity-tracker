package com.dgsspa.activitytracker.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ActivityConverter extends EntityConverter<com.dgsspa.activitytracker.objects.Activity, com.dgsspa.activitytracker.models.Activity> {

	@Autowired
	AllocationConverter allocationConverter;
	
	public ActivityConverter() {
		super(com.dgsspa.activitytracker.objects.Activity.class, com.dgsspa.activitytracker.models.Activity.class);
	}

	@Override
	protected void convertFrom(com.dgsspa.activitytracker.objects.Activity src, com.dgsspa.activitytracker.models.Activity dst) {
		src.setId(dst.getId());
		src.setName(dst.getName());
		src.setAllocations(allocationConverter.from(dst.getAllocations()));
	}

	@Override
	protected void convertTo(com.dgsspa.activitytracker.objects.Activity src, com.dgsspa.activitytracker.models.Activity dst) {
		dst.setId(src.getId());
		dst.setName(src.getName());
		dst.setAllocations(allocationConverter.to(src.getAllocations()));
	}

}
