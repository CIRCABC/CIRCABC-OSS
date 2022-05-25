/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.TypedProperty.DescriptionProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.TitleProperty;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.CategoryHeader;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Directory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Node;
import eu.cec.digit.circabc.migration.entities.generated.permissions.AccessProfile;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Application;
import eu.cec.digit.circabc.migration.entities.generated.permissions.CategoryAdmin;
import eu.cec.digit.circabc.migration.entities.generated.permissions.CircabcAdmin;
import eu.cec.digit.circabc.migration.entities.generated.permissions.GlobalAccessProfile;
import eu.cec.digit.circabc.migration.entities.generated.permissions.ImportedProfile;
import eu.cec.digit.circabc.migration.entities.generated.permissions.InformationPermissionItem;
import eu.cec.digit.circabc.migration.entities.generated.permissions.InformationUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.LibraryPermissionItem;
import eu.cec.digit.circabc.migration.entities.generated.permissions.LibraryUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.NewsgroupPermissionItem;
import eu.cec.digit.circabc.migration.entities.generated.permissions.NewsgroupUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.NotificationItem;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Notifications;
import eu.cec.digit.circabc.migration.journal.JournalLine;
import eu.cec.digit.circabc.migration.journal.JournalLine.Parameter;
import eu.cec.digit.circabc.migration.journal.JournalLine.Status;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.migration.ImportationException;
import eu.cec.digit.circabc.service.notification.NotificationStatus;
import eu.cec.digit.circabc.service.notification.NotificationSubscriptionService;
import eu.cec.digit.circabc.service.profile.CategoryProfileManagerService;
import eu.cec.digit.circabc.service.profile.CircabcRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.CircabcServices;
import eu.cec.digit.circabc.service.profile.IGRootProfileManagerService;
import eu.cec.digit.circabc.service.profile.Profile;
import eu.cec.digit.circabc.service.profile.ProfileManagerService;
import eu.cec.digit.circabc.service.profile.ProfileManagerServiceFactory;
import eu.cec.digit.circabc.service.profile.permissions.DirectoryPermissions;
import eu.cec.digit.circabc.service.profile.permissions.EventPermissions;
import eu.cec.digit.circabc.service.profile.permissions.InformationPermissions;
import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import eu.cec.digit.circabc.service.profile.permissions.NewsGroupPermissions;
import eu.cec.digit.circabc.service.profile.permissions.VisibilityPermissions;
import eu.cec.digit.circabc.util.CircabcUserDataBean;

/**
 * Import the container properties using the tree walking.
 *
 * @author Yanick Pignot
 */
public class MigratePermissions extends MigrateProcessorBase
{
	public MigratePermissions()
	{
		super();
	}

	public MigratePermissions(final ImportRoot importRoot, final MigrationTracer _journal, final CircabcServiceRegistry registry)
	{
		super(importRoot, _journal, registry);
	}

    @Override
    public void visit(final Circabc circabc, final CircabcAdmin circabcAdmin) throws Exception
    {
        if(circabcAdmin != null)
        {
            apply(new MigrateUserProfileCallback(getJournal(), circabc, circabcAdmin.getUsers(), CircabcRootProfileManagerService.Profiles.CIRCABC_ADMIN));
        }
    }

    @Override
    public void visit(final Category category, final CategoryAdmin categoryAdmin) throws Exception
    {
        if(categoryAdmin != null)
        {
            apply(new MigrateUserProfileCallback(getJournal(), category, categoryAdmin.getUsers(), CategoryProfileManagerService.Profiles.CIRCA_CATEGORY_ADMIN));
        }
    }

    @Override
    public void visit(final InterestGroup interestGroup, final Application application) throws Exception
    {
    	if(application != null)
        {
    		if (isFirstImport())
    		{
            	apply(new MigrateApplicantsCallback(getJournal(), interestGroup, application),true) ;
    		}
    		else
    		{
    			apply(new MigrateApplicantsCallback(getJournal(), interestGroup, application),false);
    		}
    		
    		
        }
    }

