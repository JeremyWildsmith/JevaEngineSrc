package jeva.communication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public final class Snapshot implements KryoSerializable
{
	private ArrayList<FieldSnapshot> m_enroutedFields;
	private ArrayList<MessageSnapshot> m_enroutedMessages;

	public Snapshot()
	{
		m_enroutedFields = new ArrayList<FieldSnapshot>();
		m_enroutedMessages = new ArrayList<MessageSnapshot>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(Kryo kryo, Input input)
	{
		try
		{
			byte[] inputBuffer = kryo.readObject(input, byte[].class);

			ByteArrayInputStream bis = new ByteArrayInputStream(inputBuffer);

			Input in = new Input(new GZIPInputStream(bis));

			m_enroutedFields = (ArrayList<FieldSnapshot>) kryo.readObject(in, ArrayList.class);
			m_enroutedMessages = (ArrayList<MessageSnapshot>) kryo.readObject(in, ArrayList.class);

			in.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void write(Kryo kryo, Output output)
	{
		try
		{
			ByteArrayOutputStream compressedOut = new ByteArrayOutputStream();
			GZIPOutputStream gop = new GZIPOutputStream(compressedOut);

			Output kryoOut = new Output(gop);

			kryo.writeObject(kryoOut, m_enroutedFields);
			kryo.writeObject(kryoOut, m_enroutedMessages);

			kryoOut.close();

			kryo.writeObject(output, compressedOut.toByteArray());

		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	protected void clear()
	{
		m_enroutedFields.clear();
		m_enroutedMessages.clear();
	}

	protected void enqueueMessage(EntityId target, Object message)
	{
		m_enroutedMessages.add(new MessageSnapshot(target, message));
	}

	protected void snapshotField(EntityId target, int fieldId, Object value)
	{
		m_enroutedFields.add(new FieldSnapshot(target, fieldId, value));
	}

	protected MessageSnapshot[] getMessages()
	{
		return m_enroutedMessages.toArray(new MessageSnapshot[m_enroutedMessages.size()]);
	}

	protected FieldSnapshot[] getFields()
	{
		return m_enroutedFields.toArray(new FieldSnapshot[m_enroutedFields.size()]);
	}

	public boolean isEmpty()
	{
		return m_enroutedFields.isEmpty() && m_enroutedMessages.isEmpty();
	}

	protected static class MessageSnapshot
	{
		EntityId m_sender;
		Object m_message;

		@SuppressWarnings("unused")
		// Used by Kryo
		private MessageSnapshot()
		{
		}

		public MessageSnapshot(EntityId sender, Object message)
		{
			m_sender = sender;
			m_message = message;
		}

		public EntityId getSender()
		{
			return m_sender;
		}

		public Object getMessage()
		{
			return m_message;
		}
	}

	protected static class FieldSnapshot
	{
		private EntityId m_sender;
		private int m_fieldId;

		private Object m_value;

		@SuppressWarnings("unused")
		// Used by Kryo
		private FieldSnapshot()
		{
		}

		public FieldSnapshot(EntityId sender, int fieldId, Object value)
		{
			m_sender = sender;
			m_fieldId = fieldId;
			m_value = value;
		}

		public EntityId getSender()
		{
			return m_sender;
		}

		public int getFieldId()
		{
			return m_fieldId;
		}

		public Object getValue()
		{
			return m_value;
		}

		@Override
		public boolean equals(Object o)
		{
			if (!(o instanceof FieldSnapshot))
				return false;

			FieldSnapshot id = (FieldSnapshot) o;

			return id.m_sender.equals(m_sender) && id.m_fieldId == m_fieldId;
		}
	}
}
