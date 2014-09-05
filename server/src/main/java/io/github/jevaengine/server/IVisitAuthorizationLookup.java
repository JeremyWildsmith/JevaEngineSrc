package io.github.jevaengine.server;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.netcommon.INetVisitor;

public interface IVisitAuthorizationLookup
{
	<T extends INetVisitor<Y>, Y> boolean isVisitAuthorized(Communicator sender, T visitor, Y host);
}
