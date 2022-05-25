/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader.impl.circa;


import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import eu.cec.digit.circabc.migration.aida.LdapHelper;
import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.XMLNode;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Directory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Forum;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InfSpace;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Information;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Newsgroups;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Space;
import eu.cec.digit.circabc.migration.entities.generated.permissions.AccessProfile;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Application;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Applications;
import eu.cec.digit.circabc.migration.entities.generated.permissions.CategoryAdmin;
import eu.cec.digit.circabc.migration.entities.generated.permissions.DirectoryPermissions;
import eu.cec.digit.circabc.migration.entities.generated.permissions.EmptyAccessProfile;
import eu.cec.digit.circabc.migration.entities.generated.permissions.EventPermissions;
import eu.cec.digit.circabc.migration.entities.generated.permissions.GlobalAccessProfile;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Guest;
import eu.cec.digit.circabc.migration.entities.generated.permissions.ImportedProfile;
import eu.cec.digit.circabc.migration.entities.generated.permissions.InformationPermissionItem;
import eu.cec.digit.circabc.migration.entities.generated.permissions.InformationPermissions;
import eu.cec.digit.circabc.migration.entities.generated.permissions.InformationUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.LibraryPermissionItem;
import eu.cec.digit.circabc.migration.entities.generated.permissions.LibraryPermissions;
import eu.cec.digit.circabc.migration.entities.generated.permissions.LibraryUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.NewsgroupPermissionItem;
import eu.cec.digit.circabc.migration.entities.generated.permissions.NewsgroupPermissions;
import eu.cec.digit.circabc.migration.entities.generated.permissions.NewsgroupUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.NotificationItem;
import eu.cec.digit.circabc.migration.entities.generated.permissions.NotificationStatus;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Notifications;
import eu.cec.digit.circabc.migration.entities.generated.permissions.RegistredUsers;
import eu.cec.digit.circabc.migration.entities.generated.permissions.SimpleDirectoryPermissions;
import eu.cec.digit.circabc.migration.entities.generated.permissions.SimpleEventPermissions;
import eu.cec.digit.circabc.migration.entities.generated.permissions.SimpleInformationPermissions;
import eu.cec.digit.circabc.migration.entities.generated.permissions.SimpleLibraryPermissions;
import eu.cec.digit.circabc.migration.entities.generated.permissions.SimpleNewsgroupPermissions;
import eu.cec.digit.circabc.migration.entities.generated.properties.I18NProperty;
import eu.cec.digit.circabc.migration.entities.generated.properties.Shared;
import eu.cec.digit.circabc.migration.entities.generated.user.Person;
import eu.cec.digit.circabc.migration.reader.SecurityReader;
import eu.cec.digit.circabc.migration.reader.UserReader;
import eu.cec.digit.circabc.migration.reader.impl.circa.berkeley.ApplicationItem;
import eu.cec.digit.circabc.migration.reader.impl.circa.berkeley.ApplicationItemConverter;
import eu.cec.digit.circabc.migration.reader.impl.circa.berkeley.DbClient;
import eu.cec.digit.circabc.migration.reader.impl.circa.berkeley.DbFileLocations;
import eu.cec.digit.circabc.migration.reader.impl.circa.berkeley.PermissionItem;
import eu.cec.digit.circabc.migration.reader.impl.circa.berkeley.PermissionItemConverter;
import eu.cec.digit.circabc.migration.reader.impl.circa.berkeley.SharedSectionsConverter;
import eu.cec.digit.circabc.migration.reader.impl.circa.dao.CircaDaoFactory;
import eu.cec.digit.circabc.migration.reader.impl.circa.domain.Section;
import eu.cec.digit.circabc.migration.reader.impl.circa.file.FileClient;
import eu.cec.digit.circabc.migration.reader.impl.circa.ldap.CircaLdapClient;
import eu.cec.digit.circabc.migration.reader.impl.circa.util.ParsedPath;
import eu.cec.digit.circabc.migration.tools.FilePathUtils;
import eu.cec.digit.circabc.repo.migration.BinderUtils;
import eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Concrete implementation of a security reader for circa using berkeley db files
 * stored on the target circa installation system.
 *
 * @author Yanick Pignot
 */
public class CircaSecurityReaderImpl implements SecurityReader
{
    private static final Log logger = LogFactory.getLog(CircaSecurityReaderImpl.class);

    private static final String SLASH = "/";
    private CircaLdapClient ldapClient;
    private CircaDaoFactory daoFactory;
    private FileClient fileClient;
    private DbClient dbClient;
    private UserReader userReader;
    private String circaDomainPrefix;

    public static final String KEY_PROFILE_NAMES = CircaLdapClient.CLASS_NAMES;

    public static final String POSITIVE_BOOLEAN_VALUE = "1";
    public static final String RIGHTS_SEPARATOR = " ";

    private static final String UID_EQUALS = "uid=";
    private static final String CN_EQUALS = "cn=";
    private static final String CN_END = ",";
    private static final String TITLE_LANG_SEPARATION = ":";


    private static final String DEFAULT_PERMISSIONS = "000 000 000 000 000 000";
    private static final String GUEST_RESERVED_NAME = "EN:Anonymous";
    private static final String REGISTRED_RESERVED_NAME = "EN:Registered";

