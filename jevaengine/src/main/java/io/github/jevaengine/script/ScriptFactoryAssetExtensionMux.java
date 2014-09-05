package io.github.jevaengine.script;

import java.util.HashMap;
import java.util.Map;

public final class ScriptFactoryAssetExtensionMux implements IScriptFactory
{
	private final Map<String, IScriptFactory> m_factories = new HashMap<>();
	
	public ScriptFactoryAssetExtensionMux(Map<String, IScriptFactory> factories)
	{
		m_factories.putAll(factories);
	}

	@Override
	public IScript create(Object context, String name) throws ScriptConstructionException
	{
		for(Map.Entry<String, IScriptFactory> factory : m_factories.entrySet())
		{
			if(name.endsWith(factory.getKey()))
					return factory.getValue().create(context, name);
		}
		
		throw new ScriptConstructionException(name, new NoScriptFactoryMappedToAssetExtensionException());
	}

	@Override
	public IScript create() throws ScriptConstructionException {
		throw new ScriptConstructionException(null, new MuxScriptFactoryCannotInstantiateEmptyScriptsException());
	}

	@Override
	public IFunctionFactory getFunctionFactory()
	{
		return new IFunctionFactory() {
			@Override
			public IFunction wrap(Object function) throws UnrecognizedFunctionException
			{
				for(IScriptFactory f : m_factories.values())
				{
					if(f.getFunctionFactory().recognizes(function))
						return f.getFunctionFactory().wrap(function);
				}
				
				throw new UnrecognizedFunctionException();
			}

			@Override
			public boolean recognizes(Object function)
			{
				for(IScriptFactory f : m_factories.values())
				{
					if(f.getFunctionFactory().recognizes(function))
						return true;
				}
				
				return false;
			}
		};
	}
	
	public static final class MuxScriptFactoryCannotInstantiateEmptyScriptsException extends Exception
	{
		private static final long serialVersionUID = 1L;
	
		private MuxScriptFactoryCannotInstantiateEmptyScriptsException() { }
	}
	
	public static final class NoScriptFactoryMappedToAssetExtensionException extends Exception
	{
		private static final long serialVersionUID = 1L;
	
		private NoScriptFactoryMappedToAssetExtensionException() { }
	}
}
