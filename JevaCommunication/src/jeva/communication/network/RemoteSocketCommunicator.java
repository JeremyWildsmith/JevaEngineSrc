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
package jeva.communication.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import jeva.IDisposable;
import jeva.communication.Snapshot;
import jeva.communication.UnboundCommunicatorException;
import jeva.communication.Communicator.RemoteCommunicator;
import jeva.communication.SnapshotSynchronizationException;

public class RemoteSocketCommunicator extends RemoteCommunicator implements IDisposable
{
	private Output m_out;

	private DataListener m_dataListener;

	private Socket m_remote;

	private Kryo m_kryo;

	private boolean m_isConnected = true;

	public RemoteSocketCommunicator(Socket remote) throws IOException
	{
		m_kryo = new Kryo(null);

		m_remote = remote;
		m_out = new Output(remote.getOutputStream());
		m_dataListener = new DataListener(remote.getInputStream());
	}

	private void disconnect()
	{
		if (!m_isConnected)
			return;

		m_isConnected = false;
		m_dataListener.end();

		try
		{
			if (!m_remote.isClosed())
				m_remote.close();

			m_out.close();
		} catch (IOException | KryoException e)
		{
			System.out.println(e);
		}
	}

	@Override
	public synchronized void remoteDestroyPair(long id) throws IOException
	{
		if (!m_isConnected)
			return;

		try
		{
			m_out.write(DataTransmitType.PairDestroyed.getId());
			m_kryo.writeObject(m_out, new PairDestroyed(id));
			m_out.flush();
		} catch (KryoException e)
		{
			System.out.println(e);
			disconnect();
		}
	}

	@Override
	public synchronized void remoteQueryPair(long id, String blassName) throws IOException
	{
		if (!m_isConnected)
			return;

		try
		{
			m_out.write(DataTransmitType.QueryPair.getId());
			m_kryo.writeObject(m_out, new QueryPair(id, blassName));
			m_out.flush();
		} catch (KryoException e)
		{
			System.out.println(e);
			disconnect();
		}
	}

	@Override
	public synchronized void remoteSnapshot(Snapshot snapshot) throws IOException
	{
		if (!m_isConnected)
			return;

		try
		{
			m_out.write(DataTransmitType.Snapshot.getId());
			m_kryo.writeObject(m_out, snapshot);
			m_out.flush();
		} catch (KryoException e)
		{
			System.out.println(e);
			disconnect();
		}
	}

	@Override
	public void dispose()
	{
		disconnect();
	}

	private class DataListener implements Runnable
	{
		private Input m_in;

		private Kryo m_kryo = new Kryo(null);

		private volatile boolean m_queryEnd;

		public DataListener(InputStream inputStream) throws IOException
		{
			m_in = new Input(inputStream);

			Thread t = new Thread(this);
			t.start();
		}

		public void run()
		{
			while (!m_queryEnd)
			{
				try
				{
					byte type = m_in.readByte();

					if (type > DataTransmitType.Max.getId())
						throw new ClassNotFoundException();

					switch (DataTransmitType.values()[type])
					{
					case PairDestroyed:
						RemoteSocketCommunicator.this.getListener().remoteDestroyPair(m_kryo.readObject(m_in, PairDestroyed.class).id);
						break;
					case QueryPair:
						QueryPair request = m_kryo.readObject(m_in, QueryPair.class);
						RemoteSocketCommunicator.this.getListener().remoteQueryPair(request.id, request.className);
						break;
					case Snapshot:
						RemoteSocketCommunicator.this.getListener().remoteSnapshot(m_kryo.readObject(m_in, Snapshot.class));
						break;
					default:
						throw new ClassNotFoundException();
					}

				} catch (KryoException | IOException | UnboundCommunicatorException e)
				{
					System.out.println(e);
					end();
				} catch (ClassNotFoundException | SnapshotSynchronizationException e)
				{
					System.out.println(e);
					end();
				}

			}
		}

		public void end()
		{
			try
			{
				m_in.close();
			} catch (KryoException e)
			{
			}

			m_queryEnd = true;
		}
	}

	private static class QueryPair
	{
		public long id;
		public String className;

		@SuppressWarnings("unused")
		// Used by Kryo
		private QueryPair()
		{
		}

		public QueryPair(long _id, String _className)
		{
			id = _id;
			className = _className;
		}
	}

	private static class PairDestroyed
	{
		public long id;

		@SuppressWarnings("unused")
		// Used by Kryo
		private PairDestroyed()
		{
		}

		public PairDestroyed(long _id)
		{
			id = _id;
		}
	}

	private static enum DataTransmitType
	{
		Snapshot((byte) 0), QueryPair((byte) 1), PairDestroyed((byte) 2), Max((byte) 2);

		private byte type;

		DataTransmitType(byte _type)
		{
			type = _type;
		}

		public byte getId()
		{
			return type;
		}
	}
}
