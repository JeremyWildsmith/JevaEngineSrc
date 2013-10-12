package jeva.world;

/**
 * The Class SearchForTask.
 * 
 * @param <T>
 *            the generic type
 */
public abstract class SearchForTask<T extends Entity> implements ITask
{

	/** The Constant SEARCH_INTERVAL. */
	private static final int SEARCH_INTERVAL = 600;

	/** The m_searcher. */
	Actor m_searcher;

	/** The m_seeking class. */
	private Class<T> m_seekingClass;

	/** The m_next search. */
	private int m_nextSearch;

	/** The m_is query cancel. */
	private boolean m_isQueryCancel;

	/** The m_search entity name. */
	private String m_searchEntityName;

	/**
	 * Instantiates a new search for task.
	 * 
	 * @param searcher
	 *            the searcher
	 * @param seekingClass
	 *            the seeking class
	 */
	public SearchForTask(Actor searcher, Class<T> seekingClass)
	{
		m_searcher = searcher;
		m_seekingClass = seekingClass;
		m_isQueryCancel = false;
		m_nextSearch = 0;
		m_searchEntityName = null;
	}

	/**
	 * Instantiates a new search for task.
	 * 
	 * @param searcher
	 *            the searcher
	 * @param seekingClass
	 *            the seeking class
	 * @param name
	 *            the name
	 */
	public SearchForTask(Actor searcher, Class<T> seekingClass, String name)
	{
		m_searcher = searcher;
		m_seekingClass = seekingClass;
		m_isQueryCancel = false;
		m_nextSearch = 0;
		m_searchEntityName = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#cancel()
	 */
	@Override
	public void cancel()
	{
		m_isQueryCancel = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#doCycle(int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean doCycle(int deltaTime)
	{
		if (m_isQueryCancel)
			return true;

		m_nextSearch -= deltaTime;

		if (m_nextSearch <= 0)
		{
			m_nextSearch = SEARCH_INTERVAL;

			boolean foundEntity = false;

			for (Entity e : m_searcher.getVisibleEntities())
			{
				if (m_seekingClass.isAssignableFrom(e.getClass()) && (m_searchEntityName == null ? true : e.getName().compareTo(m_searchEntityName) == 0))
				{
					foundEntity = true;
					if (found((T) e))
						break;
				}
			}

			if (!foundEntity)
				nothingFound();
		}

		return !continueSearch();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#isParallel()
	 */
	@Override
	public boolean isParallel()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#ignoresPause()
	 */
	@Override
	public boolean ignoresPause()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#begin(jeva.world.Entity)
	 */
	@Override
	public void begin(Entity entity)
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#end()
	 */
	@Override
	public void end()
	{

	}

	/**
	 * Found.
	 * 
	 * @param entity
	 *            the entity
	 * @return true, if successful
	 */
	public abstract boolean found(T entity);

	/**
	 * Nothing found.
	 */
	public abstract void nothingFound();

	/**
	 * Continue search.
	 * 
	 * @return true, if successful
	 */
	public abstract boolean continueSearch();

}
