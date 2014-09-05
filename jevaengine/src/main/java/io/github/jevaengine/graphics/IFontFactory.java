package io.github.jevaengine.graphics;

import io.github.jevaengine.AssetConstructionException;

import java.awt.Color;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultFontFactory.class)
public interface IFontFactory
{
	IFont create(String name, Color color) throws FontConstructionException;
	
	public static final class FontConstructionException extends AssetConstructionException
	{
		private static final long serialVersionUID = 1L;

		public FontConstructionException(String assetName, Exception cause) {
			super(assetName, cause);
		}
	}
}
