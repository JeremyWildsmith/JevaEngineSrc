package dialogeditor;

import jeva.game.DialogPath.Answer;

public class AnswerNode extends Node
{
	private QueryNode m_nextDialog;

	public AnswerNode()
	{
		super("Default", -1);

		m_nextDialog = null;
	}

	public QueryNode getDialog()
	{
		return m_nextDialog;
	}

	public void setDialog(QueryNode next)
	{
		m_nextDialog = next;
	}

	@Override
	public String toString()
	{
		return "A: " + super.toString();
	}

	public Answer toJevaAnswer()
	{
		return new Answer(getText(), getEventCode(), (m_nextDialog == null ? null : m_nextDialog.toJevaQuery()));
	}
}
