package info.kapable.utils.owanotifier.resource;

import java.io.IOException;

public class AuthProperties extends ResourceProperties
{
	private static AuthProperties instance = new AuthProperties();
	
	public static AuthProperties getInstance()
	{
		return instance;
	}
	
	private AuthProperties()
	{}
	
	public static void setProperty(String key, String value) throws IOException
	{
		getInstance().setOrRemoveProperty(key, value);
	}

	public static String getProperty(String key) throws IOException
	{
		return getInstance().getProperties().getProperty(key);
	}

	public static String getProperty(String key, String defaultValue) throws IOException
	{
		return getInstance().getProperties().getProperty(key, defaultValue);
	}
}
