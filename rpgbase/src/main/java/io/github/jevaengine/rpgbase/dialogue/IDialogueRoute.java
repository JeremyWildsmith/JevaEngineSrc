package io.github.jevaengine.rpgbase.dialogue;

import io.github.jevaengine.world.entity.DefaultEntity;

public interface IDialogueRoute
{
	DialogueSession begin(DefaultEntity speaker, DefaultEntity listener);
}
