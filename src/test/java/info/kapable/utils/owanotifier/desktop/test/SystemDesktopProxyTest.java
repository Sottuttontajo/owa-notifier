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
package info.kapable.utils.owanotifier.desktop.test;

import org.junit.Test;

import info.kapable.utils.owanotifier.desktop.SystemDesktopProxy;
import info.kapable.utils.owanotifier.event.InboxChangeEvent;
import info.kapable.utils.owanotifier.event.InboxChangeEvent.EventType;
import info.kapable.utils.owanotifier.resource.Labels;
import info.kapable.utils.owanotifier.service.EmailAddress;
import info.kapable.utils.owanotifier.service.Folder;
import info.kapable.utils.owanotifier.service.Message;
import info.kapable.utils.owanotifier.service.Recipient;
import junit.framework.TestCase;

public class SystemDesktopProxyTest extends TestCase
{

	@Test
	public void test()
	{
		SystemDesktopProxy s = new SystemDesktopProxy();
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
		String toolTip = s.getToolTip();
		assertTrue(toolTip.contains("1 " + Labels.getLabel("mail.notification.not_read")));

		// Test receive a new messages
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
		event = new InboxChangeEvent();
		event.setInbox(folder);
		event.setEventType(EventType.ONE_NEW_MESSAGE);
		event.setMessage(message);
		try
		{
			s.update(null, event);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("IOException");
		}
		assertTrue(s.getToolTip().contains("2 " + Labels.getLabel("mail.notification.not_read")));

		// Test mark a message as read
		folder.setUnreadItemCount(1);
		event = new InboxChangeEvent();
		event.setInbox(folder);
		event.setEventType(EventType.SOME_MESSAGES_READ);
		try
		{
			s.update(null, event);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("IOException");
		}
		assertTrue(s.getToolTip().contains("1 " + Labels.getLabel("mail.notification.not_read")));

		// Test last message mark as read
		folder.setUnreadItemCount(0);
		event = new InboxChangeEvent();
		event.setInbox(folder);
		event.setEventType(EventType.SOME_MESSAGES_READ);
		try
		{
			s.update(null, event);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("IOException");
		}
		assertTrue(s.getToolTip().contains(Labels.getLabel("mail.notification.all_read")));
	}

}
