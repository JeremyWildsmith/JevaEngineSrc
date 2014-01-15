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
package io.github.jevaengine.ui;

import java.awt.Color;
import java.util.HashMap;

import io.github.jevaengine.Core;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.audio.Audio;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.graphics.Font;
import io.github.jevaengine.graphics.Sprite;

public class UIStyle
{

	private static HashMap<String, HashMap<Color, Font>> m_fontCache = new HashMap<String, HashMap<Color, Font>>();

	private static HashMap<String, Sprite> m_spriteCache = new HashMap<String, Sprite>();

	private String m_srcFont;

	private Sprite m_frameFill;

	private Sprite m_frameLeft;

	private Sprite m_frameRight;

	private Sprite m_frameTop;

	private Sprite m_frameBottom;

	private Sprite m_frameTopLeft;

	private Sprite m_frameTopRight;

	private Sprite m_frameBottomLeft;

	private Sprite m_frameBottomRight;

	private Audio m_overButtonAudio;

	private Audio m_pressButtonAudio;

	public UIStyle(String srcFont, Sprite frameFill, Sprite frameLeft, Sprite frameRight, Sprite frameTop, Sprite frameBottom, Sprite frameTopLeft, Sprite frameTopRight, Sprite frameBottomLeft, Sprite frameBottomRight, Audio overButton, Audio pressButton)
	{
		m_srcFont = srcFont;

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

	public static UIStyle create(IImmutableVariable root)
	{
		UIStyleDeclaration styleDecl = root.getValue(UIStyleDeclaration.class);
		
		return new UIStyle(styleDecl.font,
							getSpriteFromCache(styleDecl.frameFill),
							getSpriteFromCache(styleDecl.frameLeft),
							getSpriteFromCache(styleDecl.frameRight),
							getSpriteFromCache(styleDecl.frameTop),
							getSpriteFromCache(styleDecl.frameBottom),
							getSpriteFromCache(styleDecl.frameTopLeft),
							getSpriteFromCache(styleDecl.frameTopRight),
							getSpriteFromCache(styleDecl.frameBottomLeft),
							getSpriteFromCache(styleDecl.frameBottomRight),
							new Audio(styleDecl.audioButtonOver),
							new Audio(styleDecl.audioButtonPress));
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
			m_fontCache.get(formalName).put(color, Font.create(Core.getService(ResourceLibrary.class).openConfiguration(formalName), color));
		}

		return m_fontCache.get(formalName).get(color);
	}

	public static Sprite getSpriteFromCache(String name)
	{
		String formalName = getFormalName(name);

		if (!m_spriteCache.containsKey(formalName))
		{
			m_spriteCache.put(formalName, Sprite.create(Core.getService(ResourceLibrary.class).openConfiguration(formalName)));
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

	public Audio getOverButtonAudio()
	{
		return m_overButtonAudio;
	}

	public Audio getPressButtonAudio()
	{
		return m_pressButtonAudio;
	}
	
	/*
	root.getChild("font").getValue(String.class),
							getSpriteFromCache(root.getChild("button").getValue(String.class)),
							getSpriteFromCache(root.getVariable("frameFill").getValue().getString()),
							getSpriteFromCache(root.getVariable("frameLeft").getValue().getString()),
							getSpriteFromCache(root.getVariable("frameRight").getValue().getString()),
							getSpriteFromCache(root.getVariable("frameTop").getValue().getString()),
							getSpriteFromCache(root.getVariable("frameBottom").getValue().getString()),
							getSpriteFromCache(root.getVariable("frameTopLeft").getValue().getString()),
							getSpriteFromCache(root.getVariable("frameTopRight").getValue().getString()),
							getSpriteFromCache(root.getVariable("frameBottomLeft").getValue().getString()),
							getSpriteFromCache(root.getVariable("frameBottomRight").getValue().getString()),
							new Audio(root.getVariable("audioButtonOver").getValue().getString()),
							new Audio(root.getVariable("audioButtonPress").getValue().getString()));
	*/

	public static class UIStyleDeclaration implements ISerializable
	{
		public String font;
		public String frameFill;
		public String frameLeft;
		public String frameRight;
		public String frameTop;
		public String frameBottom;
		public String frameTopLeft;
		public String frameTopRight;
		public String frameBottomLeft;
		public String frameBottomRight;
		public String audioButtonOver;
		public String audioButtonPress;
		
		public UIStyleDeclaration() { }

		@Override
		public void serialize(IVariable target)
		{
			target.addChild("font").setValue(font);
			target.addChild("frameFill").setValue(frameFill);
			target.addChild("frameLeft").setValue(frameLeft);
			target.addChild("frameRight").setValue(frameRight);
			target.addChild("frameTop").setValue(frameTop);
			target.addChild("frameBottom").setValue(frameBottom);
			target.addChild("frameTopLeft").setValue(frameTopLeft);
			target.addChild("frameTopRight").setValue(frameTopRight);
			target.addChild("frameBottomLeft").setValue(frameBottomLeft);
			target.addChild("frameBottomRight").setValue(frameBottomRight);
			target.addChild("audioButtonOver").setValue(audioButtonOver);
			target.addChild("audioButtonPress").setValue(audioButtonPress);
		}

		@Override
		public void deserialize(IImmutableVariable source)
		{
			font = source.getChild("font").getValue(String.class);
			frameFill = source.getChild("frameFill").getValue(String.class);
			frameLeft = source.getChild("frameLeft").getValue(String.class);
			frameRight = source.getChild("frameRight").getValue(String.class);
			frameTop = source.getChild("frameTop").getValue(String.class);
			frameBottom = source.getChild("frameBottom").getValue(String.class);
			frameTopLeft = source.getChild("frameTopLeft").getValue(String.class);
			frameBottomLeft = source.getChild("frameBottomLeft").getValue(String.class);
			frameTopRight = source.getChild("frameTopRight").getValue(String.class);
			frameBottomRight = source.getChild("frameBottomRight").getValue(String.class);
			audioButtonOver = source.getChild("audioButtonOver").getValue(String.class);
			audioButtonPress = source.getChild("audioButtonPress").getValue(String.class);
		}
	}
}
