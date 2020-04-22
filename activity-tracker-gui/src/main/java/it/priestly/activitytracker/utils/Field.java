package it.priestly.activitytracker.utils;

import java.util.Map;

public class Field<T> {
	
	private Class<T> type;
	
	private String label;
	
	private Map<T, String> options;
	
	private T value;

	public Field(Class<T> type, String label) {
		this(type, label, null, null);
	}

	public Field(Class<T> type, String label, Map<T, String> options) {
		this(type, label, options, null);
	}

	public Field(Class<T> type, String label, T value) {
		this(type, label, null, value);
	}

	public Field(Class<T> type, String label, Map<T, String> options, T value) {
		this.type = type;
		this.label = label;
		this.options = options;
		this.value = value;
	}

	public Class<T> getType() {
		return type;
	}

	public String getLabel() {
		return label;
	}

	public Map<T, String> getOptions() {
		return options;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}
}
