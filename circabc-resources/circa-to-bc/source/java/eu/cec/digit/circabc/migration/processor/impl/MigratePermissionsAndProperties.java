/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor.impl;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.util.PropertyMap;
import org.apache.commons.jxpath.JXPathContext;

import eu.cec.digit.circabc.migration.entities.TypedPreference;
import eu.cec.digit.circabc.migration.entities.TypedProperty;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.CategoryHeader;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Directory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Node;
import eu.cec.digit.circabc.migration.entities.generated.permissions.AccessProfile;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Application;
import eu.cec.digit.circabc.migration.entities.generated.permissions.ImportedProfile;
import eu.cec.digit.circabc.migration.entities.generated.permissions.InformationUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.LibraryUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.NewsgroupUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Notifications;
import eu.cec.digit.circabc.migration.entities.generated.properties.ExtendedProperty;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordReferences;
import eu.cec.digit.circabc.migration.entities.generated.user.Person;
import eu.cec.digit.circabc.migration.entities.generated.user.Persons;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.profile.CircabcRootProfileManagerService;

/**
 * Import the container properties using the tree walking.
 *
 * <blockquote>
 * 	Extends migrate properties for performance issues. Theses two classes need to walk throuht the entire tree to process.
 * </blockquote>
 *
 * @author Yanick Pignot
 */
public class MigratePermissionsAndProperties extends MigrateProcessorBase
{

    @Override
    public void visit(final Persons persons) throws Exception
    {
    	for(final Person person: persons.getPersons())
    	{
    		visit(person);
    	}
    }
    @Override
    public void visit(final Person person) throws Exception
    {
    	visitPermPropsAndNotifications(getMigrateProperties(), getMigratePermissions(), person);
    }

    @Override
    public void visit(final Circabc circabc) throws Exception
    {
    	final MigratePermissions migratePermissions = getMigratePermissions();
    	final MigrateProperties migrateProperties = getMigrateProperties();

    	migratePermissions.visit(circabc, circabc.getCircabcAdmin());

    	for(final CategoryHeader header: circabc.getCategoryHeaders())
    	{
    		this.visit(header);
    	}

    	visitPermPropsAndNotifications(migrateProperties, migratePermissions, circabc);
    }


	@Override
	public void visit(final CategoryHeader header) throws Exception
	{
		for(final Category cat: header.getCategories())
    	{
    		this.visit(cat);
    	}
	}

    @Override
	public void visit(final Category category) throws Exception
	{
    	final MigratePermissions migratePermissions = getMigratePermissions();

    	migratePermissions.visit(category, category.getCategoryAdmin());

    	for(final InterestGroup ig: category.getInterestGroups())
    	{
    		this.visit(ig);
    	}
    	// ensure the creation of profiles before import ones
    	for(final InterestGroup ig: category.getInterestGroups())
    	{
    		for(final ImportedProfile profile : ig.getDirectory().getImportedProfiles())
    		{
    			migratePermissions.visit(ig.getDirectory(), profile);
    		}
    	}
	}

    @Override
	public void visit(final InterestGroup interestGroup) throws Exception
	{
    	final MigratePermissions migratePermissions = getMigratePermissions();
    	final MigrateProperties migrateProperties = getMigrateProperties();

    	if(interestGroup.getApplications() != null)
    	{
    		for(final Application application: interestGroup.getApplications().getApplications())
        	{
    			migratePermissions.visit(interestGroup, application);
        	}
    	}
		if(interestGroup.getDynamicPropertyDefinitions() != null)
    	{
			migrateProperties.visit(interestGroup, interestGroup.getDynamicPropertyDefinitions());
    	}
		if(interestGroup.getKeywordDefinitions() != null)
    	{
			migrateProperties.visit(interestGroup, interestGroup.getKeywordDefinitions());
    	}
		if(interestGroup.getLogoDefinitions() != null)
    	{
			migrateProperties.visit(interestGroup, interestGroup.getLogoDefinitions());
    	}
		this.visit(interestGroup.getDirectory());
	}