    @Override
    public void visit(final Directory directory, final String name, final AccessProfile profile) throws Exception
    {
    	apply(new MigrateProfileCallback(getJournal(), directory, profile, name));
    	apply(new MigrateUserProfileCallback(getJournal(), (Node)ElementsHelper.getParent(directory), profile.getUsers(), name));
    }

    @Override
    public void visit(final Directory directory, final String name, final GlobalAccessProfile profile) throws Exception
    {
        apply(new MigrateProfileCallback(getJournal(), directory, profile, name));
        apply( new MigrateUserProfileCallback(getJournal(), (Node)ElementsHelper.getParent(directory), Collections.<String> emptyList(), name));
    }

    @Override
    public void visit(final Directory directory, final ImportedProfile profile) throws Exception
    {
    	apply(new MigrateProfileCallback(getJournal(), directory, profile));
    }

    @Override
    public void visit(final Node node, final LibraryUserRights permissions) throws Exception
    {
        if(permissions != null && permissions.getPermissions() != null)
        {
            for(LibraryPermissionItem permItem: permissions.getPermissions())
            {
                apply(new MigrateUserPermissionCallback(getJournal(), node, permItem.getProfile(), permItem.getUser(), permItem.getRight().value(), false));
            }
        }
    }

    @Override
    public void visit(final Node node, final NewsgroupUserRights permissions) throws Exception
    {
        if(permissions != null && permissions.getPermissions() != null)
        {
            for(NewsgroupPermissionItem permItem: permissions.getPermissions())
            {
                apply( new MigrateUserPermissionCallback(getJournal(), node, permItem.getProfile(), permItem.getUser(), permItem.getRight().value(), false));
            }
        }
    }

    @Override
    public void visit(final Node node, final InformationUserRights permissions) throws Exception
    {
        if(permissions != null && permissions.getPermissions() != null)
        {
            for(InformationPermissionItem permItem: permissions.getPermissions())
            {
                apply( new MigrateUserPermissionCallback(getJournal(), node, permItem.getProfile(), permItem.getUser(), permItem.getRight().value(), false));
            }
        }
    }

    @Override
    public void visit(final Node node, final Notifications notifications) throws Exception
    {
        if(notifications != null && notifications.getNotifications() != null)
        {
            for(NotificationItem notifItem: notifications.getNotifications())
            {
                apply(new MigrateUserPermissionCallback(getJournal(), node, notifItem.getProfile(), notifItem.getUser(), notifItem.getStatus().value(), true));
            }
        }
    }

    class MigrateUserPermissionCallback extends JournalizedTransactionCallback
    {
        private final Node node;
        private final String profile;
        private final String user;
        private final String permName;
        private final boolean notification;

        public MigrateUserPermissionCallback(final MigrationTracer journal, final Node node, final String profile, final String user, final String permName, final boolean notification)
        {
        	super(journal);
            this.node = node;
            if(profile != null && profile.contains(":"))
            {
            	this.profile = profile.replace(":", "_");
            }
            else
            {
            	this.profile = profile;
            }

            this.user = user;
            this.permName = permName;
            this.notification = notification;
        }

