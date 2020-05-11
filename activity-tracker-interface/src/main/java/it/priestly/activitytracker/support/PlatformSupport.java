package it.priestly.activitytracker.support;

import it.priestly.activitytracker.enums.PlatformKey;

public interface PlatformSupport<T> {
	public Class<T> getType();
	
	public boolean isSupported();
	
	public PlatformKey getKey();
	
	public T get();
	
	public void set(T value);
}