	@Override
	public void visit(final Directory directory) throws Exception
	{
		final MigratePermissions migratePermissions = getMigratePermissions();
		if(directory.getGuest() != null)
		{
			migratePermissions.visit(directory, CircabcConstant.GUEST_AUTHORITY, directory.getGuest());
		}
		if(directory.getRegistredUsers() != null)
		{
			migratePermissions.visit(directory, CircabcRootProfileManagerService.ALL_CIRCA_USERS_PROFILE_NAME, directory.getRegistredUsers());
		}

		for(final AccessProfile profile : directory.getAccessProfiles())
		{
			migratePermissions.visit(directory, profile.getName(), profile);
		}
	}

    private void visitPermPropsAndNotifications(final MigrateProperties migrateProperties, final MigratePermissions migratePermissions, final Node node) throws Exception
    {
    	// TypedProperty and Extended property
    	final PropertyMap properties = new PropertyMap();
    	final PropertyMap preferences = new PropertyMap();
    	final List<Node> childs = new ArrayList<Node>();
    	Notifications notifications = null;
    	KeywordReferences keywordReferences = null;
    	LibraryUserRights libraryUserRights = null;
    	NewsgroupUserRights newsgroupUserRights = null;
    	InformationUserRights informationUserRights = null;

    	final JXPathContext context = JXPathContext.newContext(node);

    	for(final Object child : context.selectNodes(SELECT_ALL_CHILDS))
    	{
    		if(child != null)
    		{
    			if(child instanceof Node)
    			{
    				childs.add((Node) child);
    			}
				else if(child instanceof TypedProperty)
				{
					final TypedProperty prop = (TypedProperty) child;
					properties.put(prop.getIdentifier(), prop.getValue());
				}
				else if(child instanceof ExtendedProperty)
				{
					final ExtendedProperty prop = (ExtendedProperty) child;
					properties.put(prop.getQname(), prop.getValue());
				}
				else if(child instanceof TypedPreference)
				{
					final TypedPreference prop = (TypedPreference) child;
					preferences.put(prop.getIdentifier(), prop.getValue());
				}
				else if(child instanceof Notifications)
				{
					notifications = (Notifications)child;
				}
				else if(child instanceof LibraryUserRights)
				{
					libraryUserRights = (LibraryUserRights)child;
				}
				else if(child instanceof NewsgroupUserRights)
				{
					newsgroupUserRights = (NewsgroupUserRights)child;
				}
				else if(child instanceof InformationUserRights)
				{
					informationUserRights = (InformationUserRights)child;
				}
				else if(child instanceof KeywordReferences)
				{
					keywordReferences = (KeywordReferences)child;
				}
    			// properties already setted for oldest versions:
    			// stop for (LibraryContentVersions, LibraryTranslationVersions, InformationContentVersions and InformationTranslationVersions)
    		}
    	}

    	if(notifications != null)
    	{
    		migratePermissions.visit(node, notifications);
    	}
    	if(keywordReferences != null)
    	{
    		migrateProperties.visit(node, keywordReferences);
    	}
    	if(libraryUserRights != null)
    	{
    		migratePermissions.visit(node, libraryUserRights);
    	}
    	if(newsgroupUserRights != null)
    	{
    		migratePermissions.visit(node, newsgroupUserRights);
    	}
    	if(informationUserRights != null)
    	{
    		migratePermissions.visit(node, informationUserRights);
    	}

    	
    	if (isFirstImport())
    	{
    		migrateProperties.visit(node, properties, preferences, true);
    	}	
    	else
    	{
    		migrateProperties.visit(node, properties, preferences, false);
    	}

    	for(final Node child: childs)
    	{
    		visitPermPropsAndNotifications(migrateProperties, migratePermissions, child);
    	}
    }

    private MigrateProperties getMigrateProperties()
 	{
 		final MigrateProperties migrateProperties = new MigrateProperties(importRoot, getJournal(), getRegistry());
 		return migrateProperties;
 	}

    private MigratePermissions getMigratePermissions()
 	{
 		final MigratePermissions migratePermissions = new MigratePermissions(importRoot, getJournal(), getRegistry());
 		return migratePermissions;
 	}
}
