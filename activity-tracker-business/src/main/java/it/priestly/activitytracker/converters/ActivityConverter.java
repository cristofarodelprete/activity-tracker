package it.priestly.activitytracker.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ActivityConverter extends EntityConverter<it.priestly.activitytracker.objects.Activity, it.priestly.activitytracker.models.Activity> {

	@Autowired
	AllocationConverter allocationConverter;
	
	public ActivityConverter() {
		super(it.priestly.activitytracker.objects.Activity.class, it.priestly.activitytracker.models.Activity.class);
	}

	@Override
	protected void convertFrom(it.priestly.activitytracker.objects.Activity src, it.priestly.activitytracker.models.Activity dst) {
		src.setId(dst.getId());
		src.setName(dst.getName());
		src.setAllocations(allocationConverter.from(dst.getAllocations()));
	}

	@Override
	protected void convertTo(it.priestly.activitytracker.objects.Activity src, it.priestly.activitytracker.models.Activity dst) {
		dst.setId(src.getId());
		dst.setName(src.getName());
		dst.setAllocations(allocationConverter.to(src.getAllocations()));
	}

}
