package it.priestly.activitytracker.converters;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class AllocationConverter extends EntityConverter<it.priestly.activitytracker.objects.Allocation, it.priestly.activitytracker.models.Allocation> {

	public AllocationConverter() {
		super(it.priestly.activitytracker.objects.Allocation.class, it.priestly.activitytracker.models.Allocation.class);
	}

	@Override
	protected void convertFrom(it.priestly.activitytracker.objects.Allocation src, it.priestly.activitytracker.models.Allocation dst) {
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
	protected void convertTo(it.priestly.activitytracker.objects.Allocation src, it.priestly.activitytracker.models.Allocation dst) {
		dst.setId(src.getId());
		dst.setStart(Date.from(src.getStart().toInstant(ZoneOffset.UTC)));
		if (src.getEnd() != null)
			dst.setEnd(Date.from(src.getEnd().toInstant(ZoneOffset.UTC)));
		if (src.getActivityId() != null)
			dst.setActivity(new it.priestly.activitytracker.models.Activity(src.getActivityId()));
	}

}
