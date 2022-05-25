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
public class NNTPResourceHandler implements ResourceHandler
{

	private ResourceManager resourceManager;
	private int connectiontimeout = 0;
	private int timeout = 0;


	/**
	 * Regex for a custom nntp url
	 *
	 * Accept
	 * 		[mandatory] protocole nntp://
	 * 		[optional]  username
	 * 		[optional]  :password
	 * 		[mandatory] hostname
	 *		[optional]  :port
	 * 		[mandatory] newsgroup name
	 * 		[mandatory] article index
	 * 		[mandatory] (in article) content index
	 *
	 * Accept:
	 * nntp://username:password@hostname:8080/newsgroup_name/0/0
	 * nntp://username:@hostname:8080/newsgroup_name/1/0
	 * nntp://:@hostname:8080/newsgroup_name/1/0
	 * nntp://username@hostname:8080/newsgroup_name/15465/0
	 * nntp://hostname:8080/newsgroup_name/0/45787847
	 * nntp://hostname/newsgroup_name/57/857
	 * nntp://hostname/newsgroup_name/1/1
	 * nntp://:password@hostname:8080/newsgroup_name/0/0
	 * Reject:
	 * nntp://username:password@hostname:8080/newsgroup_name/x/0
	 * nntp://username@hostname:8080/newsgroup_name/xxxx/15465/0
	 * nntp:///hostname:8080/newsgroup_name/0/45787847
	 * nntp:///hostname:8080/0/45787847
	 */
	private static final String NNTP_URL_REGEX =  "nntp\\:\\/\\/[^\\/](.[^\\ \\:]+(\\:(.[^\\ \\:])*)?@)?.[^\\/\\ \\:]+(\\:[0-9]+)?\\/.[^\\/\\ \\:]+\\/[0-9]+\\/[0-9]+";


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
				return new NNTPResource(resourceUrl, connectiontimeout, timeout);
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
			return resourceUrl.matches(NNTP_URL_REGEX);
		}
	}

	public final void setResourceManager(ResourceManager resourceManager)
	{
		this.resourceManager = resourceManager;
	}

	public final void setConnectiontimeout(int connectiontimeout)
	{
		this.connectiontimeout = connectiontimeout;
	}

	public final void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

}
