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
package jeva.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

import jeva.config.Variable;
import jeva.config.VariableStore;
import jeva.config.VariableValue;

/**
 * The Class DialogPath.
 */
public final class DialogPath
{

	/** The Constant EVENT_NONE. */
	public static final int EVENT_NONE = -1;

	/** The Constant NEXTNODE_NONE. */
	public static final int NEXTNODE_NONE = -1;

	/** The m_queries. */
	private Query[] m_queries;

	/**
	 * Instantiates a new dialog path.
	 * 
	 * @param queryNodes
	 *            the query nodes
	 */
	public DialogPath(Query[] queryNodes)
	{
		m_queries = queryNodes;
	}

	/**
	 * Creates the.
	 * 
	 * @param root
	 *            the root
	 * @return the dialog path
	 */
	public static DialogPath create(Variable root)
	{
		Variable[] textTable = root.getVariable("text").getVariableArray();
		Variable[] queryTable = root.getVariable("query").getVariableArray();

		ArrayList<RawQuery> primitiveQueries = new ArrayList<RawQuery>();

		// Construct raw model of dialog
		for (Variable vQuery : queryTable)
		{
			int queryText;
			int queryEvent = EVENT_NONE;

			VariableValue[] queryArguments = vQuery.getValue().getObjectArguments();

			if (queryArguments.length < 1)
				throw new ResourceLoadingException("Dialog query is missing sufficient arguments.");

			queryText = queryArguments[0].getInt();

			if (queryText >= textTable.length)
				throw new ResourceLoadingException("Query associates with text of index greater than that available on text table.");

			if (queryArguments.length > 1)
				queryEvent = queryArguments[1].getInt();

			ArrayList<RawAnswer> answers = new ArrayList<RawAnswer>();

			for (Variable vAnswer : vQuery.getVariable("answer").getVariableArray())
			{
				VariableValue[] answerArguments = vAnswer.getValue().getObjectArguments();

				if (answerArguments.length < 1)
					throw new ResourceLoadingException("Dialog answer is missing sufficient arguments.");

				int answerText = answerArguments[0].getInt();

				if (answerText >= textTable.length)
					throw new ResourceLoadingException("Query answer associates with text of index greater than that available on text table.");

				int nextNode = NEXTNODE_NONE;
				int event = EVENT_NONE;

				if (answerArguments.length > 1)
					nextNode = answerArguments[1].getInt();

				if (answerArguments.length > 2)
					event = answerArguments[2].getInt();

				answers.add(new RawAnswer(textTable[answerText].getValue().getString(), nextNode, event));
			}

			primitiveQueries.add(new RawQuery(textTable[queryText].getValue().getString(), answers.toArray(new RawAnswer[answers.size()]), queryEvent));
		}

		// Construct abstract model
		HashMap<Integer, Query> abstractQueries = new HashMap<Integer, Query>();

		for (int i = 0; i < primitiveQueries.size(); i++)
		{
			abstractQueries.put(i, new Query(i, primitiveQueries.get(i).getQuery(), primitiveQueries.get(i).getEventCode()));
		}

		ArrayList<Query> parentlessQueries = new ArrayList<Query>();
		parentlessQueries.addAll(abstractQueries.values());

		for (int queryIndex = 0; queryIndex < primitiveQueries.size(); queryIndex++)
		{
			RawAnswer[] answers = primitiveQueries.get(queryIndex).getAnswers();

			for (int answerIndex = 0; answerIndex < answers.length; answerIndex++)
			{
				Query nextDialog = (answers[answerIndex].getNextNode() >= 0 ? abstractQueries.get(answers[answerIndex].getNextNode()) : null);
				abstractQueries.get(queryIndex).addAnswer(new Answer(answers[answerIndex].getAnswer(), answers[answerIndex].getEventCode(), nextDialog));

				parentlessQueries.remove(nextDialog);

			}
		}

		return new DialogPath(parentlessQueries.toArray(new Query[parentlessQueries.size()]));
	}