    private static final String INVALID_PROFILE_ID = "-1";
    private static final String GUEST_ORIGINAL_ID = "5";
    private static final String REGISTRED_ORIGINAL_ID = "6";

    private static Map<String, InformationPermissions> infPermissions = new HashMap<String, InformationPermissions>();
    private static Map<String, LibraryPermissions> libPermissions = new HashMap<String, LibraryPermissions>();
    private static Map<String, DirectoryPermissions> dirPermissions = new HashMap<String, DirectoryPermissions>();
    private static Map<String, EventPermissions> evePermissions = new HashMap<String, EventPermissions>();
    private static Map<String, NotificationStatus> notificationStatus = new HashMap<String, NotificationStatus>();
    private static Map<String, NewsgroupPermissions> newPermissions = new HashMap<String, NewsgroupPermissions>();
    private static Map<String, LibraryPermissions> sharedStatus = new HashMap<String, LibraryPermissions>();

    static{
        infPermissions.put("FFF", InformationPermissions.INF_ADMIN);
        infPermissions.put("001", InformationPermissions.INF_ACCESS);
        infPermissions.put("000", InformationPermissions.INF_NO_ACCESS);
        infPermissions.put("03F", InformationPermissions.INF_MANAGE);

        libPermissions.put("FFF", LibraryPermissions.LIB_ADMIN);
        libPermissions.put("001", LibraryPermissions.LIB_ACCESS);
        libPermissions.put("000", LibraryPermissions.LIB_NO_ACCESS);
        libPermissions.put("03F", LibraryPermissions.LIB_FULL_EDIT);
        libPermissions.put("003", LibraryPermissions.LIB_MANAGE_OWN);

        dirPermissions.put("FFF", DirectoryPermissions.DIR_ADMIN);
        dirPermissions.put("001", DirectoryPermissions.DIR_ACCESS);
        dirPermissions.put("000", DirectoryPermissions.DIR_NO_ACCESS);
        dirPermissions.put("007", DirectoryPermissions.DIR_MANAGE_MEMBERS);
        dirPermissions.put("03F", DirectoryPermissions.DIR_MANAGE_MEMBERS);

        evePermissions.put("FFF", EventPermissions.EVE_ADMIN);
        evePermissions.put("001", EventPermissions.EVE_ACCESS);
        evePermissions.put("000", EventPermissions.EVE_NO_ACCESS);

        newPermissions.put("FFF", NewsgroupPermissions.NWS_ADMIN);
        newPermissions.put("001", NewsgroupPermissions.NWS_ACCESS);
        newPermissions.put("000", NewsgroupPermissions.NWS_NO_ACCESS);
        newPermissions.put("03F", NewsgroupPermissions.NWS_POST);
        newPermissions.put("0FF", NewsgroupPermissions.NWS_MODERATE);

        notificationStatus.put("1", NotificationStatus.SUSCRIBE);
        notificationStatus.put("2", NotificationStatus.SUSCRIBE);
        notificationStatus.put("3", NotificationStatus.UNSUSCRIBE);

        sharedStatus.put("000", LibraryPermissions.LIB_NO_ACCESS);
        sharedStatus.put("001", LibraryPermissions.LIB_ACCESS);
        sharedStatus.put("003", LibraryPermissions.LIB_MANAGE_OWN);
    }

    public void setNotification(final XMLNode node) throws ExportationException
    {
        final XMLNode service = ElementsHelper.getElementIgService(node);
        final ParsedPath parsedPath = new ParsedPath(ElementsHelper.getExportationPath(node), fileClient, circaDomainPrefix);
        final String notificationFile;

        if(parsedPath.isFromLibrary())
        {

            notificationFile =
                 DbFileLocations.getLibraryItemNotificationFile(fileClient.getDataRoot(), parsedPath.getVirtualCirca(), parsedPath.getInterestGroup());
        }
        else if (parsedPath.isFromNewsgroup() && service instanceof Newsgroups)
        {
            // in circa; notifications are only setted on the newsgroup
            notificationFile =
                 DbFileLocations.getNewsgroupItemNotificationFile(fileClient.getDataRoot(), parsedPath.getVirtualCirca(), parsedPath.getInterestGroup());
        }
        else
        {
            return;
        }

        final List<PermissionItem> permissionValue = dbClient.readKey(notificationFile, parsedPath.getInServicePath(), new PermissionItemConverter());

        if(logger.isDebugEnabled() && permissionValue.size() < 1)
        {
            logger.debug("  ---  No Notification defined");
        }

        for(final PermissionItem item: permissionValue)
        {
            try
            {

                final Method getNotifMethod = node.getClass().getMethod("getNotifications", new Class[]{});
                Notifications notifications = (Notifications) getNotifMethod.invoke(node, new Object[]{});

                if(notifications == null)
                {
                    final Method setNotifMethod = node.getClass().getMethod("setNotifications", new Class[]{Notifications.class});
                    notifications = new Notifications();
                    setNotifMethod.invoke(node, new Object[]{notifications});
                }

                final NotificationItem notificationItem = new NotificationItem();
                final NotificationStatus notificationStatusValue = notificationStatus.get(item.getPermissionCode());
                notificationItem.withStatus(notificationStatusValue);

                if(logger.isDebugEnabled())
                {
                    logger.debug("  ---  With notification:" );
                    logger.debug("  -----  Status:   " + notificationStatusValue.value());
                    logger.debug("  -----  Autority: " + item.getAuthority());
                    logger.debug("  -----  Type:     " + ((item.isProfile()) ? "Profile" : "User"));
                }

                if(item.isProfile())
                {
                	final InterestGroup ig = ElementsHelper.getElementInterestGroup(node);
                    notificationItem.withProfile(LdapHelper.generateValidProfileName(ig, item.getAuthority()));
                }
                else
                {
                    notificationItem.withUser(item.getAuthority());
                }

                notifications.withNotifications(notificationItem);
            }
            catch (Exception e)
            {
                throw new ExportationException("Impossible to set notification " + item.getPermissionCode() + " to " + item.getAuthority(), e);
            }
        }

    }

