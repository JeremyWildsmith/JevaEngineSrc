package UnitTest;

public enum Day
{
	Saturday("Saturday"), Sunday("Sunday"), Monday("Monday"), Tuesday("Tuesday"), Wednesday("Wednesday"), Thursday("Thursday"), Friday("Friday");

	private final String m_name;

	private Day(String name)
	{
		m_name = name;
	}

	public String getName()
	{
		return m_name;
	}

	public Day getNextDay()
	{
		Day[] days = values();
		if (this == days[days.length - 1])
			return days[0];
		else
			return days[this.ordinal() + 1];
	}

	public Day getPreviousDay()
	{
		Day[] days = values();

		if (this == days[0])
			return days[days.length - 1];
		else
			return days[this.ordinal() + 1];
	}
}
