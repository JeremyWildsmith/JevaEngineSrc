package io.github.jevaengine.rpgbase.client.ui;

import java.awt.Color;

import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.rpgbase.DialogueController;
import io.github.jevaengine.rpgbase.DialogueController.IDialogueControlObserver;
import io.github.jevaengine.ui.TextArea;
import io.github.jevaengine.ui.TextArea.DisplayEffect;
import io.github.jevaengine.ui.UIStyle;
import io.github.jevaengine.ui.Window;

public final class Dialogue extends Window implements IDialogueControlObserver
{
	private DialogueController m_controller;
	private TextArea m_speakerText;
	
	public Dialogue(UIStyle style)
	{
		super(style, 400, 400);
		
		m_speakerText = new TextArea(Color.yellow, 380, 210);
		m_speakerText.setEffect(DisplayEffect.Typewriter);
		
		addControl(m_speakerText, new Vector2D(5,5));
	}
	
	public void watch(DialogueController controller)
	{
		m_controller = controller;
		controller.addObserver(this);
	}
	
	public void unwatch()
	{
		if(m_controller != null)
		{
			m_controller.removeObserver(this);
			m_controller = null;
		}
	}
	
	@Override
	public void beginDialogue()
	{
		setVisible(true);
	}

	@Override
	public void endDialogue()
	{
		setVisible(false);
	}

	@Override
	public void dialogueEvent(int event) { }

	@Override
	public void speakerSaid(String message)
	{
		m_speakerText.setText(message);
	}

	@Override
	public void listenerSaid(String message) { }

}