        public String execute() throws Throwable
        {
            try
            {
                final InterestGroup igRoot = ElementsHelper.getElementInterestGroup(node);
                final NodeRef nodeRef = ElementsHelper.getNodeRef(node);
                final NodeRef igNodeRef = ElementsHelper.getNodeRef(igRoot);

                String authority = null;

                if(profile != null)
                {
                    final ProfileManagerService profileService = getProfileManagerServiceFactory().getProfileManagerService(igNodeRef);
                    authority = profileService.getProfile(igNodeRef, profile).getPrefixedAlfrescoGroupName();
                }
                else if(user != null)
                {
                    authority = user;
                }


                if(notification)
                {
                	final NotificationSubscriptionService notificationSubscriptionService = getRegistry().getNotificationSubscriptionService();

                    final eu.cec.digit.circabc.migration.entities.generated.permissions.NotificationStatus xmlStatus = eu.cec.digit.circabc.migration.entities.generated.permissions.NotificationStatus.fromValue(permName);

                    NotificationStatus repoStatus = null;

                    switch (xmlStatus)
                    {
                        case SUSCRIBE:
                            repoStatus = NotificationStatus.SUBSCRIBED;
                            break;
                        case UNSUSCRIBE:
                            repoStatus = NotificationStatus.UNSUBSCRIBED;
                            break;
                        case DEFAULT:
                            if(node instanceof InterestGroup)
                            {
                                repoStatus = NotificationStatus.UNSUBSCRIBED;
                            }
                            else
                            {
                                repoStatus = NotificationStatus.INHERITED;
                            }
                            break;
                    }
                    notificationSubscriptionService.setNotificationStatus(nodeRef, authority, repoStatus);
                }
                else
                {
                	final PermissionService permissionService = getRegistry().getAlfrescoServiceRegistry().getPermissionService();

                    permissionService.setPermission(nodeRef, authority, permName, true);
                }

                getReport().appendSection(ElementsHelper.getQualifiedPath(node) + " successfully updated, "
                        + ((profile != null) ? " profile " + profile : " user " + user)
                        + " successfully granted with "
                        + ((notification) ? "notification: " : "permission: " + permName));


                getJournal().journalize(JournalLine.updateNode(
                        Status.SUCCESS,
                        ElementsHelper.getQualifiedPath(node),
                        nodeRef.toString(),
                        ((notification) ? JournalLine.UpdateOperation.SET_NOTIFICATION : JournalLine.UpdateOperation.SET_PERMISSION),
                        buildParameterMap(user, profile, permName)
                    ));
            }
            catch(Throwable t)
            {
            	getReport().appendSection("Impossible to grant on " +  ElementsHelper.getQualifiedPath(node)
                        + ((notification) ? " the notification: " : " the permission: " + permName)
                        + " to the "
                        + ((profile != null) ? " profile " + profile : " user " + user) +
                        ". " + t.getMessage());

                if(isFailOnError())
                {
                    throw t;
                }
                else
                {
                    getJournal().journalize(JournalLine.updateNode(
                                            Status.FAIL,
                                            ElementsHelper.getQualifiedPath(node),
                                            null,
                                            ((notification) ? JournalLine.UpdateOperation.SET_NOTIFICATION : JournalLine.UpdateOperation.SET_PERMISSION),
                                            buildParameterMap(user, profile, permName)
                        ));
                }
            }

            return null;
        }

		private Map<Parameter, Serializable> buildParameterMap(final String _user, final String _profile, final String _permName)
        {
            final Map<Parameter, Serializable> parameter = new HashMap<Parameter, Serializable>(2);

            if(_profile != null)
            {
                parameter.put(Parameter.PROFILE, _profile);
            }
            else
            {
                parameter.put(Parameter.USER_NAME, _user);
            }
            parameter.put(Parameter.PERMISSION, _permName);

            return parameter;
        }

    }

    class MigrateProfileCallback extends JournalizedTransactionCallback
    {
         private final InformationPermissions infPerm;
         private final LibraryPermissions libPerm;
         private final DirectoryPermissions dirPerm;
         private final EventPermissions evePerm;
         private final NewsGroupPermissions newsPerm;
         private final String profileName;
         private final String targetProfileName;
         private final Boolean visibility;
         private final InterestGroup interestGroup;
         private final Boolean exported;
         private final TitleProperty titles;
         private final DescriptionProperty descriptions;
         private NodeRef targetInterestGroup = null;

         public MigrateProfileCallback(final MigrationTracer journal, final Directory directory, final GlobalAccessProfile globalAccessProfile, final String name)
         {
        	 super(journal);
             infPerm = InformationPermissions.withPermissionString(globalAccessProfile.getInformationPermission().value());
             libPerm = LibraryPermissions.withPermissionString(globalAccessProfile.getLibraryPermission().value());
             dirPerm = DirectoryPermissions.withPermissionString(globalAccessProfile.getDirectoryPermission().value());
             evePerm = EventPermissions.withPermissionString(globalAccessProfile.getEventPermission().value());
             newsPerm = NewsGroupPermissions.withPermissionString(globalAccessProfile.getNewsgroupPermission().value());
             interestGroup = ElementsHelper.getElementInterestGroup(directory);
             visibility = globalAccessProfile.isVisibility();
             profileName = name;
             exported = false;
             this.titles = globalAccessProfile.getTitle();
             this.descriptions = globalAccessProfile.getDescription();
             targetInterestGroup = null;
             targetProfileName = null;
         }

