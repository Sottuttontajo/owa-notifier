package info.kapable.utils.owanotifier.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class ResourceProperties extends ResourceConfiguration<Properties>
{
	protected Properties loadConfig() throws IOException
	{
		InputStream propertiesInputStream = getClass().getClassLoader().getResourceAsStream(getResourcePath());

		if(propertiesInputStream != null)
		{
			Properties properties = new Properties();
			properties.load(propertiesInputStream);
			return properties;
		}
		else
		{
			throw new FileNotFoundException("Property file '" + getResourcePath() + "' not found in the classpath.");
		}
	}
	
	protected void setOrRemoveProperty(String key, String value) throws IOException
	{
		if(value != null)
			getResource().put(key, value);
		else
			getResource().remove(key);
	}
}
