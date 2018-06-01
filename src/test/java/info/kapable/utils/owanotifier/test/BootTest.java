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
	private SwingExceptionViewerMock swingExceptionViewer;

	@Override
	protected void setUp() throws Exception
	{
		OwaNotifier.testMode = true;
		swingExceptionViewer = new SwingExceptionViewerMock("Error");		
		OwaNotifier.setSwingExceptionViewer(swingExceptionViewer);
	}
	
	@Test
	public void testAll() throws IOException
	{
		errorOnBoot();
		swingExceptionViewer.setErrorShown(false);
		errorOnUpdate();
	}
	
	public void errorOnBoot()
	{
		Boot boot = new Boot(null);

		LoginHandler loginHandler = new LoginHandler(null)
		{
			@Override
			public void login()
			{
				throw new RuntimeException("Test error");
			}
		};
		
		boot.setLoginHandler(loginHandler);
		boot.boot();
		assertTrue(swingExceptionViewer.isErrorShown());
	}

	public void errorOnUpdate() throws IOException
	{
		Boot boot = new Boot(null)
		{
			@Override
			public void infiniteLoop() throws JsonParseException, JsonMappingException, IOException, InterruptedException
			{
				throw new RuntimeException("Test error");
			}
		};
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
		
		public void setErrorShown(boolean errorShown)
		{
			this.errorShown = errorShown;
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
