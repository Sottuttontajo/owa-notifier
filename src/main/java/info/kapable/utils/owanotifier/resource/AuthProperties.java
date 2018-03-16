package info.kapable.utils.owanotifier.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import info.kapable.utils.owanotifier.auth.AuthHelper;

public abstract class AuthProperties
{
	public static Properties properties;

	/**
	 * Load config from properties in ressource
	 * 
	 * @throws IOException
	 */
	private static void loadConfig() throws IOException
	{
		String authConfigFile = "property/auth.properties";
		InputStream authConfigStream = AuthHelper.class.getClassLoader().getResourceAsStream(authConfigFile);

		if(authConfigStream != null)
		{
			properties = new Properties();
			properties.load(authConfigStream);
		}
		else
		{
			throw new FileNotFoundException("Property file '" + authConfigFile + "' not found in the classpath.");
		}
	}
	
	/**
	 * return properties
	 * 
	 * @return Return application properties
	 * @throws IOException
	 *             In case of exception during loading properties
	 */
	private static Properties getProperties() throws IOException
	{
		if(properties == null)
		{
			loadConfig();
		}
		return properties;
	}

	/**
	 * Update a property
	 * 
	 * @param key
	 *            String to identify property in store
	 * @param value
	 *            The value to store
	 * @throws IOException
	 *             In case of error when storing in properties
	 */
	public static void setProperty(String key, String value) throws IOException
	{
		getProperties();
		if(value != null)
			properties.put(key, value);
		else
			properties.remove(key);
	}

	public static String getProperty(String key) throws IOException
	{
		return getProperties().getProperty(key);
	}

	public static String getProperty(String key, String defaultValue) throws IOException
	{
		return getProperties().getProperty(key, defaultValue);
	}
}
