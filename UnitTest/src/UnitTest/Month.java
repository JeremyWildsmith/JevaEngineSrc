package UnitTest;

import java.util.Calendar;
import java.util.Iterator;
import java.util.NoSuchElementException;

public enum Month
{
	January("January", Season.Winter, 1, 31), February("February", Season.Winter, 2, 28), March("March", Season.Spring, 3, 31), April("April", Season.Spring, 4, 30), May("May", Season.Spring, 5, 31), June("June", Season.Summer, 6, 30), July("July", Season.Summer, 7, 31), Augest("Augest", Season.Summer, 8, 31), September("September", Season.Autumn, 9, 30), October("October", Season.Autumn, 10, 31), November("November", Season.Autumn, 11, 30), December("December", Season.Autumn, 12, 31);

	private final int m_monthNumber;
	private final int m_daysOfMonth;
	private final String m_name;
	private final Season m_season;

	private Month(String name, Season season, int monthNumber, int daysOfMonth)
	{
		m_name = name;
		m_season = season;
		m_monthNumber = monthNumber;
		m_daysOfMonth = daysOfMonth;
	}

	public static Month getMonth(int number)
	{
		for (Month month : values())
		{
			if (month.m_monthNumber == number)
				return month;
		}

		throw new NoSuchElementException();
	}

	public static Month getCurrent()
	{
		return getMonth(Calendar.getInstance().get(Calendar.MONTH) + 1);
	}

	public String getName()
	{
		return m_name;
	}

	public Month getPreviousMonth()
	{
		if (m_monthNumber == 1)
			return getMonth(12);
		else
			return getMonth(m_monthNumber - 1);
	}

	public Month getNextMonth()
	{
		if (m_monthNumber == 12)
			return getMonth(1);
		else
			return getMonth(m_monthNumber + 1);
	}

	public Season getSeason()
	{
		return m_season;
	}

	public int getDays(int year)
	{
		return (year - 1) % 4 == 0 && this == February ? m_daysOfMonth + 1 : m_daysOfMonth;
	}

	public Day getFirstDay(int year)
	{
		int daysSinceStartOfYear = 0;

		for (int i = this.m_monthNumber - 1; i > 0; i--)
			daysSinceStartOfYear += getMonth(i).getDays(0);

		int days = ((year - 1) / 4) + daysSinceStartOfYear + 365 * (year - 1);

		return Day.values()[days % 7];
	}

	public Iterator<Day> getIterator(final int year)
	{
		return new Iterator<Day>()
		{
			private int m_currentDay = getFirstDay(year).ordinal();

			@Override
			public boolean hasNext()
			{
				return m_currentDay != m_daysOfMonth - 1;
			}

			@Override
			public Day next()
			{
				m_currentDay++;

				return Day.values()[m_currentDay % 7];
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}
}
