package info.kapable.utils.owanotifier;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import info.kapable.utils.owanotifier.desktop.SwingDesktopProxy;
import info.kapable.utils.owanotifier.desktop.SystemDesktopProxy;
import info.kapable.utils.owanotifier.event.InboxChangeEvent;
import info.kapable.utils.owanotifier.event.InboxChangeEvent.EventType;
import info.kapable.utils.owanotifier.exception.SwingExceptionViewer;
import info.kapable.utils.owanotifier.resource.AuthProperties;
import info.kapable.utils.owanotifier.resource.Labels;
import info.kapable.utils.owanotifier.service.Folder;
import info.kapable.utils.owanotifier.service.Message;
import info.kapable.utils.owanotifier.service.MessageCollection;
import info.kapable.utils.owanotifier.service.OutlookService;
import info.kapable.utils.owanotifier.service.OutlookServiceBuilder;
import info.kapable.utils.owanotifier.webserver.InternalWebServer;

public class Boot extends Observable implements Observer
{
	private static Logger logger = LoggerFactory.getLogger(Boot.class);
	private SwingExceptionViewer swingExceptionViewer;
	private LoginHandler loginHandler;
	// A public object to store auth
	public TokenResponse tokenResponse;
	private IdToken idToken;
	private File lock;

	public Boot(File lock)
	{
		this.lock = lock;
	}

	public void boot()
	{
		try
		{
			logger.info(Labels.getLabel("boot"));

			// Add different notification observer
			//
			addObserver(new SystemDesktopProxy());
			addObserver(new SwingDesktopProxy());

			// Load token from oauth2 process
			// Display login page
			if(loginHandler == null)
				loginHandler = new LoginHandler(this);
			loginHandler.login();
		}
		catch (Throwable t)
		{
			if(swingExceptionViewer == null)
				swingExceptionViewer = new SwingExceptionViewer(Labels.getLabel("dialog.error.title.boot"));

			swingExceptionViewer.show(t);

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
		int lastUnreadCount = -1;
		String folder = "inbox";
		JacksonConverter c = new JacksonConverter(new ObjectMapper());
		OutlookService outlookService = OutlookServiceBuilder.getOutlookService(tokenResponse.getAccessToken(), null);

		int loopWaitTime = Integer.parseInt(AuthProperties.getProperty("loopWaitTime"));
		while (true)
		{
			Thread.sleep(loopWaitTime);
			Calendar now = Calendar.getInstance();
			updateLock();

			// If token is expired refresh token
			if(tokenResponse.getExpirationTime().before(now.getTime()))
			{
				logger.info(Labels.getLabel("token.refresh"));
				tokenResponse = AuthHelper.getTokenFromRefresh(tokenResponse, idToken.getTenantId());
				outlookService = OutlookServiceBuilder.getOutlookService(tokenResponse.getAccessToken(), null);
			}
			// Retrieve messages from the inbox
			Folder inbox = (Folder) c.fromBody(outlookService.getFolder(folder).getBody(), Folder.class);
			logger.info(Labels.getLabel("notification.new_unread_item_count") + inbox.getUnreadItemCount());

			EventType eventType = null;

			if(lastUnreadCount <= 0)
				eventType = EventType.SOME_MESSAGES_READ;
			else if(inbox.getUnreadItemCount() > 0 && inbox.getUnreadItemCount() > (lastUnreadCount + 1))
				eventType = EventType.MORE_THAN_ONE_NEW_MESSAGE;
			else if(inbox.getUnreadItemCount() > 0 && inbox.getUnreadItemCount() == (lastUnreadCount + 1))
				eventType = EventType.ONE_NEW_MESSAGE;
			else if(inbox.getUnreadItemCount() > 0 && inbox.getUnreadItemCount() < lastUnreadCount)
				eventType = EventType.SOME_MESSAGES_READ;

			if(eventType != null)
			{
				this.setChanged();
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

	/**
	 * Update lock file
	 * 
	 * @throws IOException
	 */
	private void updateLock() throws IOException
	{
		logger.debug(Labels.getLabel("lock.update"));
		FileWriter writer = new FileWriter(lock);
		writer.write(System.currentTimeMillis() + "");
		writer.close();
	}

	public File getLock()
	{
		return lock;
	}

	public void setLock(File lock)
	{
		this.lock = lock;
	}

	public SwingExceptionViewer getSwingExceptionViewer()
	{
		return swingExceptionViewer;
	}

	public void setSwingExceptionViewer(SwingExceptionViewer swingExceptionViewer)
	{
		this.swingExceptionViewer = swingExceptionViewer;
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
				if(swingExceptionViewer == null)
					swingExceptionViewer = new SwingExceptionViewer(Labels.getLabel("dialog.error.title.infinite_loop"));

				swingExceptionViewer.show(t);
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