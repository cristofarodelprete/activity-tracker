package it.priestly.activitytracker.utils;

import org.springframework.stereotype.Component;

import it.priestly.activitytracker.objects.Allocation;

@Component
public class AllocationPrinter extends CsvPrinter<Allocation> {

	@Override
	protected String[] getColumns() {
		return new String[] {
				"Activity",
				"Start",
				"End"
		};
	}

	@Override
	protected Object[] getValues(Allocation item) {
		return new Object[] {
				item.getActivityName(),
				item.getStart(),
				item.getEnd()
		};
	}

}
