package it.priestly.activitytracker.enums;

public enum PlatformKey {
	autorun(Boolean.class);
	
	private final Class<?> type;
	
	PlatformKey() {
		this.type = String.class;
	}
	
	PlatformKey(Class<?> type) {
		this.type = type;
	}
	
	public Class<?> type() {
		return type;
	}
}
