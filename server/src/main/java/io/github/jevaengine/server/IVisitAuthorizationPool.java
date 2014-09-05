package io.github.jevaengine.server;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.netcommon.INetVisitor;

public interface IVisitAuthorizationPool extends IVisitAuthorizationLookup
{
	<T extends INetVisitor<Y>, Y> void addVisitAuthorizer(Communicator recipient, Class<Y> hostClass, IVisitAuthorizer<T, Y> authorizer);
	<T extends INetVisitor<Y>, Y> void removeVisitAuthorizer(Communicator recipient, Class<Y> hostClass, IVisitAuthorizer<T, Y> authorizer);
}
