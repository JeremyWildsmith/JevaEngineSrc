package dialogeditor;

public class Node
{
	private String m_text;
	private int m_eventCode;

	public Node(String text, int eventCode)
	{
		m_text = text;
		m_eventCode = -1;
		;

	}

	public String getText()
	{
		return m_text;
	}

	public int getEventCode()
	{
		return m_eventCode;
	}

	public void setText(String text)
	{
		m_text = text;
	}

	public void setEvent(int event)
	{
		m_eventCode = event;
	}

	@Override
	public String toString()
	{
		return m_text.substring(0, Math.min(m_text.length(), 50)) + (m_text.length() >= 50 ? "..." : "");
	}
}
