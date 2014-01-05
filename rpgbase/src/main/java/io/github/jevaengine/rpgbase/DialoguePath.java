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
package io.github.jevaengine.rpgbase;

import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.util.Nullable;

/**
 *
 * @author Jeremy
 */
public class DialoguePath implements ISerializable
{
	public Query[] queries;
	
	public DialoguePath() { }

	@Override
	public void serialize(IVariable target)
	{
		target.addChild("queries").setValue(queries);
	}

	@Override
	public void deserialize(IVariable source)
	{
		queries = source.getChild("queries").getValues(Query[].class);
	}
	
	public static class Query implements ISerializable
	{
		public String query;
		
		@Nullable
		public String entryCondition;
		
		public Answer[] answers;

		public Query() { }
		
		@Override
		public void serialize(IVariable target)
		{
			target.addChild("query").setValue(query);
			target.addChild("answers").setValue(answers);
			
			if(entryCondition != null)
				target.addChild("entryCondition").setValue(entryCondition);
		}

		@Override
		public void deserialize(IVariable source)
		{
			query = source.getChild("query").getValue(String.class);
			answers = source.getChild("answers").getValues(Answer[].class);
			
			if(source.childExists("entryCondition"))
				entryCondition = source.getChild("entryCondition").getValue(String.class);
		}
	}
	
	public static class Answer implements ISerializable
	{
		public String answer;
		
		@Nullable
		public String condition;
		
		public int next;
		public int event;

		public Answer() { }

		@Override
		public void serialize(IVariable target)
		{
			target.addChild("answer").setValue(answer);
			
			if(next >= 0)
				target.addChild("next").setValue(next);
			
			if(event >= 0)
				target.addChild("event").setValue(event);
			
			if(condition != null)
				target.addChild("condition").setValue(condition);
		}

		@Override
		public void deserialize(IVariable source)
		{
			answer = source.getChild("answer").getValue(String.class);
			
			next = -1;
			event = -1;
			
			if(source.childExists("next"))
				next = source.getChild("next").getValue(Integer.class);
			
			if(source.childExists("event"))
				event = source.getChild("event").getValue(Integer.class);
			
			if(source.childExists("condition"))
				condition = source.getChild("condition").getValue(String.class);
		}
	}
}
