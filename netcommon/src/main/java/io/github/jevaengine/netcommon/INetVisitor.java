package io.github.jevaengine.netcommon;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;

public interface INetVisitor<T>
{
	void visit(Communicator sender, T host, boolean onServer) throws InvalidMessageException;
	Class<T> getHostComponentClass();
}
