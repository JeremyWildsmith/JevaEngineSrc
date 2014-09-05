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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.jevaengine.ui;

import io.github.jevaengine.graphics.IFont;
import io.github.jevaengine.graphics.IImmutableGraphic;
import io.github.jevaengine.graphics.NullFont;
import io.github.jevaengine.graphics.NullGraphic;
import io.github.jevaengine.joystick.InputKeyEvent;
import io.github.jevaengine.joystick.InputMouseEvent;
import io.github.jevaengine.joystick.InputMouseEvent.MouseEventType;
import io.github.jevaengine.joystick.InputMouseEvent.MouseButton;
import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.math.Vector2D;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class TextArea extends Control
{
	public static final String COMPONENT_NAME = "textArea";
	
	private static final int PADDING = 3;
	
	private String m_workingText;
	
	private IImmutableGraphic m_frame;
	private IFont m_font = new NullFont();

	private boolean m_allowEdit;

	private int m_cursorLocation = 0;
	private int m_blinkTimeout = 0;

	private TextLayout m_textLayout;
	
	private boolean m_isWordWrapped = true;
	
	private final int m_width;
	private final int m_height;
	
	public TextArea(String text, int width, int height)
	{
		super(COMPONENT_NAME);
		
		m_width = width;
		m_height = height;
		
		m_allowEdit = true;
		m_workingText = text;
		m_frame = new NullGraphic(width, height);
		m_textLayout = new TextLayout(text, m_font, m_frame.getBounds(), 0);
	}
	
	public TextArea(String instanceName, String text, int width, int height)
	{
		super(COMPONENT_NAME, instanceName);
		
		m_width = width;
		m_height = height;
		
		m_allowEdit = true;
		m_workingText = text;
		m_frame = new NullGraphic(width, height);
		m_textLayout = new TextLayout(text, m_font, m_frame.getBounds(), 0);
	}

	public TextArea(int width, int height)
	{
		this("", width, height);
	}	

	private static String wordWrap(IFont font, String text, int maxLength)
	{
		LinkedList<String> words = new LinkedList<>();
		words.addAll(Arrays.asList(text.split("(?=[\\s])")));
		
		StringBuilder lineBuffer = new StringBuilder();
		
		while(!words.isEmpty())
		{
			int lineLength = 0;
			int nextLineLength = 0;
			
			do
			{
				String word = words.poll();

				int newLineIndex = word.lastIndexOf("\n");
				
				if(newLineIndex >= 0)
				{
					if(word.length() > 1)
						words.addFirst(word.substring(newLineIndex + 1));
					
					word = word.substring(0, newLineIndex + 1);
				}

				lineLength += font.getTextBounds(word).width;
				
				lineBuffer.append(word);
				
				String nextWord = words.peek();
				
				if(word.equals("\n"))
					lineLength = 0;
				
				if(nextWord != null)
				{
					int nextWordNewline = nextWord.indexOf('\n');
					int nextLength = font.getTextBounds(nextWordNewline >= 0 ? nextWord.substring(0, nextWordNewline) : nextWord).width;	
					nextLineLength = nextLength + lineLength;
				}
				
			}while(!words.isEmpty() && nextLineLength < maxLength);
			
			if(!words.isEmpty() && lineBuffer.charAt(lineBuffer.length() - 1) != '\n' && !words.peek().equals("\n"))
				lineBuffer.append("\n");
		}
		
		return lineBuffer.toString().replaceAll(" *\n *", "\n");
	}

	@Override
	public Rect2D getBounds()
	{
		return m_frame.getBounds();
	}
	
	private Rect2D getEffectiveBounds()
	{
		Rect2D bounds = getBounds();
		
		bounds.width -= PADDING;
		bounds.height -= PADDING;
		bounds.x += PADDING;
		bounds.y += PADDING;
		
		return bounds;
	}
	
	private String stripUnsupportedChars(String input)
	{
		return input.replace("\r", "");
	}
	
	public void setWordWrapped(boolean isWordWrapped)
	{
		m_isWordWrapped = isWordWrapped;
	}
	
	public String getText()
	{
		return m_workingText;
	}

	public void setText(String text)
	{
		String safeString = stripUnsupportedChars(text);
		
		if(m_isWordWrapped && m_font != null)
			m_workingText = wordWrap(m_font, safeString, getEffectiveBounds().width);
		else
			m_workingText = safeString;
		
		m_textLayout = new TextLayout(m_workingText, m_font, getEffectiveBounds(), 0);
		
		m_cursorLocation = m_workingText.length() == 0 ? 0 : m_workingText.length() - 1;
	}

	public void writeText(String text)
	{
		String safeString = stripUnsupportedChars(text);
		//backspace
		if(safeString == "\b")
		{
			m_workingText = m_workingText.isEmpty() ? "" : new StringBuilder(m_workingText).deleteCharAt(m_cursorLocation).toString();
		}else
		{
			String begin = m_workingText.isEmpty() ? "" : m_workingText.substring(0, m_cursorLocation);
			String end = m_workingText.isEmpty() ? "" : m_workingText.substring(m_cursorLocation);
			
			m_workingText = begin + safeString + end;
		}
		
		if(m_isWordWrapped)
		{
			int cursorDifference = -m_workingText.length();
			m_workingText = wordWrap(m_font, m_workingText, getEffectiveBounds().width);
			m_cursorLocation += cursorDifference + m_workingText.length();
		}
		
		m_cursorLocation = Math.min(m_cursorLocation, m_workingText.length());
		
		m_textLayout = new TextLayout(m_workingText, m_font, getEffectiveBounds(), m_textLayout.getScroll());
		
		m_textLayout.makeLineVisible(m_textLayout.getCursorLineIndex(m_cursorLocation));
	}

	public void setEditable(boolean isEditable)
	{
		m_allowEdit = isEditable;
	}

	@Override
	public void render(Graphics2D g, int x, int y, float scale)
	{
		m_frame.render(g, x, y, scale);
		
		Shape oldClip = g.getClip();
		
		Rect2D myBounds = getEffectiveBounds();
		Vector2D myLocation = getAbsoluteLocation();
		
		g.setClip(new Rectangle(myLocation.x, myLocation.y, myBounds.width, myBounds.height));
		m_textLayout.render(g, x + PADDING, y + PADDING, scale, m_allowEdit && hasFocus() && m_blinkTimeout / 500 % 2 == 0 ? m_cursorLocation : - 1);
		g.setClip(oldClip);
	}

	public void scrollToEnd()
	{
		m_cursorLocation = m_workingText.length() == 0 ? 0 : m_workingText.length() - 1;
	}
	
	@Override
	public void onStyleChanged()
	{
		super.onStyleChanged();

		m_frame = getComponentStyle().getStateStyle(ComponentState.Default).createFrame(m_width, m_height);
		m_font = getComponentStyle().getStateStyle(ComponentState.Default).getFont();
		
		setText(m_workingText);
	}

	@Override
	public boolean onMouseEvent(InputMouseEvent mouseEvent)
	{
		if (mouseEvent.type == MouseEventType.MouseClicked && mouseEvent.mouseButton == MouseButton.Left)
		{
			Vector2D relativeLocation = mouseEvent.location.difference(getAbsoluteLocation());
			m_cursorLocation = m_textLayout.pickLineLayout(relativeLocation.y).pickCursorLocation(relativeLocation.x);
		}
		
		return true;
	}

	@Override
	public boolean onKeyEvent(InputKeyEvent keyEvent)
	{
		if (keyEvent.type == InputKeyEvent.KeyEventType.KeyTyped && m_allowEdit)
		{
			if (keyEvent.keyChar == '\b')
			{
				m_cursorLocation = Math.min(m_workingText.length(), Math.max(0, m_cursorLocation - 1));
				writeText("\b");				
			} else if (keyEvent.keyChar == '\n')
			{
				writeText(String.valueOf(keyEvent.keyChar));
				m_cursorLocation = Math.min(m_workingText.length(), Math.max(0, m_cursorLocation + 1));
			} else if (m_font.doesMappingExists(keyEvent.keyChar))
			{
				writeText(String.valueOf(keyEvent.keyChar));
				m_cursorLocation = Math.min(m_workingText.length(), Math.max(0, m_cursorLocation + 1));
			}
		}else if(keyEvent.type == InputKeyEvent.KeyEventType.KeyUp)
		{
			if (keyEvent.keyCode == KeyEvent.VK_LEFT)
			{
				m_cursorLocation = Math.min(m_workingText.length(), Math.max(0, m_cursorLocation - 1));
				m_textLayout.makeLineVisible(m_textLayout.getCursorLineIndex(m_cursorLocation));
			} else if (keyEvent.keyCode == KeyEvent.VK_RIGHT)
			{
				m_cursorLocation = Math.min(m_workingText.length(), Math.max(0, m_cursorLocation + 1));	
				m_textLayout.makeLineVisible(m_textLayout.getCursorLineIndex(m_cursorLocation));
			} else if (keyEvent.keyCode == KeyEvent.VK_UP)
			{
				LineLayout currentLine = m_textLayout.getLineAt(m_textLayout.getCursorLineIndex(m_cursorLocation));
				LineLayout nextLine = m_textLayout.getLineAt(Math.max(0, m_textLayout.getCursorLineIndex(m_cursorLocation) - 1));
				
				int currentCursorRelativeToLine = m_cursorLocation - currentLine.getStartIndex();
				m_cursorLocation = nextLine.getStartIndex() + Math.min(nextLine.getText().length(), currentCursorRelativeToLine);
				
				m_textLayout.makeLineVisible(m_textLayout.getCursorLineIndex(m_cursorLocation));
			} else if (keyEvent.keyCode == KeyEvent.VK_DOWN)
			{				
				LineLayout currentLine = m_textLayout.getLineAt(m_textLayout.getCursorLineIndex(m_cursorLocation));
				LineLayout nextLine = m_textLayout.getLineAt(Math.min(m_textLayout.getLineCount() - 1, m_textLayout.getCursorLineIndex(m_cursorLocation) + 1));
				
				int currentCursorRelativeToLine = m_cursorLocation - currentLine.getStartIndex();
				m_cursorLocation = nextLine.getStartIndex() + Math.min(nextLine.getText().length(), currentCursorRelativeToLine);
				
				m_textLayout.makeLineVisible(m_textLayout.getCursorLineIndex(m_cursorLocation));
			}
		}
		
		return true;
	}

	@Override
	public void update(int deltaTime)
	{
		m_blinkTimeout += deltaTime;
	}
	
	private static class LineLayout
	{
		private String m_text;
		private int m_startIndex;
		private IFont m_font;
		
		public LineLayout(String text, int startIndex, IFont font)
		{
			m_text = text;
			m_startIndex = startIndex;
			m_font = font;
		}

		public String getText()
		{
			return m_text;
		}

		public int getStartIndex()
		{
			return m_startIndex;
		}

		public int pickCursorLocation(int x)
		{
			int i = 0;
			int cursorX = x;
			
			for(i = 0; cursorX > 0 && i < m_text.length(); i++)
				cursorX -= m_font.getTextBounds(String.valueOf(m_text.charAt(i))).width;
			
			return Math.min(m_text.length(), i) + m_startIndex;
		}
		
		public void render(Graphics2D g, int x, int y, float scale, int cursorLocationX)
		{
			int cursorLocation = cursorLocationX - m_startIndex;
			
			m_font.drawText(g, x, y, scale, m_text);
			
			if(cursorLocation > 0 && cursorLocation <= m_text.length())
			{
				int cursorOffsetX = 0;
				for(int i = 0; cursorLocation > 0; i++, cursorLocation--)
					cursorOffsetX += m_font.getTextBounds(String.valueOf(m_text.charAt(i))).width;
				
				g.setColor(Color.gray);
				g.fillRect(x + cursorOffsetX, y, 1, m_font.getMaxCharacterBounds().height);
			}
		}
	}
	
	private static class TextLayout
	{
		private ArrayList<LineLayout> m_lines = new ArrayList<LineLayout>();
		private IFont m_font;
		private int m_scroll = 0;
		private int m_height;
		
		public TextLayout(String text, IFont font, Rect2D bounds, int scroll)
		{
			m_font = font;
			m_height = bounds.height / font.getMaxCharacterBounds().height;
	
			int lineWidth = 0;
			int lastStartIndex = 0;
			
			StringBuilder buffer = new StringBuilder();
			
			for(int i = 0; i < text.length(); i++)
			{
				String character = String.valueOf(text.charAt(i));
				Rect2D charBounds = font.getTextBounds(character);
				
				if((lineWidth != 0 && lineWidth + charBounds.width >= bounds.width) || character.equals("\n"))
				{
					lineWidth = 0;
					m_lines.add(new LineLayout(buffer.toString(), lastStartIndex, font));
					buffer = new StringBuilder();
					lastStartIndex = i;
				}
				
				lineWidth += charBounds.width;
				buffer.append(character);
			}
			
			if(buffer.length() != 0)
				m_lines.add(new LineLayout(buffer.toString(), lastStartIndex, font));
			
			setScroll(scroll);
		}
		
		public int getLineCount()
		{
			return m_lines.size();
		}

		public LineLayout getLineAt(int lineIndex)
		{
			return m_lines.get(lineIndex);
		}

		public void setScroll(int scroll)
		{
			m_scroll = Math.min(m_lines.size() - 1, Math.max(0, scroll));
		}
		
		public int getScroll()
		{
			return m_scroll;
		}
		public int getCursorLineIndex(int index)
		{
			int currentLine = index;
			int i;
			for(i = 0; currentLine > 0 && i < m_lines.size(); currentLine -= m_lines.get(i).m_text.length(), i++);
			
			return i - 1;
		}
		
		public void makeLineVisible(int line)
		{
			if(line - m_scroll > m_height || line - m_scroll < 0)
			{
				setScroll(line - m_height);
			}
		}
		
		public LineLayout pickLineLayout(int y)
		{
			if(m_lines.isEmpty())
				return new LineLayout("", 0, m_font);
			
			int index = Math.max(0, Math.min(m_lines.size() - 1, y / m_font.getMaxCharacterBounds().height + m_scroll));
			
			return m_lines.get(index);
		}
		
		public void render(Graphics2D g, int x, int y, float scale, int cursorLocationX)
		{
			for(int i = 0; i < m_lines.size(); i++)
			{
				m_lines.get(i).render(g, x, y + (i - m_scroll) * m_font.getMaxCharacterBounds().height, scale, cursorLocationX);
			}
		}
	}
}