	/**
	 * Adds the queries.
	 * 
	 * @param dest
	 *            the dest
	 * @param src
	 *            the src
	 */
	public void addQueries(ArrayList<Query> dest, Query src)
	{
		dest.add(src);
		for (Answer a : src.m_answers)
		{
			if (a.getNextQuery() != null && !dest.contains(a.getNextQuery()))
				addQueries(dest, a.getNextQuery());
		}
	}

	/**
	 * Serialize.
	 * 
	 * @param store
	 *            the store
	 */
	public void serialize(VariableStore store)
	{
		ArrayList<Query> queries = new ArrayList<Query>();
		ArrayList<String> textTable = new ArrayList<String>();

		ArrayList<Query> sortedQueries = new ArrayList<Query>();
		ArrayList<Query> outstandingQueries = new ArrayList<Query>();

		// It's not a elegant solution, and I don't like it
		// but I'm too tired to implement a proper compareTo in query...

		for (Query q : m_queries)
			addQueries(queries, q);

		for (int i = 0; i < queries.size(); i++)
			sortedQueries.add(null);

		for (Query query : queries)
		{
			if (query.m_id >= 0)
			{
				sortedQueries.set(query.m_id, query);
			} else
				outstandingQueries.add(query);
		}

		for (Query query : outstandingQueries)
		{
			for (int i = 0; i < queries.size(); i++)
			{
				if (sortedQueries.get(i) == null)
				{
					sortedQueries.set(i, query);
					break;
				}
			}
		}

		for (Query q : sortedQueries)
		{
			if (!textTable.contains(q.getQuery()))
				textTable.add(q.getQuery());

			int queryTextTableIndex = textTable.indexOf(q.getQuery());

			Variable var = store.setVariable("query/" + sortedQueries.indexOf(q), new VariableValue(new VariableValue(queryTextTableIndex), new VariableValue(q.getEventCode())));

			Answer[] answers = q.getAnswers();

			for (int i = 0; i < answers.length; i++)
			{
				if (!textTable.contains(answers[i].getAnswer()))
					textTable.add(answers[i].getAnswer());

				int answerTextTableIndex = textTable.indexOf(answers[i].getAnswer());

				var.setVariable("answer/" + i, new VariableValue(new VariableValue(answerTextTableIndex), new VariableValue(answers[i].getNextQuery() == null ? -1 : sortedQueries.indexOf(answers[i].getNextQuery())), new VariableValue(answers[i].getEventCode())));
			}
		}

		for (int i = 0; i < textTable.size(); i++)
			store.setVariable("text/" + i, new VariableValue(textTable.get(i)));
	}

	/**
	 * Gets the queries.
	 * 
	 * @return the queries
	 */
	public Query[] getQueries()
	{
		return m_queries;
	}

	/**
	 * Gets the query.
	 * 
	 * @param id
	 *            the id
	 * @return the query
	 */
	public Query getQuery(int id)
	{
		for (Query q : m_queries)
		{
			if (id == q.getId())
				return q;
		}

		throw new NoSuchElementException();
	}

	/**
	 * The Class Answer.
	 */
	public static final class Answer
	{

		/** The m_answer. */
		private String m_answer;

		/** The m_event code. */
		private int m_eventCode;

		/** The m_next query. */
		private Query m_nextQuery;

		/**
		 * Instantiates a new answer.
		 * 
		 * @param answer
		 *            the answer
		 * @param eventCode
		 *            the event code
		 * @param nextQuery
		 *            the next query
		 */
		public Answer(String answer, int eventCode, Query nextQuery)
		{
			m_answer = answer;
			m_eventCode = eventCode;
			m_nextQuery = nextQuery;
		}

		/**
		 * Gets the answer.
		 * 
		 * @return the answer
		 */
		public String getAnswer()
		{
			return m_answer;
		}

		/**
		 * Gets the event code.
		 * 
		 * @return the event code
		 */
		public int getEventCode()
		{
			return m_eventCode;
		}

