/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.validation.impl;

import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;

import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.processor.ResourceManager;
import eu.cec.digit.circabc.migration.validation.JXPathValidator;
import eu.cec.digit.circabc.service.migration.ImportationException;

/**
 * Validate the external ressources setted in the given object graph.
 *
 * @see org.springframework.core.io.Resource
 *
 * @author yanick pignot
 */
public class ResourcesValidator extends JXPathValidator
{
	private static final Log logger = LogFactory.getLog(ResourcesValidator.class);
	private static final String QUERY_URI = ".//uri";
	private static final String QUERY_URIS = ".//uris";

	
	@Override
	protected void validateCircabcImpl(JXPathContext context, final MigrationTracer<ImportRoot> journal)
	{
		validateUri(context, journal, QUERY_URI);
		validateUri(context, journal, QUERY_URIS);
	}

	@SuppressWarnings("unchecked")
	protected void validateUri(JXPathContext context,
			final MigrationTracer<ImportRoot> journal, String query) {
		List<String> selectNodes = (List<String>) context.selectNodes(query);
		for(final String resourceAsString: selectNodes)
		{

			try
			{
				if(checkExists(resourceAsString, 1) == false)
				{
					error(journal, "The ressource doesn't exists.", resourceAsString);
				}
				else
				{
					debug(journal, "Ressource succesfully found.", resourceAsString);
				}
			}
			catch (Exception exception)
			{
				error(journal, "Impossible to open ressource, reason: " + exception.getMessage(), resourceAsString);
			}
		}
	}

	/**
	 * @param resource
	 * @return
	 * @throws ImportationException
	 */
	private boolean checkExists(final String resourceAsString, int tries) throws ImportationException
	{
		if(resourceAsString == null || resourceAsString.length() < 1)
		{
			return true;
		}
		else
		{
			final ResourceManager resourceManager = ResourceManager.getInstance(getRegistry());
			final Resource resource = resourceManager.adaptRessource(resourceAsString);

			if(resource.exists())
			{
				return true;
			}
			else if(tries > resourceManager.getMaxTries())
			{
				return false;
			}
			else
			{
				try
				{
					final int realTimeWait = tries * resourceManager.getTimeToWaitMs();

					if(logger.isWarnEnabled())
					{
						logger.warn(resourceAsString + " is not accessible for the " + tries + " time(s). Wait " + realTimeWait + " milliseconds before rerying.");
					}

					Thread.sleep(realTimeWait);
				} catch (InterruptedException ignore){}

				return checkExists(resourceAsString, ++tries);
			}
		}
	}

}
