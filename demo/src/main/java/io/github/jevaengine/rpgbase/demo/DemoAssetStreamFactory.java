package io.github.jevaengine.rpgbase.demo;

import io.github.jevaengine.IAssetStreamFactory;

import java.io.File;
import java.io.InputStream;

public class DemoAssetStreamFactory implements IAssetStreamFactory
{
	private static final String BASE_PATH = "res";
	
	public String resolvePath(String path)
	{
		return new File(new File(BASE_PATH), path).getPath().replace("\\", "/");
	}
	
	@Override
	public InputStream create(String name) throws AssetStreamConstructionException
	{
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(resolvePath(name));

		if (is == null)
			throw new AssetStreamConstructionException(name, new UnresolvedResourcePathException());

		return is;
	}

	public final class UnresolvedResourcePathException extends Exception
	{
		private static final long serialVersionUID = 1L;

		private UnresolvedResourcePathException() { }
	}
}
