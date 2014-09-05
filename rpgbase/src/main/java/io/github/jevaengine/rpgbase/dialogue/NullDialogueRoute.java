package io.github.jevaengine.rpgbase.dialogue;

import io.github.jevaengine.rpgbase.dialogue.DialogueSession.DialogueQuery;
import io.github.jevaengine.rpgbase.dialogue.DialogueSession.IDialogueSessionController;
import io.github.jevaengine.world.entity.DefaultEntity;

public final class NullDialogueRoute implements IDialogueRoute
{

	@Override
	public DialogueSession begin(DefaultEntity speaker, DefaultEntity listener) {
		return new DialogueSession(new IDialogueSessionController() {
			
			@Override
			public boolean parseAnswer(String answer)
			{
				return false;
			}
			
			@Override
			public DialogueQuery getCurrentQuery()
			{
				return null;
			}
			
			@Override
			public void end() { }
		});
	}

}
