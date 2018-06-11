package info.kapable.utils.owanotifier.resource;

import java.io.IOException;
import java.util.ResourceBundle;

public abstract class ResourceBundleProperties extends ResourceConfiguration<ResourceBundle>
{
	@Override
	protected ResourceBundle loadConfig() throws IOException
	{
		return ResourceBundle.getBundle(getResourcePath());
	}

	@Override
	protected void setOrRemoveProperty(String key, String value) throws IOException
	{
	}
}
