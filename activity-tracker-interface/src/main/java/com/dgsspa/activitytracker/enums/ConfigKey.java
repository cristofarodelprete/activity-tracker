package com.dgsspa.activitytracker.enums;

public enum ConfigKey {
	enableTransparency(Boolean.class),
	hiddenOpacity(Float.class),
	fadeDuration(Integer.class),
	language;
	
	private final Class<?> type;
	
	ConfigKey() {
		this.type = String.class;
	}
	
	ConfigKey(Class<?> type) {
		this.type = type;
	}
	
	public Class<?> type() {
		return type;
	}
}
