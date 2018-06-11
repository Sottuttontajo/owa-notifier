package info.kapable.utils.owanotifier;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import info.kapable.utils.owanotifier.event.ApplicationStateChangeEvent;
import info.kapable.utils.owanotifier.event.ApplicationStateChangeEvent.StateChange;
import info.kapable.utils.owanotifier.event.InboxChangeEvent;
import info.kapable.utils.owanotifier.event.InboxChangeEvent.EventType;
import info.kapable.utils.owanotifier.event.dispatcher.SwingEventDispatcher;
import info.kapable.utils.owanotifier.event.dispatcher.SystemEventDispatcher;
import info.kapable.utils.owanotifier.resource.AuthProperties;
import info.kapable.utils.owanotifier.resource.Labels;
import info.kapable.utils.owanotifier.service.Folder;
import info.kapable.utils.owanotifier.service.Message;
import info.kapable.utils.owanotifier.service.MessageCollection;
import info.kapable.utils.owanotifier.webserver.InternalWebServer;

public class Boot extends Observable implements Observer
{
	private static Logger logger = LoggerFactory.getLogger(Boot.class);
	private LoginHandler loginHandler;
	private InboxManager inboxManager = new InboxManager();

	public void boot()
	{
		try
		{
			logger.info(Labels.getLabel("boot"));

			// Add different notification observer
			//
			SystemEventDispatcher systemEventDispatcher = new SystemEventDispatcher();
			SwingEventDispatcher swingEventDispatcher = new SwingEventDispatcher();
			addObserver(systemEventDispatcher);
			addObserver(swingEventDispatcher);
			inboxManager.addObserver(systemEventDispatcher);
			inboxManager.addObserver(swingEventDispatcher);

			// Load token from oauth2 process
			// Display login page
			if(loginHandler == null)
				loginHandler = new LoginHandler(this);
			loginHandler.login();
		}
		catch (Throwable t)
		{
			OwaNotifier.handleError("dialog.error.title.boot", t);
			OwaNotifier.exit(7);
		}
	}

	/**
	 * Loop until end to get unread mail count
	 * 
	 * @throws JsonParseException
	 *             In case of bad response fron api
	 * @throws JsonMappingException
	 *             In case of bad response fron api
	 * @throws IOException
	 *             In case of exception during api call
	 * @throws InterruptedException
	 *             In case of interupt
	 */
	public void infiniteLoop() throws JsonParseException, JsonMappingException, IOException, InterruptedException
	{
		setChanged();
		ApplicationStateChangeEvent applicationStateChangeEvent = new ApplicationStateChangeEvent();
		applicationStateChangeEvent.setStateChange(StateChange.STARTED);
		notifyObservers(applicationStateChangeEvent);

		int lastUnreadCount = -1;

		int checkInboxOnIdleTime = Integer.parseInt(AuthProperties.getProperty("checkInboxOnIdleTime"));
		int checkInboxOnNewMessagesTime = Integer.parseInt(AuthProperties.getProperty("checkInboxOnNewMessagesTime"));
		while (true)
		{
			Folder inbox = inboxManager.getInbox();
			if(inbox== null)
			{
				logger.info(Labels.getLabel("notification.inbox_not_available"));
				lastUnreadCount = -1;
			}
			else
			{
				logger.info(Labels.getLabel("notification.new_unread_item_count") + ": " + inbox.getUnreadItemCount());

				EventType eventType = null;
	
				if(inbox.getUnreadItemCount() <= 0)
					eventType = EventType.SOME_MESSAGES_READ;
				else if(inbox.getUnreadItemCount() > 0 && inbox.getUnreadItemCount() > (lastUnreadCount + 1))
					eventType = EventType.MORE_THAN_ONE_NEW_MESSAGE;
				else if(inbox.getUnreadItemCount() > 0 && inbox.getUnreadItemCount() == (lastUnreadCount + 1))
					eventType = EventType.ONE_NEW_MESSAGE;
				else if(inbox.getUnreadItemCount() > 0 && inbox.getUnreadItemCount() < lastUnreadCount)
					eventType = EventType.SOME_MESSAGES_READ;
	
				if(eventType != null)
				{
					setChanged();
					InboxChangeEvent inboxChangeEvent = new InboxChangeEvent();
					inboxChangeEvent.setInbox(inbox);
					inboxChangeEvent.setEventType(eventType);
					if(eventType == EventType.ONE_NEW_MESSAGE)
					{
						MessageCollection m = inboxManager.getMessages(inbox.getId());
						Message message = (Message) m.getValue().get(0);
						inboxChangeEvent.setMessage(message);
					}
					this.notifyObservers(inboxChangeEvent);
	
				}
				lastUnreadCount = inbox.getUnreadItemCount();
			}
			
			if(lastUnreadCount <= 0)
				Thread.sleep(checkInboxOnIdleTime);
			else
				Thread.sleep(checkInboxOnNewMessagesTime);

			System.gc();
			Runtime runtime = Runtime.getRuntime();
			NumberFormat format = NumberFormat.getInstance();
			long maxMemory = runtime.maxMemory();
			long allocatedMemory = runtime.totalMemory();
			long freeMemory = runtime.freeMemory();
			logger.debug("==================================================");
			logger.debug("free memory: " + format.format(freeMemory / 1024));
			logger.debug("allocated memory: " + format.format(allocatedMemory / 1024));
			logger.debug("max memory: " + format.format(maxMemory / 1024));
			logger.debug("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024));
		}
	}

	@Override
	public void update(Observable o, Object arg)
	{
		InternalWebServer authListner = (InternalWebServer) o;
		if(authListner.tokenResponse == null)
		{
			logger.error(Labels.getLabel("token.error.no_token"));
			OwaNotifier.exit(5);
		}
		else
		{
			inboxManager.getTokenResponseValidator().setTokenResponse(authListner.tokenResponse);
			inboxManager.getTokenResponseValidator().setIdToken(authListner.idTokenObj);
		}
		try
		{
			this.infiniteLoop();
		}
		catch (Throwable t)
		{
			try
			{
				OwaNotifier.handleError("dialog.error.title.infinite_loop", t);
				throw t;
			}
			catch (JsonParseException e)
			{
				logger.error(Labels.getLabel("infinite_loop.json.error.parse"), e);
				OwaNotifier.exit(6);
			}
			catch (JsonMappingException e)
			{
				logger.error(Labels.getLabel("infinite_loop.json.error.mapping"), e);
				OwaNotifier.exit(6);
			}
			catch (IOException e)
			{
				logger.error(Labels.getLabel("infinite_loop.io_error"), e);
				OwaNotifier.exit(6);
			}
			catch (InterruptedException e)
			{
				logger.error(Labels.getLabel("infinite_loop.interrupt_error"), e);
				OwaNotifier.exit(6);
			}
			catch (Throwable e)
			{
				logger.error(Labels.getLabel("unknown.error"), e);
				OwaNotifier.exit(8);
			}
		}
	}

	public LoginHandler getLoginHandler()
	{
		return loginHandler;
	}

	public void setLoginHandler(LoginHandler loginHandler)
	{
		this.loginHandler = loginHandler;
	}
}