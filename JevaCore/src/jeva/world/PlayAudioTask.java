package jeva.world;

import jeva.audio.Audio;

/**
 * The Class PlayAudioTask.
 */
public class PlayAudioTask implements ITask
{

	/** The m_is blocking. */
	private boolean m_isBlocking;

	/** The m_audio. */
	private Audio m_audio;

	/**
	 * Instantiates a new play audio task.
	 * 
	 * @param audio
	 *            the audio
	 * @param block
	 *            the block
	 */
	public PlayAudioTask(Audio audio, boolean block)
	{
		m_isBlocking = block;
		m_audio = audio;

		audio.play();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#begin(jeva.world.Entity)
	 */
	@Override
	public void begin(Entity entity)
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#end()
	 */
	@Override
	public void end()
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#cancel()
	 */
	@Override
	public void cancel()
	{
		m_audio.stop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#doCycle(int)
	 */
	@Override
	public boolean doCycle(int deltaTime)
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#isParallel()
	 */
	@Override
	public boolean isParallel()
	{
		if (!m_isBlocking)
			return true;

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#ignoresPause()
	 */
	@Override
	public boolean ignoresPause()
	{
		return true;
	}
}
