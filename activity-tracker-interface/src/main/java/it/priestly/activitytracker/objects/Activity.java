package it.priestly.activitytracker.objects;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class Activity {
	
	private Long id;
	
	private String name;
	
	private List<Allocation> allocations;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Allocation> getAllocations() {
		return allocations;
	}

	public void setAllocations(List<Allocation> allocations) {
		this.allocations = allocations;
	}

	public boolean isActive() {
		return allocations.stream().anyMatch(a -> a.getEnd() == null);
	}
	
	public Duration getTotalAllocatedTime() {
		return allocations.stream()
				.map(a -> Duration.between(
						a.getStart(),
						a.getEnd() != null ? a.getEnd() : LocalDateTime.now()
				)).reduce(Duration.ZERO, (a, b) -> a.plus(b));
	}
}
