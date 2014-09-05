package io.github.jevaengine.server;

import io.github.jevaengine.netcommon.world.NetWorldIdentifier;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.World;

public interface IServerWorldLookup
{
	@Nullable
	ServerWorld lookupServerWorld(NetWorldIdentifier worldName);
	
	@Nullable
	ServerWorld lookupServerWorld(World world);
}
