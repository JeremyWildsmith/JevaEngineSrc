/*******************************************************************************
 * Copyright (c) 2013 Jeremy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * If you'd like to obtain a another license to this code, you may contact Jeremy to discuss alternative redistribution options.
 * 
 * Contributors:
 *     Jeremy - initial API and implementation
 ******************************************************************************/
package io.github.jevaengine.world;

import io.github.jevaengine.audio.Audio;

public class PlayAudioTask implements ITask
{

	private boolean m_isBlocking;

	private Audio m_audio;

	public PlayAudioTask(Audio audio, boolean block)
	{
		m_isBlocking = block;
		m_audio = audio;

		audio.play();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ITask#begin(jeva.world.Entity)
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
	 * @see io.github.jeremywildsmith.jevaengine.world.ITask#doCycle(int)
	 */
	@Override
	public boolean doCycle(int deltaTime)
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ITask#isParallel()
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
	 * @see io.github.jeremywildsmith.jevaengine.world.ITask#ignoresPause()
	 */
	@Override
	public boolean ignoresPause()
	{
		return true;
	}
}
