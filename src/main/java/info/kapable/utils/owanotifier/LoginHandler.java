package info.kapable.utils.owanotifier;

import java.net.MalformedURLException;
import java.util.Observer;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.kapable.utils.owanotifier.auth.AuthHelper;
import info.kapable.utils.owanotifier.auth.AuthListner;
import info.kapable.utils.owanotifier.resource.AuthProperties;
import info.kapable.utils.owanotifier.resource.Labels;
import info.kapable.utils.owanotifier.utils.Browser;
import info.kapable.utils.owanotifier.webserver.InternalWebServer;

public class LoginHandler
{
	private static Logger logger = LoggerFactory.getLogger(LoginHandler.class);

	public void login(Observer observer)
	{
		// Generate UUID for login
		UUID state = UUID.randomUUID();
		UUID nonce = UUID.randomUUID();

		try
		{
			// Start AuthListener
			// Start a web server to handle return of OAuth
			AuthListner listner = new InternalWebServer(nonce);

			// Add observer listener to start loop after authentication
			listner.addObserver(observer);

			// Redirect user to MS authentication web page
			int listenPort = Integer.parseInt(AuthProperties.getProperty("listenPort"));
			String loginUrl = AuthHelper.getLoginUrl(state, nonce, listenPort);
			logger.info(Labels.getLabel("login.redirect") + loginUrl);
			try
			{
				Browser.browse(loginUrl);
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
				OwaNotifier.exit(3);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			OwaNotifier.exit(2);
		}
	}
}