    public void setPermission(final XMLNode node) throws ExportationException
    {
        if(node instanceof Circabc)
        {
            // don't retreive circa admin
        }
        else if(node instanceof Category)
        {
            final Category category = (Category) node ;

            CategoryAdmin admins = category.getCategoryAdmin();
            if(admins == null)
            {
                admins = new CategoryAdmin();
                category.setCategoryAdmin(admins);
            }

            final List<Attributes> categoryAttributes = ldapClient.queryCircaRoot(MessageFormat.format(CircaLdapClient.QUERY_SPECIFIC_CATEGORY, (String) category.getName().getValue()));
            try
            {
                final List<String> userNames = LdapHelper.safeValues(categoryAttributes.get(0), CircaLdapClient.KEY_INVITED_PERSONS, true, ldapClient.getSystemEncoding());

                if(logger.isDebugEnabled() && userNames.size() < 1)
                {
                    logger.debug("  ---  No category admin defined");
                }

                for(String userName: userNames)
                {
                    final String admin = LdapHelper.retrieveValueWithIdInList(userName, UID_EQUALS);
					if(admins.getUsers().contains(admin) == false)
                    {
						admins.withUsers(admin);
                    }
                }
            }
            catch (NamingException e)
            {
                throw new ExportationException("Problem accessing to the category " + category.getName() + " ldap definition ", e);
            }
        }
        else if(node instanceof Space)
        {
        	final Space space = (Space) node;

        	final ParsedPath parsedPath = new ParsedPath(ElementsHelper.getExportationPath(node), fileClient, circaDomainPrefix);

            final String permissionFile =
                DbFileLocations.getLibraryItemAclFile(fileClient.getDataRoot(), parsedPath.getVirtualCirca(), parsedPath.getInterestGroup());

            final List<PermissionItem> permissionValue = dbClient.readKey(permissionFile, parsedPath.getInServicePath(), new PermissionItemConverter());

            if(logger.isDebugEnabled() && permissionValue.size() < 1)
            {
                logger.debug("  ---  No Permission defined");
            }

            for(final PermissionItem item: permissionValue)
            {
            	if(item.isInterestGroup())
                {
            		 final LibraryPermissions sharedPerms = sharedStatus.get(item.getPermissionCode());

            		 final Category cat = ElementsHelper.getElementCategory(space);

            		 final StringBuffer igReference = new StringBuffer(SLASH)
                             .append(Circabc.class.getSimpleName())
                             .append(SLASH)
                             .append(cat.getName().getValue()) // the category
                             .append(SLASH)
                             .append(item.getAuthority()); // the ig root

            	   space.withShareds(new Shared(sharedPerms, igReference.toString()));

	            	if(logger.isDebugEnabled())
	                {
	                    logger.debug("  ---  Shared to " + igReference + " with max permission " + sharedPerms.value());
	                }
                }
                else
                {
                	 LibraryUserRights permissions = space.getLibraryUserRights();
                     if(permissions == null)
                     {
                         permissions = new LibraryUserRights();
                         space.setLibraryUserRights(permissions);
                     }

                     final LibraryPermissionItem permissionItem = new LibraryPermissionItem();
                     final LibraryPermissions libraryPermissionValue = libPermissions.get(item.getPermissionCode());
                     permissionItem.withRight(libraryPermissionValue);

                     if(logger.isDebugEnabled())
                     {
                         logger.debug("  ---  With permission:" );
                         logger.debug("  -----  Permission: " + libraryPermissionValue.value());
                         logger.debug("  -----  Autority:   " + item.getAuthority());
                         logger.debug("  -----  Type:       " + ((item.isProfile()) ? "Profile" : "User"));
                     }

                	if(item.isProfile())
                	{
                		final InterestGroup ig = ElementsHelper.getElementInterestGroup(node);
                		permissionItem.withProfile(LdapHelper.generateValidProfileName(ig, item.getAuthority()));
                	}
                	else
                	{
                		permissionItem.withUser(item.getAuthority());
                	}

                	permissions.withPermissions(permissionItem);
                }
            }
        }
        else if(node instanceof InfSpace || node instanceof Information)
        {
            boolean isRoot = node instanceof Information;

            final ParsedPath parsedPath = new ParsedPath(ElementsHelper.getExportationPath(node), fileClient, circaDomainPrefix);

            final String permissionFile =
                DbFileLocations.getInformationItemAclFile(fileClient.getDataRoot(), parsedPath.getVirtualCirca(), parsedPath.getInterestGroup());

            final String inServicePath = isRoot ? "/" : parsedPath.getInServicePath();
            final List<PermissionItem> permissionValue = dbClient.readKey(permissionFile, inServicePath, new PermissionItemConverter());

            if(logger.isDebugEnabled() && permissionValue.size() < 1)
            {
                logger.debug("  ---  No Permission defined");
            }

            if(isRoot)
            {
                if(permissionValue.size() > 0 && logger.isWarnEnabled())
                {
                    logger.warn("Circabc doesn't support the settings of permissions on the Information Root Node. For that, please to use the 'Manage Access Profile Dialog'");

                    for(final PermissionItem item: permissionValue)
                    {
                        logger.warn("!!! IGNORED: (" + parsedPath.getVirtualCirca() + ":" + parsedPath.getInterestGroup() + ":information) " + item.getPermissionCode() + " for " + ((item.isProfile()) ? " profile " : " user ") + item.getAuthority());
                    }

                }
            }
            else
            {
                final InfSpace space = (InfSpace) node;

                for(final PermissionItem item: permissionValue)
                {
                    InformationUserRights permissions = space.getInformationUserRights();
                    if(permissions == null)
                    {
                        permissions = new InformationUserRights();
                        space.setInformationUserRights(permissions);
                    }

                    final InformationPermissionItem permissionItem = new InformationPermissionItem();
                    final InformationPermissions informationPermissionValue = infPermissions.get(item.getPermissionCode());
                    permissionItem.withRight(informationPermissionValue);

                    if(logger.isDebugEnabled())
                    {
                        logger.debug("  ---  With permission:" );
                        logger.debug("  -----  Permission: " + informationPermissionValue.value());
                        logger.debug("  -----  Autority:   " + item.getAuthority());
                        logger.debug("  -----  Type:       " + ((item.isProfile()) ? "Profile" : "User"));
                    }

                    if(item.isProfile())
                    {
                    	final InterestGroup ig = ElementsHelper.getElementInterestGroup(node);
                        permissionItem.withProfile(LdapHelper.generateValidProfileName(ig, item.getAuthority()));
                    }
                    else
                    {
                        permissionItem.withUser(item.getAuthority());
                    }

                    permissions.withPermissions(permissionItem);

                }
            }
        }
        else if(node instanceof Forum)
        {
            final ParsedPath parsedPath = new ParsedPath(ElementsHelper.getExportationPath(node), fileClient, circaDomainPrefix);
            final String permissionFile =
                DbFileLocations.getNewsgroupItemAclFile(fileClient.getDataRoot(), parsedPath.getVirtualCirca(), parsedPath.getInterestGroup());

            final String inServicePath = parsedPath.getInServicePath();
			List<PermissionItem> permissionValue = dbClient.readKey(permissionFile, inServicePath, new PermissionItemConverter());
            if (permissionValue.isEmpty() && inServicePath.startsWith("/"))
            {
            	final String servicePath = inServicePath.substring(1);
            	permissionValue = dbClient.readKey(permissionFile, servicePath, new PermissionItemConverter());
            }

            if(logger.isDebugEnabled() && permissionValue.size() < 1)
            {
                logger.debug("  ---  No Permission defined");
            }

            final Forum forum = (Forum) node;

            for(final PermissionItem item: permissionValue)
            {
                NewsgroupUserRights permissions = forum.getNewsgroupUserRights();
                if(permissions == null)
                {
                    permissions = new NewsgroupUserRights();
                    forum.setNewsgroupUserRights(permissions);
                }

                final NewsgroupPermissionItem permissionItem = new NewsgroupPermissionItem();
                final NewsgroupPermissions newsPermissionValue = newPermissions.get(item.getPermissionCode());
                permissionItem.withRight(newsPermissionValue);

                if(logger.isDebugEnabled())
                {
                    logger.debug("  ---  With permission:" );
                    logger.debug("  -----  Permission: " + newsPermissionValue.value());
                    logger.debug("  -----  Autority:   " + item.getAuthority());
                    logger.debug("  -----  Type:       " + ((item.isProfile()) ? "Profile" : "User"));
                }

                if(item.isProfile())
                {
                	final InterestGroup ig = ElementsHelper.getElementInterestGroup(node);
                    permissionItem.withProfile(LdapHelper.generateValidProfileName(ig, item.getAuthority()));
                }
                else
                {
                    permissionItem.withUser(item.getAuthority());
                }

                permissions.withPermissions(permissionItem);
            }
        }
    }