         public MigrateProfileCallback(final MigrationTracer journal, final Directory directory, final AccessProfile accessProfile, final String name)
         {
        	 super(journal);
             infPerm = InformationPermissions.withPermissionString(accessProfile.getInformationPermission().value());
             libPerm = LibraryPermissions.withPermissionString(accessProfile.getLibraryPermission().value());
             dirPerm = DirectoryPermissions.withPermissionString(accessProfile.getDirectoryPermission().value());
             evePerm = EventPermissions.withPermissionString(accessProfile.getEventPermission().value());
             newsPerm = NewsGroupPermissions.withPermissionString(accessProfile.getNewsgroupPermission().value());
             interestGroup = ElementsHelper.getElementInterestGroup(directory);
             visibility = Boolean.TRUE;
             profileName = name;
             exported = accessProfile.isExported();
             this.titles = accessProfile.getTitle();
             this.descriptions =  accessProfile.getDescription();
             targetInterestGroup = null;
             targetProfileName = null;
         }

         public MigrateProfileCallback(final MigrationTracer journal, final Directory directory, final ImportedProfile importedProfile) throws ImportationException
         {
        	 super(journal);
             infPerm = InformationPermissions.withPermissionString(importedProfile.getInformationPermission().value());
             libPerm = LibraryPermissions.withPermissionString(importedProfile.getLibraryPermission().value());
             dirPerm = DirectoryPermissions.withPermissionString(importedProfile.getDirectoryPermission().value());
             evePerm = EventPermissions.withPermissionString(importedProfile.getEventPermission().value());
             newsPerm = NewsGroupPermissions.withPermissionString(importedProfile.getNewsgroupPermission().value());
             interestGroup = ElementsHelper.getElementInterestGroup(directory);
             visibility = Boolean.TRUE;
             exported = false;
             this.titles = importedProfile.getTitle();
             this.descriptions = importedProfile.getDescription();
             if(importRoot.getCircabc() == null)
             {
            	 final Category category = ElementsHelper.getElementCategory(directory);
            	 final CategoryHeader header = (CategoryHeader) ElementsHelper.getParent(category);
            	 final Circabc circabc = (Circabc) ElementsHelper.getParent(header);
            	 importRoot = (ImportRoot) ElementsHelper.getParent(circabc);

             }

             final Category category = ElementsHelper.getElementCategory(interestGroup);
             final NodeRef categoryRef = ElementsHelper.getNodeRef(category);

             final String targetIgName = importedProfile.getTargetInterestgroup();
             targetProfileName = importedProfile.getTargetProfile();
             profileName = targetIgName + "_" + targetProfileName;

             for(final InterestGroup ig: category.getInterestGroups())
             {
            	 if(ig.getName().getValue().equals(targetIgName))
            	 {
            		 targetInterestGroup = ElementsHelper.getNodeRef(ig);
            		 break;
            	 }
             }

             if(targetInterestGroup == null)
             {
            	 // it should be already created
            	 targetInterestGroup = getNodeService().getChildByName(categoryRef, ContentModel.ASSOC_CONTAINS, targetIgName);
             }

             if(this.targetInterestGroup == null)
             {
            	 throw new ImportationException("The target interest group '" + targetIgName + "' must be created before importing profile:  " + profileName + " in interestgroup: " + interestGroup.getName().getValue());
             }
         }

