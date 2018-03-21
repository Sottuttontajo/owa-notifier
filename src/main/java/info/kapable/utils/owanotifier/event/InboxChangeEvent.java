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
package info.kapable.utils.owanotifier.event;

import info.kapable.utils.owanotifier.resource.Labels;
import info.kapable.utils.owanotifier.service.EmailAddress;
import info.kapable.utils.owanotifier.service.Folder;
import info.kapable.utils.owanotifier.service.Message;

/**
 * This class store all informations to display notification
 */
public class InboxChangeEvent implements Event
{
	public enum EventType
	{
		ONE_NEW_MESSAGE,
		MORE_THAN_ONE_NEW_MESSAGE,
		SOME_MESSAGES_READ
	}
	
	// The folder
	private Folder inbox;
	// The type of event
	private EventType eventType;
	// Event data
	private Message message;

	/**
	 * Return the title of new event depending of eventType
	 * 
	 * @return The title of Event to display
	 */
	public String getEventTitle()
	{
		if(this.eventType == EventType.MORE_THAN_ONE_NEW_MESSAGE)
			return Labels.getLabel("mail.notification.new_mail");

		if(this.eventType == EventType.ONE_NEW_MESSAGE)
			return this.message.getSubject();

		if(this.eventType == EventType.SOME_MESSAGES_READ)
		{
			if(inbox.getUnreadItemCount() > 0)
				return inbox.getUnreadItemCount() + Labels.getLabel("mail.notification.not_read");
			else
				return Labels.getLabel("mail.notification.all_read");
		}
		return "";
	}

	/**
	 * Return inbox associated to event
	 * 
	 * @return The inbox object associated to event
	 */
	public Folder getInbox()
	{
		return inbox;
	}

	/**
	 * Set inbox on event
	 * 
	 * @param inbox
	 *            The inbox object associated to event
	 */
	public void setInbox(Folder inbox)
	{
		this.inbox = inbox;
	}

	/**
	 * Return mail message
	 * 
	 * @return the mail message
	 */
	public Message getMessage()
	{
		return message;
	}

	/**
	 * Update mail message
	 * 
	 * @param message
	 *            the mail message
	 */
	public void setMessage(Message message)
	{
		this.message = message;
	}

	/**
	 * Return the event type
	 * 
	 * @return integer to identify event type
	 */
	public EventType getEventType()
	{
		return eventType;
	}

	/**
	 * Set the event type
	 * 
	 * @param eventType
	 *            the event type
	 */
	public void setEventType(EventType eventType)
	{
		this.eventType = eventType;
	}

	/**
	 * Return the text associated to event
	 * 
	 * @return the text
	 */
	public String getEventText()
	{
		if(this.eventType == EventType.MORE_THAN_ONE_NEW_MESSAGE)
			return inbox.getUnreadItemCount() + " " + Labels.getLabel("mail.notification.not_read");

		if(this.eventType == EventType.ONE_NEW_MESSAGE)
			return this.message.getBodyPreview();
		
		if(this.eventType == EventType.SOME_MESSAGES_READ)
		{
			if(inbox.getUnreadItemCount() > 0)
				return inbox.getUnreadItemCount() + " " + Labels.getLabel("mail.notification.not_read");
			else
				return Labels.getLabel("mail.notification.all_read");
		}
		return "";
	}

	/**
	 * Get from field
	 * 
	 * @return a string to identify from of mail
	 */
	public String getEventFrom()
	{
		if(message != null)
		{
			EmailAddress addr = message.getFrom().getEmailAddress();
			return addr.getName() + " <" + addr.getAddress() + ">";
		}
		return null;
	}
}