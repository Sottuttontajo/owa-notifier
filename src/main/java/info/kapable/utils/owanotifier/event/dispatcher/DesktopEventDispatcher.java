package info.kapable.utils.owanotifier.event.dispatcher;

import java.io.IOException;

import info.kapable.utils.owanotifier.event.ConnectionEvent;
import info.kapable.utils.owanotifier.event.Event;
import info.kapable.utils.owanotifier.event.InboxChangeEvent;

public abstract class DesktopEventDispatcher extends EventDispatcher
{
	@Override
	protected void processEvent(Event event) throws IOException
	{
		if(event instanceof InboxChangeEvent)
			processInboxChangeEvent((InboxChangeEvent) event);
		if(event instanceof ConnectionEvent)
			processConnectionEvent((ConnectionEvent) event);
	}

	protected abstract void processConnectionEvent(ConnectionEvent event);
	protected abstract void processInboxChangeEvent(InboxChangeEvent event) throws IOException;
}
