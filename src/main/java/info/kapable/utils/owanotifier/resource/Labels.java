package info.kapable.utils.owanotifier.resource;

import java.io.IOException;

public class Labels extends ResourceBundleProperties
{
	private static Labels instance = new Labels();
	
	public static Labels getInstance()
	{
		return instance;
	}

	public static String getLabel(String key)
	{
		try
		{
			String string = getInstance().getResource().getString(key);
			return string;
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error while getting label: " + key);
		}
	}

	@Override
	protected String getResourcePath()
	{
		return "labels";
	}
}
