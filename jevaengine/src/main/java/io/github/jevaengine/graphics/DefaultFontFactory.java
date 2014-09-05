package io.github.jevaengine.graphics;

import io.github.jevaengine.config.IConfigurationFactory;
import io.github.jevaengine.config.IConfigurationFactory.ConfigurationConstructionException;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.config.NoSuchChildVariableException;
import io.github.jevaengine.config.ValueSerializationException;
import io.github.jevaengine.graphics.DefaultFontFactory.FontDeclaration.GlyphDeclaration;
import io.github.jevaengine.graphics.IGraphicFactory.GraphicConstructionException;
import io.github.jevaengine.math.Rect2D;

import java.awt.Color;
import java.awt.image.RGBImageFilter;
import java.util.HashMap;

import javax.inject.Inject;

public final class DefaultFontFactory implements IFontFactory
{
	private final IConfigurationFactory m_configurationFactory;
	private final IGraphicFactory m_graphicFactory;
	
	@Inject
	public DefaultFontFactory(IConfigurationFactory configurationFactory, IGraphicFactory graphicFactory)
	{
		m_configurationFactory = configurationFactory;
		m_graphicFactory = graphicFactory;
	}
	
	@Override
	public DefaultFont create(String name, Color color) throws FontConstructionException
	{
		try
		{
			FontDeclaration fontDecl = m_configurationFactory.create(name).getValue(FontDeclaration.class);
			
			IImmutableGraphic srcImage = m_graphicFactory.create(fontDecl.texture);
	
			srcImage = filterImage(srcImage, color);
	
			HashMap<Character, Rect2D> charMap = new HashMap<Character, Rect2D>();
	
			for (GlyphDeclaration glyph : fontDecl.glyphs)
				charMap.put(glyph.character, glyph.region);
			
			return new DefaultFont(srcImage, charMap);
			
		} catch (ValueSerializationException |
				 ConfigurationConstructionException | 
				 GraphicConstructionException e)
		{
			throw new FontConstructionException(name, e);
		}
	}

	private static IImmutableGraphic filterImage(IImmutableGraphic src, final Color color)
	{
		return src.filterImage(new RGBImageFilter()
		{

			@Override
			public int filterRGB(int x, int y, int rgb)
			{
				if ((rgb & 0xFF000000) != 0)
				{
					if ((rgb & 0x00FF0000) >> 16 == (rgb & 0x0000FF00) >> 8 && (rgb & 0x0000FF00) >> 8 == (rgb & 0x000000FF))
					{
						float scale = ((float) (rgb & 0x000000FF)) / (float) 0xFF;

						rgb = (rgb & 0xFF000000) | ((int) (color.getRed() * scale)) << 16 | ((int) (color.getGreen() * scale) << 8) | ((int) (color.getBlue() * scale));
					}
				}
				return rgb;
			}
		});
	}
	
	public static class FontDeclaration implements ISerializable
	{
		public String texture;
		public GlyphDeclaration[] glyphs;
		
		public FontDeclaration() { }

		@Override
		public void serialize(IVariable target) throws ValueSerializationException
		{
			target.addChild("texture").setValue(texture);
			target.addChild("glyphs").setValue(glyphs);
		}

		@Override
		public void deserialize(IImmutableVariable source) throws ValueSerializationException
		{
			try
			{
				texture = source.getChild("texture").getValue(String.class);
				glyphs = source.getChild("glyphs").getValues(GlyphDeclaration[].class);
			} catch (NoSuchChildVariableException e)
			{
				throw new ValueSerializationException(e);
			}
		}
		
		public static class GlyphDeclaration implements ISerializable
		{
			public char character;
			public Rect2D region;

			public GlyphDeclaration() { }
			
			@Override
			public void serialize(IVariable target) throws ValueSerializationException
			{
				target.addChild("char").setValue((int)this.character);
				target.addChild("region").setValue(this.region);
			}

			@Override
			public void deserialize(IImmutableVariable source) throws ValueSerializationException
			{
				try
				{
					this.character = (char)source.getChild("char").getValue(Integer.class).intValue();
					this.region = source.getChild("region").getValue(Rect2D.class);
				} catch(NoSuchChildVariableException e)
				{
					throw new ValueSerializationException(e);
				}
			}
		}
	}
}