         public String execute() throws Throwable
         {
            try
            {
            	updateProfile(ElementsHelper.getNodeRef(interestGroup), CircabcServices.DIRECTORY, dirPerm.toString(), visibility);
            	updateProfile(ElementsHelper.getNodeRef(interestGroup.getLibrary()), CircabcServices.LIBRARY, libPerm.toString(), null);
                updateProfile(ElementsHelper.getNodeRef(interestGroup.getNewsgroups()), CircabcServices.NEWSGROUP, newsPerm.toString(), null);
                updateProfile(ElementsHelper.getNodeRef(interestGroup.getInformation()), CircabcServices.INFORMATION, infPerm.toString(), null);
                updateProfile(ElementsHelper.getNodeRef(interestGroup.getEvents()), CircabcServices.EVENT, evePerm.toString(), null);
            }
            catch(Throwable t)
            {
            	getReport().appendSection("Impossible update " +  ElementsHelper.getQualifiedPath(interestGroup)
                        + ", impossible to create / update profile " + profileName
                        + ". " + t.getMessage());

            	if(isFailOnError())
                {
                    throw t;
                }
                else
                {
                    getJournal().journalize(JournalLine.updateNode(
                                            Status.FAIL,
                                            ElementsHelper.getQualifiedPath(interestGroup),
                                            null,
                                            JournalLine.UpdateOperation.UPDATE_PROFILE,
                                            buildParameterMap(profileName, null)
                        ));
                }
            }

            return null;
        }

        private void updateProfile(final NodeRef noderef, final CircabcServices service, final String permission, final Boolean profileVisibility)
        {
        	final Map<String, Set<String>> profileMap = new HashMap<String, Set<String>>();
        	profileMap.put(service.toString(), Collections.<String> singleton(permission));

        	if(profileVisibility != null)
        	{
        		if(profileVisibility)
	        	{
	        		profileMap.put(CircabcServices.VISIBILITY.toString(), Collections.<String> singleton(VisibilityPermissions.VISIBILITY.toString()));
	        	}
	        	else
	        	{
	        		profileMap.put(CircabcServices.VISIBILITY.toString(), Collections.<String> singleton(VisibilityPermissions.NOVISIBILITY.toString()));
	        	}
        	}

        	final ProfileManagerService profileService = getProfileManagerServiceFactory().getProfileManagerService(noderef);

        	boolean update = true;
        	boolean isIGRoot = profileService instanceof IGRootProfileManagerService;

        	// we can create a profile only at the Interest Goup level.
        	if(isIGRoot)
        	{
        		final Map<String, Profile> profiles = profileService.getProfileMap(noderef);
    			update = profiles.containsKey(profileName);
        	}

			if(!update)
			{
				if(targetInterestGroup != null)
				{
					profileService.importProfile(noderef, targetInterestGroup, targetProfileName, profileMap);
				}
				else
				{
					profileService.addProfile(noderef, profileName, profileMap);
				}
			}
			else
			{
				profileService.updateProfile(noderef, profileName, profileMap, true);
			}

			if(isIGRoot)
			{
				 if(exported != null && exported.booleanValue())
				 {
					 if(profileService.getProfile(noderef, profileName).isExported() == false)
					 {
						 profileService.exportProfile(noderef, profileName, true);
					 }
				 }
				 if(titles != null && titles.getValue() != null)
	             {
					 final MLText mltext;
					 if(titles.getValue() instanceof MLText)
					 {
						 mltext = (MLText) titles.getValue() ;
					 }
					 else
					 {
						 mltext = new MLText(titles.getValue().toString());
					 }

					 profileService.addProfileTitles(noderef, profileName, mltext);
	             }
				 if(descriptions != null && descriptions.getValue() != null)
				 {
					 final MLText mltext;
					 if(descriptions.getValue() instanceof MLText)
					 {
						 mltext = (MLText) descriptions.getValue() ;
					 }
					 else
					 {
						 mltext = new MLText(descriptions.getValue().toString());
					 }
					 profileService.addProfileDescriptions(noderef, profileName, mltext);
				 }
			}


			getReport().appendSection(ElementsHelper.getQualifiedPath(interestGroup) + " successfully updated, the profile is successfully "
					+ (update ? "updated" : "created")
					+ " with the permissions:  " + profileMap);


			getJournal().journalize(JournalLine.updateNode(
					Status.SUCCESS,
					ElementsHelper.getQualifiedPath(interestGroup),
					noderef.toString(),
					((update) ? JournalLine.UpdateOperation.UPDATE_PROFILE : JournalLine.UpdateOperation.ADD_PROFILE),
					buildParameterMap(profileName, profileMap)
				));
        }

