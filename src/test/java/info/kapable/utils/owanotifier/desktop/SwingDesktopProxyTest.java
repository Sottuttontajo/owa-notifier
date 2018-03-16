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
package info.kapable.utils.owanotifier.desktop;

import info.kapable.utils.owanotifier.OwaNotifier;
import info.kapable.utils.owanotifier.event.InboxChangeEvent;
import info.kapable.utils.owanotifier.event.InboxChangeEvent.EventType;
import info.kapable.utils.owanotifier.service.EmailAddress;
import info.kapable.utils.owanotifier.service.Folder;
import info.kapable.utils.owanotifier.service.Message;
import info.kapable.utils.owanotifier.service.Recipient;
import junit.framework.TestCase;

import org.junit.Test;

public class SwingDesktopProxyTest extends TestCase
{

	@Test
	public void testReceiveMoreThanOneNewMessage()
	{
		SwingDesktopProxy s = new SwingDesktopProxy();
		OwaNotifier.setMute(false);
		// Initial Notification
		Folder folder = new Folder();
		folder.setUnreadItemCount(1);
		InboxChangeEvent event = new InboxChangeEvent();
		event.setInbox(folder);
		event.setEventType(EventType.MORE_THAN_ONE_NEW_MESSAGE);
		
		try
		{
			s.processEvent(event);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("IOException");
		}
		assertTrue(s.getNotification().getTitle().contains("Nouveaux Messages"));
		assertTrue(s.getNotification().getSubtitle().contains("1 message(s) non lu"));
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

		SwingDesktopProxy s = new SwingDesktopProxy();
		try
		{
			s.processEvent(event);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("IOException");
		}
		assertTrue(s.getNotification() != null);
		assertTrue(s.getNotification().getTitle().contains("Subject de Junit"));
		assertTrue(s.getNotification().getSubtitle().contains("BodyPreview de testUnitaire"));
		assertTrue(s.getNotification().getFrom().contains("De: Foo Bar"));
		assertTrue(s.getNotification().getFrom().contains("foo@bar.com"));
	}

	@Test
	public void testSomeMessagesAreRead()
	{
		Folder folder = new Folder();
		folder.setUnreadItemCount(4);
		InboxChangeEvent event = new InboxChangeEvent();
		event.setInbox(folder);
		event.setEventType(EventType.SOME_MESSAGES_READ);

		SwingDesktopProxy s = new SwingDesktopProxy();
		try
		{
			s.processEvent(event);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("IOException");
		}
		assertTrue(s.getNotification() != null);
		assertTrue(s.getNotification().getSubtitle().contains("4 message(s) non lu"));
	}


	@Test
	public void allMessagesAreRead()
	{
		Folder folder = new Folder();
		folder.setUnreadItemCount(0);
		InboxChangeEvent event = new InboxChangeEvent();
		event.setInbox(folder);
		event.setEventType(EventType.SOME_MESSAGES_READ);

		SwingDesktopProxy s = new SwingDesktopProxy();
		try
		{
			s.processEvent(event);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("IOException");
		}
		assertTrue(s.getNotification() == null);
	}
}