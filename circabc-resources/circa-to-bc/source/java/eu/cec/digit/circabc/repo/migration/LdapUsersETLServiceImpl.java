/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.repo.migration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import eu.cec.digit.circabc.migration.aida.LdapHelper;
import eu.cec.digit.circabc.migration.aida.circa.EmailSpacesRemover;
import eu.cec.digit.circabc.migration.archive.ArchiveException;
import eu.cec.digit.circabc.migration.archive.FileArchiver;
import eu.cec.digit.circabc.migration.archive.MigrationIteration;
import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.TypedProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.URLProperty;
import eu.cec.digit.circabc.migration.entities.XMLElement;
import eu.cec.digit.circabc.migration.entities.XMLNode;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.aida.Users.User;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Content;
import eu.cec.digit.circabc.migration.entities.generated.nodes.ContentNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InfMLContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslation;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InformationTranslationVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslation;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LibraryTranslationVersion;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Link;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Message;
import eu.cec.digit.circabc.migration.entities.generated.nodes.MlContent;
import eu.cec.digit.circabc.migration.entities.generated.nodes.NamedNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Node;
import eu.cec.digit.circabc.migration.entities.generated.nodes.TitledNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Url;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Application;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Applications;
import eu.cec.digit.circabc.migration.entities.generated.permissions.InformationPermissionItem;
import eu.cec.digit.circabc.migration.entities.generated.permissions.InformationUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.LibraryPermissionItem;
import eu.cec.digit.circabc.migration.entities.generated.permissions.LibraryUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.NewsgroupPermissionItem;
import eu.cec.digit.circabc.migration.entities.generated.permissions.NewsgroupUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.NotificationItem;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Notifications;
import eu.cec.digit.circabc.migration.entities.generated.user.Person;
import eu.cec.digit.circabc.migration.entities.generated.user.Persons;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.journal.etl.ETLReport;
import eu.cec.digit.circabc.migration.journal.etl.PathologicUser;
import eu.cec.digit.circabc.migration.journal.etl.TransformationElement;
import eu.cec.digit.circabc.migration.validation.ValidationException;
import eu.cec.digit.circabc.migration.validation.ValidationHandler;
import eu.cec.digit.circabc.service.migration.ETLException;
import eu.cec.digit.circabc.service.migration.ETLService;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.util.CircabcUserDataBean;

/**
 * @author Yanick Pignot
 */
public class LdapUsersETLServiceImpl implements ETLService
{
    private static final String CEC_USER_DOMAIN = "@cec";

    private static final Log logger = LogFactory.getLog(LdapUsersETLServiceImpl.class);

    private static final String MSG_ERROR_LDAP = "Error accessing LDAP.";
    private static final String MSG_DIFFERENT_MONIKER = "The circac username different than the moniker.";
    private static final String MSG_TO_MANY_USERS_FOUND = "To many users found with this email.";
    private static final String MSG_NO_USER_FOUND = "No user found with this email.";
    private static final String MSG_EMAIL_IS_INVALID = "Email is invalid.";
    private static final String MSG_EMAIL_IS_EMPTY = "Email is empty.";
    private static final String MSG_USER_BY_UID = "User found with it's UID.";
    private static final String MSG_NO_USER_DATA = "No user data found in the circa invited users.";
    private static final String MSG_USER_BY_CN = "User found with its CN (common name).";

    private static final String VALID_VERSION_DESCRIPTION = "Valid file version create by ETL. ";
    private static final String INVALID_VERSION_DESCRIPTION = "Residual file version create by ETL.";

    private static final String STAT_VALID_PERSON = "Valid users";
    private static final String STAT_INVALID_PERSON = "Invalid users";
    private static final String STAT_VALID_BUILD_TIME = "Time to build valid file in minute";
    private static final String STAT_INVALID_BUILD_TIME = "Time to build residual file in minute";

    private static final String GET_USERS_METHOD_NAME = "getUsers";
    private static final String WITH_USERS_METHOD_NAME = "withUsers";
    private static final String GET_USER_METHOD_NAME = "getUser";
    private static final String SET_USER_METHOD_NAME = "setUser";

    private static final String QUERY_ALL_OWNER = ".//*[owner]" ;
    private static final String QUERY_ALL_CREATOR = ".//*[creator]";
    private static final String QUERY_ALL_MODIFIER = ".//*[modifier]";
    private static final String QUERY_ALL_USER =  ".//*[user]";
    private static final String QUERY_ALL_USERS = ".//*[users]";
    private static final String QUERY_ALL_AUTHOR = ".//*[author]";

