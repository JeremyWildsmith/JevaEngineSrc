package io.github.jevaengine.world.entity;

import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.script.ScriptExecuteException;
import io.github.jevaengine.script.ScriptHiddenMember;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.physics.IPhysicsBody;
import io.github.jevaengine.world.scene.model.IImmutableSceneModel;

import java.util.Map;

public interface IEntity
{
	/*
	 * World association methods.
	 */
	@Nullable
	World getWorld();
	void associate(World world);
	void disassociate();
	
	/*
	 * Entity property methods
	 */
	String getInstanceName();
	Map<String, Integer> getFlags();
	int getFlag(String name);
	boolean testFlag(String name, int value);
	boolean isFlagSet(String name);
	boolean isStatic();
	
	/*
	 * Returns the entity's scene model.
	 */
	IImmutableSceneModel getModel();
	
	/*
	 * Physics body accessor
	 */
	IPhysicsBody getBody();
	
	/*
	 * Observer methods
	 */
	void addObserver(IEntityObserver o);
	void removeObserver(IEntityObserver o);
	
	/*
	 * Entity bridge, used as interface to entity via scripts.
	 */
	IEntityBridge getBridge();

	/*
	 * Logic routine.
	 */
	void update(int delta);
	
	public interface IEntityObserver
	{
		void enterWorld();
		void leaveWorld();
		void flagSet(String name, int value);
		void flagCleared(String name);
	}
	
	public interface IEntityBridge
	{
		@ScriptHiddenMember
		IEntity getEntity();
		
		String getName();
		Vector3F getLocation();
		
		boolean isFlagSet(String name);
		int getFlag(String name);
		
		Object invokeInterface(String interfaceName, Object ... arguments) throws NoSuchInterfaceException, InterfaceExecuteException;
		
		public static final class NoSuchInterfaceException extends Exception
		{
			private static final long serialVersionUID = 1L;

			public NoSuchInterfaceException(String interfaceName)
			{
				super("No interface by the name of " + interfaceName + " exists.");
			}
		}
		
		public static final class InterfaceExecuteException extends Exception
		{
			private static final long serialVersionUID = 1L;

			public InterfaceExecuteException(String interfaceName, ScriptExecuteException e)
			{
				super("Error occured invoknig interface " + interfaceName, e);
			}
		}
	}
	
	public final class PrimitiveEntityBridge implements IEntityBridge
	{
		private final IEntity m_host;
		
		public PrimitiveEntityBridge(IEntity host)
		{
			m_host = host;
		}

		@Override
		public IEntity getEntity()
		{
			return m_host;
		}

		@Override
		public String getName()
		{
			return m_host.getInstanceName();
		}

		@Override
		public Vector3F getLocation()
		{
			return m_host.getBody().getLocation();
		}

		@Override
		public boolean isFlagSet(String name)
		{
			return m_host.isFlagSet(name);
		}

		@Override
		public int getFlag(String name)
		{
			return m_host.getFlag(name);
		}

		@Override
		public Object invokeInterface(String interfaceName, Object... arguments) throws NoSuchInterfaceException, InterfaceExecuteException
		{
			throw new NoSuchInterfaceException(interfaceName);
		}
	}
}
