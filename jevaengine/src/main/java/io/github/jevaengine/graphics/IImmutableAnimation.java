package io.github.jevaengine.graphics;

import io.github.jevaengine.graphics.Animation.IAnimationEventListener;
import io.github.jevaengine.util.Nullable;

public interface IImmutableAnimation
{
	void reset();
	void setState(AnimationState state);	
	void setState(AnimationState state, @Nullable IAnimationEventListener eventHandler);
	void update(int deltaTime);
	Frame getCurrentFrame();
}
