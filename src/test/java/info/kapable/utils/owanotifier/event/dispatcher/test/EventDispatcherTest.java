/**
The MIT License (MIT)

Copyright (c) 2017 Mathieu GOULIN

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package info.kapable.utils.owanotifier.event.dispatcher.test;

import java.io.IOException;
import java.util.Observable;

import org.junit.Test;

import info.kapable.utils.owanotifier.event.Event;
import info.kapable.utils.owanotifier.event.InboxChangeEvent;
import info.kapable.utils.owanotifier.event.InboxChangeEvent.EventType;
import info.kapable.utils.owanotifier.event.dispatcher.EventDispatcher;
import info.kapable.utils.owanotifier.service.Folder;
import info.kapable.utils.owanotifier.utils.Browser;
import junit.framework.TestCase;

public class EventDispatcherTest extends TestCase
{
	public static InboxChangeEvent event;

	@Test
	public void testBroswe()
	{
		try
		{
			Browser.browse("http://www.google.com");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("MalformedURLException");
		}
		assertTrue(true);
	}

	@Test
	public void testEventPocess()
	{
		EventDispatcher c = new EventDispatcher()
		{
			@Override
			protected void processEvent(Event event) throws IOException
			{
				EventDispatcherTest.event = (InboxChangeEvent) event;
			};
		};
		Folder folder = new Folder();
		folder.setUnreadItemCount(1);
		InboxChangeEvent inboxChangeEvent = new InboxChangeEvent();
		inboxChangeEvent.setInbox(folder);
		inboxChangeEvent.setEventType(EventType.MORE_THAN_ONE_NEW_MESSAGE);

		c.update(new Observable(), inboxChangeEvent);
		assertTrue(EventDispatcherTest.event.getEventTitle().contains(inboxChangeEvent.getEventTitle()));
	}

}
