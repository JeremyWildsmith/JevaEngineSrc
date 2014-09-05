package io.github.jevaengine.world.scene.model;

import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.math.Rect3F;

import java.util.List;

public interface IImmutableSceneModel extends Cloneable
{
	ISceneModel clone();
	
	List<ISceneModelComponent> getComponents();

	Rect3F getAABB();
	
	void addObserver(ISceneModelObserver o);
	void removeObserver(ISceneModelObserver o);
	
	void update(int delta);
	
	public interface ISceneModelComponent extends IRenderable
	{
		boolean testPick(int x, int y, float scale);
		Rect3F getBounds();
	}
	
	public interface ISceneModelObserver
	{
		void onEvent(String name);
	}
	
	public interface ISceneModelAnimation
	{
		void setState(SceneModelAnimationState mode);
		SceneModelAnimationState getState();
		
		public enum SceneModelAnimationState
		{
			Play,
			PlayWrap,
			Stop,
			PlayToEnd,
		}
	}
	
	public static final class NoSuchAnimationException extends Exception
	{
		private static final long serialVersionUID = 1L;
	
		public NoSuchAnimationException(String animationName)
		{
			super("Animation by the name of " + animationName + " does not exist in this model.");
		}
	}
	
	public static final class NullSceneModelAnimation implements ISceneModelAnimation
	{

		@Override
		public void setState(SceneModelAnimationState mode) { }

		@Override
		public SceneModelAnimationState getState()
		{
			return SceneModelAnimationState.Stop;
		}
		
	}
}
