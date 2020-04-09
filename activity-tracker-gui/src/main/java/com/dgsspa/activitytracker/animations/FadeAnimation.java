package com.dgsspa.activitytracker.animations;

import java.awt.Window;
import java.util.Optional;

public class FadeAnimation extends Animation<Float> {
	
	private static final long deltaTransition = 20;

	public enum Direction {
		FadeIn,
		FadeOut
	}
	
	private FadeAnimation(Window window, float hiddenOpacity, float transitionDuration, float transitionStep, Direction direction) {
		super(deltaTransition,
				() -> window.getOpacity(),
				value -> window.setOpacity(value),
				direction == Direction.FadeIn ?
						value -> value < 1f ?
								Optional.of(Math.min(value + transitionStep, 1f)) :
								Optional.empty() :
						value -> value > hiddenOpacity ?
								Optional.of(Math.max(value - transitionStep, hiddenOpacity)) :
								Optional.empty());
	}
	
	public FadeAnimation(Window window, float hiddenOpacity, float transitionDuration, Direction direction) {
		this(window, hiddenOpacity, transitionDuration, (1f - hiddenOpacity) * deltaTransition / transitionDuration, direction);
	}

}
