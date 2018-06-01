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

import org.junit.Test;

import info.kapable.utils.owanotifier.OwaNotifier;
import info.kapable.utils.owanotifier.event.ConnectionEvent;
import info.kapable.utils.owanotifier.event.InboxChangeEvent;
import info.kapable.utils.owanotifier.event.InboxChangeEvent.EventType;
import info.kapable.utils.owanotifier.event.dispatcher.SwingEventDispatcher;
import info.kapable.utils.owanotifier.resource.Labels;
import info.kapable.utils.owanotifier.service.EmailAddress;
import info.kapable.utils.owanotifier.service.Folder;
import info.kapable.utils.owanotifier.service.Message;
import info.kapable.utils.owanotifier.service.Recipient;
import junit.framework.TestCase;

public class SwingEventDispatcherTest extends TestCase
{

	@Test
	public void testReceiveMoreThanOneNewMessage()
	{
		SwingEventDispatcher s = new SwingEventDispatcher();
		OwaNotifier.setMute(false);
		// Initial Notification
		Folder folder = new Folder();
		folder.setUnreadItemCount(1);
		InboxChangeEvent event = new InboxChangeEvent();
		event.setInbox(folder);
		event.setEventType(EventType.MORE_THAN_ONE_NEW_MESSAGE);
		
		try
		{
			s.update(null, event);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("IOException");
		}
		assertTrue(s.getInboxChangeNotification().getTitle().contains(Labels.getLabel("mail.notification.new_mail")));
		assertTrue(s.getInboxChangeNotification().getSubtitle().contains(Labels.getLabel("mail.notification.not_read")));
	}

	@Test
	public void testReceiveOneNewMessage()
	{
		Folder folder = new Folder();
		folder.setUnreadItemCount(2);
		EmailAddress emailAddress = new EmailAddress();
		emailAddress.setAddress("foo@bar.com");
		emailAddress.setName("Foo Bar");
		Message message = new Message();
		Recipient from = new Recipient();
		from.setEmailAddress(emailAddress);
		message.setBodyPreview("BodyPreview de testUnitaire");
		message.setFrom(from);
		message.setSubject("Subject de Junit");
		InboxChangeEvent event = new InboxChangeEvent();
		event.setInbox(folder);
		event.setEventType(EventType.ONE_NEW_MESSAGE);
		event.setMessage(message);

		SwingEventDispatcher s = new SwingEventDispatcher();
		try
		{
			s.update(null, event);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("IOException");
		}
		assertTrue(s.getInboxChangeNotification() != null);
		assertTrue(s.getInboxChangeNotification().getTitle().contains("Subject de Junit"));
		assertTrue(s.getInboxChangeNotification().getSubtitle().contains("BodyPreview de testUnitaire"));
		assertTrue(s.getInboxChangeNotification().getFrom().contains("From: Foo Bar"));
		assertTrue(s.getInboxChangeNotification().getFrom().contains("foo@bar.com"));
	}

	@Test
	public void testSomeMessagesAreRead()
	{
		int unreadMessages = 4;
		Folder folder = new Folder();
		folder.setUnreadItemCount(unreadMessages);
		InboxChangeEvent event = new InboxChangeEvent();
		event.setInbox(folder);
		event.setEventType(EventType.SOME_MESSAGES_READ);

		SwingEventDispatcher s = new SwingEventDispatcher();
		try
		{
			s.update(null, event);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("IOException");
		}
		assertTrue(s.getInboxChangeNotification() != null);
		assertTrue(s.getInboxChangeNotification().getSubtitle().contains(unreadMessages + " " + Labels.getLabel("mail.notification.not_read")));
	}


	@Test
	public void testAllMessagesAreRead()
	{
		Folder folder = new Folder();
		folder.setUnreadItemCount(0);
		InboxChangeEvent event = new InboxChangeEvent();
		event.setInbox(folder);
		event.setEventType(EventType.SOME_MESSAGES_READ);

		SwingEventDispatcher s = new SwingEventDispatcher();
		try
		{
			s.update(null, event);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("IOException");
		}
		assertTrue(s.getInboxChangeNotification() == null);
	}

	@Test
	public void testConnectionLost()
	{
		String title = "Connection";
		String text = "lost";

		ConnectionEvent event = new ConnectionEvent();
		event.setConnected(false);
		event.setTitle(title);
		event.setText(text);

		SwingEventDispatcher s = new SwingEventDispatcher();
		try
		{
			s.update(null, event);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("IOException");
		}
		assertTrue(s.getConnectionNotification() != null);
		assertEquals(title, s.getConnectionNotification().getTitle());
		assertEquals(text, s.getConnectionNotification().getSubtitle());
	}

	@Test
	public void testConnectionResumed()
	{
		String title = "Connection lost";
		String text = "is resumed";

		ConnectionEvent event = new ConnectionEvent();
		event.setConnected(true);
		event.setTitle(title);
		event.setText(text);

		SwingEventDispatcher s = new SwingEventDispatcher();
		try
		{
			s.update(null, event);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("IOException");
		}
		assertTrue(s.getConnectionNotification() != null);
		assertEquals(title, s.getConnectionNotification().getTitle());
		assertEquals(text, s.getConnectionNotification().getSubtitle());
	}
}