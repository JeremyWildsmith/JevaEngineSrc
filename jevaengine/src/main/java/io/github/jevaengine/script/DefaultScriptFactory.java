package io.github.jevaengine.script;

import io.github.jevaengine.IAssetStreamFactory;
import io.github.jevaengine.script.rhino.RhinoScriptFactory;

import java.util.HashMap;

import javax.inject.Inject;

public final class DefaultScriptFactory implements IScriptFactory
{
	private final ScriptFactoryAssetExtensionMux m_factoryMux;

	@Inject
	public DefaultScriptFactory(final IAssetStreamFactory assetFactory)
	{
		HashMap<String, IScriptFactory> factories = new HashMap<>();
		factories.put(".js", new RhinoScriptFactory(assetFactory));
		
		m_factoryMux = new ScriptFactoryAssetExtensionMux(factories);
	}
	
	@Override
	public IScript create(Object context, String name) throws ScriptConstructionException
	{
		return m_factoryMux.create(context, name);
	}

	@Override
	public IScript create() throws ScriptConstructionException
	{
		return m_factoryMux.create();
	}

	@Override
	public IFunctionFactory getFunctionFactory()
	{
		return m_factoryMux.getFunctionFactory();
	}
}
