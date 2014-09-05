package io.github.jevaengine.world.entity;

import io.github.jevaengine.world.scene.model.ISceneModel;

public interface IEntityModelMapperFactory
{
	IEntityModelMapping createMapping(ISceneModel model, IEntity subject) throws UnsupportedSubjectException;
	
	public static final class UnsupportedSubjectException extends Exception
	{
		private static final long serialVersionUID = 1L;	
	}
}
