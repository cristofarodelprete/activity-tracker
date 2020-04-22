package it.priestly.activitytracker.enums;

import java.math.BigDecimal;
import java.math.BigInteger;

public enum ConfigKey {
	enableTransparency(Boolean.class),
	hiddenOpacity(BigDecimal.class),
	fadeDuration(BigInteger.class),
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
