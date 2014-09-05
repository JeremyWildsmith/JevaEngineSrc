package io.github.jevaengine.script;

import io.github.jevaengine.AssetConstructionException;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultScriptFactory.class)
public interface IScriptFactory
{
	IScript create(Object context, String name) throws ScriptConstructionException;
	IScript create() throws ScriptConstructionException;
	IFunctionFactory getFunctionFactory();
	
	public static class ScriptConstructionException extends AssetConstructionException
	{
		private static final long serialVersionUID = 1L;
		
		public ScriptConstructionException(String assetName, Exception cause) {
			super(assetName, cause);
		}
		
	}
}
