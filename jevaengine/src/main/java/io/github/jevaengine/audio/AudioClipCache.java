package io.github.jevaengine.audio;

import io.github.jevaengine.IDisposable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioClipCache implements IDisposable
{
	private ArrayList<SoftReference<Clip>> m_clips = new ArrayList<SoftReference<Clip>>();

	private ArrayList<Clip> m_busyClips = new ArrayList<Clip>();

	private ByteBufferAdapter m_clipStream;

	private final String m_clipName;

	private final ReferenceQueue<Clip> m_clipCleanupQueue;
	
	public AudioClipCache(String name, byte[] source, ReferenceQueue<Clip> clipCleanupQueue)
	{
		m_clipName = name;
		m_clipCleanupQueue = clipCleanupQueue;

		m_clipStream = new ByteBufferAdapter(ByteBuffer.wrap(source));
	}

	@Override
	public void dispose()
	{
		try
		{
			m_clipStream.close();
		} catch (IOException e)
		{
			// This should never happen
			throw new RuntimeException(e);
		}
	}

	public Clip getClip() throws IOException, UnsupportedAudioFileException, UnsupportedAudioFormatException, LineUnavailableException
	{
		Clip clip = null;

		ArrayList<SoftReference<Clip>> garbageClips = new ArrayList<SoftReference<Clip>>();

		for (SoftReference<Clip> entry : m_clips)
		{
			if (entry.get() == null)
				garbageClips.add(entry);
			else if (!m_busyClips.contains(entry.get()))
				clip = entry.get();
		}

		m_clips.removeAll(garbageClips);

		if (clip == null)
		{
			m_clipStream.reset();
			try (AudioInputStream ais = AudioSystem.getAudioInputStream(m_clipStream))
			{
				AudioFormat baseFormat = ais.getFormat();
				
				AudioFormat[] supportedTargets = AudioSystem.getTargetFormats(AudioFormat.Encoding.PCM_SIGNED, baseFormat);
				
				if(supportedTargets.length == 0)
					throw new UnsupportedAudioFormatException(baseFormat.toString());
				
				clip = AudioSystem.getClip();
				try (AudioInputStream targetAis = AudioSystem.getAudioInputStream(supportedTargets[0], ais))
				{
					clip.open(targetAis);
				}
			}

			m_clips.add(new SoftReference<Clip>(clip, m_clipCleanupQueue));
		}

		m_busyClips.add(clip);

		return clip;
	}

	public void freeClip(Clip clip)
	{
		m_busyClips.remove(clip);
	}

	public void cleanupCache()
	{
		for (SoftReference<Clip> clip : m_clips)
		{
			if (clip.get() == null)
				m_clips.remove(clip);
		}
	}

	public String getName()
	{
		return m_clipName;
	}

	public boolean isEmpty()
	{
		return m_clips.isEmpty();
	}

	private static class ByteBufferAdapter extends InputStream
	{
		private ByteBuffer m_buffer;

		public ByteBufferAdapter(ByteBuffer buffer)
		{
			m_buffer = buffer;
		}

		@Override
		public synchronized void reset()
		{
			m_buffer.rewind();
		}

		@Override
		public int read() throws IOException
		{
			if (!m_buffer.hasRemaining())
				return -1;

			// And with 0xFF to mask just one byte of data from the read
			// operation.
			return m_buffer.get() & 0xFF;
		}

		public int read(byte[] bytes, int off, int len) throws IOException
		{
			if (!m_buffer.hasRemaining())
				return -1;

			len = Math.min(len, m_buffer.remaining());
			m_buffer.get(bytes, off, len);
			return len;
		}
	}	
}
