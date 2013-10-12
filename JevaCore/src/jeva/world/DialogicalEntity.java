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
package jeva.world;

import javax.script.ScriptException;

import com.sun.istack.internal.Nullable;

import proguard.annotation.KeepPublicClassMemberNames;
import jeva.Core;
import jeva.CoreScriptException;
import jeva.IResourceLibrary;
import jeva.config.Variable;
import jeva.config.VariableStore;
import jeva.game.DialogPath;
import jeva.game.DialogPath.Query;
import jeva.util.StaticSet;

/**
 * The Class DialogicalEntity.
 */
public abstract class DialogicalEntity extends Entity
{

	/** The m_dialog. */
	private DialogPath m_dialog;

	/** The m_observers. */
	private Observers m_observers = new Observers();

	/**
	 * Instantiates a new dialogical entity.
	 */
	public DialogicalEntity()
	{
		super();
	}

	/**
	 * Instantiates a new dialogical entity.
	 * 
	 * @param name
	 *            the name
	 */
	public DialogicalEntity(String name)
	{
		super(name);
	}

	/**
	 * Adds the observer.
	 * 
	 * @param observer
	 *            the observer
	 */
	public void addObserver(IDialogueObserver observer)
	{
		m_observers.add(observer);
		super.addObserver(observer);
	}

	/**
	 * Removes the observer.
	 * 
	 * @param observer
	 *            the observer
	 */
	public void removeObserver(IDialogueObserver observer)
	{
		m_observers.remove(observer);
		super.removeObserver(observer);
	}

	/**
	 * Instantiates a new dialogical entity.
	 * 
	 * @param <Y>
	 *            the generic type
	 * @param <T>
	 *            the generic type
	 * @param name
	 *            the name
	 * @param root
	 *            the root
	 * @param entityContext
	 *            the entity context
	 */
	public <Y extends DialogicalEntity, T extends DialogicalBridge<Y>> DialogicalEntity(@Nullable String name, Variable root, T entityContext)
	{
		super(name, root, entityContext);

		if (root.variableExists("dialog"))
		{
			m_dialog = DialogPath.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream(root.getVariable("dialog").getValue().getString())));
		} else
			m_dialog = null;
	}

	/**
	 * Invoke dialog event.
	 * 
	 * @param subject
	 *            the subject
	 * @param event
	 *            the event
	 * @return the int
	 */
	public final int invokeDialogEvent(@Nullable Entity subject, int event)
	{
		try
		{

			m_observers.onDialogEvent(subject, event);

			Object oReturn = getScript().invokeScriptFunction("onDialogEvent", subject, event);

			if (oReturn instanceof Double)
				return ((Double) oReturn).intValue();
			else if (oReturn instanceof Integer)
				return ((Integer) oReturn).intValue();
			else
				return -1;
		} catch (ScriptException e)
		{
			throw new CoreScriptException("Unable to invoke entity onDialogEvent routine." + e.getMessage());
		} catch (NoSuchMethodException e)
		{
			return -1;
		}
	}

	/**
	 * The Class DialogicalBridge.
	 * 
	 * @param <A>
	 *            the generic type
	 */
	@KeepPublicClassMemberNames
	public static class DialogicalBridge<A extends DialogicalEntity> extends EntityBridge<A>
	{

		/**
		 * Dialog.
		 * 
		 * @param subject
		 *            the subject
		 * @param dialogId
		 *            the dialog id
		 */
		public final void dialog(@Nullable final EntityBridge<Entity> subject, final int dialogId)
		{
			final DialogPath dialog = ((DialogicalEntity) getMe()).m_dialog;

			if (dialog == null)
				throw new CoreScriptException("Entity attempting to initiate dialog without defining a dialog path.");

			getMe().addTask(new DialogTask(subject.getMe())
			{
				@Override
				public Query onEvent(Entity subject, int eventCode)
				{
					int dialogOverride = getMe().invokeDialogEvent(subject, eventCode);

					if (dialogOverride >= 0)
						return dialog.getQuery(dialogOverride);
					else
						return null;
				}

				@Override
				public void onDialogEnd()
				{
				}

				@Override
				public Query getEntryDialog()
				{
					return dialog.getQuery(dialogId);
				}
			});
		}
	}

	/**
	 * An asynchronous update interface for receiving notifications about
	 * IDialogue information as the IDialogue is constructed.
	 */
	public interface IDialogueObserver extends IEntityObserver
	{

		/**
		 * This method is called when information about an IDialogue which was
		 * previously requested using an asynchronous interface becomes
		 * available.
		 * 
		 * @param subject
		 *            the subject
		 * @param event
		 *            the event
		 */
		void onDialogEvent(Entity subject, int event);
	}

	/**
	 * The Class Observers.
	 */
	private static class Observers extends StaticSet<IDialogueObserver>
	{

		/**
		 * On dialog event.
		 * 
		 * @param subject
		 *            the subject
		 * @param event
		 *            the event
		 */
		public void onDialogEvent(Entity subject, int event)
		{
			for (IDialogueObserver observer : this)
				observer.onDialogEvent(subject, event);
		}
	}
}
