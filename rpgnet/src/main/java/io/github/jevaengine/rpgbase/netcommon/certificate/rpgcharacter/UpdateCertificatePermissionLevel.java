package io.github.jevaengine.rpgbase.netcommon.certificate.rpgcharacter;

public class UpdateCertificatePermissionLevel
{
	private CertificatePermissionLevel m_currentLevel;
	
	//Used by kryo
	@SuppressWarnings("unused")
	private UpdateCertificatePermissionLevel() { }
	
	public UpdateCertificatePermissionLevel(CertificatePermissionLevel currentLevel)
	{
		m_currentLevel = currentLevel;
	}
	
	public CertificatePermissionLevel getPermissionLevel()
	{
		return m_currentLevel;
	}
}
