package info.kapable.utils.owanotifier.resource;

import java.io.IOException;

public class AuthProperties extends ResourceProperties
{
	private static AuthProperties instance = new AuthProperties();

	public static AuthProperties getInstance()
	{
		return instance;
	}

	public static void setProperty(String key, String value) throws IOException
	{
		getInstance().setOrRemoveProperty(key, value);
	}

	public static String getProperty(String key) throws IOException
	{
		return getInstance().getResource().getProperty(key);
	}

	public static String getProperty(String key, String defaultValue) throws IOException
	{
		return getInstance().getResource().getProperty(key, defaultValue);
	}

	@Override
	protected String getResourcePath()
	{
		return "property/auth.properties";
	}
}
