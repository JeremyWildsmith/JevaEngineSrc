package jeva.audio;

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
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.sun.istack.internal.Nullable;

import jeva.Core;
import jeva.IDisposable;
import jeva.IResourceLibrary;

/**
 * The Class Audio.
 */
public final class Audio
{

	/** A list of all allocated clip caches. */
	private static final ArrayList<ClipCache> m_clipCaches = new ArrayList<ClipCache>();

	/** A thread responsible for cleaning up dereferenced clips. */
	private static final ClipCleanup m_cleanup = new ClipCleanup();

	/**
	 * The audio line listener assigned to this instance's clip to determine
	 * when it is and is not is use.
	 */
	private AudioLineListener m_lineListener = new AudioLineListener();

	/** The name of this instance's corresponding audio resource. */
	private String m_clipName;

	/**
	 * A reference to this Audio's working clip, assigned when it is using the
	 * clip.
	 */
	@Nullable
	private Clip m_clip;

	/** The clip cache from which the clip was allocated from. */
	@Nullable
	private ClipCache m_clipOwner;

	/**
	 * Instantiates a new audio clip to play audio with.
	 * 
	 * @param name
	 *            The name of the audio resource to initialize with.
	 */
	public Audio(String name)
	{
		String formal = name.replace("\\", "/").trim().toLowerCase();

		if (formal.startsWith("/"))
			formal = formal.substring(1);

		m_clipName = formal;
		m_clip = null;
	}

	/**
	 * Cleans up the cache, searching empty caches and disposing of them.
	 */
	private static void cleanupCache()
	{
		synchronized (m_clipCaches)
		{
			ArrayList<ClipCache> garbageCaches = new ArrayList<ClipCache>();

			for (ClipCache cache : m_clipCaches)
			{
				cache.cleanupCache();

				if (cache.isEmpty())
				{
					cache.dispose();
					garbageCaches.add(cache);
				}
			}

			m_clipCaches.removeAll(garbageCaches);
		}
	}

	/**
	 * Loads a Clip instance initialized with the appropriate data from the
	 * source resource specified when this instance was constructed. The clip is
	 * lazily loaded if it does not exist in the clip cache. It is softly
	 * referenced by the clip cache while not in use.
	 * 
	 * @return A clip instnace initialized with the appropriate audio specified
	 *         by the audio source.
	 */
	private synchronized Clip getClip()
	{
		if (m_clip != null)
			return m_clip;

		synchronized (m_clipCaches)
		{
			for (ClipCache cache : m_clipCaches)
			{
				if (cache.getName().equals(m_clipName))
				{
					m_clipOwner = cache;
					m_clip = cache.getClip();
					m_clip.addLineListener(m_lineListener);
					return m_clip;
				}
			}
		}

		// If we get here there is no clip-cache under m_clipName.
		ClipCache cache = new ClipCache(m_clipName);
		m_clipOwner = cache;
		m_clipCaches.add(cache);

		m_clip = cache.getClip();

		m_clip.addLineListener(m_lineListener);

		return m_clip;
	}

	/**
	 * Frees the clip from this audio instance and returns it to its source
	 * cache where it may be allocated for later use.
	 */
	private synchronized void freeClip()
	{
		m_clip.removeLineListener(m_lineListener);

		m_clipOwner.freeClip(m_clip);
		m_clipOwner = null;
		m_clip = null;
	}

	/**
	 * Precaches the audio clip. This method forces the clip to be loaded into
	 * the cache even if it is not being used;
	 */
	public void precache()
	{
		getClip();
		freeClip();
	}

	/**
	 * Begins the audio from the start and plays until it finishes.
	 */
	public void play()
	{
		getClip().setFramePosition(0);
		getClip().start();
	}

	/**
	 * Stops playing the audio.
	 */
	public void stop()
	{
		getClip().stop();
	}

	/**
	 * Begins the audio from the start and continues to play in a loop.
	 */
	public void repeat()
	{
		getClip().setFramePosition(0);
		getClip().loop(Clip.LOOP_CONTINUOUSLY);
	}

