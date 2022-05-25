/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.migration.validation.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Unmarshaller.Listener;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.xml.sax.SAXException;

import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.journal.MigrationLog;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.validation.ValidationException;
import eu.cec.digit.circabc.migration.validation.ValidationHandler;
import eu.cec.digit.circabc.migration.validation.Validator;
import eu.cec.digit.circabc.repo.migration.ReflexionUtils;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;

/**
 * Handle the validation for a migration file
 *
 * @author Yanick Pignot
 */
public class ValidationHandlerImpl implements ValidationHandler
{
	private static final Log logger = LogFactory.getLog(ValidationHandlerImpl.class);

	private Resource schemaAsRessource;
	private Listener listener;
	private List<String> validators;
	private ValidationEventHandler eventHandler;
	private CircabcServiceRegistry circabcServiceRegistry;


	public void validateDataStructure(final MigrationTracer<ImportRoot> journal) throws ValidationException
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("Start to validate the migration data structure ");
		}

		validateCircabcImpl(journal);
	}


	public void validateXMLFile(final Unmarshaller unmarshaller, final InputStream resource, final MigrationTracer<ImportRoot> journal) throws ValidationException
	{
		if(schemaAsRessource == null)
		{
			throw new NullPointerException("A default schema is required");
		}

		validateXMLFile(unmarshaller, resource, schemaAsRessource, journal);
	}

	public void validateXMLFile(final Unmarshaller unmarshaller, final InputStream resource, final Resource xsdResource, final MigrationTracer<ImportRoot> journal) throws ValidationException
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("Start to validate the migration xml file");
		}

		try
		{
			final ImportRoot root = validateXmlImpl(unmarshaller, resource, xsdResource);
			journal.setUnmarshalledObject(root);
		}
		catch (final ClassCastException e)
		{
			throw new ValidationException(
					buildFatalMessage("The xml root element must be 'ImportRoot'", e));
		}
		catch (final JAXBException e)
		{
			throw new ValidationException(
					buildFatalMessage("Error during binding xml", e));
		}
		catch (final SAXException e)
		{
			throw new ValidationException(
					buildFatalMessage("Error during parsing xml", e));
		}
		catch (final IOException e)
		{
			throw new ValidationException(
					buildFatalMessage("Impossible to read the specified file", e));
		}
		catch (final NullPointerException e)
		{
			throw new ValidationException(
					buildFatalMessage("The element /importRoot is mandatory", e));
		}

	}

	protected ImportRoot validateXmlImpl(final Unmarshaller unmarshaller, final InputStream resource, final Resource xsdFile) throws JAXBException, SAXException, IOException, ClassCastException
	{
		//	set the listener
		if(listener != null)
		{
			unmarshaller.setListener(listener);

			if(logger.isDebugEnabled())
			{
				logger.debug("Listener setted: " + listener.getClass().getName());
			}
		}

		final ValidationEventHandler eventHandler = getEventHandler();

		// set the event handler
		unmarshaller.setEventHandler(eventHandler);

		if(logger.isDebugEnabled())
		{
			logger.debug("Validation Event Handler setted: " + eventHandler.getClass().getName());
		}

		if(xsdFile != null)
		{
			// set	the schema
			final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			final Schema schema = sf.newSchema(xsdFile.getURL());

			unmarshaller.setSchema(schema);
		}


		if(logger.isDebugEnabled())
		{
			logger.debug("Schema setted: " + schemaAsRessource.getFile());
		}

		long start = 0;
		if(logger.isInfoEnabled())
		{
			start = System.currentTimeMillis();
		}
		ImportRoot rootElement = null; 
		XMLStreamReader xmlStreamReader = null;
		try
		{
			XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
	        xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
	        xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
	        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
	        xmlStreamReader = xmlInputFactory.createXMLStreamReader(resource);			
			
			rootElement = (ImportRoot) unmarshaller.unmarshal(xmlStreamReader);
		}
		catch (XMLStreamException e) {
			logger.error(e);
			throw new IOException(e);
		}
		finally
        {
        	if (resource != null)
		    {
        		try 
        		{
        			resource.close();
        		}
        		catch (IOException ex) {
    				logger.warn("Could not close InputStream", ex);
    			}
		    }
        	
        	if (xmlStreamReader != null) {
        		try {
					xmlStreamReader.close();
				} 
        		catch (XMLStreamException e) {
    				logger.warn("Could not close XMLStreamReader", e);
				}
        	}
        }
	

		if(logger.isInfoEnabled())
		{
			logger.info("**********************************************************************************");
			logger.info("Unmarshall time: " + ((System.currentTimeMillis() - start)/1000l) + " secondes.");
			logger.info("*********************************************************************************");
		}

		// test null pointer on root element and cirabc
		if(rootElement == null)
		{
			throw new NullPointerException("The root element is mandatory");
		}

		return rootElement;
	}

	private void validateCircabcImpl(final MigrationTracer<ImportRoot> journal) throws ValidationException
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("XML first elements successfully retreived: /importRoot");
		}

		long start = 0;
		if(logger.isInfoEnabled())
		{
			start = System.currentTimeMillis();
		}

		for(final String validatorClass : validators)
		{
			final Validator validator = buildValidator(validatorClass);

			try
			{
				validator.performValidation(circabcServiceRegistry, journal);
			}
			catch (Exception e)
			{
				final MigrationLog errorMsg = buildFatalMessage(
						"An fatal exception occurs launching validator: " + validator.getClass() , e);

				journal.addValidationMessage(errorMsg);

				logger.error("Validator " + errorMsg, e);
			}

			if(logger.isDebugEnabled())
			{
				logger.debug("Validation performed: " + validator.getClass().getName());
			}
		}

		final List<MigrationLog> errors = new ArrayList<MigrationLog>();

		for(final MigrationLog msg : journal.getValidationMessages())
		{
			switch (msg.getMessageLevel())
			{
				case ERROR : case FATAL :
					errors.add(msg);
					break;
			}
		}

		if(logger.isInfoEnabled())
		{
			logger.info("**********************************************************************************");
			logger.info("JXPath java validation: " + ((System.currentTimeMillis() - start)/1000l) + " secondes.");
			logger.info("*********************************************************************************");
		}

		if(errors.size() > 0)
		{
			if(logger.isDebugEnabled())
			{
				logger.debug(errors.size() + " errors were found in the import file");
			}

			throw new ValidationException(errors);
		}
		else
		{
			if(logger.isDebugEnabled())
			{
				logger.debug("No error found in the import file");
			}
		}
	}

	private final Validator buildValidator(final String validatorClassName) throws ValidationException
	{
		try
		{
			return ReflexionUtils.buildValidator(validatorClassName);
		}
		catch(Exception e)
		{
			throw new ValidationException(e.getMessage());
		}
	}


	/**
	 * @param validator
	 * @param exception
	 * @return
	 */
	private MigrationLog buildFatalMessage(final String message, final Exception exception)
	{
		final MigrationLog errorMsg = new MigrationLog(
				MigrationLog.LogLevel.FATAL,
				message +
				"\n\tExcpetion Type: " + exception.getClass() +
				"\n\tMessage: " + exception.getMessage(),
				exception);

		return errorMsg;
	}

	/**
	 * @param listener the listener to set
	 */
	public final void setListener(final Listener listener)
	{
		this.listener = listener;
	}

	/**
	 * @param schemaLocation the schemaLocation to set
	 */
	public final void setSchemaLocation(final String schemaLocation)
	{
		ParameterCheck.mandatoryString("The schema Location", schemaLocation);

		final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		this.schemaAsRessource = resolver.getResource(schemaLocation);

		if(logger.isDebugEnabled())
		{
			logger.debug("The schema is located under: " + schemaLocation);
		}
	}

	/**
	 * @param eventHandler	the validationEventHandler to set.
	 *
	 **/
	public final void setEventHandler(final ValidationEventHandler eventHandler)
	{
		this.eventHandler = eventHandler;
	}

	/**
	 * Return the validation event handler. If no specified, return the default one.
	 *
	 * @see javax.xml.bind.helpers.DefaultValidationEventHandler
	 * @return the eventHandler
	 */
	public final ValidationEventHandler getEventHandler()
	{
		if(eventHandler != null)
		{
			return eventHandler;
		}
		else
		{
			return new javax.xml.bind.helpers.DefaultValidationEventHandler();
		}
	}


	/**
	 * @param validators the validators to set
	 */
	public final void setValidators(List<String> validators)
	{
		this.validators = validators;
	}


	/**
	 * @param circabcServiceRegistry the circabcServiceRegistry to set
	 */
	public final void setCircabcServiceRegistry(CircabcServiceRegistry circabcServiceRegistry)
	{
		this.circabcServiceRegistry = circabcServiceRegistry;
	}
}
