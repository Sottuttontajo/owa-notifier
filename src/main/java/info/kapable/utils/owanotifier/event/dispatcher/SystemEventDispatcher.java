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

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.kapable.utils.owanotifier.OwaNotifier;
import info.kapable.utils.owanotifier.desktop.LogWindowPanel;
import info.kapable.utils.owanotifier.event.ApplicationStateChangeEvent;
import info.kapable.utils.owanotifier.event.ConnectingEvent;
import info.kapable.utils.owanotifier.event.ConnectionEvent;
import info.kapable.utils.owanotifier.event.Event;
import info.kapable.utils.owanotifier.event.InboxChangeEvent;
import info.kapable.utils.owanotifier.resource.AuthProperties;
import info.kapable.utils.owanotifier.resource.Labels;

public class SystemEventDispatcher extends DesktopEventDispatcher
{

	private TrayIcon trayIcon;

	private String toolTip = Labels.getLabel("mail.notification.new_mail");
	private Image imageWaiting;
	private Image imageNewMail;
	private Image imageNoMail;

	// The logger
	private static Logger logger = LoggerFactory.getLogger(SystemEventDispatcher.class);

	/**
	 * On build load icon.png
	 */
	public SystemEventDispatcher()
	{
		super();

		try
		{
			this.imageWaiting = ImageIO.read(getClass().getClassLoader().getResource("assets/image/icon-waiting.png"));
			this.imageNewMail = ImageIO.read(getClass().getClassLoader().getResource("assets/image/icon.png"));
			this.imageNoMail = ImageIO.read(getClass().getClassLoader().getResource("assets/image/icon-no-mail.png"));
			trayIcon = new TrayIcon(imageWaiting, "Emails");
			// Let the system resizes the image if needed
			trayIcon.setImageAutoSize(true);
			// Set tooltip text for the tray icon

			this.setToolTip(toolTip);
			trayIcon.addMouseListener(new MouseAdapter()
			{
				public void mouseClicked(MouseEvent e)
				{
					if(e.getClickCount() == 2)
					{
						Desktop dt = Desktop.getDesktop();
						try
						{
							URL f = new URL(AuthProperties.getProperty("owaUrl"));
							dt.browse(f.toURI());
						}
						catch (IOException e1)
						{
							e1.printStackTrace();
							logger.error(Labels.getLabel("browse.io_error"), e1);
						}
						catch (URISyntaxException e1)
						{
							logger.error(Labels.getLabel("browse.uri_syntax_error"), e1);
						}
					}
				}
			});
			trayIcon.setPopupMenu(this.getPopupMenu());
		}
		catch (UnsupportedOperationException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			OwaNotifier.exit(105);
		}

		try
		{
			SystemTray tray = SystemTray.getSystemTray();
			tray.add(trayIcon);
		}
		catch (UnsupportedOperationException e)
		{
			e.printStackTrace();
		}
		catch (AWTException e1)
		{
			e1.printStackTrace();
			OwaNotifier.exit(106);
		}
	}

	/**
	 * Update toolTipMessage
	 * 
	 * @param toolTipMessage
	 */
	private void setToolTip(String toolTipMessage)
	{
		this.toolTip = toolTipMessage;
		if(trayIcon != null)
		{
			trayIcon.setToolTip(toolTipMessage);
		}
	}

	/**
	 * Return the toolTipMessage
	 * 
	 * @return A string display on tray
	 */
	public String getToolTip()
	{
		return this.toolTip;
	}

	private PopupMenu getPopupMenu()
	{
		final PopupMenu popup = new PopupMenu();

		// Create a pop-up menu components
		MenuItem aboutItem = new MenuItem(Labels.getLabel("about"));
		aboutItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JOptionPane.showMessageDialog(null, Labels.getLabel("link") + ": https://github.com/matgou/owa-notifier", "OwaNotifier: ", JOptionPane.INFORMATION_MESSAGE);

			}

		});
		// Create a pop-up menu components
		final CheckboxMenuItem displayLogItem = new CheckboxMenuItem(Labels.getLabel("log.show"));
		LogWindowPanel.getInstance().displayLogItem = displayLogItem;

		displayLogItem.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if(LogWindowPanel.getInstance().isVisible())
				{
					LogWindowPanel.getInstance().setVisible(false);
					displayLogItem.setLabel(Labels.getLabel("log.show"));
				}
				else
				{
					LogWindowPanel.getInstance().setVisible(true);
					displayLogItem.setLabel(Labels.getLabel("log.hide"));
				}
			}
		});
		// Create a pop-up menu components
		final CheckboxMenuItem muteLogItem = new CheckboxMenuItem(Labels.getLabel("notification.hide"));
		muteLogItem.setState(OwaNotifier.isMute());
		muteLogItem.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if(e.getStateChange() == ItemEvent.SELECTED)
				{
					OwaNotifier.setMute(true);
				}
				else
				{
					OwaNotifier.setMute(false);
				}
			}
		});
		MenuItem exitItem = new MenuItem(Labels.getLabel("exit"));
		exitItem.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				logger.error(Labels.getLabel("exit"));
				OwaNotifier.exit(0);
			}
		});

		// Add components to pop-up menu
		popup.add(aboutItem);
		popup.add(displayLogItem);
		popup.add(muteLogItem);
		popup.addSeparator();
		popup.add(exitItem);
		return popup;
	}

	@Override
	protected void processEvent(Event event) throws IOException
	{
		if(event instanceof InboxChangeEvent)
			processInboxChangeEvent((InboxChangeEvent) event);
		if(event instanceof ApplicationStateChangeEvent)
			processApplicationStateChangeEvent((ApplicationStateChangeEvent) event);
	}

	protected void processInboxChangeEvent(InboxChangeEvent event) throws IOException
	{
		if(SystemTray.isSupported())
		{
			if(event.getInbox().getUnreadItemCount() > 0)
			{
				trayIcon.setImage(this.imageNewMail);
			}
			else
			{
				trayIcon.setImage(this.imageNoMail);
			}

			if(AuthProperties.getProperty("notification.type").contentEquals("system"))
			{
				trayIcon.displayMessage(event.getEventTitle(), event.getEventText(), MessageType.INFO);
			}
		}
		else
		{
			logger.error(Labels.getLabel("system_tray.error.not_supported"));
		}
		if(event.getInbox().getUnreadItemCount() > 0)
		{
			this.setToolTip(event.getInbox().getUnreadItemCount() + " " + Labels.getLabel("mail.notification.not_read"));
		}
		else
		{
			this.setToolTip(Labels.getLabel("mail.notification.all_read"));
		}
	}

	private void processApplicationStateChangeEvent(ApplicationStateChangeEvent event) throws IOException
	{
		switch (event.getStateChange())
		{
			case STARTED:
				String title = Labels.getLabel("application.started.title");
				String text = Labels.getLabel("application.started.text");
				int checkInboxOnIdleTime = Integer.parseInt(AuthProperties.getProperty("checkInboxOnIdleTime")) / 1000;
				text = MessageFormat.format(text, checkInboxOnIdleTime);
				trayIcon.displayMessage(title, text, MessageType.INFO);
		}
	}
}