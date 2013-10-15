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
package jeva.graphics.ui;

import java.awt.Color;
import java.util.HashMap;

import jeva.Core;
import jeva.IResourceLibrary;
import jeva.audio.Audio;
import jeva.config.Variable;
import jeva.config.VariableStore;
import jeva.graphics.Font;
import jeva.graphics.Sprite;


public class UIStyle
{

	/** The m_font cache. */
	private static HashMap<String, HashMap<Color, Font>> m_fontCache = new HashMap<String, HashMap<Color, Font>>();

	/** The m_sprite cache. */
	private static HashMap<String, Sprite> m_spriteCache = new HashMap<String, Sprite>();

	/** The m_src font. */
	private String m_srcFont;

	/** The m_frame fill. */
	private Sprite m_frameFill;

	/** The m_frame left. */
	private Sprite m_frameLeft;

	/** The m_frame right. */
	private Sprite m_frameRight;

	/** The m_frame top. */
	private Sprite m_frameTop;

	/** The m_frame bottom. */
	private Sprite m_frameBottom;

	/** The m_frame top left. */
	private Sprite m_frameTopLeft;

	/** The m_frame top right. */
	private Sprite m_frameTopRight;

	/** The m_frame bottom left. */
	private Sprite m_frameBottomLeft;

	/** The m_frame bottom right. */
	private Sprite m_frameBottomRight;

	/** The m_button sprite. */
	private Sprite m_buttonSprite;

	/** The m_over button audio. */
	private Audio m_overButtonAudio;

	/** The m_press button audio. */
	private Audio m_pressButtonAudio;

	
	public UIStyle(String srcFont, Sprite botton, Sprite frameFill, Sprite frameLeft, Sprite frameRight, Sprite frameTop, Sprite frameBottom, Sprite frameTopLeft, Sprite frameTopRight, Sprite frameBottomLeft, Sprite frameBottomRight, Audio overButton, Audio pressButton)
	{
		m_srcFont = srcFont;
		m_buttonSprite = botton;

		m_frameFill = frameFill;
		m_frameLeft = frameLeft;
		m_frameRight = frameRight;
		m_frameTop = frameTop;
		m_frameBottom = frameBottom;

		m_frameBottomLeft = frameBottomLeft;
		m_frameBottomRight = frameBottomRight;
		m_frameTopRight = frameTopRight;
		m_frameTopLeft = frameTopLeft;

		m_overButtonAudio = overButton;
		m_pressButtonAudio = pressButton;
	}

	
	public static UIStyle create(Variable root)
	{
		return new UIStyle(root.getVariable("font").getValue().getString(), getSpriteFromCache(root.getVariable("button").getValue().getString()), getSpriteFromCache(root.getVariable("frameFill").getValue().getString()), getSpriteFromCache(root.getVariable("frameLeft").getValue().getString()), getSpriteFromCache(root.getVariable("frameRight").getValue().getString()), getSpriteFromCache(root.getVariable("frameTop").getValue().getString()), getSpriteFromCache(root.getVariable("frameBottom").getValue().getString()), getSpriteFromCache(root.getVariable("frameTopLeft").getValue().getString()), getSpriteFromCache(root.getVariable("frameTopRight").getValue().getString()), getSpriteFromCache(root.getVariable("frameBottomLeft").getValue().getString()), getSpriteFromCache(root.getVariable("frameBottomRight").getValue().getString()), new Audio(root.getVariable("audioButtonOver").getValue().getString()), new Audio(root.getVariable("audioButtonPress").getValue().getString()));
	}

	
	private static Font getFontFromCache(String name, Color color)
	{
		String formalName = getFormalName(name);
		if (!m_fontCache.containsKey(formalName))
		{
			m_fontCache.put(formalName, new HashMap<Color, Font>());
		}

		if (!m_fontCache.get(formalName).containsKey(color))
		{
			m_fontCache.get(formalName).put(color, Font.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream(formalName)), color));
		}

		return m_fontCache.get(formalName).get(color);
	}

	
	public static Sprite getSpriteFromCache(String name)
	{
		String formalName = getFormalName(name);

		if (!m_spriteCache.containsKey(formalName))
		{
			m_spriteCache.put(formalName, Sprite.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream(formalName))));
		}

		return m_spriteCache.get(formalName);
	}

	
	private static String getFormalName(String name)
	{
		String formalName = name.toLowerCase().trim();

		if (formalName.startsWith("/") || formalName.startsWith("\\"))
		{
			formalName = formalName.substring(1);
		}

		return formalName;
	}

	
	public Font getFont(Color color)
	{
		return getFontFromCache(m_srcFont, color);
	}

	
	public Sprite createFrameFillSprite()
	{
		return new Sprite(m_frameFill);
	}

	
	public Sprite createFrameLeftSprite()
	{
		return new Sprite(m_frameLeft);
	}

	
	public Sprite createFrameRightSprite()
	{
		return new Sprite(m_frameRight);
	}

	
	public Sprite createFrameTopSprite()
	{
		return new Sprite(m_frameTop);
	}

	
	public Sprite createFrameBottomSprite()
	{
		return new Sprite(m_frameBottom);
	}

	
	public Sprite createFrameTopLeftSprite()
	{
		return new Sprite(m_frameTopLeft);
	}

	
	public Sprite createFrameTopRightSprite()
	{
		return new Sprite(m_frameTopRight);
	}

	
	public Sprite createFrameBottomLeftSprite()
	{
		return new Sprite(m_frameBottomLeft);
	}

	
	public Sprite createFrameBottomRightSprite()
	{
		return new Sprite(m_frameBottomRight);
	}

	
	public Sprite createButtonSprite()
	{
		return new Sprite(m_buttonSprite);
	}

	
	public Audio getOverButtonAudio()
	{
		return m_overButtonAudio;
	}

	
	public Audio getPressButtonAudio()
	{
		return m_pressButtonAudio;
	}

}
