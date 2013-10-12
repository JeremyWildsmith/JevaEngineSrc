package jeva.communication;

public enum SharePolicy
{
	ClientRW, ClientR;

	public boolean canRead(boolean isOwner)
	{
		return (isOwner || this == ClientR || this == ClientRW);
	}

	public boolean canWrite(boolean isOwner)
	{
		if (isOwner)
			return !(this == ClientRW);
		else
			return this == ClientRW;
	}

	@Override
	public String toString()
	{
		switch (this)
		{
		case ClientR:
			return "Client Read";
		case ClientRW:
			return "Client Read & Write";
		default:
			return "Unrecognized policy";
		}
	}
}
