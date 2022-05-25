package eu.cec.digit.circabc.repo.migration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.migration.archive.ArchiveException;
import eu.cec.digit.circabc.migration.archive.MigrationIteration;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.aida.Users.User;
import eu.cec.digit.circabc.migration.entities.generated.user.Person;
import eu.cec.digit.circabc.migration.journal.etl.ETLReport;
import eu.cec.digit.circabc.migration.journal.etl.PathologicUser;
import eu.cec.digit.circabc.migration.journal.etl.TransformationElement;
import eu.cec.digit.circabc.service.migration.ETLException;
import eu.cec.digit.circabc.service.migration.ETLService;
import eu.cec.digit.circabc.util.CircabcUserDataBean;


public class LdapUsersETLOneShotServiceImpl extends  LdapUsersETLServiceImpl implements ETLService
{
	
	private static final Log logger = LogFactory.getLog(LdapUsersETLOneShotServiceImpl.class);

	private XmlUserType flag;
	private XMLEventFactory eventFactory ; 
	private PersonService personService;
	private UserField flagUserField;

	private static final String ENCODING = "UTF-8";;
	
	@Override
	public void applyEtl(ETLReport report) throws ETLException
	{
			try
			{
				applyETLWork(report);
			} catch (FactoryConfigurationError e)
			{
				 throw new ETLException("Impossible to apply etl the transformed xml", e);
			} catch (XMLStreamException e)
			{
				throw new ETLException("Impossible to apply etl the transformed xml", e);
			} catch (IOException e)
			{
				throw new ETLException("Impossible to apply etl the transformed xml", e);
			}


	        
	        
		
	}
	/**
	 * @param report
	 * @throws FactoryConfigurationError
	 * @throws ETLException
	 * @throws XMLStreamException 
	 * @throws IOException 
	 */
	private void applyETLWork(ETLReport report) throws FactoryConfigurationError, ETLException, XMLStreamException, IOException
	{
		flag = XmlUserType.none;
		flagUserField = UserField.none;
		eventFactory = XMLEventFactory.newInstance();

		ParameterCheck.mandatory("A report ", report);
		ParameterCheck.mandatory("An iteration", report.getIteration().getIdentifier());
		ParameterCheck.mandatory("A migration structure ", report.getOriginalImportRoot());

		final Map<String, TransformationElement> validUsers = new HashMap<String, TransformationElement>(report.getTransformationElements().size() + report.getPathologicUsers().size());
		for(final TransformationElement element: report.getTransformationElements())
		{
		    validUsers.put(safeUid(element.getOriginalPerson()), element);
		}
		storeValidUsers(validUsers);
		
		Set<PathologicUser> createdPatologicalUsers = new HashSet< PathologicUser>(report.getPathologicUsers().size());
		for(final PathologicUser element: report.getPathologicUsers())
		{
			if (element.getPersonWasNull())
			{
				if (logger.isWarnEnabled())
				{
					logger.warn("Person was null" + element);
				}
			}
			else
			{
				CircabcUserDataBean circabcUser = element.createUserDataBean();
				if ( !personService.personExists(circabcUser.getUserName()))
				{
					userService.createUser(circabcUser, false);
				}
				createdPatologicalUsers.add(element);
				validUsers.put(safeUid(element.getPerson()), new TransformationElement(element) );
			}
		}
		for (PathologicUser element : createdPatologicalUsers)
		{
			report.removePathologicalUser(element);
		}
		
		
		NodeRef validFileRef;
		final Date importProcessId = new Date();
		try
		{
		    validFileRef = this.fileArchiver.storeTransformedValidFile(report.getIteration(), importProcessId, null);
		}
		catch (Exception e)
		{
		    throw new ETLException("Impossible to store the transformed xml", e);
		}
		report.setValidImportRoot(validFileRef);
		
		

		NodeRef residualFileRef;
		try
		{
		    residualFileRef = fileArchiver.storeTransformedResidualFile(report.getIteration(), importProcessId, null);
		}
		catch (Exception e)
		{
		    throw new ETLException("Impossible to store the transformed xml", e);
		}

		report.setResidualImportRoot(residualFileRef);
		
		
		
		Reader inputStreamReader = null;
		final NodeRef resource = report.getIteration().getOriginalFileNodeRef();
		final InputStream contentInputStream = super.fileArchiver.getContentInputStream(resource);
		inputStreamReader = new InputStreamReader(contentInputStream,ENCODING);
		final ContentWriter contentWriter = fileArchiver.getContentWriter(validFileRef);
		
		
		
		File tempFile = TempFileProvider.createTempFile(report.getIteration().getIdentifier()+ "abc", "tmp");
		FileOutputStream fos = new FileOutputStream(tempFile);

	    XMLEventReader reader =null;
		XMLEventWriter writer = null; 
        final XMLInputFactory inputInstance = XMLInputFactory.newInstance();
        inputInstance.setProperty(XMLInputFactory.IS_COALESCING, true);
        inputInstance.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        inputInstance.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        inputInstance.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        reader = inputInstance.createXMLEventReader(inputStreamReader);
		final XMLOutputFactory outputInstance = XMLOutputFactory.newInstance();
		writer = outputInstance.createXMLEventWriter(fos,ENCODING);
		boolean isEmptyUserField = false ;

		User currentUser = null;
		while (reader.hasNext())
		{
			XMLEvent event = reader.nextEvent();
			if (event.isStartElement())
			{
				
				StartElement element = (StartElement) event;
				//todo analyze prefix as well  element.getName().getPrefix();
				final String localPart = element.getName().getLocalPart();
				if (localPart.equals("owner") || localPart.equals("creator") || localPart.equals("modifier") || localPart.equals("user") || localPart.equals("users") || localPart.equals("userId"))
				{
					flag = XmlUserType.valueOf(localPart);
				}
				if (localPart.equals("firstName") || localPart.equals("lastName") || localPart.equals("email") )
				{
					flagUserField = UserField.valueOf(localPart);
					isEmptyUserField = true;
				}
 				writer.add(event);
			}
			else if (event.isCharacters())
			{
				Characters characters = (Characters) event;

				if (!flag.equals(XmlUserType.none))
				{
					final String data = characters.getData();
					if (validUsers.containsKey(data))
					{
						final TransformationElement transformationElement = validUsers.get(data);
						final String validUserId = transformationElement.getValidUserId();
						if (validUserId != null)
						{
							writer.add(getNewCharactersEvent(event.asCharacters(), validUserId ));
							if ( flag.equals(XmlUserType.userId  ))
							{
								currentUser = transformationElement.getValidUserData() ;
							}
						}
						else
						{
							writer.add(event);
						}
					}
					else
					{
						writer.add(event);
					}
				}
				else if  ( !flagUserField.equals(UserField.none))
				{
					if (currentUser != null)
					{
						isEmptyUserField = false;
						if ( flagUserField.equals(UserField.firstName ))
						{
							writer.add(getNewCharactersEvent(event.asCharacters(), currentUser.getFirstname()));
						}
						else if ( flagUserField.equals(UserField.lastName))
						{
							writer.add(getNewCharactersEvent(event.asCharacters(), currentUser.getLastname()));
						}
						else if ( flagUserField.equals(UserField.email))
						{
							writer.add(getNewCharactersEvent(event.asCharacters(), currentUser.getEmail()));
						}
					}
					
				}
				else
				{
					writer.add(event);
				}
				flag = XmlUserType.none;
			}
			else if (event.isEndElement())
			{
				EndElement element = (EndElement) event;
				final String localPart = element.getName().getLocalPart();
				if (localPart.equals("firstName") || localPart.equals("lastName") || localPart.equals("email") )
				{
					flagUserField = UserField.valueOf(localPart);
					if (currentUser != null)
					{
						if (isEmptyUserField)
						{
							if ( flagUserField.equals(UserField.firstName ))
							{
								writer.add(getNewEndElementEvent(currentUser.getFirstname()));
							}
							else if ( flagUserField.equals(UserField.lastName))
							{
								writer.add(getNewEndElementEvent(currentUser.getLastname()));
							}
							else if ( flagUserField.equals(UserField.email))
							{
								writer.add(getNewEndElementEvent(currentUser.getEmail()));
							}
							
						}
					}
					flagUserField =  UserField.none;
				}
 				writer.add(event);
			}
			else
			{
				writer.add(event);
			}
		}
		
		writer.flush();
		writer.close();
		contentWriter.putContent(tempFile);
		tempFile.delete();
		
	}
	private XMLEvent getNewCharactersEvent(Characters event , String newValue)
	{
		if (event.getData()!= null) 
		{
            return eventFactory.createCharacters(newValue);
        }
        else {
            return event;
        }
	}
	
