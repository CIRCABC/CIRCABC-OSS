/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor.common;

import org.springframework.core.io.Resource;

import eu.cec.digit.circabc.migration.processor.ResourceManager;
import eu.cec.digit.circabc.migration.processor.ResourceManager.ResourceHandler;
import eu.cec.digit.circabc.service.migration.ImportationException;

/**
 * @author Yanick Pignot
 *
 */
public class FTPResourceHandler implements ResourceHandler
{

	private ResourceManager resourceManager;

	private static final String FTP_PREFIX =  "ftp://";
	private boolean passiveMode;
	private String systemEncoding;

	public void init()
	{
		// register this
		resourceManager.registerResource(this);
	}

	public Resource getResource(String resourceUrl) throws ImportationException
	{
		if(handle(resourceUrl))
		{
			try
			{
				return new CachedFTPResource(resourceUrl, passiveMode, systemEncoding);
			}
			catch (final Exception e)
			{
				throw new ImportationException("Error reading the ftp resource " + resourceUrl + ": " + e.getMessage(), e);
			}
		}
		else
		{
			throw new ImportationException(resourceUrl + " does nt matches managed ftp url.");
		}
	}


	public boolean handle(String resourceUrl)
	{
		if(resourceUrl == null)
		{
			return false;
		}
		else
		{
			return resourceUrl.startsWith(FTP_PREFIX);
		}
	}

	public final void setResourceManager(ResourceManager resourceManager)
	{
		this.resourceManager = resourceManager;
	}

	public final boolean isPassiveMode()
	{
		return passiveMode;
	}

	public final void setPassiveMode(boolean passiveMode)
	{
		this.passiveMode = passiveMode;
	}

	/**
	 * @param systemEncoding the systemEncoding to set
	 */
	public final void setSystemEncoding(String systemEncoding)
	{
		this.systemEncoding = systemEncoding;
	}

}
