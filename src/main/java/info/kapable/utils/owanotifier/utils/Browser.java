package info.kapable.utils.owanotifier.utils;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.kapable.utils.owanotifier.OwaNotifier;
import info.kapable.utils.owanotifier.resource.Labels;

public class Browser
{
	// The logger
	private static Logger logger = LoggerFactory.getLogger(Browser.class);

	/**
	 * Static method to start browser redirect user to url
	 * 
	 * @param url
	 *            the page web to follow
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static void browse(String url) throws IOException, URISyntaxException
	{
		// Start browser
		if(Desktop.isDesktopSupported())
		{
			Desktop dt = Desktop.getDesktop();
			if(dt.isSupported(Desktop.Action.BROWSE))
			{
				URL f = new URL(url);
				dt.browse(f.toURI());
			}
		}
		else
		{
			// browser is unsuported
			logger.error(Labels.getLabel("desktop.error.unsupported"));
			OwaNotifier.exit(1);
		}
	}
}
