package jevarpg.net.client;

import jeva.communication.SharedEntity;

public interface IClientShared
{
	void update(int deltaTime);

	SharedEntity getSharedEntity();
}