    private final EmailSpacesRemover emailConverter = new EmailSpacesRemover();
    protected UserService userService;
    protected ValidationHandler validationHandler;
    protected FileArchiver fileArchiver;


    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.service.migration.ETLService#proposeEtl(java.lang.String)
     */
    public ETLReport proposeEtl(final String iterationName) throws ETLException
    {
        final NodeRef xmlDocument = getIterationNodeRef(iterationName);
        return proposeEtl(xmlDocument, iterationName);
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.service.migration.ETLService#proposeUsers(eu.cec.digit.circabc.migration.journal.etl.PathologicUser, java.lang.String, java.lang.String, java.lang.String)
     */
    public void proposeUsers(final PathologicUser pathologicUser, final String uid, final String moniker, final String email, final String cn) throws ETLException
    {
        ParameterCheck.mandatory("A pathologic user ", pathologicUser);

        final List<String> ldapUsers = this.userService.getLDAPUserIDByIdMonikerEmailCn(uid, moniker, email, cn, true);

        pathologicUser.resetPropositions(convertUsers(ldapUsers));
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.service.migration.ETLService#getIterations(boolean)
     */
    public List<MigrationIteration> getIterations(final boolean sortAscending) throws ETLException
    {
        try
        {
            final List<MigrationIteration> transformationAllowedIteration = new ArrayList<MigrationIteration>();
            for(final MigrationIteration iteration: this.fileArchiver.getIterations())
            {
                if(this.fileArchiver.isIterationReadyForTransformation(iteration))
                {
                    transformationAllowedIteration.add(iteration);
                }
            }

            Collections.sort(transformationAllowedIteration, new IterationComparator(sortAscending));
            return transformationAllowedIteration;
        }
        catch(final ArchiveException e)
        {
            throw new ETLException(e.getMessage(), e);
        }
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.service.migration.ETLService#applyEtl(eu.cec.digit.circabc.migration.journal.ETLReport)
     */
    
    public void applyEtl(final ETLReport report) throws ETLException
    {
        ParameterCheck.mandatory("A report ", report);
        ParameterCheck.mandatory("An iteration", report.getIteration().getIdentifier());
        ParameterCheck.mandatory("A migration structure ", report.getOriginalImportRoot());

        final Date importProcessId = new Date();

        final Map<String, TransformationElement> validUsers = new HashMap<String, TransformationElement>(report.getTransformationElements().size());
        final Map<String, PathologicUser> pathologicUsers = new HashMap<String, PathologicUser>(report.getPathologicUsers().size());

        for(final TransformationElement element: report.getTransformationElements())
        {
            validUsers.put(safeUid(element.getOriginalPerson()), element);
        }
        for(final PathologicUser element: report.getPathologicUsers())
        {
            pathologicUsers.put(safeUid(element.getPerson()), element);
        }

        final NodeRef iterationNodeRef = getIterationNodeRef(report.getIteration());
        
        final ImportRoot originalImportRoot = unmarshall(iterationNodeRef);
        final Circabc circabc = originalImportRoot.getCircabc();
        final JXPathContext context = JXPathContext.newContext(circabc);
        @SuppressWarnings("unchecked")
		final List<Node> nodesWithOwner = context.selectNodes(QUERY_ALL_OWNER);
        @SuppressWarnings("unchecked")
		final List<Node> nodesWithCreator = context.selectNodes(QUERY_ALL_CREATOR);
        @SuppressWarnings("unchecked")
		final List<Node> nodesWithModifier = context.selectNodes(QUERY_ALL_MODIFIER);
        
        final Set<Node> nodesWithUser = new HashSet<Node>(nodesWithOwner);
        nodesWithUser.addAll(new HashSet<Node>(nodesWithCreator));
        nodesWithUser.addAll(new HashSet<Node>(nodesWithModifier));
        
        @SuppressWarnings("unchecked")
		final List<Node> nodesWithAuthor = context.selectNodes(QUERY_ALL_AUTHOR);
        @SuppressWarnings("unchecked")
		final List<XMLElement> elementsWithUser  = context.selectNodes(QUERY_ALL_USER);
        @SuppressWarnings("unchecked")
		final List<XMLElement> elementsWithUsers = context.selectNodes(QUERY_ALL_USERS);
        
        

        //	Generate the valid file
        generateValidXML(report, importProcessId, validUsers, 
        		iterationNodeRef,originalImportRoot,
        		nodesWithUser,nodesWithAuthor,
        		elementsWithUser,elementsWithUsers
        		);
        //	Generate the invalid file
        generateResidualXML(report, importProcessId, 
        		validUsers, pathologicUsers, 
        		iterationNodeRef ,originalImportRoot,
        		nodesWithUser,elementsWithUser,
        		elementsWithUsers);
        //	Make persistent the auto/manual transformed users
        storeValidUsers(validUsers);
    }

    /**
     * @param report
     * @param importProcessId
     * @param validUsers
     * @param pathologicUsers
     * @throws ETLException
     */
    @SuppressWarnings("unchecked")
    protected void generateResidualXML(final ETLReport report, final Date importProcessId, 
    		final Map<String, TransformationElement> validUsers,final Map<String, PathologicUser> pathologicUsers, 
    		final NodeRef iterationNodeRef,ImportRoot originalImportRoot,
    		Set<Node> nodesWithUser,List<XMLElement> elementsWithUser,
    		List<XMLElement> elementsWithUsers) throws ETLException
    {
        

        final ImportRoot residualRoot = new ImportRoot();
        final long startTime = System.currentTimeMillis();

        

        if(logger.isDebugEnabled())
        {
            logger.debug("Time to search users (in second): " + (System.currentTimeMillis() - startTime) / 1000);
        }

        for(final Node node: nodesWithUser)
        {
            final String owner = safeOwner(node);
            final String creator = safeCreator(node);
            final String modifier = safeModifier(node);

            if(creator != null && validUsers.containsKey(creator) == false)
            {
                addResidualElement(residualRoot, node, creator);

                if(logger.isDebugEnabled())
                {
                    logger.debug("Building residual ETL File: Node added to residual xml due to its invalid creator " + creator + ". Element: " + node.toString());
                }
            }
            if(modifier != null && validUsers.containsKey(modifier) == false)
            {
                addResidualElement(residualRoot, node, modifier);

                if(logger.isDebugEnabled())
                {
                    logger.debug("Building residual ETL File: Node added to residual xml due to its invalid modifier " + modifier + ". Element: " + node.toString());
                }
            }
            if(owner != null && validUsers.containsKey(owner) == false)
            {
                addResidualElement(residualRoot, node, owner);

                if(logger.isDebugEnabled())
                {
                    logger.debug("Building residual ETL File: Node added to residual xml due to its invalid owner " + owner + ". Element: " + node.toString());
                }
            }
        }
        
        for(final XMLElement element: elementsWithUser)
        {
            try
            {
                final Method getter = element.getClass().getMethod(GET_USER_METHOD_NAME);
                final String oldId  = (String) getter.invoke(element);

                if(oldId != null && validUsers.containsKey(oldId) == false)
                {
                    addResidualElement(residualRoot, element, oldId);

                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Building residual ETL File: Element added to residual xml due to its invalid user definition " + oldId + ". Element: " + element.toString());
                    }
                }
            }
            catch (final Exception e)
            {
                throw new ETLException("Impossible to access to the user of " + element.toString() + " of type " + element.getClass(), e) ;
            }
        }
        for(final XMLElement element: elementsWithUsers)
        {
            try
            {
                final Method getter = element.getClass().getMethod(GET_USERS_METHOD_NAME);
                final Method setter = element.getClass().getMethod(WITH_USERS_METHOD_NAME, Collection.class);
                final List<String> users = (List<String>) getter.invoke(element);

                if(users != null && users.size() > 0)
                {
                    final List<String> usersToKeep = new ArrayList<String>(users.size());

                    for(final String user: users)
                    {
                        if(validUsers.containsKey(user) == false)
                        {
                            usersToKeep.add(user);
                        }
                    }

                    if(usersToKeep.size() > 0)
                    {
                        users.clear();
                        setter.invoke(element, usersToKeep);
                        addResidualElement(residualRoot, element, null);

                        if(logger.isDebugEnabled())
                        {
                            logger.debug("Building residual ETL File: Element added to residual xml due to it contains at least one invalid user. Invalid users: " + usersToKeep + ". Element: " + element.toString());
                        }
                    }
                }
            }
            catch (final Exception e)
            {
                throw new ETLException("Impossible to access to the user list of " + element.toString() + " of type " + element.getClass(), e) ;
            }
        }

        final Persons persons = new Persons();
        residualRoot.setPersons(persons);
        for(final Map.Entry<String, PathologicUser> entry: pathologicUsers.entrySet())
        {
            final PathologicUser element = entry.getValue();
            if(!element.getPersonWasNull())
            {
            	persons.getPersons().add(element.getPerson());
            	if(logger.isDebugEnabled())
                {
                    logger.debug("Building residual ETL File: Person " + entry.getValue() + " successfully added to the main person list.");
                }
            }
        }

        // copy history and statistics
        residualRoot.withVersionHistory(originalImportRoot.getVersionHistory());
        residualRoot.withStatistics(originalImportRoot.getStatistics());

        // 2c.	Update the version (major)
        final Pair<Integer, Integer> residualVersion = ElementsHelper.addVersion(residualRoot, true, INVALID_VERSION_DESCRIPTION);

        // 2d.	update statistics
        ElementsHelper.addStatistics(residualRoot, STAT_INVALID_BUILD_TIME, "" + (System.currentTimeMillis() - startTime) / 60000, residualVersion);
        ElementsHelper.addStatistics(residualRoot, STAT_VALID_PERSON, "" + report.getTransformationElements().size(), residualVersion);
        ElementsHelper.addStatistics(residualRoot, STAT_INVALID_PERSON, "" + report.getPathologicUsers().size(), residualVersion);

        // 2e.	Store the residual file
        NodeRef residualFileRef;
        try
        {
            residualFileRef = fileArchiver.storeTransformedResidualFile(report.getIteration(), importProcessId, JavaXmlBinder.marshallInStream(residualRoot));
        }
        catch (Exception e)
        {
            throw new ETLException("Impossible to store the transformed xml", e);
        }

        report.setResidualImportRoot(residualFileRef);
    }

    /**
     * @param report
     * @param importProcessId
     * @param validUsers
     * @throws ETLException
     */
    @SuppressWarnings("unchecked")
    protected void generateValidXML(final ETLReport report, final Date importProcessId, 
    		final Map<String, TransformationElement> validUsers, final NodeRef iterationNodeRef,
    		final ImportRoot originalImportRoot,
    		final Set<Node> nodesWithUser,
    		final List<Node> nodesWithAuthor,
    		final List<XMLElement> elementsWithUser,
    		final List<XMLElement> elementsWithUsers
    		) throws ETLException
    {
        
        final ImportRoot newValidImportRoot;

        // get if the valid xml is build from the original document or a previous ETL.
        boolean firstTransformation = iterationNodeRef.equals(report.getIteration().getOriginalFileNodeRef());
        if(firstTransformation)
        {
            newValidImportRoot = null;

            if(logger.isDebugEnabled())
            {
                logger.debug("Since it's the first ETL performed, the changes will be made on the original file.");
            }
        }
        else
        {
            newValidImportRoot = new ImportRoot();

            if(logger.isDebugEnabled())
            {
                logger.debug("Since it's NOT the first ETL performed, the changes will be made in a new file to avoid to keep elements that are not corrected. ");
            }
        }

        final long startTime = System.currentTimeMillis();

        

        if(logger.isDebugEnabled())
        {
            logger.debug("Time to search users (in second): " + (System.currentTimeMillis() - startTime) / 1000);
        }
        
        for(final Node node: nodesWithAuthor)
        {
        	final String author = safeAuthor(node);

            if(author != null)
            {
                if(validUsers.containsKey(author))
                {
                	final TransformationElement transformationElement = validUsers.get(author);
                    final User validUserData = transformationElement.getValidUserData();
					final String newFullName = validUserData.getFirstname() + " " + validUserData.getLastname();  

                    if(firstTransformation)
                    {
                        ((Content)node).setAuthor((new TypedProperty.AuthorProperty(newFullName)));
                    }
                    else
                    {
                    	 addResidualElement(newValidImportRoot, node, newFullName);
                    }

                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Building valid ETL File: Author " + newFullName + " successfully setted instead of " + author + " on node " + node.toString());
                    }
                }
                else if(firstTransformation)
                {
                	((Content)node).setAuthor((new TypedProperty.AuthorProperty(author)));

                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Building valid ETL File: Author successfully reseted on node " + node.toString() + ". The user " + author + " is invalid.");
                    }
                }
            }
        }
        
        
        

        for(final Node node: nodesWithUser)
        {
            final String owner = safeOwner(node);
            final String creator = safeCreator(node);
            final String modifier = safeModifier(node);

            if(owner != null)
            {
                if(validUsers.containsKey(owner))
                {
                    final String newUid = validUsers.get(owner).getValidUserId();

                    if(firstTransformation)
                    {
                        ((TitledNode)node).setOwner(new TypedProperty.OwnerProperty(newUid));
                    }
                    else
                    {
                        addResidualElement(newValidImportRoot, node, newUid);
                    }

                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Building valid ETL File: Owner " + newUid + " successfully setted instead of " + owner + " on node " + node.toString());
                    }
                }
                else if(firstTransformation)
                {
                    ((TitledNode)node).setOwner(new TypedProperty.OwnerProperty(buildUnknowUsername(owner)));

                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Building valid ETL File: Owner successfully reseted on node " + node.toString() + ". The user " + owner + " is invalid.");
                    }
                }
            }

