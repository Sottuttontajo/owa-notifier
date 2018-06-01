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
package info.kapable.utils.owanotifier;

import java.io.IOException;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.kapable.utils.owanotifier.desktop.LogWindowPanel;
import info.kapable.utils.owanotifier.exception.SwingExceptionViewer;
import info.kapable.utils.owanotifier.resource.Labels;

/**
 * OwaNotifier main class - Load config - start oauth2 client daemon - main loop
 * to check new mail
 * 
 * @author Mathieu GOULIN
 */
public class OwaNotifier
{
	// testMode is true when using this class on jUnit context
	public static boolean testMode = false;

	// the return code for exit
	private static int rc;

	// The logger
	private static Logger logger = LoggerFactory.getLogger(OwaNotifier.class);
	private static OwaNotifier owanotifier;

	// variable to store mute status
	private static boolean mute;

	private static SwingExceptionViewer swingExceptionViewer;

	/**
	 * Update mute status in Java/Prefs, this state is saving and restoring at
	 * OwaNotifier boot
	 * 
	 * @param value
	 *            true to set OwaNotifier in mute mode
	 */
	public static void setMute(boolean value)
	{
		Preferences p = Preferences.userRoot();
		if(value)
		{
			p.putInt("OwaNotifierMute", 1);
			OwaNotifier.mute = true;
		}
		else
		{
			p.putInt("OwaNotifierMute", 0);
			OwaNotifier.mute = false;
		}
	}

	/**
	 * Retrieve mute status
	 * 
	 * @return true if OwaNotifier is in mute mode
	 */
	public static boolean isMute()
	{
		return OwaNotifier.mute;
	}

	/**
	 * Exit application with code
	 * 
	 * @param rc
	 *            code to exit application
	 */
	public static void exit(int rc)
	{
		logger.info("Exit with code : " + rc);
		if(!testMode)
		{
			System.exit(rc);
		}
		else
		{
			OwaNotifier.rc = rc;
		}

	}

	/**
	 * Main function swith from static domain to object
	 * 
	 * @param args
	 *            Arguments passed to application
	 * @throws IOException
	 *             In case of exception during configuration loading
	 */
	public static void main(String[] args) throws IOException
	{
		Preferences p = Preferences.userRoot();
		int owaNotifierMute = p.getInt("OwaNotifierMute", 0);
		if(owaNotifierMute > 0)
			setMute(true);
		else
			setMute(false);

		owanotifier = getInstance();
		LogWindowPanel.getInstance();
		Boot boot = new Boot();
		boot.boot();
	}

	/**
	 * @return The return code of application in case of exit
	 */
	public static int getRc()
	{
		return rc;
	}

	/**
	 * @return Singleton of OwaNotifier
	 */
	public static OwaNotifier getInstance()
	{
		if(owanotifier == null)
		{
			owanotifier = new OwaNotifier();
		}
		return owanotifier;
	}

	public synchronized static void handleError(String labelCode, Throwable t)
	{
		String label = Labels.getLabel(labelCode);
		logger.error(label, t);

		if(swingExceptionViewer == null)
			swingExceptionViewer = new SwingExceptionViewer(label);

		swingExceptionViewer.show(t);
	}

	public static SwingExceptionViewer getSwingExceptionViewer()
	{
		return swingExceptionViewer;
	}

	public static void setSwingExceptionViewer(SwingExceptionViewer swingExceptionViewer)
	{
		OwaNotifier.swingExceptionViewer = swingExceptionViewer;
	}
}