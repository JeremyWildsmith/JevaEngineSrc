package io.github.jevaengine.rpgbase.dialogue;

import io.github.jevaengine.AssetConstructionException;

public interface IDialogueRouteFactory
{
	IDialogueRoute create(String name) throws DialogueRouteConstructionException;
	
	public static final class DialogueRouteConstructionException extends AssetConstructionException
	{
		private static final long serialVersionUID = 1L;

		public DialogueRouteConstructionException(String assetName,
				Exception cause) {
			super(assetName, cause);
		}
		
	}
}
