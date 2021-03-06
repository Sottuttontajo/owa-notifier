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
package info.kapable.utils.owanotifier.event.dispatcher;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.notification.NotificationFactory;
import com.notification.NotificationFactory.Location;
import com.notification.types.IconNotification;

import info.kapable.utils.owanotifier.OwaNotifier;
import info.kapable.utils.owanotifier.event.ConnectionEvent;
import info.kapable.utils.owanotifier.event.Event;
import info.kapable.utils.owanotifier.event.InboxChangeEvent;
import info.kapable.utils.owanotifier.event.InboxChangeEvent.EventType;
import info.kapable.utils.owanotifier.notification.manager.SimpleManager;
import info.kapable.utils.owanotifier.resource.AuthProperties;
import info.kapable.utils.owanotifier.theme.ThemePackagePresets;
import info.kapable.utils.owanotifier.utils.Time;

public class SwingEventDispatcher extends DesktopEventDispatcher
{
	IconNotification inboxChangeNotification;
	IconNotification connectionNotification;
	private Image icon;

	public SwingEventDispatcher()
	{
		try
		{
			this.icon = ImageIO.read(getClass().getClassLoader().getResource("assets/image/icon.png"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void processEvent(Event event) throws IOException
	{
		if(event instanceof InboxChangeEvent)
			processInboxChangeEvent((InboxChangeEvent) event);
		if(event instanceof ConnectionEvent)
			processConnectionEvent((ConnectionEvent) event);
	}

	protected void processInboxChangeEvent(InboxChangeEvent event) throws IOException
	{
		// If mute don't display notification
		if(OwaNotifier.isMute())
			return;

		SimpleManager fade = new SimpleManager(Location.SOUTHEAST);
		NotificationFactory factory = new NotificationFactory(ThemePackagePresets.cleanLight());

		IconNotification oldNotification = inboxChangeNotification;
		// The notification window :
		if(event.getEventType() == EventType.ONE_NEW_MESSAGE)
			inboxChangeNotification = factory.buildIconNotification("From: " + event.getEventFrom(), event.getEventTitle(), event.getEventText(), new ImageIcon(this.icon.getScaledInstance(50, 50, Image.SCALE_DEFAULT)));
		else if(event.getEventType() == EventType.MORE_THAN_ONE_NEW_MESSAGE)
			inboxChangeNotification = factory.buildIconNotification(null, event.getEventTitle(), event.getEventText(), new ImageIcon(this.icon.getScaledInstance(50, 50, Image.SCALE_DEFAULT)));
		else if(event.getEventType() == EventType.SOME_MESSAGES_READ && event.getInbox().getUnreadItemCount() > 0)
			inboxChangeNotification = factory.buildIconNotification(null, event.getEventTitle(), event.getEventText(), new ImageIcon(this.icon.getScaledInstance(50, 50, Image.SCALE_DEFAULT)));
		else if(inboxChangeNotification != null && inboxChangeNotification.isShown() && event.getInbox().getUnreadItemCount() == 0)
		{
			inboxChangeNotification.removeFromManager();
			return;
		}
		else
			return;

		// Display it :
		try
		{
			String disappearAfterFadeTimeString = AuthProperties.getProperty("disappear_after_fade_time");
			boolean disappearAfterFadeTime = Boolean.parseBoolean(disappearAfterFadeTimeString);
			Time time = disappearAfterFadeTime ? Time.seconds(Integer.parseInt(AuthProperties.getProperty("notification.fade_time"))) : Time.infinite();
			fade.addNotification(inboxChangeNotification, time);
			if(oldNotification != null)
				oldNotification.removeFromManager();
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected void processConnectionEvent(ConnectionEvent event)
	{
		NotificationFactory factory = new NotificationFactory(ThemePackagePresets.cleanDark());

		IconNotification oldNotification = connectionNotification;
		connectionNotification = factory.buildIconNotification(null, event.getTitle(), event.getText(), new ImageIcon(this.icon.getScaledInstance(50, 50, Image.SCALE_DEFAULT)));

		try
		{
			Time time = event.isConnected() ? Time.seconds(Integer.parseInt(AuthProperties.getProperty("notification.fade_time"))) : Time.infinite();
			SimpleManager fade = new SimpleManager(Location.SOUTHEAST);
			fade.addNotification(connectionNotification, time);
			if(oldNotification != null)
				oldNotification.removeFromManager();

		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @return the notification
	 */
	public IconNotification getInboxChangeNotification()
	{
		return inboxChangeNotification;
	}

	/**
	 * @param notification
	 *            the notification to set
	 */
	public void setInboxChangeNotification(IconNotification inboxChangeNotification)
	{
		this.inboxChangeNotification = inboxChangeNotification;
	}

	public IconNotification getConnectionNotification()
	{
		return connectionNotification;
	}

	public void setConnectionNotification(IconNotification connectionNotification)
	{
		this.connectionNotification = connectionNotification;
	}
}
