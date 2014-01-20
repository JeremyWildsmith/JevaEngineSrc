package io.github.jevaengine.graphics.pipeline;

import java.awt.Color;
import java.awt.RenderingHints.Key;

import com.jogamp.opengl.util.texture.Texture;

public class GraphicRenderHints
{
	public static final Key KEY_MODE = new Key(106101118 /*ascii for jev*/){
		@Override
		public boolean isCompatibleValue(Object val)
		{
			return val instanceof GraphicMode;
		}
	};
	
	public static abstract class GraphicMode
	{
		private GraphicMode() { }
		protected abstract PrimitiveShader.PrimitiveMode create(GraphicDrawer drawer, Texture source, Color workingColor);
	}
	
	public static final class ColorReplace extends GraphicMode
	{
		private Color m_search;
		
		public ColorReplace(Color search)
		{
			m_search = search;
		}
		
		@Override
		public PrimitiveShader.PrimitiveMode create(GraphicDrawer drawer, Texture source, Color workingColor)
		{
			return new PrimitiveShader.ColorReplace(source, m_search, workingColor);
		}
	}
	
	public static final class ColorMap extends GraphicMode
	{
		private Graphic m_map;
		private Color m_use;
		
		public ColorMap(Graphic map, Color use)
		{
			m_map = map;
			m_use = use;
		}
		
		@Override
		public PrimitiveShader.PrimitiveMode create(GraphicDrawer drawer, Texture source, Color workingColor)
		{
			Texture map = drawer.getTexture(m_map);
			
			return new PrimitiveShader.ColorMap(source, map, m_use);
		}
	}
}