	private XMLEvent getNewEndElementEvent(String newValue)
	{
		return eventFactory.createCharacters(newValue);
	}
	@Override
	public ETLReport proposeEtl(final String iterationName) throws ETLException
    {
        final NodeRef xmlDocument = getIterationNodeRef(iterationName);
        return proposeEtl(xmlDocument, iterationName);
    }
	
	
	private ETLReport proposeEtl(final NodeRef resource, final String iterationName) throws ETLException
    {
        ParameterCheck.mandatory("The xml noderef ", resource);
        final MigrationIteration iteration;
        final Map<Object, Object> previousUserMatch;
        try
        {
            iteration = this.fileArchiver.getNodeIteration(resource);
            previousUserMatch = fileArchiver.retreiveAlreadyTransformedUser();
        }
        catch(final ArchiveException e)
        {
            throw new ETLException(e.getMessage(), e);
        }

        try
        {
            final ETLReport report = new ETLReport();
            final Set<String> allusers = getAllUsers(resource);
            final ImportRoot root = unmarshall(resource);

            report.setOriginalImportRoot(resource);
            report.setIteration(iteration);

            if(root == null)
            {
                throw new ETLException("No import root element found in the given export file.");
            }


            
            final Map<String, Person> personsById = new HashMap<String, Person>(allusers.size());

            for(final String user: allusers)
            {
                if(user.trim().length() > 0)
                {
                    personsById.put(user.trim(), null);
                }
            }

            final List<Person> persons  = (root.getPersons() == null) ? Collections.<Person>emptyList() : root.getPersons().getPersons();
            for(final Person person: persons)
            {
                personsById.put(safeUid(person), person);
            }

            for(final Map.Entry<String, Person> entry: personsById.entrySet())
            {
                checkPerson(report, entry.getValue(), entry.getKey(), (String) previousUserMatch.get(entry.getKey()));
            }

            return report;
        }
        catch (final ETLException e)
        {
            throw e;
        }
        catch (final Exception e)
        {
            throw new ETLException("", e);
        }
    }
	
