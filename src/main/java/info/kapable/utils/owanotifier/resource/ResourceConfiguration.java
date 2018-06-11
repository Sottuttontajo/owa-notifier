package info.kapable.utils.owanotifier.resource;

import java.io.IOException;

public abstract class ResourceConfiguration<RESOURCE>
{
	private RESOURCE resource;

	protected abstract RESOURCE loadConfig() throws IOException;

	protected abstract void setOrRemoveProperty(String key, String value) throws IOException;

	protected abstract String getResourcePath();

	protected RESOURCE getResource() throws IOException
	{
		if(resource == null)
			resource = loadConfig();

		return resource;
	}
}
