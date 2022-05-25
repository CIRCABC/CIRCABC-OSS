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
public class FileNNTPResourceHandler implements ResourceHandler
{
	private ResourceManager resourceManager;

	/**
	 * Regex for a custom file nntp url
	 *
	 * Accept
	 * 		[mandatory] nntp:// protocole
	 * 		[mandatory] embeded file access protocole (ftp, hhtp, file, classpath
	 * 		[mandatory] any path (/ separated)
	 * 		[mandatory] the content index
	 *
	 * Accept:
	 * nntp://file://path/0
	 * nntp://ftp://path/0
	 * nntp://http://path/0
	 * nntp://https://path/0
	 * nntp://classpath*:path/0
	 * nntp://classpath:path/0
	 *
	 */
	private static final String FILE_NNTP_URL_REGEX =  "nntp\\:\\/\\/(file\\:\\/\\/|ftp\\:\\/\\/|http(s)?\\:\\/\\/|classpath(\\*)?\\:).+\\/[0-9]+";

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
				return new FileNNTPResource(resourceUrl, resourceManager);
			}
			catch (final Exception e)
			{
				throw new ImportationException("Error reading the nntp resource " + resourceUrl + ": " + e.getMessage(), e);
			}
		}
		else
		{
			throw new ImportationException(resourceUrl + " doens't matches managed nntp url.");
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
			return resourceUrl.matches(FILE_NNTP_URL_REGEX);
		}
	}

	public final void setResourceManager(ResourceManager resourceManager)
	{
		this.resourceManager = resourceManager;
	}
}
