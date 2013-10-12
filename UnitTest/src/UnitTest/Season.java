package UnitTest;

public enum Season
{
	Winter("Winter"), Spring("Spring"), Summer("Summer"), Autumn("Autumn");

	private final String m_name;

	private Season(String name)
	{
		m_name = name;
	}

	public String getName()
	{
		return m_name;
	}
}
