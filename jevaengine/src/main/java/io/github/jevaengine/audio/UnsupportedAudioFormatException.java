package io.github.jevaengine.audio;

public class UnsupportedAudioFormatException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	public UnsupportedAudioFormatException(String format)
	{
		super("Audio format is not supported: " + format);
	}
}
