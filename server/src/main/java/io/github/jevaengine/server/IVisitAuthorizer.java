package io.github.jevaengine.server;

import io.github.jevaengine.netcommon.INetVisitor;

public interface IVisitAuthorizer<T extends INetVisitor<Y>, Y>
{
	boolean isVisitorAuthorized(T visitor, Y target);
}
