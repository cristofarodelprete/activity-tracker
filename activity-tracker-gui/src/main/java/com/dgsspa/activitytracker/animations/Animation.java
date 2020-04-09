package com.dgsspa.activitytracker.animations;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Animation<T> implements Runnable {
	
	private long interval;
	
	private boolean stopped = false;
	
	private Supplier<T> getter;
	
	private Consumer<T> setter;
	
	private Function<T,Optional<T>> step;
	
	public Animation(long interval, Supplier<T> getter, Consumer<T> setter, Function<T,Optional<T>> step) {
		this.interval = interval;
		this.getter = getter;
		this.setter = setter;
		this.step = step;
	}
	
	@Override
	public void run() {
		do {
			if (stopped) break;
			T current = getter.get();
			Optional<T> next = step.apply(current);
			if (next.isPresent()) {
				setter.accept(next.get());
				try {
					Thread.sleep(interval);
				} catch (Exception e) {
					break;
				}
			} else {
				break;
			}
		} while (true);
	}

	public void stop() {
		stopped = true;
	}
	
}