	/**
	 * The listener interface for receiving audioLine events. The class that is
	 * interested in processing a audioLine event implements this interface, and
	 * the object created with that class is registered with a component using
	 * the component's <code>addAudioLineListener<code> method. When
	 * the audioLine event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see AudioLineEvent
	 */
	private class AudioLineListener implements LineListener
	{
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.sound.sampled.LineListener#update(javax.sound.sampled.LineEvent
		 * )
		 */
		@Override
		public void update(LineEvent event)
		{
			if (event.getType() == LineEvent.Type.STOP)
				freeClip();
		}
	}

	/**
	 * A class which contains cached clips and instantiates clips for one audio
	 * resource. There is one ClipCache per every audio resource.
	 */
	private static class ClipCache implements IDisposable
	{
		/** A list of all clips maintained by this cache. */
		private ArrayList<SoftReference<Clip>> m_clips = new ArrayList<SoftReference<Clip>>();

		/** A list of clips maintained by this cache which are also in use. */
		private ArrayList<Clip> m_busyClips = new ArrayList<Clip>();

		/**
		 * A buffer containing the raw data of the stream in memory allowing new
		 * clips to be promptly instantiated.
		 */
		private ByteBufferAdapter m_clipStream;

		/** The name\path of the clip resource. */
		private String m_clipPath;

		/**
		 * Instantiates a new clip cache for the specified audio resource.
		 * 
		 * @param path
		 *            The path\name of the audio resource for which to create
		 *            this cache.
		 */
		public ClipCache(String path)
		{
			m_clipPath = path;

			m_clipStream = new ByteBufferAdapter(Core.getService(IResourceLibrary.class).openResourceRaw(path));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jeva.IDisposable#dispose()
		 */
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

		/**
		 * Allocates a clip from this cache, if one is not available is is
		 * created. The clip is also place in to a busy state.
		 * 
		 * @return A newly allocated clip from the cache.
		 */
		public Clip getClip()
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
				try
				{
					m_clipStream.reset();

					AudioInputStream ais = AudioSystem.getAudioInputStream(m_clipStream);

					AudioFormat baseFormat = ais.getFormat();

					// Specify desired Audio Line format to find compatible line
					AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);

					clip = AudioSystem.getClip();

					clip.open(AudioSystem.getAudioInputStream(targetFormat, ais));

					ais.close();

					m_clips.add(new SoftReference<Clip>(clip, m_cleanup.getCleanupQueue()));

				} catch (UnsupportedAudioFileException e)
				{
					throw new AudioException("IO Error when loading " + m_clipPath + " " + e.toString());
				} catch (IOException e)
				{
					throw new AudioException("Unsupported Audio Format when loading " + m_clipPath + ", " + e.getMessage());
				} catch (LineUnavailableException e)
				{
					throw new AudioException("No accessible line: " + e.toString());
				}
			}

			m_busyClips.add(clip);

			return clip;
		}

		/**
		 * Frees a specified clip so that is may be used later. The Clip is no
		 * longer considered busy after it has been freed.
		 * 
		 * @param clip
		 *            the clip
		 */
		public void freeClip(Clip clip)
		{
			m_busyClips.remove(clip);
		}

		/**
		 * Locates garbage collected softly referenced clips in the clip cache
		 * and removes from the list of clips maintained by the cache.
		 */
		public void cleanupCache()
		{
			for (SoftReference<Clip> clip : m_clips)
			{
				if (clip.get() == null)
					m_clips.remove(clip);
			}
		}

		/**
		 * Returns the name of the audio\resource this cache contains clips for.
		 * 
		 * @return The name of the audio\resource this cache contains clips for.
		 */
		public String getName()
		{
			return m_clipPath;
		}

		/**
		 * Checks if the clip cache is empty and maintains no clips in its
		 * cache.
		 * 
		 * @return True, if the cache is empty of items cached, or false
		 *         otherwise.
		 */
		public boolean isEmpty()
		{
			return m_clips.isEmpty();
		}

		/**
		 * An adapter used to adapt a back-end ByteBuffer to an InputStream so
		 * that it can be used to initialize an AudioInputStream.
		 */
		private static class ByteBufferAdapter extends InputStream
		{

			/** The byte buffer from which the inputstream will read. */
			private ByteBuffer m_buffer;

			/**
			 * Instantiates a new byte buffer adapter.
			 * 
			 * @param buffer
			 *            The byte buffer to be adapted to an input stream.
			 */
			public ByteBufferAdapter(ByteBuffer buffer)
			{
				m_buffer = buffer;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.io.InputStream#reset()
			 */
			@Override
			public void reset()
			{
				m_buffer.rewind();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.io.InputStream#read()
			 */
			@Override
			public int read() throws IOException
			{
				if (!m_buffer.hasRemaining())
					return -1;

				// And with 0xFF to mask just one byte of data from the read
				// operation.
				return m_buffer.get() & 0xFF;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.io.InputStream#read(byte[], int, int)
			 */
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

	/**
	 * The Class ClipCleanup.
	 */
	private static class ClipCleanup extends Thread
	{

		/** The m_reference queue. */
		private final ReferenceQueue<Clip> m_referenceQueue = new ReferenceQueue<Clip>();

		/**
		 * Instantiates a new clip cleanup.
		 */
		public ClipCleanup()
		{
			this.setDaemon(true);
			this.start();
		}

		/**
		 * Gets the cleanup queue.
		 * 
		 * @return the cleanup queue
		 */
		public ReferenceQueue<Clip> getCleanupQueue()
		{
			return m_referenceQueue;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					m_referenceQueue.remove().get().close();
					cleanupCache();
				} catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
			}
		}
	}
}
