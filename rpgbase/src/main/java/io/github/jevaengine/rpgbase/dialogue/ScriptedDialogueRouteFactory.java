package io.github.jevaengine.rpgbase.dialogue;

import io.github.jevaengine.config.IConfigurationFactory;
import io.github.jevaengine.config.IConfigurationFactory.ConfigurationConstructionException;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.config.NoSuchChildVariableException;
import io.github.jevaengine.config.ValueSerializationException;
import io.github.jevaengine.rpgbase.dialogue.ScriptedDialogueRoute.ScriptedDialogueAnswer;
import io.github.jevaengine.rpgbase.dialogue.ScriptedDialogueRoute.ScriptedDialogueQuery;
import io.github.jevaengine.rpgbase.dialogue.ScriptedDialogueRouteFactory.ScriptedDialogueRouteDeclaration.ScriptedAnswerDeclaration;
import io.github.jevaengine.rpgbase.dialogue.ScriptedDialogueRouteFactory.ScriptedDialogueRouteDeclaration.ScriptedQueryDeclaration;
import io.github.jevaengine.script.IScriptFactory;
import io.github.jevaengine.util.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ScriptedDialogueRouteFactory implements IDialogueRouteFactory
{
	private final Logger m_logger = LoggerFactory.getLogger(ScriptedDialogueRouteFactory.class);
	
	private final IConfigurationFactory m_configurationFactory;
	private final IScriptFactory m_scriptFactory;
	
	@Inject
	public ScriptedDialogueRouteFactory(IConfigurationFactory configrationFactory, IScriptFactory scriptFactory)
	{
		m_configurationFactory = configrationFactory;
		m_scriptFactory = scriptFactory;
	}
	
	private ScriptedDialogueQuery parseQuery(Map<String, ScriptedDialogueQuery> labels, Map<ScriptedDialogueAnswer, List<String>> links, ScriptedQueryDeclaration q)
	{
		ArrayList<ScriptedDialogueAnswer> answers = new ArrayList<>();
			
		for(ScriptedAnswerDeclaration a : q.answers)
		{
			ScriptedDialogueAnswer answer = new ScriptedDialogueAnswer(a.answer, a.eval, a.exec);
			
			if(a.queries !=  null)
			{
				for(ScriptedQueryDeclaration queryDecl : a.queries)
					answer.addQuery(parseQuery(labels, links, queryDecl));
			}
			
			if(a.links != null)
				links.put(answer, Arrays.asList(a.links));
			
			answers.add(answer);
		}
		
		ScriptedDialogueQuery query = new ScriptedDialogueQuery(q.query, q.eval, answers.toArray(new ScriptedDialogueAnswer[answers.size()]));
		
		if(q.label != null)
			labels.put(q.label, query);
		
		return query;
	}
	
	@Override
	public IDialogueRoute create(String name) throws DialogueRouteConstructionException
	{
		try
		{
			ScriptedDialogueRouteDeclaration decl = m_configurationFactory.create(name).getValue(ScriptedDialogueRouteDeclaration.class);
	
			ArrayList<ScriptedDialogueQuery> rootQueries = new ArrayList<>();
			
			HashMap<String, ScriptedDialogueQuery> labels = new HashMap<>();
			HashMap<ScriptedDialogueAnswer, List<String>> links = new HashMap<>();
			
			for(ScriptedQueryDeclaration q : decl.entrys)
				rootQueries.add(parseQuery(labels, links, q));
			
			for(Map.Entry<ScriptedDialogueAnswer, List<String>> link : links.entrySet())
			{
				for(String s : link.getValue())
				{
					ScriptedDialogueQuery linkTo = labels.get(s);
					
					if(linkTo != null)
						link.getKey().addQuery(linkTo);
					else
						m_logger.error("Dialogue query answer was linked to non-existant label " + link.getValue() + ". Ignoring link.");
				}
			}
			
			return new ScriptedDialogueRoute(m_scriptFactory, decl.script, rootQueries.toArray(new ScriptedDialogueQuery[rootQueries.size()]));
		} catch (ValueSerializationException | ConfigurationConstructionException e)
		{
			throw new DialogueRouteConstructionException(name, e);
		}
	}

	public static final class ScriptedDialogueRouteDeclaration implements ISerializable
	{
		public ScriptedQueryDeclaration[] entrys;
		public String script;
		
		public ScriptedDialogueRouteDeclaration() { }

		@Override
		public void serialize(IVariable target) throws ValueSerializationException
		{
			target.addChild("entrys").setValue(entrys);
			
			if(script != null)
				target.addChild("script").setValue(script);
		}

		@Override
		public void deserialize(IImmutableVariable source) throws ValueSerializationException
		{
			try
			{
				entrys = source.getChild("entrys").getValues(ScriptedQueryDeclaration[].class);
				
				if(source.childExists("script"))
					script = source.getChild("script").getValue(String.class);
			} catch (NoSuchChildVariableException e)
			{
				throw new ValueSerializationException(e);
			}
		}
		
		public static final class ScriptedQueryDeclaration implements ISerializable
		{
			@Nullable
			public String label;
			
			public String query;
			
			@Nullable
			public String eval;
			
			public ScriptedAnswerDeclaration[] answers;

			public ScriptedQueryDeclaration() { }
			
			@Override
			public void serialize(IVariable target) throws ValueSerializationException
			{
				
				if(label != null)
					target.addChild("label").setValue(label);
				
				target.addChild("query").setValue(query);
				target.addChild("answers").setValue(answers);
				
				if(eval != null)
					target.addChild("entryCondition").setValue(eval);
			}

			@Override
			public void deserialize(IImmutableVariable source) throws ValueSerializationException
			{
				try
				{
					if(source.childExists("label"))
						label = source.getChild("label").getValue(String.class);
					
					query = source.getChild("query").getValue(String.class);
					answers = source.getChild("answers").getValues(ScriptedAnswerDeclaration[].class);
					
					if(source.childExists("eval"))
						eval = source.getChild("eval").getValue(String.class);
				} catch (NoSuchChildVariableException e)
				{
					throw new ValueSerializationException(e);
				}
			}
		}
		
		public static final class ScriptedAnswerDeclaration implements ISerializable
		{
			public String answer;

			@Nullable
			public String eval;
			
			@Nullable
			public String exec;
			
			@Nullable
			public ScriptedQueryDeclaration queries[];

			@Nullable
			public String links[];
			
			@Override
			public void serialize(IVariable target) throws ValueSerializationException
			{
				target.addChild("answer").setValue(answer);
				
				if(eval != null)
					target.addChild("eval").setValue(eval);
				
				if(exec != null)
					target.addChild("exec").setValue(exec);
				
				if(queries != null)
					target.addChild("queries").setValue(queries);
				
				if(links != null)
					target.addChild("links").setValue(links);
			}

			@Override
			public void deserialize(IImmutableVariable source) throws ValueSerializationException
			{
				try
				{
					answer = source.getChild("answer").getValue(String.class);
					
					if(source.childExists("eval"))
						eval = source.getChild("eval").getValue(String.class);
					
					if(source.childExists("exec"))
						exec = source.getChild("exec").getValue(String.class);
					
					if(source.childExists("queries"))
						queries = source.getChild("queries").getValues(ScriptedQueryDeclaration[].class);
					
					if(source.childExists("links"))
						links = source.getChild("links").getValues(String[].class);
					
				} catch (NoSuchChildVariableException e)
				{
					throw new ValueSerializationException(e);
				}
			}
		}
	}
	
}
