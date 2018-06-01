package info.kapable.utils.owanotifier;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import info.kapable.utils.owanotifier.auth.AuthHelper;
import info.kapable.utils.owanotifier.auth.IdToken;
import info.kapable.utils.owanotifier.auth.TokenResponse;
import info.kapable.utils.owanotifier.event.ApplicationStateChangeEvent;
import info.kapable.utils.owanotifier.event.ApplicationStateChangeEvent.StateChange;
import info.kapable.utils.owanotifier.event.ConnectionEvent;
import info.kapable.utils.owanotifier.event.InboxChangeEvent;
import info.kapable.utils.owanotifier.event.InboxChangeEvent.EventType;
import info.kapable.utils.owanotifier.event.dispatcher.SwingEventDispatcher;
import info.kapable.utils.owanotifier.event.dispatcher.SystemEventDispatcher;
import info.kapable.utils.owanotifier.resource.AuthProperties;
import info.kapable.utils.owanotifier.resource.Labels;
import info.kapable.utils.owanotifier.service.Folder;
import info.kapable.utils.owanotifier.service.Message;
import info.kapable.utils.owanotifier.service.MessageCollection;
import info.kapable.utils.owanotifier.service.OutlookService;
import info.kapable.utils.owanotifier.service.OutlookServiceBuilder;
import info.kapable.utils.owanotifier.webserver.InternalWebServer;
import retrofit.RetrofitError;

public class Boot extends Observable implements Observer
{
	private static Logger logger = LoggerFactory.getLogger(Boot.class);
	private LoginHandler loginHandler;
	// A public object to store auth
	public TokenResponse tokenResponse;
	private IdToken idToken;

	public void boot()
	{
		try
		{
			logger.info(Labels.getLabel("boot"));

			// Add different notification observer
			//
			addObserver(new SystemEventDispatcher());
			addObserver(new SwingEventDispatcher());

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
		String folderName = "inbox";
		JacksonConverter c = new JacksonConverter(new ObjectMapper());
		OutlookService outlookService = OutlookServiceBuilder.getOutlookService(tokenResponse.getAccessToken(), null);

		int checkInboxOnIdleTime = Integer.parseInt(AuthProperties.getProperty("checkInboxOnIdleTime"));
		int checkInboxOnNewMessagesTime = Integer.parseInt(AuthProperties.getProperty("checkInboxOnNewMessagesTime"));
		while (true)
		{
			Calendar now = Calendar.getInstance();

			// If token is expired refresh token
			if(tokenResponse.getExpirationTime().before(now.getTime()))
			{
				logger.info(Labels.getLabel("token.refresh"));
				TokenResponse tokenResponse = AuthHelper.getTokenFromRefresh(this.tokenResponse, idToken.getTenantId());
				if(tokenResponse.getError() == null)
				{
					this.tokenResponse = tokenResponse;
					outlookService = OutlookServiceBuilder.getOutlookService(this.tokenResponse.getAccessToken(), null);
				}
			}
			Folder inbox = getInbox(c, outlookService, folderName);
			logger.info(Labels.getLabel("notification.new_unread_item_count") + inbox.getUnreadItemCount());

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
					MessageCollection m = (MessageCollection) c.fromBody(outlookService.getMessages(inbox.getId(), "receivedDateTime desc", "from,subject,bodyPreview", "isRead eq false", 1).getBody(), MessageCollection.class);
					Message message = (Message) m.getValue().get(0);
					inboxChangeEvent.setMessage(message);
				}
				this.notifyObservers(inboxChangeEvent);

			}
			lastUnreadCount = inbox.getUnreadItemCount();
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
			this.tokenResponse = authListner.tokenResponse;
			this.idToken = authListner.idTokenObj;
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
	
	private Folder getInbox(JacksonConverter c, OutlookService outlookService, String folderName) throws JsonParseException, JsonMappingException, IOException, InterruptedException
	{
		boolean connected = true;
		int reconnectWaitTime = Integer.parseInt(AuthProperties.getProperty("reconnectWaitTime"));
		
		// Retrieve messages from the inbox
		Folder inbox = null;
		do
		{
			try
			{
				inbox = (Folder) c.fromBody(outlookService.getFolder(folderName).getBody(), Folder.class);
			}
			catch(RetrofitError e)
			{
				if(e.getCause() instanceof NoRouteToHostException)
					if(connected)
					{
						String title = Labels.getLabel("notification.connection_lost.title");
						String text = MessageFormat.format(Labels.getLabel("notification.connection_lost.text"), reconnectWaitTime / 1000);
						logger.info(title + ": " + text);
						setChanged();
						ConnectionEvent connectionEvent = new ConnectionEvent();
						connectionEvent.setConnected(false);
						connectionEvent.setTitle(title);
						connectionEvent.setText(text);
						this.notifyObservers(connectionEvent);
						connected = false;
					}
				Thread.sleep(reconnectWaitTime);
			}
		}
		while(inbox == null);
		
		if(!connected)
		{
			String title = Labels.getLabel("notification.connection_resumed.title");
			String text = MessageFormat.format(Labels.getLabel("notification.connection_resumed.text"), reconnectWaitTime / 1000);
			logger.info(title + ": " + text);
			setChanged();
			ConnectionEvent connectionEvent = new ConnectionEvent();
			connectionEvent.setConnected(true);
			connectionEvent.setTitle(title);
			connectionEvent.setText(text);
			this.notifyObservers(connectionEvent);
		}
		
		return inbox;
	}
}