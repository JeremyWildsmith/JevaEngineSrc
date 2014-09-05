package io.github.jevaengine.script.rhino;

import io.github.jevaengine.AssetConstructionException;
import io.github.jevaengine.IAssetStreamFactory;
import io.github.jevaengine.script.IFunctionFactory;
import io.github.jevaengine.script.IScript;
import io.github.jevaengine.script.IScriptFactory;
import io.github.jevaengine.script.ScriptExecuteException;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

public final class RhinoScriptFactory implements IScriptFactory
{
	private static final String SCRIPT_ENCODING = "UTF-8";
	
	private final IAssetStreamFactory m_assetFactory;
	
	@Inject
	public RhinoScriptFactory(IAssetStreamFactory assetFactory)
	{
		m_assetFactory = assetFactory;
	}

	@Override
	public IScript create(Object context, String name) throws ScriptConstructionException
	{
		try
		{
			IScript script = new RhinoScript(name);
			script.put("me", context);
			script.evaluate(IOUtils.toString(m_assetFactory.create(name), SCRIPT_ENCODING));

			return script;
		} catch (AssetConstructionException | IOException | ScriptExecuteException e) {
			throw new ScriptConstructionException(name, e);
		}
	}

	@Override
	public IScript create() throws ScriptConstructionException
	{
		return new RhinoScript("Unnamed");
	}
	
	@Override
	public IFunctionFactory getFunctionFactory()
	{
		return new RhinoFunctionFactory();
	}
}