    public void setApplicants(final ImportRoot root, final InterestGroup igRoot) throws ExportationException
    {
        final ParsedPath parsedPath = new ParsedPath(ElementsHelper.getExportationPath(igRoot), fileClient, circaDomainPrefix);

        final String permissionFile =
            DbFileLocations.getApplicantsFile(fileClient.getDataRoot(), parsedPath.getVirtualCirca(), parsedPath.getInterestGroup());

        final Map<String, ApplicationItem> applicationItems = dbClient.readFile(permissionFile, new ApplicationItemConverter());

        Applications applications = igRoot.getApplications();

        if(applications == null)
        {
            applications = new Applications();
            igRoot.setApplications(applications);
        }

        final Set<Entry<String, ApplicationItem>> applicationSet = applicationItems.entrySet();

        if(logger.isDebugEnabled() && applicationSet.size() < 1)
        {
            logger.debug("  ---  No Application defined");
        }

        for(final Map.Entry<String, ApplicationItem> item: applicationSet)
        {
            final Application application = new Application(item.getValue().getDate(), item.getKey(), item.getValue().getMessage());
            applications.withApplications(application);

            final Person person = userReader.getPerson(item.getKey());
            if(person != null)
            {
                BinderUtils.addPersons(root, Collections.<Person>singletonList(person), logger);
            }

            if(logger.isDebugEnabled())
            {
                logger.debug("  ---  With Application for membership:" );
                logger.debug("  -----  Date:    " + application.getDate());
                logger.debug("  -----  User:    " + application.getUser());
                logger.debug("  -----  Message: " + application.getMessage());
            }
        }
    }

