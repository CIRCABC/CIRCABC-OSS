/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.migration.ImportationException;
import eu.cec.digit.circabc.service.namespace.CircabcNameSpaceService;

/**
 * Manage any resource url. Allow registration of custom ones.
 *
 *
 * @author Yanick Pignot
 */
public class ResourceManager
{
    private static final Log logger = LogFactory.getLog(ResourceManager.class);
    private static final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private static final QName VALIDATION_MANAGER = QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, "migration.resourceManager");


    private int maxTries = 3;
    private int timeToWaitMs = 350;

    public static final ResourceManager getInstance(final CircabcServiceRegistry registry)
    {
    	return (ResourceManager) registry.getService(VALIDATION_MANAGER);
    }


    /**
     * Base interface of any resource resolver.
     *
     * @author Yanick Pignot
     */
    public interface ResourceHandler
    {
        /**
         * Check if the handler manages this kind of resource target
         *
         * @param resourceUrl
         *
         * @return
         */
        public abstract boolean handle(final String resourceUrl);

        /**
         * Resolve the url to a resource
         *
         * @see org.springframework.core.io.Resource
         *
         * @param resourceUrl
         * @return
         * @throws ImportationException
         */
        public abstract Resource getResource(final String resourceUrl) throws ImportationException;
    }

    private List<ResourceHandler> resourceHandlers = new ArrayList<ResourceHandler>();

    /**
     * Register a custom resource handler that will be called before the default one.
     *
     * @param resourceHandler
     */
    public void registerResource(final ResourceHandler resourceHandler)
    {
        ParameterCheck.mandatory("A Custom Resource Handler", resourceHandler);

        resourceHandlers.add(resourceHandler);

        if(logger.isInfoEnabled())
        {
            logger.info("Custom resource handler registred: " + resourceHandler.getClass());
        }

    }

    /**
     * Convert an URI to a spring resource.
     *
     * @param xmlAnyUri
     * @return
     * @throws ImportationException
     */
    public Resource adaptRessource(final String xmlAnyUri) throws ImportationException
    {
        try
        {
        	Resource resource = null;

            for(ResourceHandler resourceHandler: resourceHandlers)
            {
                if(resourceHandler.handle(xmlAnyUri))
                {
                    resource = resourceHandler.getResource(xmlAnyUri);
                    // don't break, the last registred one is keeped

                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Resource " + xmlAnyUri + " managed by " + resourceHandler.getClass());
                    }
                }
            }

            if(resource == null)
            {
                resource = resolver.getResource(xmlAnyUri);
            }

            return resource;
        }
        catch (final ImportationException ex)
        {
        	throw ex;
        }
        catch (final Exception ex)
        {
        	throw new ImportationException("Impossible to read resource " + xmlAnyUri + ": " + ex.getMessage(), ex);
        }

    }

	/**
	 * @return the maxTries
	 */
	public final int getMaxTries()
	{
		return maxTries;
	}

	/**
	 * @param maxTries the maxTries to set
	 */
	public final void setMaxTries(int maxTries)
	{
		this.maxTries = maxTries;
	}

	/**
	 * @return the timeToWaitMs
	 */
	public final int getTimeToWaitMs()
	{
		return timeToWaitMs;
	}

	/**
	 * @param timeToWaitMs the timeToWaitMs to set
	 */
	public final void setTimeToWaitMs(int timeToWaitMs)
	{
		this.timeToWaitMs = timeToWaitMs;
	}
}
