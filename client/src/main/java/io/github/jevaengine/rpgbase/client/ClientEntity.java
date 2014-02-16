package io.github.jevaengine.rpgbase.client;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.SharedEntity;
import io.github.jevaengine.rpgbase.netcommon.NetEntity;
import io.github.jevaengine.rpgbase.netcommon.NetEntity.EntityVisitor;
import io.github.jevaengine.rpgbase.netcommon.NetEntity.InitializeEntity;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.IWorldAssociation;
import io.github.jevaengine.world.World;

public abstract class ClientEntity<T extends Entity> extends SharedEntity implements IWorldAssociation
{
	private static final int SYNC_INTERVAL = 50;
	
	private boolean m_dispatchedInit = false;
	
	private int m_tickCount = 0;
	
	private Class<T> m_class;

	private World m_world;
	
	private boolean m_isOwner = false;
	
	@Nullable
	private T m_entity;
	
	public ClientEntity(Class<T> clazz)
	{
		m_class = clazz;
	}
	
	public boolean isOwner()
	{
		return m_isOwner;
	}
	
	@Override
	public boolean isAssociated()
	{
		if (m_entity != null)
			return m_entity.isAssociated();
		else
			return false;
	}

	@Override
	public void disassociate()
	{
		if (m_entity != null && m_entity.isAssociated())
			m_entity.getWorld().removeEntity(m_entity);

		m_world = null;
	}

	@Override
	public void associate(World world)
	{
		m_world = world;

		if (m_entity != null)
			m_world.addEntity(m_entity);
	}
	
	@Nullable
	public final T getEntity()
	{
		return m_entity;
	}
	
	public final void update(int deltaTime)
	{
		m_tickCount += deltaTime;

		if (m_tickCount >= SYNC_INTERVAL)
		{
			m_tickCount = 0;
			snapshot();
		}
		if (!m_dispatchedInit)
		{
			m_dispatchedInit = true;
			send(new NetEntity.InitializeRequest());
		}
	}
	
	@Override
	protected final boolean onMessageRecieved(Communicator sender, Object recv) throws InvalidMessageException
	{
		if(recv instanceof EntityVisitor)
		{
			EntityVisitor visitor = (EntityVisitor)recv;
			
			beginVisit();
			visitor.visit(sender, getEntity(), false);
			endVisit();
		}else if(recv instanceof InitializeEntity)
		{
			InitializeEntity init = (InitializeEntity)recv;
			
			if(m_entity != null)
				throw new InvalidMessageException(sender, recv, "Attempted to initialize entity when it was already initialized.");

			
			m_isOwner = init.isOwned();
			
			m_entity = init.create(sender, m_class, false);

			if (m_world != null)
				m_world.addEntity(m_entity);
			
			entityCreated();
		}else
			throw new InvalidMessageException(sender, recv, "Unrecognized message");

		return true;
	}
	
	public abstract void beginVisit();
	public abstract void endVisit();
	
	public abstract void entityCreated();

}