            if(creator != null)
            {
                if(validUsers.containsKey(creator))
                {
                    final String newUid = validUsers.get(creator).getValidUserId();

                    if(firstTransformation)
                    {
                        node.setCreator(new TypedProperty.CreatorProperty(newUid));
                    }
                    else
                    {
                        addResidualElement(newValidImportRoot, node, newUid);
                    }

                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Building valid ETL File: Creator " + newUid + " successfully setted instead of " + creator + " on node " + node.toString());
                    }
                }
                else if(firstTransformation)
                {
                    node.setCreator(new TypedProperty.CreatorProperty(buildUnknowUsername(creator)));

                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Building valid ETL File: Creator successfully reseted on node " + node.toString() + ". The user " + creator + " is invalid.");
                    }
                }
            }

            if(modifier != null)
            {
                if(validUsers.containsKey(modifier))
                {
                    final String newUid = validUsers.get(modifier).getValidUserId();

                    if(firstTransformation)
                    {
                        node.setModifier(new TypedProperty.ModifierProperty(newUid));
                    }
                    else
                    {
                        addResidualElement(newValidImportRoot, node, newUid);
                    }

                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Building valid ETL File: Modifier " + newUid + " successfully setted instead of " + modifier + " on node " + node.toString());
                    }
                }
                else if(firstTransformation)
                {
                    node.setModifier(new TypedProperty.ModifierProperty(buildUnknowUsername(modifier)));

                    if(logger.isDebugEnabled())
                    {
                        logger.debug("Building valid ETL File: Modifier successfully reseted on node " + node.toString() + ". The user " + modifier + " is invalid.");
                    }
                }
            }
        }

        for(final XMLElement element: elementsWithUser)
        {
            try
            {
                final Method getter = element.getClass().getMethod(GET_USER_METHOD_NAME);
                final Method setter = element.getClass().getMethod(SET_USER_METHOD_NAME, String.class);

                final String oldId  = (String) getter.invoke(element);

                if(oldId != null)
                {
                    if(validUsers.containsKey(oldId))
                    {
                        final String newUid = validUsers.get(oldId).getValidUserId();
                        setter.invoke(element, newUid);

                        if(firstTransformation == false)
                        {
                            addResidualElement(newValidImportRoot, element, newUid);
                        }

                        if(logger.isDebugEnabled())
                        {
                            logger.debug("Building valid ETL File: User " + newUid + " successfully setted instead of " + oldId + " on element " + element.toString());
                        }
                    }
                    else if(firstTransformation)
                    {
                        // remove the element of its parent. It s not longer necessary.
                        final XMLElement parent = ElementsHelper.getParent(element);

                        if(element instanceof LibraryPermissionItem)
                        {
                            ((LibraryUserRights)parent).getPermissions().remove(element);
                        }
                        else if(element instanceof NotificationItem)
                        {
                            ((Notifications)parent).getNotifications().remove(element);
                        }
                        else if(element instanceof Application)
                        {
                            ((Applications)parent).getApplications().remove(element);
                        }
                        else if(element instanceof NewsgroupPermissionItem)
                        {
                            ((NewsgroupUserRights)parent).getPermissions().remove(element);
                        }
                        else if(element instanceof InformationPermissionItem)
                        {
                            ((InformationUserRights)parent).getPermissions().remove(element);
                        }
                        else
                        {
                            throw new ETLException("The class " + element.getClass() + " define a setUser method but its not recognized.");
                        }

                        if(logger.isDebugEnabled())
                        {
                            logger.debug("Building valid ETL File: Element " + element.toString() + " successfully removed. The user " + oldId + " is invalid.");
                        }
                    }
                }
            }
            catch (final Exception e)
            {
                throw new ETLException("Impossible to access to the user of " + element.toString() + " of type " + element.getClass(), e) ;
            }
        }
        for(final XMLElement element: elementsWithUsers)
        {
            try
            {
                final Method setter = element.getClass().getMethod(WITH_USERS_METHOD_NAME, Collection.class);
                final Method getter = element.getClass().getMethod(GET_USERS_METHOD_NAME);
                final List<String> users = (List<String>) getter.invoke(element);

                if(users != null && users.size() > 0)
                {
                    final List<String> usersToKeep = new ArrayList<String>(users.size());

                    for(final String user: users)
                    {
                        if(validUsers.containsKey(user))
                        {
                            final String newUid = validUsers.get(user).getValidUserId();
                            usersToKeep.add(newUid);

                            if(logger.isDebugEnabled())
                            {
                                logger.debug("Building valid ETL File: User " + newUid + " successfully added to the users list instead of " + user + " on element " + element.toString());
                            }
                        }
                        else
                        {
                            if(logger.isDebugEnabled())
                            {
                                logger.debug("Building valid ETL File: User " + user + " successfully removed from the users list on element " + element.toString());
                            }
                        }
                    }
                    users.clear();
                    setter.invoke(element, usersToKeep);

                    if(firstTransformation == false && usersToKeep.size() > 0)
                    {
                        addResidualElement(newValidImportRoot, element, null);
                    }
                }
            }
            catch (final Exception e)
            {
                throw new ETLException("Impossible to access to the user list of " + element.toString() + " of type " + element.getClass(), e) ;
            }
        }

        storePersons(validUsers, firstTransformation ? originalImportRoot : newValidImportRoot);

        storeNewValidFile(report, firstTransformation ? originalImportRoot : newValidImportRoot, startTime, importProcessId);
    }

	/**
	 * @param modifier
	 * @return
	 */
	private String buildUnknowUsername(final String modifier)
	{
		final String uid = LdapHelper.removeDomainFromUid(modifier);
		return '[' + (uid == null ?  "" : uid.trim()) + ']';
	}

    /**
     * @param validUsers
     * @param importRoot
     */
    private void storePersons(final Map<String, TransformationElement> validUsers, final ImportRoot importRoot)
    {
        Persons persons = importRoot.getPersons();
        if(persons == null)
        {
            importRoot.setPersons(persons = new Persons());
        }

        final Set<String> addedPersons = new HashSet<String>();
        persons.getPersons().clear();
        for(final Map.Entry<String, TransformationElement> entry: validUsers.entrySet())
        {
            final TransformationElement element = entry.getValue();
            final String validUserId = element.getValidUserId();

            // ensure that a person is only added one time.
            if(addedPersons.contains(validUserId) == false)
            {
                persons.getPersons().add(updatePerson(element.getOriginalPerson(), validUserId, element.getValidUserData()));

                if(logger.isDebugEnabled())
                {
                    logger.debug("Building valid ETL File: Person " + entry.getValue() + " successfully added to the main person list with the new valid ID: " + validUserId);
                }

                addedPersons.add(validUserId);
            }
        }
    }

    private void storeNewValidFile(final ETLReport report, final ImportRoot importRoot, final long startTime, final Date importProcessId) throws ETLException
    {
        //1c.	Update the version (minor)
        final Pair<Integer, Integer> validVersion = ElementsHelper.addVersion(importRoot, false, VALID_VERSION_DESCRIPTION);

        // 1d.	update statistics
        ElementsHelper.addStatistics(importRoot, STAT_VALID_BUILD_TIME, "" + (System.currentTimeMillis() - startTime) / 60000, validVersion);
        ElementsHelper.addStatistics(importRoot, STAT_VALID_PERSON, "" + report.getTransformationElements().size(), validVersion);
        ElementsHelper.addStatistics(importRoot, STAT_INVALID_PERSON, "" + report.getPathologicUsers().size(), validVersion);

        // 1e.	Store the valid file
        NodeRef validFileRef;
        try
        {
            validFileRef = this.fileArchiver.storeTransformedValidFile(report.getIteration(), importProcessId, JavaXmlBinder.marshallInStream(importRoot));
        }
        catch (Exception e)
        {
            throw new ETLException("Impossible to store the transformed xml", e);
        }
        report.setValidImportRoot(validFileRef);
    }

    private void addResidualElement(final ImportRoot residual, final XMLElement element, String userId) throws ETLException
    {
        final List<XMLElement> allParents = new ArrayList<XMLElement>();
        boolean isNode = element instanceof XMLNode;

        XMLElement parent = isNode ? element : ElementsHelper.getParent(element);
        for(int x = 0; parent!= null; ++x)
        {
            allParents.add(x, parent);
            parent = ElementsHelper.getParent(parent);
        }

        Collections.reverse(allParents);

        XMLElement originalParent = null;
        XMLElement residualParent = residual;
        XMLElement residualChild;

        try
        {
            for(int x = 0; x < allParents.size(); ++x)
            {
                originalParent = allParents.get(x);
                residualChild = instanciateXMLElement(originalParent);
                residualParent = BinderUtils.addChild(residualParent, residualChild, logger);
            }

            if(isNode == false)
            {
                // if the element to copy is not a node all fields must be keeped.
                BinderUtils.addChild(residualParent, element, false, logger);
            }
            else
            {
                final String owner = safeOwner((Node) element);
                final String creator = safeCreator((Node) element);
                final String modifier = safeModifier((Node) element);

                if(userId.equals(owner))
                {
                    ((TitledNode)residualParent).setOwner(new TypedProperty.OwnerProperty(userId));
                }
                if(userId.equals(creator))
                {
                    ((Node)residualParent).setCreator(new TypedProperty.CreatorProperty(userId));
                }
                if(userId.equals(modifier))
                {
                    ((Node)residualParent).setModifier(new TypedProperty.ModifierProperty(userId));
                }
            }
        }
        catch (final Exception e)
        {
            throw new ETLException("", e);
        }

    }

    protected void storeValidUsers(Map<String, TransformationElement> validUsers) throws ETLException
    {
        final Map<Object, Object> userNameMatch = new HashMap<Object, Object>(validUsers.size());

        for(final Map.Entry<String, TransformationElement> entry: validUsers.entrySet())
        {
            // key = circa ID, value = Ecas id
            userNameMatch.put(entry.getKey(), entry.getValue().getValidUserId());
        }

        try
        {
            fileArchiver.storeAlreadyTransformedUser(userNameMatch);
        }
        catch (final ArchiveException e)
        {
            throw new ETLException(e.getMessage(), e);
        }
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
        if(logger.isDebugEnabled())
        {
            logger.debug("Start to run migration ETL with iteration: " + iteration.getIdentifier());
            logger.debug(previousUserMatch.size() + " already validated users will be taken in account.");
        }

        try
        {
            final ETLReport report = new ETLReport();
            final ImportRoot root = unmarshall(resource);

            report.setOriginalImportRoot(resource);
            report.setIteration(iteration);

            if(root == null)
            {
                throw new ETLException("No import root element found in the given export file.");
            }

            if(logger.isDebugEnabled())
            {
                logger.debug("Import root successfully unmarshalled");
            }

            final Set<String> allusers = getAllUsers(root);
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

    @SuppressWarnings("unchecked")
    private Set<String> getAllUsers(final ImportRoot root) throws ETLException
    {
         final Circabc circabc = root.getCircabc();
         final JXPathContext context = JXPathContext.newContext(circabc);

         final Set<String> allUsers = new HashSet<String>();

         final long startTime = System.currentTimeMillis();

         final List<Node> nodesWithOwner = context.selectNodes(QUERY_ALL_OWNER);
         final List<Node> nodesWithCreator = context.selectNodes(QUERY_ALL_CREATOR);
         final List<Node> nodesWithModifier = context.selectNodes(QUERY_ALL_MODIFIER);
         
         final Set<Node> nodesWithUser = new HashSet<Node>(nodesWithOwner);
         nodesWithUser.addAll(new HashSet<Node>(nodesWithCreator));
         nodesWithUser.addAll(new HashSet<Node>(nodesWithModifier));
         
         
         final List<XMLElement> elementsWithUser  = context.selectNodes(QUERY_ALL_USER);
         final List<XMLElement> elementsWithUsers = context.selectNodes(QUERY_ALL_USERS);

         if(logger.isDebugEnabled())
         {
             logger.debug("Time to search users (in second): " + (System.currentTimeMillis() - startTime) / 1000);
         }

         for(final Node node: nodesWithUser)
         {
             final String owner = safeOwner(node);
             final String creator = safeCreator(node);
             final String modifier = safeModifier(node);

             if(owner != null)
             {
                 allUsers.add(owner);
             }
             if(creator != null)
             {
                 allUsers.add(creator);
             }
             if(modifier != null)
             {
                 allUsers.add(modifier);
             }
         }

         for(final XMLElement element: elementsWithUser)
         {
             try
             {
                 final Method getter = element.getClass().getMethod(GET_USER_METHOD_NAME);
                 final String usersid  = (String) getter.invoke(element);

                 if(usersid != null)
                 {
                     allUsers.add(usersid);
                 }
             }
             catch (final Exception e)
             {
                 throw new ETLException("Impossible to access to the user of " + element.toString() + " of type " + element.getClass(), e) ;
             }
         }

         for(final XMLElement element: elementsWithUsers)
         {
             try
             {
                 final Method getter = element.getClass().getMethod(GET_USERS_METHOD_NAME);
                 final List<String> users = (List<String>) getter.invoke(element);

                 if(users != null && users.size() > 0)
                 {

                     for(final String usersid: users)
                     {
                         if(usersid != null)
                         {
                             allUsers.add(usersid);
                         }
                     }
                 }
             }
             catch (final Exception e)
             {
                 throw new ETLException("Impossible to access to the user list of " + element.toString() + " of type " + element.getClass(), e) ;
             }
         }

         return allUsers;
    }

    /**
     * @param report
     * @param person
     * @param alreadyTranformedID
     */
    protected void checkPerson(final ETLReport report, final Person person, final String personId, final String alreadyTranformedID)
    {
    	final boolean isPersonNull = person == null || (person.getEmail() == null && person.getUserId() == null);

    	try
    	{


        	if(alreadyTranformedID != null)
            {
        		if(logger.isDebugEnabled())
                {
                    logger.debug("Found in the previous validated user list with new UID: " + alreadyTranformedID);
                }

                final CircabcUserDataBean ldapUser = this.userService.getLDAPUserDataByUid(alreadyTranformedID);

                if(ldapUser == null)
                {
                    logger.error("User list corrupted. The defined user " + alreadyTranformedID + " is not found in ecas. Ignore it.");
                    checkPerson(report, person, personId, null);
                }
                else
                {
                    final User aidaUser = convertUser(ldapUser);
                    final Person safePerson;

                    if(isPersonNull)
                    {
                    	safePerson = new Person()
                    		.withUserId(new TypedProperty.UserIdProperty(personId));
                    }
                    else
                    {
                    	safePerson = person;
                    }

    				final TransformationElement transformationElement = new TransformationElement(
                    		safePerson,
                            alreadyTranformedID,
                            aidaUser);
                    report.withTransformationElements(transformationElement);

                    if(logger.isDebugEnabled())
                    {
                        logger.debug(alreadyTranformedID + " SUCCEFULLY found in ecas with the email "  + aidaUser.getEmail());
                    }
                }
            }
        	else if(isPersonNull)
            {
                if(LdapHelper.isUserid(personId))
                {
                     if(logger.isDebugEnabled())
                     {
                         logger.debug(personId + " seems to be a valid userid, try to get with that.");
                     }

                    checkPerson(report, new Person().withUserId(new TypedProperty.UserIdProperty(personId)), personId, alreadyTranformedID);
                }
                else
                {
                    // check CN (common name)
                    final List<String> usersWithcn = userService.getLDAPUserIDByIdMonikerEmailCn(null, null, null, personId, true);

                    if(usersWithcn.size() == 0)
                    {
                        final PathologicUser pathologicUser = new PathologicUser(
                                new Person().withUserId(new TypedProperty.UserIdProperty(personId)),
                                MSG_NO_USER_DATA,
                                Collections.<String, User>emptyMap(),
                                Boolean.TRUE);

                        report.withPathologicUsers(pathologicUser);

                        if(logger.isDebugEnabled())
                        {
                            logger.debug(personId + " has no details found in circa invited users");
                        }
                    }
                    else
                    {
                    	PathologicUser pathologicUser;

                    	try
                    	{
                    		pathologicUser  = new PathologicUser(
                            		 new Person().withUserId(new TypedProperty.UserIdProperty(personId)), MSG_USER_BY_CN, convertUsers(usersWithcn), Boolean.TRUE);

                            if(logger.isDebugEnabled())
                            {
                                logger.debug("Some users found ( " + usersWithcn.size() + ") with the cn: " + usersWithcn);
                            }
                    	}
                    	catch(Throwable t)
                    	{
                    		pathologicUser = new PathologicUser(
                                     new Person().withUserId(new TypedProperty.UserIdProperty(personId)),
                                     MSG_NO_USER_DATA,
                                     Collections.<String, User>emptyMap(),
                                     Boolean.TRUE);
                    	}

                    	 report.withPathologicUsers(pathologicUser);
                    }
                }
            }
            else
            {
                final String email = safeEmail(person);
                final String circaUserName = (String) person.getUserId().getValue();

                if(email.length() == 0)
                {
                    CircabcUserDataBean userByUID = null;
                    final String uidNoDomain = LdapHelper.removeDomainFromUid(circaUserName);

                    if(circaUserName.endsWith(CEC_USER_DOMAIN))
                    {
                        try
                        {
                            userByUID = userService.getLDAPUserDataByUid(uidNoDomain);
                        }
                        catch(Exception ignore){}

                    }

                    final PathologicUser pathologicUser;

                    if(userByUID != null)
                    {
                        final User userById = getUserById(uidNoDomain);

                        if(isValid(userById.getEmail()) == false || userById.getMoniker() == null)
                        {
                            pathologicUser = new PathologicUser(person, MSG_USER_BY_UID,
                                    Collections.<String, User>emptyMap(), Boolean.FALSE);
                        }
                        else
                        {
                            pathologicUser = new PathologicUser(person, MSG_USER_BY_UID,
                                    Collections.singletonMap(uidNoDomain, userById), Boolean.FALSE);
                        }

                        if(logger.isDebugEnabled())
                        {
                            logger.debug(circaUserName + " has an empty email but its is found with it's UID");
                        }
                    }
                    else
                    {
                         pathologicUser = new PathologicUser(
                                 person, MSG_EMAIL_IS_EMPTY, Collections.<String, User>emptyMap(), Boolean.FALSE);
                         report.withPathologicUsers(pathologicUser);

                         if(logger.isDebugEnabled())
                         {
                             logger.debug(circaUserName + " has an empty email");
                         }
                    }

                    report.withPathologicUsers(pathologicUser);
                }
                else if(isValid(email) == false)
                {
                    final PathologicUser pathologicUser = new PathologicUser(
                            person, MSG_EMAIL_IS_INVALID, Collections.<String, User>emptyMap(), Boolean.FALSE);
                    report.withPathologicUsers(pathologicUser);

                    if(logger.isDebugEnabled())
                    {
                        logger.debug(circaUserName + " has an invalid email: " + email);
                    }
                }
                else
                {
                    final List<String> ldapUsers = this.userService.getLDAPUserIDByMail(email);
                    final int occurences = ldapUsers.size();

                    if(occurences < 1)
                    {
                        final PathologicUser pathologicUser = new PathologicUser(
                                person, MSG_NO_USER_FOUND,Collections.<String, User>emptyMap(), Boolean.FALSE);
                        // no user found ...
                        report.withPathologicUsers(pathologicUser);

                        if(logger.isDebugEnabled())
                        {
                            logger.debug(circaUserName + " no user found in ecas with the email " + email);
                        }

                    }
                    else if(occurences == 1)
                    {
                        final String ldapUserName = ldapUsers.get(0);
                        final User aidaUser = getUserById(ldapUserName);

                        if(isMonikerEqualCircaUid(aidaUser, circaUserName) == false)
                        {
                            final PathologicUser pathologicUser = new PathologicUser(
                                    person, MSG_DIFFERENT_MONIKER, Collections.<String, User>singletonMap(ldapUserName, aidaUser), Boolean.FALSE);
                            // the circa username is different that the moniker.
                            report.withPathologicUsers(pathologicUser);

                            if(logger.isDebugEnabled())
                            {
                                logger.debug(circaUserName + " has a moniker too different that the circa username (" + aidaUser.getMoniker() + "). It requires an human confirmation.");
                            }
                        }
                        else
                        {
                            final TransformationElement transformationElement = new TransformationElement(
                                    person, ldapUserName, aidaUser);
                            report.withTransformationElements(transformationElement);

                            if(logger.isDebugEnabled())
                            {
                                logger.debug(circaUserName + " SUCCEFULLY found in ecas with the email "  + email);
                            }
                        }
                    }
                    else
                    {
                        final PathologicUser pathologicUser = new PathologicUser(
                                person, MSG_TO_MANY_USERS_FOUND, convertUsers(ldapUsers), Boolean.FALSE);
                        // to many user found ...
                        report.withPathologicUsers(pathologicUser);


                        if(logger.isDebugEnabled())
                        {
                            logger.debug(circaUserName + " to many users ( " + ldapUsers.size() + ") found in ecas with the email " + email);
                        }
                    }
                }
            }
    	}
    	catch(Exception exception)
    	{
            final Person safePerson;

            if(isPersonNull)
            {
            	safePerson = new Person()
            		.withUserId(new TypedProperty.UserIdProperty(personId));
            }
            else
            {
            	safePerson = person;
            }

    		final PathologicUser pathologicUser = new PathologicUser(
    				safePerson, MSG_ERROR_LDAP,Collections.<String, User>emptyMap(), Boolean.FALSE);
    		
    		if(logger.isErrorEnabled())
            {
                logger.error(MSG_ERROR_LDAP, exception);
            }

            report.withPathologicUsers(pathologicUser);
    	}
    }

    /**
     * @param ldapUserName
     * @param circaUserName
     * @return
     */
    private boolean isMonikerEqualCircaUid(final User aidaUser, final String circaUserName)
    {
        final String circaShortUid = LdapHelper.removeDomainFromUid(circaUserName);
        return circaShortUid.equalsIgnoreCase(aidaUser.getMoniker());
    }

    /**
     * @param person
     * @return
     */
    private String safeEmail(final Person person)
    {
        final String email = safeProperty(person.getEmail());
        return this.emailConverter.convert(email);

    }

    /**
     * @param person
     * @return
     */
    protected String safeUid(final Person person)
    {
        return safeProperty(person.getUserId());
    }

    private String safeOwner(final XMLNode node)
    {
        if(node instanceof TitledNode)
        {
            return safeProperty(((TitledNode)node).getOwner());
        }
        else
        {
            return null;
        }
    }

    private String safeAuthor(final XMLNode node)
    {
        if(node instanceof Content)
        {
            return safeProperty(((Content)node).getAuthor());
        }
        else
        {
            return null;
        }
    }
    private String safeCreator(final Node node)
    {
        return safeProperty(node.getCreator());
    }

    private String safeModifier(final Node node)
    {
        return safeProperty(node.getModifier());
    }

    private String safeProperty(final TypedProperty value)
    {
        if(value == null)
        {
            return null;
        }
        else
        {
            return (String)value.getValue();
        }
    }


    /**
     * @param userEnteredEmailString
     * @return
     */
    private boolean isValid(final String email) {

        if(email == null)
        {
            return false;
        }
        else
        {
            try
            {
                new InternetAddress(email.trim()).validate();
                return true;
            }
            catch (final AddressException e)
            {
                return false;
            }
        }
    }

    private Person updatePerson(final Person person, final String ldapUserId, final User user)
    {
        // update valid users with valid data
        person
            .withUserId(new TypedProperty.UserIdProperty(ldapUserId))
            .withEmail(new TypedProperty.EmailProperty(user.getEmail()))
            .withFirstName(new TypedProperty.FirstNameProperty(user.getFirstname()))
            .withLastName(new TypedProperty.LastNameProperty(user.getLastname()));

        return person;
    }


    private Map<String, User> convertUsers(final List<String> ldapUserIds)
    {
        final Map<String, User> users = new HashMap<String, User>(ldapUserIds.size());

        for(final String uid: ldapUserIds)
        {
            final User user = getUserById(uid);
            users.put(uid, user);
        }

        return users;
    }

    /**
     * @param uid
     * @return
     */
    private User getUserById(final String uid)
    {
        final CircabcUserDataBean person = this.userService.getLDAPUserDataByUid(uid);

        if(person != null)
        {
        	final User user = convertUser(person);
            return user;
        }
        else
        {
        	return new User();
        }


    }

    /**
     * @param person
     * @return
     */
    private User convertUser(final CircabcUserDataBean person)
    {
        final User user = new User(
                person.getEcasUserName(),
                person.getFirstName(),
                person.getLastName(),
                person.getEmail());

        return user;
    }

    private XMLElement instanciateXMLElement(final XMLElement source) throws ETLException
    {
        final XMLElement newChild;
        try
        {
            if(source instanceof InterestGroup)
            {
                newChild = BinderUtils.createInterestGroup((String)((NamedNode) source).getName().getValue());
            }
            else
            {
                newChild = source.getClass().newInstance();

                if(source instanceof NamedNode)
                {
                    final TypedProperty.NameProperty prop = ((NamedNode) source).getName();
                    ((NamedNode) newChild).setName(prop);

                    if(source instanceof Url)
                    {
                        final URLProperty url = ((Url) source).getTarget();
                        ((Url) newChild).setTarget(url);
                    }
                    else if(source instanceof MlContent)
                    {
                        final TypedProperty.LocaleProperty locale = ((MlContent) source).getPivotLang();
                        ((MlContent) newChild).setPivotLang(locale);
                    }
                    else if(source instanceof InfMLContent)
                    {
                        final TypedProperty.LocaleProperty locale = ((InfMLContent) source).getPivotLang();
                        ((InfMLContent) newChild).setPivotLang(locale);
                    }
                    else if(source instanceof LibraryTranslation)
                    {
                        final TypedProperty.LocaleProperty locale = ((LibraryTranslation) source).getLang();
                        ((LibraryTranslation) newChild).setLang(locale);
                    }
                    else if(source instanceof LibraryTranslationVersion)
                    {
                        final TypedProperty.LocaleProperty locale = ((LibraryTranslationVersion) source).getLang();
                        ((LibraryTranslationVersion) newChild).setLang(locale);
                    }
                    else if(source instanceof InformationTranslation)
                    {
                        final TypedProperty.LocaleProperty locale = ((InformationTranslation) source).getLang();
                        ((InformationTranslation) newChild).setLang(locale);
                    }
                    else if(source instanceof InformationTranslationVersion)
                    {
                        final TypedProperty.LocaleProperty locale = ((InformationTranslationVersion) source).getLang();
                        ((InformationTranslationVersion) newChild).setLang(locale);
                    }
                }

                if(source instanceof ContentNode)
                {
                    final String prop = ((ContentNode) source).getUri();
                    ((ContentNode) newChild).setUri(prop);
                }
                else if(source instanceof Message)
                {
                    final String content = ((Message) source).getContent();
                    ((Message) newChild).setContent(content);
                }
                else if(source instanceof Link)
                {
                    final String reference = ((Link) source).getReference();
                    ((Link) newChild).setReference(reference);
                }
            }
            ElementsHelper.setXPath(newChild, ElementsHelper.getXPath(source));

        }
        catch (final Exception e)
        {
            throw new ETLException("Impossible to generate the residual XML. Fail to instanciate object of type" + source.getClass(), e) ;
        }

        return newChild;
    }

    protected ImportRoot unmarshall(final NodeRef resource) throws ETLException
    {
        if(logger.isDebugEnabled())
        {
            logger.debug("Start to unmarshall resource: " + resource);
        }
        try
        {
            final Unmarshaller unmarshaller = JavaXmlBinder.createUnmarshaller();

            final MigrationTracer<ImportRoot> etljournal = new MigrationTracer<ImportRoot>("etl files", false);
            this.validationHandler.validateXMLFile(unmarshaller, this.fileArchiver.getContentInputStream(resource), null, etljournal);
            final ImportRoot importRoot = etljournal.getUnmarshalledObject();

            if(logger.isDebugEnabled())
            {
                logger.debug("Import root successfully unmarshalled");
            }

            return importRoot;

        }
        catch (final JAXBException e)
        {
            throw(new ETLException("Problem reading the export file: " + resource, e));
        }
        catch (final SAXException e)
        {
            throw(new ETLException("Problem reading the export file: " + resource, e));
        }
        catch (final ValidationException e)
        {
            throw new ETLException("Problem reading the export file, it is not XSD valid: " + resource, e);
        }
    }

    protected final NodeRef getIterationNodeRef(final String iterationName) throws ETLException
    {
        try
        {
            return getIterationNodeRef(this.fileArchiver.getIterationByName(iterationName));
        }
        catch (final ArchiveException e)
        {
            throw new ETLException(e.getMessage(), e);
        }
    }
    protected final NodeRef getIterationNodeRef( MigrationIteration selectedIteration) throws ETLException
    {
        try
        {
            return this.fileArchiver.getTransformationResource(selectedIteration);
        }
        catch (final ArchiveException e)
        {
            throw new ETLException(e.getMessage(), e);
        }
    }

    /**
     * @param userService the userService to set
     */
    public final void setUserService(final UserService userService)
    {
        this.userService = userService;
    }

    /**
     * @param validationHandler the validationHandler to set
     */
    public final void setValidationHandler(final ValidationHandler validationHandler)
    {
        this.validationHandler = validationHandler;
    }

    /**
     * @param fileArchiver the fileArchiver to set
     */
    public final void setFileArchiver(final FileArchiver fileArchiver)
    {
        this.fileArchiver = fileArchiver;
    }
}
