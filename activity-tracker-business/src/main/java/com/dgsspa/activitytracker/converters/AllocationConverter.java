package com.dgsspa.activitytracker.converters;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class AllocationConverter extends EntityConverter<com.dgsspa.activitytracker.objects.Allocation, com.dgsspa.activitytracker.models.Allocation> {

	public AllocationConverter() {
		super(com.dgsspa.activitytracker.objects.Allocation.class, com.dgsspa.activitytracker.models.Allocation.class);
	}

	@Override
	protected void convertFrom(com.dgsspa.activitytracker.objects.Allocation src, com.dgsspa.activitytracker.models.Allocation dst) {
		src.setId(dst.getId());
		src.setStart(dst.getStart().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
		if (dst.getEnd() != null)
			src.setEnd(dst.getEnd().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime());
		if (dst.getActivity() != null) {
			src.setActivityId(dst.getActivity().getId());
			src.setActivityName(dst.getActivity().getName());
		}
	}

	@Override
	protected void convertTo(com.dgsspa.activitytracker.objects.Allocation src, com.dgsspa.activitytracker.models.Allocation dst) {
		dst.setId(src.getId());
		dst.setStart(Date.from(src.getStart().toInstant(ZoneOffset.UTC)));
		if (src.getEnd() != null)
			dst.setEnd(Date.from(src.getEnd().toInstant(ZoneOffset.UTC)));
		if (src.getActivityId() != null)
			dst.setActivity(new com.dgsspa.activitytracker.models.Activity(src.getActivityId()));
	}

}
