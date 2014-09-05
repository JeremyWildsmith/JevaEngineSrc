package io.github.jevaengine;

public abstract class AssetConstructionException extends Exception
{
	private static final long serialVersionUID = 1L;

	private final String m_assetName;
	
	public AssetConstructionException(String assetName, Exception cause)
	{
		super("Error constructing asset: " + assetName, cause);
		m_assetName = assetName;
	}
	
	public final String getAssetName()
	{
		return m_assetName;
	}
}
