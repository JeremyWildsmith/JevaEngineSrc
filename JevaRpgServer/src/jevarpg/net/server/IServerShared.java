package jevarpg.net.server;

import jeva.communication.SharedEntity;

public interface IServerShared
{
	void update(int deltaTime);

	SharedEntity getSharedEntity();
}