		/**
		 * Gets the next query.
		 * 
		 * @return the next query
		 */
		public Query getNextQuery()
		{
			return m_nextQuery;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o)
		{
			if (!(o instanceof Answer))
				return false;

			Answer a = (Answer) o;

			return m_answer.compareTo(a.m_answer) == 0 && m_eventCode == a.m_eventCode && (m_nextQuery == null ? (a.m_nextQuery == null ? true : false) : (a.m_nextQuery == null ? false : a.m_nextQuery.equals(m_nextQuery)));
		}
	}

	/**
	 * The Class Query.
	 */
	public static final class Query
	{

		/** The m_query. */
		private String m_query;

		/** The m_event code. */
		private int m_eventCode;

		/** The m_answers. */
		private ArrayList<Answer> m_answers;

		/** The m_id. */
		private int m_id;

		/**
		 * Instantiates a new query.
		 * 
		 * @param id
		 *            the id
		 * @param query
		 *            the query
		 * @param eventCode
		 *            the event code
		 */
		public Query(int id, String query, int eventCode)
		{
			m_answers = new ArrayList<Answer>();

			m_query = query;
			m_eventCode = eventCode;
			m_id = id;
		}

		/**
		 * Adds the answer.
		 * 
		 * @param answer
		 *            the answer
		 */
		public void addAnswer(Answer answer)
		{
			m_answers.add(answer);
		}

		/**
		 * Gets the query.
		 * 
		 * @return the query
		 */
		public String getQuery()
		{
			return m_query;
		}

		/**
		 * Gets the event code.
		 * 
		 * @return the event code
		 */
		public int getEventCode()
		{
			return m_eventCode;
		}

		/**
		 * Gets the id.
		 * 
		 * @return the id
		 */
		public int getId()
		{
			return m_id;
		}

		/**
		 * Gets the answers.
		 * 
		 * @return the answers
		 */
		public Answer[] getAnswers()
		{
			return m_answers.toArray(new Answer[m_answers.size()]);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o)
		{
			if (!(o instanceof Query))
				return false;

			Query q = (Query) o;

			return m_query.compareTo(q.m_query) == 0 && m_id == q.getId() && m_answers.containsAll(q.m_answers);
		}
	}

	/**
	 * The Class RawAnswer.
	 */
	private static class RawAnswer
	{

		/** The m_answer. */
		private String m_answer;

		/** The m_next node. */
		private int m_nextNode;

		/** The m_event code. */
		private int m_eventCode;

		/**
		 * Instantiates a new raw answer.
		 * 
		 * @param answer
		 *            the answer
		 * @param nextNode
		 *            the next node
		 * @param eventCode
		 *            the event code
		 */
		public RawAnswer(String answer, int nextNode, int eventCode)
		{
			m_answer = answer;
			m_nextNode = nextNode;
			m_eventCode = eventCode;
		}

		/**
		 * Gets the answer.
		 * 
		 * @return the answer
		 */
		public String getAnswer()
		{
			return m_answer;
		}

		/**
		 * Gets the next node.
		 * 
		 * @return the next node
		 */
		public int getNextNode()
		{
			return m_nextNode;
		}

		/**
		 * Gets the event code.
		 * 
		 * @return the event code
		 */
		public int getEventCode()
		{
			return m_eventCode;
		}
	}

	/**
	 * The Class RawQuery.
	 */
	private static class RawQuery
	{
		/** The m_query. */
		private String m_query;

		/** The m_event code. */
		private int m_eventCode;

		/** The m_answers. */
		private RawAnswer[] m_answers;

		/**
		 * Instantiates a new raw query.
		 * 
		 * @param query
		 *            the query
		 * @param answers
		 *            the answers
		 * @param eventCode
		 *            the event code
		 */
		public RawQuery(String query, RawAnswer[] answers, int eventCode)
		{
			m_query = query;
			m_answers = answers;
			m_eventCode = eventCode;
		}

		/**
		 * Gets the query.
		 * 
		 * @return the query
		 */
		public String getQuery()
		{
			return m_query;
		}

		/**
		 * Gets the event code.
		 * 
		 * @return the event code
		 */
		public int getEventCode()
		{
			return m_eventCode;
		}

		/**
		 * Gets the answers.
		 * 
		 * @return the answers
		 */
		public RawAnswer[] getAnswers()
		{
			return m_answers;
		}
	}
}
