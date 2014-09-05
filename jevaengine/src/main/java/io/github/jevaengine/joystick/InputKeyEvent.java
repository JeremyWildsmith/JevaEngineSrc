package io.github.jevaengine.joystick;

public class InputKeyEvent implements IInputEvent
{

	public enum KeyEventType
	{
		KeyTyped,
		KeyDown,
		KeyUp,
	}

	public InputKeyEvent.KeyEventType type;
	public int keyCode;
	public char keyChar;

	protected InputKeyEvent(InputKeyEvent.KeyEventType _type, int _keyCode, char _keyChar)
	{
		type = _type;
		keyCode = _keyCode;
		keyChar = _keyChar;
	}

	public void relay(IInputSourceProcessor handler)
	{
		switch (type)
		{
			case KeyTyped:
				handler.keyTyped(this);
				break;
			case KeyUp:
				handler.keyUp(this);
				break;
			case KeyDown:
				handler.keyDown(this);
				break;
		}
	}
}