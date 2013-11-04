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
package io.github.jevaengine.rpgbase.netcommon;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.SharedEntity;
import io.github.jevaengine.config.Variable;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Actor;
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.World;


public abstract class NetWorld extends SharedEntity
{
	protected static class InitializationArguments
	{
		private String m_varStore;

		@SuppressWarnings("unused")
		// Used by Kryo
		private InitializationArguments()
		{
		}

		public InitializationArguments(String varStore)
		{
			m_varStore = varStore;
		}

		public String getStore()
		{
			return m_varStore;
		}
	}

	protected static enum PrimitiveQuery
	{
		Initialize,
	}

	protected interface IWorldVisitor
	{
		void visit(Communicator sender, World world) throws InvalidMessageException;

		boolean isServerOnly();
	}

	protected static class DialogEvent implements IWorldVisitor
	{
		private String m_entityName;
		private String m_subjectName;
		private int m_eventCode;

		@SuppressWarnings("unused")
		// Used by Kryo
		private DialogEvent()
		{
		}

		public DialogEvent(String entity, int eventId, @Nullable String subjectName)
		{
			m_entityName = entity;
			m_subjectName = subjectName;
			m_eventCode = eventId;
		}

		public DialogEvent(String entity, int eventId)
		{
			this(entity, eventId, null);
		}

		@Override
		public void visit(Communicator sender, World world) throws InvalidMessageException
		{
			if (!world.variableExists(m_entityName))
				throw new InvalidMessageException(sender, this, "Entity name is invalid");

			Variable dialogEntity = world.getVariable(m_entityName);

			if (!(dialogEntity instanceof Actor))
				throw new InvalidMessageException(sender, this, "Entity cannot posses dialog.");

			if (m_subjectName != null && (!world.variableExists(m_subjectName) || !(world.getVariable(m_subjectName) instanceof Actor)))
				throw new InvalidMessageException(sender, this, "Invalid subject name or type.");

			((Actor) dialogEntity).invokeDialogEvent(m_subjectName == null ? null : (Entity) world.getVariable(m_subjectName), m_eventCode);
		}

		@Override
		public boolean isServerOnly()
		{
			return false;
		}
	}
}
