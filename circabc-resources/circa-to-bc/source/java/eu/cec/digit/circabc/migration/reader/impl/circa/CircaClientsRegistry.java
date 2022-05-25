/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa;

import org.alfresco.service.namespace.QName;

import eu.cec.digit.circabc.migration.processor.ResourceManager;
import eu.cec.digit.circabc.migration.reader.CalendarReader;
import eu.cec.digit.circabc.migration.reader.LogFileReader;
import eu.cec.digit.circabc.migration.reader.MetadataReader;
import eu.cec.digit.circabc.migration.reader.NewsgroupReader;
import eu.cec.digit.circabc.migration.reader.RemoteFileReader;
import eu.cec.digit.circabc.migration.reader.SecurityReader;
import eu.cec.digit.circabc.migration.reader.UserReader;
import eu.cec.digit.circabc.migration.reader.impl.circa.berkeley.DbClient;
import eu.cec.digit.circabc.migration.reader.impl.circa.dao.CircaDaoFactory;
import eu.cec.digit.circabc.migration.reader.impl.circa.file.CircaHomePageReader;
import eu.cec.digit.circabc.migration.reader.impl.circa.file.CircaIGConfigReader;
import eu.cec.digit.circabc.migration.reader.impl.circa.file.FileClient;
import eu.cec.digit.circabc.migration.reader.impl.circa.ldap.CircaLdapClient;
import eu.cec.digit.circabc.migration.reader.impl.circa.nntp.NNTPClient;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.namespace.CircabcNameSpaceService;

/**
 * @author Yanick Pignot
 *
 */
public class CircaClientsRegistry
{
	private static final String SERVICE_NAME = "migration.circa.clients.registry";
	private static final QName SERVICE_QNAME = QName.createQName(CircabcNameSpaceService.CEC_DIGIT_URI, SERVICE_NAME);

	private RemoteFileReader libFileReader;
	private RemoteFileReader infFileReader;
	private SecurityReader securityReader;
	private MetadataReader metadataReader;
	private UserReader userReader;
	private CalendarReader calendarReader;
	private NewsgroupReader newsgroupReader;
	private LogFileReader logFileReader;
	private ResourceManager resourceManager;

	private DbClient dbClient;
	private CircaDaoFactory daoFactory;
	private CircaHomePageReader homePageReader;
	private CircaIGConfigReader configReader;
	private FileClient fileClient;
	private CircaLdapClient ldapClient;
	private NNTPClient nntpClient;

	public static final CircaClientsRegistry getInstance(CircabcServiceRegistry circabcServiceRegistry)
	{
		return (CircaClientsRegistry) circabcServiceRegistry.getService(SERVICE_QNAME);
	}


	/**
	 * @return the configReader
	 */
	public final CircaIGConfigReader getConfigReader()
	{
		return configReader;
	}

	/**
	 * @param configReader the configReader to set
	 */
	public final void setConfigReader(CircaIGConfigReader configReader)
	{
		this.configReader = configReader;
	}

	/**
	 * @return the daoFactory
	 */
	public final CircaDaoFactory getDaoFactory()
	{
		return daoFactory;
	}

	/**
	 * @param daoFactory the daoFactory to set
	 */
	public final void setDaoFactory(CircaDaoFactory daoFactory)
	{
		this.daoFactory = daoFactory;
	}

	/**
	 * @return the dbClient
	 */
	public final DbClient getDbClient()
	{
		return dbClient;
	}

	/**
	 * @param dbClient the dbClient to set
	 */
	public final void setDbClient(DbClient dbClient)
	{
		this.dbClient = dbClient;
	}

	/**
	 * @return the fileClient
	 */
	public final FileClient getFileClient()
	{
		return fileClient;
	}

	/**
	 * @param fileClient the fileClient to set
	 */
	public final void setFileClient(FileClient fileClient)
	{
		this.fileClient = fileClient;
	}