        private Map<Parameter, Serializable> buildParameterMap(final String _profile, final Map<String, Set<String>> _profileMap)
        {
            final Map<Parameter, Serializable> parameter = new HashMap<Parameter, Serializable>(2);

            parameter.put(Parameter.PROFILE, _profile);
            if(_profileMap != null)
            {
                parameter.put(Parameter.PERMISSION_MAP, (Serializable) _profileMap);
            }

            return parameter;
        }

    }

    class MigrateUserProfileCallback extends JournalizedTransactionCallback
    {
        private final Node node;
        private final String profileName;
        private final List<String> users;

        public MigrateUserProfileCallback(final MigrationTracer journal, final Node node, final String user, final String profileName)
        {
        	super(journal);
            this.node = node;
            this.profileName = profileName;
            this.users = Collections.singletonList(user);
        }

        public MigrateUserProfileCallback(final MigrationTracer journal, final Node node, final List<String> users, final String profileName)
        {
        	super(journal);
            this.node = node;
            this.profileName = profileName;
            this.users = users;
        }

        public String execute() throws Throwable
        {
            try
            {
                 for(final String userId : users)
                 {
                	 if (!getUserService().isUserExists(userId) )
                	 {
                		getReport().appendSection("Impossible to grant on " +  ElementsHelper.getQualifiedPath(node) + " the user " + userId + " having profile " + profileName +  ".User does not exists." ); 
                		continue; 
                	 }
                     try
                     {
                         final NodeRef nodeRef = ElementsHelper.getNodeRef(node);

                         final ProfileManagerService profileService = getProfileManagerServiceFactory().getProfileManagerService(nodeRef);
                         final Set<String> usersInProfile = profileService.getPersonInProfile(nodeRef, profileName);

                         if(usersInProfile.contains(userId))
                         {
                        	 getReport().appendSection(ElementsHelper.getQualifiedPath(node) + " already have " + userId + " having profile " + profileName);
                         }
                         else
                         {
                        	 String personProfile = profileService.getPersonProfile(nodeRef, userId);
                     		
                     		if (personProfile != null && !personProfile.equalsIgnoreCase(CircabcRootProfileManagerService.ALL_CIRCA_USERS_PROFILE_NAME))
                     		{
                     			getReport().appendSection("Impossible to grant on " +  ElementsHelper.getQualifiedPath(node) + " the user " + userId + " having profile " + profileName +  ".User have already a profile in the group : " + personProfile ); 
                        		continue;
                     		}
                     		else
                     		{
	                            profileService.addPersonToProfile(nodeRef, userId, profileName);
	
	                            getJournal().journalize(JournalLine.updateNode(
	                                     Status.SUCCESS,
	                                     ElementsHelper.getQualifiedPath(node),
	                                     nodeRef.toString(),
	                                     JournalLine.UpdateOperation.SET_PROFILE,
	                                     buildParameterMap(userId, profileName)
	                                 ));
	
	                            getReport().appendSection(ElementsHelper.getQualifiedPath(node) + " successfully grants " + userId + " having profile " + profileName);
                     		}
                         }
                     }
                     catch(Throwable t)
                     {
                    	 getReport().appendSection("Impossible to grant on " +  ElementsHelper.getQualifiedPath(node) + " the user " + userId + " having profile " + profileName +  ". " + t.getMessage());

                         if(isFailOnError())
                         {
                             throw t;
                         }
                         else
                         {
                             getJournal().journalize(JournalLine.updateNode(
                                                     Status.FAIL,
                                                     ElementsHelper.getQualifiedPath(node),
                                                     null,
                                                     JournalLine.UpdateOperation.SET_PROFILE,
                                                     buildParameterMap(userId, profileName)
                                 ));
                         }
                     }
                 }
            }
            catch(Throwable t)
            {
                getReport().appendSection("Impossible to grant on " +  ElementsHelper.getQualifiedPath(node) + " all specified users having profile " + profileName +  ". " + t.getMessage());

                if(isFailOnError())
                {
                    throw t;
                }
                else
                {
                    getJournal().journalize(JournalLine.updateNode(
                                            Status.FAIL,
                                            ElementsHelper.getQualifiedPath(node),
                                            null,
                                            JournalLine.UpdateOperation.SET_PROFILE,
                                            buildParameterMap(users.toString(), profileName)
                        ));
                }
            }

            return null;
        }