	private Set<String> getAllUsers(final NodeRef resource) throws ETLException
    {

		flag = XmlUserType.none;
		Reader inputStreamReader=null;
		final InputStream contentInputStream = super.fileArchiver.getContentInputStream(resource);
		try {
			inputStreamReader = new InputStreamReader(contentInputStream,ENCODING);
		} catch (UnsupportedEncodingException e1) {
			if (logger.isErrorEnabled())
    		{
    			logger.error("Exceptino when  reading content of node "+   resource , e1);
    		}
		}
		XMLEventReader reader =null;
		try
		{
			XMLInputFactory factoryInstance = XMLInputFactory.newInstance();
			factoryInstance.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
			factoryInstance.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
			factoryInstance.setProperty(XMLInputFactory.SUPPORT_DTD, false);
			reader = factoryInstance.createXMLEventReader(inputStreamReader);
			
		} catch (XMLStreamException e)
		{
			if (logger.isErrorEnabled())
    		{
    			logger.error("Exceptino when  reading content of node "+   resource , e);
    		}
		} catch (FactoryConfigurationError e)
		{
			if (logger.isErrorEnabled())
    		{
    			logger.error("Exceptino when  reading content of node "+   resource , e);
    		}
		}
		
		
		
		Set<String> result = new HashSet<String>();

		if (reader != null)
		{
			while (reader.hasNext())
			{
				XMLEvent event =null;
				try
				{
					event = reader.nextEvent();
				} catch (XMLStreamException e)
				{
					if (logger.isErrorEnabled())
		    		{
		    			logger.error("Exceptino when  reading content of node "+   resource , e);
		    		}
				}
				if (event != null &&  event.isStartElement())
				{
					StartElement element = (StartElement) event;
					final String localPart = element.getName().getLocalPart();
					if (localPart.equals("owner") || localPart.equals("creator") || localPart.equals("modifier") || localPart.equals("user") || localPart.equals("users"))
					{
						flag = XmlUserType.valueOf(localPart);
					}
				}
				else if (event != null && event.isCharacters())
				{
					Characters characters = (Characters) event;
	
					if (!flag.equals(XmlUserType.none))
					{
						final String data = characters.getData();
						switch (flag)
						{
						case creator: case modifier: case owner: case user: case users:
							addIfNotExists(result, data);
							break;
						case none:
							break;
						default:
							break;
						}
	
						
					}
					flag = XmlUserType.none;
				}
			}
		}
		return result;
	
		
    }
	
	private static void addIfNotExists(Set<String> users, final String data)
	{
		if (!users.contains(data))
		{
			users.add(data);
		}
	}
	
	
	public void setPersonService(PersonService personService)
	{
		this.personService = personService;
	}

	public PersonService getPersonService()
	{
		return personService;
	}

	
	
}
