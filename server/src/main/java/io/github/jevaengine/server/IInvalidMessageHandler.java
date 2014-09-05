package io.github.jevaengine.server;

import io.github.jevaengine.communication.InvalidMessageException;

public interface IInvalidMessageHandler
{
	void handle(InvalidMessageException e);
}
