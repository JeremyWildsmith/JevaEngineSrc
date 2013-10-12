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

/**
 * The Class UIStyle.
 */
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

	/**
	 * Instantiates a new uI style.
	 * 
	 * @param srcFont
	 *            the src font
	 * @param botton
	 *            the botton
	 * @param frameFill
	 *            the frame fill
	 * @param frameLeft
	 *            the frame left
	 * @param frameRight
	 *            the frame right
	 * @param frameTop
	 *            the frame top
	 * @param frameBottom
	 *            the frame bottom
	 * @param frameTopLeft
	 *            the frame top left
	 * @param frameTopRight
	 *            the frame top right
	 * @param frameBottomLeft
	 *            the frame bottom left
	 * @param frameBottomRight
	 *            the frame bottom right
	 * @param overButton
	 *            the over button
	 * @param pressButton
	 *            the press button
	 */
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

	/**
	 * Creates the.
	 * 
	 * @param root
	 *            the root
	 * @return the uI style
	 */
	public static UIStyle create(Variable root)
	{
		return new UIStyle(root.getVariable("font").getValue().getString(), getSpriteFromCache(root.getVariable("button").getValue().getString()), getSpriteFromCache(root.getVariable("frameFill").getValue().getString()), getSpriteFromCache(root.getVariable("frameLeft").getValue().getString()), getSpriteFromCache(root.getVariable("frameRight").getValue().getString()), getSpriteFromCache(root.getVariable("frameTop").getValue().getString()), getSpriteFromCache(root.getVariable("frameBottom").getValue().getString()), getSpriteFromCache(root.getVariable("frameTopLeft").getValue().getString()), getSpriteFromCache(root.getVariable("frameTopRight").getValue().getString()), getSpriteFromCache(root.getVariable("frameBottomLeft").getValue().getString()), getSpriteFromCache(root.getVariable("frameBottomRight").getValue().getString()), new Audio(root.getVariable("audioButtonOver").getValue().getString()), new Audio(root.getVariable("audioButtonPress").getValue().getString()));
	}

	/**
	 * Gets the font from cache.
	 * 
	 * @param name
	 *            the name
	 * @param color
	 *            the color
	 * @return the font from cache
	 */
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

	/**
	 * Gets the sprite from cache.
	 * 
	 * @param name
	 *            the name
	 * @return the sprite from cache
	 */
	public static Sprite getSpriteFromCache(String name)
	{
		String formalName = getFormalName(name);

		if (!m_spriteCache.containsKey(formalName))
		{
			m_spriteCache.put(formalName, Sprite.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream(formalName))));
		}

		return m_spriteCache.get(formalName);
	}

	/**
	 * Gets the formal name.
	 * 
	 * @param name
	 *            the name
	 * @return the formal name
	 */
	private static String getFormalName(String name)
	{
		String formalName = name.toLowerCase().trim();

		if (formalName.startsWith("/") || formalName.startsWith("\\"))
		{
			formalName = formalName.substring(1);
		}

		return formalName;
	}

	/**
	 * Gets the font.
	 * 
	 * @param color
	 *            the color
	 * @return the font
	 */
	public Font getFont(Color color)
	{
		return getFontFromCache(m_srcFont, color);
	}

	/**
	 * Creates the frame fill sprite.
	 * 
	 * @return the sprite
	 */
	public Sprite createFrameFillSprite()
	{
		return new Sprite(m_frameFill);
	}

	/**
	 * Creates the frame left sprite.
	 * 
	 * @return the sprite
	 */
	public Sprite createFrameLeftSprite()
	{
		return new Sprite(m_frameLeft);
	}

	/**
	 * Creates the frame right sprite.
	 * 
	 * @return the sprite
	 */
	public Sprite createFrameRightSprite()
	{
		return new Sprite(m_frameRight);
	}

	/**
	 * Creates the frame top sprite.
	 * 
	 * @return the sprite
	 */
	public Sprite createFrameTopSprite()
	{
		return new Sprite(m_frameTop);
	}

	/**
	 * Creates the frame bottom sprite.
	 * 
	 * @return the sprite
	 */
	public Sprite createFrameBottomSprite()
	{
		return new Sprite(m_frameBottom);
	}

	/**
	 * Creates the frame top left sprite.
	 * 
	 * @return the sprite
	 */
	public Sprite createFrameTopLeftSprite()
	{
		return new Sprite(m_frameTopLeft);
	}

	/**
	 * Creates the frame top right sprite.
	 * 
	 * @return the sprite
	 */
	public Sprite createFrameTopRightSprite()
	{
		return new Sprite(m_frameTopRight);
	}

	/**
	 * Creates the frame bottom left sprite.
	 * 
	 * @return the sprite
	 */
	public Sprite createFrameBottomLeftSprite()
	{
		return new Sprite(m_frameBottomLeft);
	}

	/**
	 * Creates the frame bottom right sprite.
	 * 
	 * @return the sprite
	 */
	public Sprite createFrameBottomRightSprite()
	{
		return new Sprite(m_frameBottomRight);
	}

	/**
	 * Creates the button sprite.
	 * 
	 * @return the sprite
	 */
	public Sprite createButtonSprite()
	{
		return new Sprite(m_buttonSprite);
	}

	/**
	 * Gets the over button audio.
	 * 
	 * @return the over button audio
	 */
	public Audio getOverButtonAudio()
	{
		return m_overButtonAudio;
	}

	/**
	 * Gets the press button audio.
	 * 
	 * @return the press button audio
	 */
	public Audio getPressButtonAudio()
	{
		return m_pressButtonAudio;
	}

}
