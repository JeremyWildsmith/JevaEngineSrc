/*******************************************************************************
 * 
 * Contributors:
 *		Jeremy - initial API and implementation
 *      Alan O'Brien - Changed entity visitor from interface to abstract class
 ******************************************************************************/

package io.github.jevaengine.rpgbase.netcommon;

import java.util.HashMap;
import java.util.Map;

import io.github.jevaengine.Core;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.VisitorValidationFailedException;
import io.github.jevaengine.world.Entity;

public class NetEntity
{
	public static abstract class EntityVisitor
	{
		public final void visit(Communicator sender, Entity entity, boolean onServer) throws InvalidMessageException
		{
			try{
				verify();	
			}
			catch (VisitorValidationFailedException visitorException) {
				throw new InvalidMessageException(sender, this, visitorException);	
			}
			doVisit(sender, entity, onServer);

		}
		protected abstract void doVisit(Communicator sender, Entity entity,boolean onServer);
		protected abstract void verify() throws VisitorValidationFailedException;
		public abstract boolean isServerDispatchOnly();
		public abstract boolean requiresOwnership();
	}
	
	//I feel like this is a bad idea...
	public static final class InitializeRequest
	{
		public InitializeRequest() {}
	}
	
	public static final class InitializeEntity
	{
		private String m_className;
		private String m_configuration;
		private NetEntityName m_name;
		private boolean m_isOwned;
		
		@SuppressWarnings("unused")
		// Used by Kryo
		private InitializeEntity() { }
		
		public InitializeEntity(String className, String configuration, String name, boolean isOwned)
		{
			m_className = className;
			m_configuration = configuration;
			m_name = new NetEntityName(name);
			m_isOwned = isOwned;
		}
		
		public boolean isOwned()
		{
			return m_isOwned;
		}
		
		public <T extends Entity> T create(Communicator sender, Class<T> entityClass, boolean onServer) throws InvalidMessageException
		{
			ResourceLibrary lib = Core.getService(ResourceLibrary.class);
			
			Class<? extends Entity> entity = lib.lookupEntity(m_className);
			
			if(!entity.equals(entityClass))
				throw new InvalidMessageException(sender, this, "Specified class name not translate to expected entity.");
			
			return (T)lib.createEntity(entityClass, m_name.get(onServer), m_configuration);
		}
	}
	
	public static final class FlagSet extends EntityVisitor
	{
		public String m_name;
		public Integer m_value;
		
		@SuppressWarnings("unused")
		// Used by Kryo
		private FlagSet() { }
		
		public FlagSet(String name, Integer value)
		{
			m_name = name;
			m_value = value;
		}

		@Override
		protected void doVisit(Communicator sender, Entity entity, boolean onServer)
		{
			entity.setFlag(m_name, m_value);
		}

		@Override
		public boolean isServerDispatchOnly()
		{
			return true;
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		protected void verify() throws VisitorValidationFailedException {
			
			if(m_name == null) {
				throw new VisitorValidationFailedException("m_name cannot be null");
				}
			if ( m_value == null){
				throw new VisitorValidationFailedException("m_value cannot be null");
			}
			
		}

	}
	
	public static final class FlagCleared extends EntityVisitor
	{
		public String m_name;
		
		@SuppressWarnings("unused")
		// Used by Kryo
		private FlagCleared() { }
		
		public FlagCleared(String name)
		{
			m_name = name;
		}

		@Override
		public void doVisit(Communicator sender, Entity entity, boolean onServer)
		{
			entity.clearFlag(m_name);
		}

		@Override
		public boolean isServerDispatchOnly()
		{
			return true;
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		protected void verify() throws VisitorValidationFailedException {
			
			if(m_name == null) {
				throw new VisitorValidationFailedException("m_name cannot be null");
				}
		}
	}
	
	public static final class InitializeFlags extends EntityVisitor
	{
		HashMap<String, Integer> m_flags;
		
		@SuppressWarnings("unused")
		// Used by Kryo
		private InitializeFlags() { }
		
		public InitializeFlags(Map<String, Integer> flags)
		{
			m_flags = new HashMap<String, Integer>(flags);
		}
		
		@Override
		public void doVisit(Communicator sender, Entity entity, boolean onServer)
		{
			entity.clearFlags();
			
			for(Map.Entry<String, Integer> f : m_flags.entrySet())
				entity.setFlag(f.getKey(), f.getValue());
		}

		@Override
		public boolean isServerDispatchOnly()
		{
			return true;
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		protected void verify() throws VisitorValidationFailedException {
			
			if(m_flags == null) {
				throw new VisitorValidationFailedException("m_flags cannot be null");
				}	
		}
	}
}
