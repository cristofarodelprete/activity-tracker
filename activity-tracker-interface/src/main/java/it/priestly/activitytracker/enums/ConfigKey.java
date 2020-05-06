package it.priestly.activitytracker.enums;

public enum ConfigKey {
	checkUpdates(Boolean.class),
	enableTransparency(Boolean.class),
	hiddenOpacity(Float.class),
	fadeDuration(Integer.class),
	language,
	alwaysOnTop(Boolean.class);
	
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
