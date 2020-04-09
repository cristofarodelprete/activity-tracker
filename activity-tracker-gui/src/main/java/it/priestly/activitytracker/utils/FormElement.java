package it.priestly.activitytracker.utils;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class FormElement<T> {

	private Class<T> elementClass;
	
	private String label;
	
	private Supplier<T> getter;

	private Consumer<T> setter;
	
	public FormElement(Class<T> elementClass, String label, Supplier<T> getter, Consumer<T> setter) {
		this.label = label;
		this.getter = getter;
		this.setter = setter;
	}
	
	public Class<T> getElementClass() {
		return elementClass;
	}

	public String getLabel() {
		return label;
	}

	public T getValue() {
		return getter.get();
	}

	@SuppressWarnings("unchecked")
	public void setValue(Object value) {
		if (value == null || elementClass.isAssignableFrom(value.getClass())) {
			this.setter.accept((T)value);
		}
	}
}
