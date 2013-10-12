package jevarpg.net.server;

import jeva.world.Entity;

public interface IServerEntity extends IServerShared
{
	Entity getControlledEntity();
}
