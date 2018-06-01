package info.kapable.utils.owanotifier.event.dispatcher;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import info.kapable.utils.owanotifier.OwaNotifier;
import info.kapable.utils.owanotifier.event.Event;

public abstract class EventDispatcher implements Observer
{
	@Override
	public void update(Observable o, Object event)
	{
		try
		{
			this.processEvent((Event) event);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			OwaNotifier.exit(255);
		}
	}

	protected abstract void processEvent(Event event) throws IOException;
}
