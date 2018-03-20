package info.kapable.utils.owanotifier.test;

import java.io.File;
import java.io.IOException;
import java.util.Observable;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import info.kapable.utils.owanotifier.Boot;
import info.kapable.utils.owanotifier.LoginHandler;
import info.kapable.utils.owanotifier.OwaNotifier;
import info.kapable.utils.owanotifier.auth.TokenResponse;
import info.kapable.utils.owanotifier.exception.SwingExceptionViewer;
import info.kapable.utils.owanotifier.webserver.InternalWebServer;
import junit.framework.TestCase;

public class BootTest extends TestCase
{
	@Override
	protected void setUp() throws Exception
	{
		OwaNotifier.testMode = true;
	}
	
	@Test
	public void testErrorOnBoot()
	{
		Boot boot = new Boot(null);

		SwingExceptionViewerMock swingExceptionViewer = new SwingExceptionViewerMock("Error");		
		LoginHandler loginHandler = new LoginHandler(null)
		{
			@Override
			public void login()
			{
				throw new RuntimeException("Test error");
			}
		};
		
		boot.setLoginHandler(loginHandler);
		boot.setSwingExceptionViewer(swingExceptionViewer);
		boot.boot();
		assertTrue(swingExceptionViewer.isErrorShown());
	}

	@Test
	public void testErrorOnUpdate() throws IOException
	{
		SwingExceptionViewerMock swingExceptionViewer = new SwingExceptionViewerMock("Error");		
		Boot boot = new Boot(null)
		{
			@Override
			public void infiniteLoop() throws JsonParseException, JsonMappingException, IOException, InterruptedException
			{
				throw new RuntimeException("Test error");
			}
		};
		boot.setSwingExceptionViewer(swingExceptionViewer);
		InternalWebServer internalWebServer = new InternalWebServer(null);
		internalWebServer.tokenResponse = new TokenResponse();
		boot.update(internalWebServer, null);
		assertTrue(swingExceptionViewer.isErrorShown());
		
	}

	class SwingExceptionViewerMock extends SwingExceptionViewer
	{
		boolean errorShown = false;

		public SwingExceptionViewerMock(String title)
		{
			super(title);
		}

		@Override
		public void show(Throwable t)
		{
			errorShown  = true;
		}
		
		public boolean isErrorShown()
		{
			return errorShown;
		}
	}
	
	class BootUpdateMock extends Boot
	{
		public BootUpdateMock(File lock)
		{
			super(null);
		}
		
		@Override
		public void update(Observable o, Object arg)
		{
			throw new RuntimeException("Test error");
		}
	}
}