        final Map<JournalLine.Parameter, Serializable> buildParameterMap(final String _name, final String _profile)
        {
            final Map<JournalLine.Parameter, Serializable> parameters =  new HashMap<JournalLine.Parameter, Serializable>(3);

            if(_name != null)
            {
                parameters.put(JournalLine.Parameter.USER_NAME, _name);
            }
            if(_profile != null)
            {
                parameters.put(JournalLine.Parameter.PROFILE, _profile);
            }

            return parameters;
        }
    }

    class MigrateApplicantsCallback extends JournalizedTransactionCallback
    {
        private final InterestGroup interestGroup;
        private final  Date date;
        private final  String user;
        private final  String message;


        public MigrateApplicantsCallback(final MigrationTracer journal, final InterestGroup interestGroup, Application application)
        {
        	super(journal);
            this.interestGroup = interestGroup;
            this.date = application.getDate();
            this.user = application.getUser();
            this.message = application.getMessage();
        }

        public String execute() throws Throwable
        {
        	final NodeRef igNodeRef = ElementsHelper.getNodeRef(interestGroup);
            try
            {
                final ProfileManagerService profileService = getProfileManagerServiceFactory().getIGRootProfileManagerService();

        		//an applicant can't be invited in the Interest Group
                if(profileService.getInvitedUsers(igNodeRef).contains(user) == false)
                {
                	final CircabcUserDataBean userData = getUserService().getLDAPUserDataByUid(user);

                	if(userData != null)
                	{
                    	profileService.addApplicantPerson(igNodeRef, user, message, userData.getFirstName(), userData.getLastName(), date);
                	}
                	else
                	{
                    	profileService.addApplicantPerson(igNodeRef, user, message, null, null, date);
                	}

                	getReport().appendSection(ElementsHelper.getQualifiedPath(interestGroup) + " successfully updated, the application "
                            + "for user " + user + " at date " + date + " with message " + message
                            + " successfully setted");

                	getJournal().journalize(JournalLine.updateNode(
                            Status.SUCCESS,
                            ElementsHelper.getQualifiedPath(interestGroup),
                            igNodeRef.toString(),
                            JournalLine.UpdateOperation.ADD_APPLICATION,
                            Collections.singletonMap(JournalLine.Parameter.USER_NAME, (Serializable) user)
                        ));


                }
                else
                {
                	getReport().appendSection(ElementsHelper.getQualifiedPath(interestGroup) + " not updated, the application "
                            + "for user " + user + " at date " + date + " with message " + message
                            + " can't be updated due to that the user is already invited. This is not an error.");
                }
            }
            catch(Throwable t)
            {
            	getReport().appendSection("Impossible to update " +  ElementsHelper.getQualifiedPath(interestGroup)
                        + " with the application of user " + user + " with date " + date
                        + " and message " + message + ". " + t.getMessage());

                if(isFailOnError())
                {
                    throw t;
                }
                else
                {
                	getJournal().journalize(JournalLine.updateNode(
                            Status.FAIL,
                            ElementsHelper.getQualifiedPath(interestGroup),
                            (igNodeRef) == null ? "NULL" : igNodeRef.toString(),
                            JournalLine.UpdateOperation.ADD_APPLICATION,
                            Collections.singletonMap(JournalLine.Parameter.USER_NAME, (Serializable) user)
                        ));
                }
            }

            return null;
        }
    }

    private ProfileManagerServiceFactory getProfileManagerServiceFactory()
	{
		return getRegistry().getProfileManagerServiceFactory();
	}
}