    public void setProfileDefinition(final InterestGroup interestGroup) throws ExportationException
    {
        final Category category = ElementsHelper.getElementCategory(interestGroup);

        final Serializable catName = category.getName().getValue();
        final Serializable igName = interestGroup.getName().getValue();

        List<Attributes> result;
        try
        {
            /* Set the visibilty for registred and guest profiles */
            final String queryIg = MessageFormat.format(CircaLdapClient.QUERY_INTEREST_GROUP_SETTINGS, igName);
            result = ldapClient.queryCategory((String) catName, queryIg);
            setVisibility(interestGroup, result);

            /* Get all profiles */
            result = ldapClient.queryInterestGroup((String) catName, (String) igName, CircaLdapClient.QUERY_PROFILES);
            setProfiles(interestGroup, result);
        }
        catch (NamingException e)
        {
            throw new ExportationException("Error accessing to the ldap Interest Group data. With name: " + interestGroup,e);
        }
    }

    public void setSharedDefinition(final Space space) throws ExportationException
    {
      	final ParsedPath parsedPath = new ParsedPath(ElementsHelper.getExportationPath(space), fileClient, circaDomainPrefix);

        try
        {
            final Section section = daoFactory.getSectionDao().getSectionsByIdentifier(parsedPath.getVirtualCirca(), parsedPath.getInterestGroup(), parsedPath.toDbReference());

            if(section != null)
            {
                final LibraryPermissions sharedPerms = sharedStatus.get(section.getShareStatus());

                final String sharedSectionsFile =
                    DbFileLocations.getSharedSectionsFile(fileClient.getDataRoot(), parsedPath.getVirtualCirca());

                final String dbKey = parsedPath.toShortLibSectionPath();
                final List<String> links = dbClient.readKey(sharedSectionsFile, dbKey, new SharedSectionsConverter());

                if(links != null && links.size() > 0)
                {
                    final Set<String> igs = new HashSet<String>();

                    for(final String path: links)
                    {
                    	if(isExistingTarget(path))
                    	{
	                        final StringTokenizer tokens = new StringTokenizer(path);
	                        final StringBuffer igReference = new StringBuffer(SLASH);
	
	                        igReference
	                            .append(Circabc.class.getSimpleName())
	                            .append(SLASH)
	                            .append(tokens.nextToken(SLASH)) // the category
	                            .append(SLASH)
	                            .append(tokens.nextToken(SLASH)); // the ig root
	
	                        igs.add(igReference.toString());
                    	}
                    }
                    for(final String ig: igs)
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("  ---  Shared to " + ig + " with max permission " + sharedPerms.value());
                        }

                        space.withShareds(new Shared(sharedPerms, ig));
                    }
                }
                else
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("  ---  Not Shared");
                    }
                }
            }
            else
            {
                logger.warn("Section " + ElementsHelper.getExportationPath(space) + " not found in database. Impossible to set shared definition!");
            }

        }
        catch (SQLException e)
        {
        	throw new ExportationException("Problem reading database for the space: " + parsedPath.toDbReference(),e);
        }
        catch (IOException e)
        {
            throw new ExportationException("Problem reading database for the space: " + parsedPath.toDbReference(),e);
        }
    }

    public Set<CategoryInterestGroupPair> getAllSharedLinkTarget(final CategoryInterestGroupPair pair) throws ExportationException
    {
        final String igPathKey = pair.getCategory() + "/" + pair.getInterestGroup();

        final String sharedSectionsFile =
            DbFileLocations.getSharedSectionsFile(fileClient.getDataRoot(), pair.getCategory());

        final Set<CategoryInterestGroupPair> otherIg = new HashSet<CategoryInterestGroupPair>();

        final Map<String, String> links = dbClient.readFile(sharedSectionsFile);

        final SharedSectionsConverter converter = new SharedSectionsConverter();

        for(Map.Entry<String, String> sharedSection: links.entrySet())
        {
            final List<String> adaptedLinks = converter.adapt(sharedSection.getValue());
            final boolean thisIgKey = sharedSection.getKey().startsWith(igPathKey);

            for(final String link: adaptedLinks)
            {
                if(link.startsWith(igPathKey) && isExistingTarget(link))
                {
            		otherIg.add(FilePathUtils.getInterestGroupFromPath(sharedSection.getKey(), ""));
                }
                else if(thisIgKey  && isExistingTarget(link))
                {
            		otherIg.add(FilePathUtils.getInterestGroupFromPath(link, ""));
                }
            }


        }

        if(logger.isDebugEnabled())
        {
            logger.debug("  --- Interest group dependencies for SharedSpace: " + otherIg);
        }

        return otherIg;
    }

    private boolean isExistingTarget(String link) {
		
    	String completeLink = "./www/data/"+link;
    	boolean result = false;
    	
    	try {
    		
			result = fileClient.exists(completeLink);
			
		} catch (ExportationException e) {
			if(logger.isErrorEnabled())
			{
				logger.error("Error during target link exist verification", e);
			}
		}
    	
		return result;
	}

	public Set<CategoryInterestGroupPair> getAllImportedProfileTarget(final CategoryInterestGroupPair pair) throws ExportationException
    {
        try
        {
            final List<Attributes> result = ldapClient.queryInterestGroup(pair.getCategory(), pair.getInterestGroup(), CircaLdapClient.QUERY_PROFILES);

            final Set<String> otherIg = new HashSet<String>();

            for(final Attributes attr: result)
            {
                if(LdapHelper.safeValue(attr, CircaLdapClient.KEY_IMPORTED_SOURCE, ldapClient.getSystemEncoding()) != null)
                {
                    final String source = LdapHelper.mandatoryValue(attr, CircaLdapClient.KEY_IMPORTED_SOURCE, ldapClient.getSystemEncoding());

                    final int indexStartProfileName =  source.indexOf(CN_EQUALS);
                    final int indexStartIGName =  source.indexOf(CN_EQUALS, indexStartProfileName + 1);

                    final String targetIg = source.substring(indexStartIGName + CN_EQUALS.length(), source.indexOf(CN_END, indexStartIGName));

                    otherIg.add(targetIg);
                }
            }


            if(logger.isDebugEnabled())
            {
                logger.debug("  --- Interest group dependencies for Profiles: " + otherIg);
            }

            return convertToPairs(pair, otherIg);
        }
        catch (NamingException e)
        {
            throw new ExportationException("Error accessing to the ldap Interest Group data. With name: " + pair.toString(),e);
        }
    }

    /**
     * @param pair
     * @param otherIg
     * @return
     */
    private Set<CategoryInterestGroupPair> convertToPairs(final CategoryInterestGroupPair pair, final Set<String> otherIg)
    {
        final Set<CategoryInterestGroupPair> pairs = new HashSet<CategoryInterestGroupPair>(otherIg.size());
        for(final String ig: otherIg)
        {
            pairs.add(new CategoryInterestGroupPair(pair.getCategory(), ig));
        }

        return pairs;
    }

    /**
     * @param ldapClient the ldapClient to set
     */
    public final void setLdapClient(final CircaLdapClient ldapClient)
    {
        this.ldapClient = ldapClient;
    }


    private void setVisibility(final InterestGroup interestGroup, final List<Attributes> result) throws ExportationException, NamingException
    {
        final Directory directory = interestGroup.getDirectory();

        if(result.size() < 1)
        {
            throw new ExportationException("Impossible to found Interest Group ldap settings. With name: " + interestGroup.getName());
        }
        if(result.size() > 1)
        {
            throw new ExportationException("To many results for Interest Group ldap settings. With name: " + interestGroup + ": " + result.size());
        }
        else
        {
            final Attributes attributes = result.get(0);

            // public or registred access startus: 1=Activated - 0=Not activated
            directory.getGuest().setVisibility(isKeyPositiveBoolean(attributes, CircaLdapClient.KEY_PUBLIC));
            directory.getRegistredUsers().setVisibility(isKeyPositiveBoolean(attributes, CircaLdapClient.KEY_REGISTER));
        }
    }

    private void setProfiles(final InterestGroup interestGroup, final List<Attributes> result) throws ExportationException, NamingException
    {
        final Directory directory = interestGroup.getDirectory();

        String profileId;

        // first found the indexes of the reserved profiles (registred and guest)

        final Map<String, Attributes> profilesById = new HashMap<String, Attributes>();
        String guestId = INVALID_PROFILE_ID;
        String registredId = INVALID_PROFILE_ID;

        for(final Attributes attributes: result)
        {
            profileId = LdapHelper.mandatoryValues(attributes, CircaLdapClient.KEY_CN, true, ldapClient.getSystemEncoding()).get(0);

            profilesById.put(profileId, attributes);

            final List<String> names = LdapHelper.safeValues(attributes, KEY_PROFILE_NAMES, true, ldapClient.getSystemEncoding());

            // name can be null for imported profiles
            if(names != null)
            {
                if(names.contains(REGISTRED_RESERVED_NAME) && !registredId.equals(REGISTRED_ORIGINAL_ID))
                {
                    registredId = profileId;

                }
                else if(names.contains(GUEST_RESERVED_NAME) && !guestId.equals(GUEST_ORIGINAL_ID))
                {
                    guestId = profileId;
                }
            }
        }

        final Guest guest = directory.getGuest();
        final RegistredUsers registred = directory.getRegistredUsers();

        if((profilesById.get(guestId)) != null)
        {
        	parseWrites(guest,  LdapHelper.safeValue(profilesById.get(guestId), CircaLdapClient.KEY_PERMISSIONS, ldapClient.getSystemEncoding()));
        	ElementsHelper.setXPath(guest, guestId);
        }
        else
        {
        	parseWrites(guest,  DEFAULT_PERMISSIONS);
        }

        if((profilesById.get(registredId)) != null)
        {
        	parseWrites(registred,  LdapHelper.safeValue(profilesById.get(registredId), CircaLdapClient.KEY_PERMISSIONS, ldapClient.getSystemEncoding()));
        	ElementsHelper.setXPath(registred, registredId);
        }
        else
        {
        	parseWrites(registred,  DEFAULT_PERMISSIONS);
        }

        directory.withGuest(guest);
        directory.withRegistredUsers(registred);

        if(logger.isDebugEnabled())
        {
            logger.debug("  ---  With Guest Profile:" );
            logger.debug("  -----  Visibility:    " + guest.isVisibility());
            logger.debug("  -----  Title(s):      " + guest.getI18NTitles());
            logger.debug("  -----  InfPermission: " + guest.getInformationPermission().toString());
            logger.debug("  -----  LibPermission: " + guest.getLibraryPermission().toString());
            logger.debug("  -----  NewPermission: " + guest.getNewsgroupPermission().toString());
            logger.debug("  -----  DirPermission: " + guest.getDirectoryPermission().toString());
            logger.debug("  -----  EvePermission: " + guest.getEventPermission().toString());

            logger.debug("  ---  With Registred Profile:" );
            logger.debug("  -----  Visibility:    " + registred.isVisibility());
            logger.debug("  -----  Title(s):      " + registred.getI18NTitles());
            logger.debug("  -----  InfPermission: " + registred.getInformationPermission().toString());
            logger.debug("  -----  LibPermission: " + registred.getLibraryPermission().toString());
            logger.debug("  -----  NewPermission: " + registred.getNewsgroupPermission().toString());
            logger.debug("  -----  DirPermission: " + registred.getDirectoryPermission().toString());
            logger.debug("  -----  EvePermission: " + registred.getEventPermission().toString());
        }

        // remove guest and registred from the list of profiles.
        profilesById.remove(guestId);
        profilesById.remove(registredId);

        Attributes attributes;
        for(final Map.Entry<String, Attributes> entry: profilesById.entrySet())
        {
            profileId = entry.getKey();
            attributes = entry.getValue();

            if(LdapHelper.safeValue(attributes, CircaLdapClient.KEY_IMPORTED_SOURCE, ldapClient.getSystemEncoding()) != null)
            {
                final ImportedProfile profile = new ImportedProfile();

                final String source = LdapHelper.mandatoryValue(attributes, CircaLdapClient.KEY_IMPORTED_SOURCE, ldapClient.getSystemEncoding());

                int indexStartProfileName =  source.indexOf(CN_EQUALS);
                int indexStartIGName =  source.indexOf(CN_EQUALS, indexStartProfileName + 1);

                final String targetProfile = source.substring(indexStartProfileName + CN_EQUALS.length(), source.indexOf(CN_END, indexStartProfileName));
                final String targetIg = source.substring(indexStartIGName + CN_EQUALS.length(), source.indexOf(CN_END, indexStartIGName));

                profile.setTargetInterestgroup(targetIg);
                profile.setTargetProfile(LdapHelper.generateValidProfileName(null, targetProfile));

                parseWrites(profile, LdapHelper.mandatoryValue(attributes, CircaLdapClient.KEY_PERMISSIONS, ldapClient.getSystemEncoding()));

                directory.withImportedProfiles(profile);

                if(logger.isDebugEnabled())
                {
                    logger.debug("  ---  With Exported Profile:" );
                    logger.debug("  -----  From IG:       " + profile.getTargetInterestgroup());
                    logger.debug("  -----  From Name:     " + profile.getTargetProfile());
                    logger.debug("  -----  Title(s):      " + profile.getI18NTitles());
                    logger.debug("  -----  InfPermission: " + profile.getInformationPermission().toString());
                    logger.debug("  -----  LibPermission: " + profile.getLibraryPermission().toString());
                    logger.debug("  -----  NewPermission: " + profile.getNewsgroupPermission().toString());
                    logger.debug("  -----  DirPermission: " + profile.getDirectoryPermission().toString());
                    logger.debug("  -----  EvePermission: " + profile.getEventPermission().toString());
                }
            }
            else
            {
                final List<String> profileTitles = LdapHelper.mandatoryValues(attributes, KEY_PROFILE_NAMES, true, ldapClient.getSystemEncoding());

                final AccessProfile profile = new AccessProfile();

                profile
                    .withName(LdapHelper.generateValidProfileName(null, profileId))
                    .withExported(isKeyPositiveBoolean(attributes, CircaLdapClient.KEY_EXPORTED))
                    .withI18NTitles(convertI18NStringValues(profileTitles));

                parseWrites(profile, LdapHelper.mandatoryValue(attributes, CircaLdapClient.KEY_PERMISSIONS, ldapClient.getSystemEncoding()));

                final List<String> invitedUsers = LdapHelper.safeValues(attributes, CircaLdapClient.KEY_INVITED_PERSONS, true, ldapClient.getSystemEncoding());
                if(invitedUsers != null)
                {
                    for(final String user: invitedUsers)
                    {
                        profile.withUsers(LdapHelper.retrieveValueWithIdInList(user, UID_EQUALS));
                    }
                }

                if(logger.isDebugEnabled())
                {
                    logger.debug("  ---  With Profile:" );
                    logger.debug("  -----  Name:          " + profile.getName());
                    logger.debug("  -----  Title(s):      " + profile.getI18NTitles());
                    logger.debug("  -----  Exported:      " + profile.isExported());
                    logger.debug("  -----  InfPermission: " + profile.getInformationPermission().toString());
                    logger.debug("  -----  LibPermission: " + profile.getLibraryPermission().toString());
                    logger.debug("  -----  NewPermission: " + profile.getNewsgroupPermission().toString());
                    logger.debug("  -----  DirPermission: " + profile.getDirectoryPermission().toString());
                    logger.debug("  -----  EvePermission: " + profile.getEventPermission().toString());
                }

                directory.withAccessProfiles(profile);
            }
        }
    }

    private List<I18NProperty> convertI18NStringValues(final List<String> profileTitles)
    {
        final List<I18NProperty> titles = new ArrayList<I18NProperty>(profileTitles.size());

        String lang;
        String value;
        for(final String title : profileTitles)
        {
            int separationIndex = title.indexOf(TITLE_LANG_SEPARATION);

            lang = title.substring(0, separationIndex);
            value = title.substring(separationIndex + 1);

            titles.add(new I18NProperty(I18NUtil.parseLocale(lang), value));
        }

        return titles;
    }

    private void parseWrites(final GlobalAccessProfile profile, final String overwrites) throws ExportationException
    {
        final StringTokenizer tokens = new StringTokenizer(overwrites, RIGHTS_SEPARATOR, false);

        InformationPermissions infPerm = getPermission(infPermissions, tokens.nextToken());
        LibraryPermissions libPerm = getPermission(libPermissions, tokens.nextToken());
        DirectoryPermissions dirPerm = getPermission(dirPermissions, tokens.nextToken());
        EventPermissions evePerm = getPermission(evePermissions, tokens.nextToken());
        NewsgroupPermissions newPerm = getPermission(newPermissions, tokens.nextToken());

        /* In circa we can set perms upper than read for guest and registred. Not in circabc.*/
        if(InformationPermissions.INF_NO_ACCESS.equals(infPerm) == false)
        {
            infPerm = InformationPermissions.INF_ACCESS;
        }
        if(LibraryPermissions.LIB_NO_ACCESS.equals(libPerm) == false)
        {
            libPerm = LibraryPermissions.LIB_ACCESS;
        }
        if(DirectoryPermissions.DIR_NO_ACCESS.equals(dirPerm) == false)
        {
            dirPerm = DirectoryPermissions.DIR_ACCESS;
        }
        if(EventPermissions.EVE_NO_ACCESS.equals(evePerm) == false)
        {
            evePerm = EventPermissions.EVE_ACCESS;
        }

        if(NewsgroupPermissions.NWS_NO_ACCESS.equals(newPerm) == false)
        {
            newPerm = NewsgroupPermissions.NWS_ACCESS;
        }
        // Information
        profile.setInformationPermission(SimpleInformationPermissions.fromValue(infPerm.value()));
        // Library
        profile.setLibraryPermission(SimpleLibraryPermissions.fromValue(libPerm.value()));
        // Directory
        profile.setDirectoryPermission(SimpleDirectoryPermissions.fromValue(dirPerm.value()));
        // Events
        profile.setEventPermission(SimpleEventPermissions.fromValue(evePerm.value()));
        // News
        profile.setNewsgroupPermission(SimpleNewsgroupPermissions.fromValue(newPerm.value()));
        // Workflow -- not used
    }

    private void parseWrites(final EmptyAccessProfile profile, final String overwrites) throws ExportationException
    {
        final StringTokenizer tokens = new StringTokenizer(overwrites, RIGHTS_SEPARATOR, false);

        // Information
        profile.setInformationPermission(getPermission(infPermissions, tokens.nextToken()));
        // Library
        profile.setLibraryPermission(getPermission(libPermissions, tokens.nextToken()));
        // Directory
        profile.setDirectoryPermission(getPermission(dirPermissions, tokens.nextToken()));
        // Events
        profile.setEventPermission(getPermission(evePermissions, tokens.nextToken()));
        // News
        profile.setNewsgroupPermission(getPermission(newPermissions, tokens.nextToken()));
        // Workflow -- not used
    }

    private Boolean isKeyPositiveBoolean(final Attributes attributes, final String key) throws NamingException
    {
        final String value = LdapHelper.safeValue(attributes, key, ldapClient.getSystemEncoding());
        return Boolean.valueOf(value != null && POSITIVE_BOOLEAN_VALUE.equals(value));
    }

    private <T> T getPermission(final Map<String, T> permissions, final String code) throws ExportationException
    {
        final T permission = permissions.get(code);

        if(permission != null)
        {
            return permission;
        }
        else
        {
            throw new ExportationException("impossible to found the Circabc " + permissions.values().iterator().next().getClass() + " related to the circa code " + code);
        }
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
     * @return the circaDomainPrefix
     */
    public final String getCircaDomainPrefix()
    {
        return circaDomainPrefix;
    }

    /**
     * @param circaDomainPrefix the circaDomainPrefix to set
     */
    public final void setCircaDomainPrefix(String circaDomainPrefix)
    {
        this.circaDomainPrefix = circaDomainPrefix;
    }

    /**
     * @param dbClient the dbClient to set
     */
    public final void setDbClient(DbClient dbClient)
    {
        this.dbClient = dbClient;
    }

    /**
     * @param daoFactory the daoFactory to set
     */
    public final void setDaoFactory(CircaDaoFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    /**
     * @param userReader the userReader to set
     */
    public final void setUserReader(UserReader userReader)
    {
        this.userReader = userReader;
    }
}