	/**
	 * @return the homePageReader
	 */
	public final CircaHomePageReader getHomePageReader()
	{
		return homePageReader;
	}

	/**
	 * @param homePageReader the homePageReader to set
	 */
	public final void setHomePageReader(CircaHomePageReader homePageReader)
	{
		this.homePageReader = homePageReader;
	}

	/**
	 * @return the ldapClient
	 */
	public final CircaLdapClient getLdapClient()
	{
		return ldapClient;
	}

	/**
	 * @param ldapClient the ldapClient to set
	 */
	public final void setLdapClient(CircaLdapClient ldapClient)
	{
		this.ldapClient = ldapClient;
	}

	/**
	 * @return the nntpClient
	 */
	public final NNTPClient getNntpClient()
	{
		return nntpClient;
	}

	/**
	 * @param nntpClient the nntpClient to set
	 */
	public final void setNntpClient(NNTPClient nntpClient)
	{
		this.nntpClient = nntpClient;
	}


	/**
	 * @return the calendarReader
	 */
	public final CalendarReader getCalendarReader()
	{
		return calendarReader;
	}


	/**
	 * @param calendarReader the calendarReader to set
	 */
	public final void setCalendarReader(CalendarReader calendarReader)
	{
		this.calendarReader = calendarReader;
	}


	/**
	 * @return the infFileReader
	 */
	public final RemoteFileReader getInfFileReader()
	{
		return infFileReader;
	}


	/**
	 * @param infFileReader the infFileReader to set
	 */
	public final void setInfFileReader(RemoteFileReader infFileReader)
	{
		this.infFileReader = infFileReader;
	}


	/**
	 * @return the libFileReader
	 */
	public final RemoteFileReader getLibFileReader()
	{
		return libFileReader;
	}


	/**
	 * @param libFileReader the libFileReader to set
	 */
	public final void setLibFileReader(RemoteFileReader libFileReader)
	{
		this.libFileReader = libFileReader;
	}


	/**
	 * @return the logFileReader
	 */
	public final LogFileReader getLogFileReader()
	{
		return logFileReader;
	}


	/**
	 * @param logFileReader the logFileReader to set
	 */
	public final void setLogFileReader(LogFileReader logFileReader)
	{
		this.logFileReader = logFileReader;
	}


	/**
	 * @return the metadataReader
	 */
	public final MetadataReader getMetadataReader()
	{
		return metadataReader;
	}


	/**
	 * @param metadataReader the metadataReader to set
	 */
	public final void setMetadataReader(MetadataReader metadataReader)
	{
		this.metadataReader = metadataReader;
	}


	/**
	 * @return the newsgroupReader
	 */
	public final NewsgroupReader getNewsgroupReader()
	{
		return newsgroupReader;
	}


	/**
	 * @param newsgroupReader the newsgroupReader to set
	 */
	public final void setNewsgroupReader(NewsgroupReader newsgroupReader)
	{
		this.newsgroupReader = newsgroupReader;
	}


	/**
	 * @return the securityReader
	 */
	public final SecurityReader getSecurityReader()
	{
		return securityReader;
	}


	/**
	 * @param securityReader the securityReader to set
	 */
	public final void setSecurityReader(SecurityReader securityReader)
	{
		this.securityReader = securityReader;
	}


	/**
	 * @return the userReader
	 */
	public final UserReader getUserReader()
	{
		return userReader;
	}


	/**
	 * @param userReader the userReader to set
	 */
	public final void setUserReader(UserReader userReader)
	{
		this.userReader = userReader;
	}


	/**
	 * @return the resourceManager
	 */
	public final ResourceManager getResourceManager()
	{
		return resourceManager;
	}


	/**
	 * @param resourceManager the resourceManager to set
	 */
	public final void setResourceManager(ResourceManager resourceManager)
	{
		this.resourceManager = resourceManager;
	}

}
