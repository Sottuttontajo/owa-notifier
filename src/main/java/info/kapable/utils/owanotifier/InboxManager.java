package info.kapable.utils.owanotifier;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.text.MessageFormat;
import java.util.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import info.kapable.utils.owanotifier.event.ConnectionEvent;
import info.kapable.utils.owanotifier.resource.AuthProperties;
import info.kapable.utils.owanotifier.resource.Labels;
import info.kapable.utils.owanotifier.service.Folder;
import info.kapable.utils.owanotifier.service.MessageCollection;
import info.kapable.utils.owanotifier.service.OutlookService;
import retrofit.RetrofitError;

public class InboxManager extends Observable
{
	private TokenResponseValidator tokenResponseValidator = new TokenResponseValidator();
	private String folderName = "inbox";
	private static Logger logger = LoggerFactory.getLogger(InboxManager.class);
	private JacksonConverter c = new JacksonConverter(new ObjectMapper());

	public Folder getInbox() throws IOException, InterruptedException
	{
		if(tokenResponseValidator.validate())
		{
			Folder inbox = getInbox(c, tokenResponseValidator.getOutlookService(), folderName);
			return inbox;
		}
		else
			return null;
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
			catch (RetrofitError e)
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
		} while (inbox == null);

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
	
	public MessageCollection getMessages(String inboxId) throws JsonParseException, JsonMappingException, IOException
	{
		MessageCollection m = (MessageCollection) c.fromBody(tokenResponseValidator.getOutlookService().getMessages(inboxId, "receivedDateTime desc", "from,subject,bodyPreview", "isRead eq false", 1).getBody(), MessageCollection.class);
		return m;
	}
	
	public TokenResponseValidator getTokenResponseValidator()
	{
		return tokenResponseValidator;
	}
}