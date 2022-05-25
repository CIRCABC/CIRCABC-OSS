/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.processor.impl;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyMap;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.TypedProperty;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Appointment;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Directory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Event;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Events;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Link;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Logo;
import eu.cec.digit.circabc.migration.entities.generated.nodes.LogoDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Meeting;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Node;
import eu.cec.digit.circabc.migration.entities.generated.nodes.SharedSpacelink;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Space;
import eu.cec.digit.circabc.migration.entities.generated.permissions.AccessProfile;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Application;
import eu.cec.digit.circabc.migration.entities.generated.permissions.CategoryAdmin;
import eu.cec.digit.circabc.migration.entities.generated.permissions.CircabcAdmin;
import eu.cec.digit.circabc.migration.entities.generated.permissions.GlobalAccessProfile;
import eu.cec.digit.circabc.migration.entities.generated.permissions.LibraryPermissionItem;
import eu.cec.digit.circabc.migration.entities.generated.permissions.LibraryUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.NewsgroupPermissionItem;
import eu.cec.digit.circabc.migration.entities.generated.permissions.NewsgroupUserRights;
import eu.cec.digit.circabc.migration.entities.generated.permissions.NotificationItem;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Notifications;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynPropertyType;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynamicPropertyDefinition;
import eu.cec.digit.circabc.migration.entities.generated.properties.DynamicPropertyDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.properties.EveryTimesOccurence;
import eu.cec.digit.circabc.migration.entities.generated.properties.ExtendedProperty;
import eu.cec.digit.circabc.migration.entities.generated.properties.I18NProperty;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordDefinition;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordDefinitions;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordReferences;
import eu.cec.digit.circabc.migration.entities.generated.properties.Shared;
import eu.cec.digit.circabc.migration.entities.generated.properties.TimesOccurence;
import eu.cec.digit.circabc.migration.entities.generated.user.Person;
import eu.cec.digit.circabc.migration.entities.generated.user.Persons;
import eu.cec.digit.circabc.migration.journal.MigrationTracer;
import eu.cec.digit.circabc.migration.journal.report.Report;
import eu.cec.digit.circabc.migration.processor.Processor;
import eu.cec.digit.circabc.migration.walker.TreeWalkerVisitorBase;
import eu.cec.digit.circabc.service.CircabcServiceRegistry;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyService;
import eu.cec.digit.circabc.service.migration.ImportationException;

/**
 * Walker that fill a complete report of the structure
 *
 * @author Yanick Pignot
 */
public class ReportStructure extends TreeWalkerVisitorBase implements Processor
{
	private Report report;

	private final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");

	private CircabcServiceRegistry registry;

	public Report process(final CircabcServiceRegistry registry, final ImportRoot importRoot, final MigrationTracer journal)  throws ImportationException
	{
		ParameterCheck.mandatory("The service registry", registry);
		ParameterCheck.mandatory("The root element", importRoot);
		ParameterCheck.mandatory("An instance of the importation journal", journal);
        ParameterCheck.mandatory("An instance of the report", journal.getProcessReport());

        this.registry = registry;
		this.report = journal.getProcessReport();

		try
    	{
			walk(importRoot);
    	}
    	catch(ImportationException ex)
    	{
    		throw ex;
    	}
    	catch(Throwable t)
    	{
    		throw new ImportationException("Unexpected error when preporting structure", t);
    	}

    	final Report reportMem = report;

    	onFinish();

		return reportMem;
	}

	@Override
	protected void visitNodeChilds(final Node anyNode) throws Exception
	{
		// display the common information of each nodes
		if((anyNode instanceof Circabc) == false)
		{
			displayNodeInformation(anyNode);
		}

		super.visitNodeChilds(anyNode);
	}

	private void displayNodeInformation(final Node anyNode)
	{
		final StringBuffer buffer = new StringBuffer();
		final boolean isUpdate = ElementsHelper.isNodeCreated(anyNode);
		if(isUpdate)
		{
			buffer.append("Update ");
		}
		else
		{
			buffer.append("Create ");
		}

		buffer
			.append("node ")
			.append(ElementsHelper.getQualifiedName(anyNode));

		report.appendSection(buffer.toString());

		PropertyMap properties = ElementsHelper.getProperties(anyNode);

		if(properties != null && properties.size() > 1)
		{
			report.appendSubSection("With properties : ");

			for(final Map.Entry<QName, Serializable> entry : properties.entrySet())
			{
				report.appendLine(entry.toString());
			}
		}
		else
		{
			report.appendSubSection("No properties setted. " + ((isUpdate) ? "The original ones will be keeped. " : "The default ones will be used."));
		}
	}

	public void onFinish()
	{
		importRoot = null;
		circabcContext = null;
		personsContext = null;
		report = null;
	}

	public void visit(final Persons persons) throws Exception
	{
		if(persons != null && notEmpty(persons.getPersons()))
		{
			report.appendSection(persons.getPersons().size() + " persons defined: ");
		}

		super.visit(persons);
	}

	public void visit(final Person person) throws Exception
	{
		if(person != null)
		{
			final String userId = (String) person.getUserId().getValue();

			if(registry.getAlfrescoServiceRegistry().getPersonService().personExists(userId))
			{
				report.appendSubSection("Update user '" + userId + "'");
			}
			else
			{
				report.appendSubSection("Create user '" + userId + "'");
			}

			safeAppendLine(person.getUserId());
			safeAppendLine(person.getEmail());
			safeAppendLine(person.getFirstName());
			safeAppendLine(person.getLastName());
			safeAppendLine(person.getTitle());
			safeAppendLine(person.getOrganisation());
			safeAppendLine(person.getPhone());
			safeAppendLine(person.getPostalAddress());
			safeAppendLine(person.getDescription());
			safeAppendLine(person.getFax());
			safeAppendLine(person.getUrlAddress());
			safeAppendLine(person.getGlobalNotification());
			safeAppendLine(person.getPersonalInformation());

			if(person.getContentLanguageFilter() != null)
			{
				report.appendLine("Content Language Filter: " + person.getContentLanguageFilter());
			}
			if(person.getInterfaceLanguage() != null)
			{
				report.appendLine("Interface Language: " + person.getInterfaceLanguage());
			}

		}
	}

	private void safeAppendLine(Object object)
	{
		 safeAppendLine("", object);
	}

	private void safeAppendLine(String startOfLine, Object object)
	{
		if(object != null)
		{
			report.appendLine(startOfLine + object.toString());
		}
	}

	public void visit(final Circabc circabc, final CircabcAdmin circabcAdmin)
	{
		displayNodeInformation(circabc);

		if(circabcAdmin != null)
		{
			report.appendSubSection("With Circabc admin(s): ");
			report.appendLines(circabcAdmin.getUsers());
		}

	}

	public void visit(final Category category, final CategoryAdmin categoryAdmin)
	{
		if(categoryAdmin != null)
		{
			report.appendSubSection("With Category admin(s): ");
			report.appendLines(categoryAdmin.getUsers());
		}
	}

	public void visit(final InterestGroup interestGroup, final Application application)
	{
		if(application != null)
		{
			report.appendSubSection("With Application: ");
			report.appendLines(Arrays.asList(DATE_FORMAT.format(application.getDate()), application.getUser(), application.getMessage()));
		}

	}

	public void visit(final Events eventRoot, final Event event) throws Exception
	{
		report.appendSection("Create event definition with properties: ");
		safeAppendLine("Type: ", event.getType());
		safeAppendLine("Priority: ", event.getPriority());

		displayAppointment(event);
		super.visit(eventRoot, event);
	}

	public void visit(final Events eventRoot, final Meeting meeting) throws Exception
	{
		report.appendSection("Create meeting definition with properties: ");
		safeAppendLine("Type: ", meeting.getType());
		safeAppendLine("Availability: ", meeting.getAvailability());
	    safeAppendLine("Organization: ", meeting.getOrganization());
	    safeAppendLine("Agenda: ", meeting.getAgenda());
	    safeAppendLine("Library Section: ", meeting.getLibrarySection());

		displayAppointment(meeting);

		super.visit(eventRoot, meeting);
	}

	private void displayAppointment(final Appointment appointment)
	{
		safeAppendLine("Abstract: ", appointment.getAbstract());
		safeAppendLine("Title: ", appointment.getAppointmentTitle());
		safeAppendLine("Language: ", appointment.getLanguage());
		safeAppendLine("Location: ", appointment.getLocation());

		if(appointment.getDate() != null)
		{
			report.appendLine("Date: " + DATE_FORMAT.format(appointment.getDate()));
		}

		safeAppendLine("Time Zone: ", appointment.getTimeZoneId());
		if(appointment.getEveryTimesOccurence() != null )
		{
			EveryTimesOccurence occurence = appointment.getEveryTimesOccurence();
			safeAppendLine("Occurence: " + "Every " + occurence.getEvery() + " " + occurence.getType() + " for " + occurence.getForTimes() + " times");
			if(appointment.getStartDate() != null)
			{
				report.appendLine("Fist occurence: " + DATE_FORMAT.format(appointment.getStartDate()));
			}
		}
		else if(appointment.getTimesOccurence() != null)
		{
			TimesOccurence occurence = appointment.getTimesOccurence();
			safeAppendLine("Occurence: " + occurence.getType() + " for " + occurence.getForTimes() + " times");
			if(appointment.getStartDate() != null)
			{
				report.appendLine("Fist occurence: " + DATE_FORMAT.format(appointment.getStartDate()));
			}
		}
		else
		{
			safeAppendLine("Occurs Once");
			if(appointment.getStartDate() != null)
			{
				report.appendLine("On: " + DATE_FORMAT.format(appointment.getStartDate()));
			}
		}


		if(appointment.getStartTime() != null)
		{
			report.appendLine("Starts at: " + TIME_FORMAT.format(appointment.getStartTime()));
		}
		if(appointment.getEndTime() != null)
		{
			report.appendLine("Ends at: " + TIME_FORMAT.format(appointment.getEndTime()));
		}


		if(appointment.getAudienceClosed() != null)
		{
			safeAppendLine("The meeting audience is closed");
			safeAppendLine("Invited users: ", appointment.getAudienceClosed());
		}
		else
		{
			safeAppendLine("The meeting audience is closed");
		}


		safeAppendLine("Contact - Name: ", appointment.getContact().getName());
		safeAppendLine("Contact - Email: ", appointment.getContact().getEmail());
		safeAppendLine("Contact - Phone: ", appointment.getContact().getPhone());
		safeAppendLine("Contact - Url: ", appointment.getContact().getUrl());
	}

	public void visit(final Directory directory, final String name, final  AccessProfile profile)
	{
		report.appendSubSection(name + " profile definition:");

		report.appendLine("Information Permission: " + profile.getInformationPermission());
		report.appendLine("Library Permission: " + profile.getLibraryPermission());
		report.appendLine("Directory Permission: " + profile.getDirectoryPermission());
		report.appendLine("Event Permission: " + profile.getEventPermission());
		report.appendLine("Newsgroup Permission: " + profile.getNewsgroupPermission());

		final List<String> users = profile.getUsers();
		if(notEmpty(users))
		{
			report.appendSubSection(name + " profile granted users:");
			report.appendLines(users);
		}
		else
		{
			report.appendSubSection(name + " profile has no granted users !");
		}
	}

	public void visit(final Directory directory, final String name, final GlobalAccessProfile profile)
	{
		report.appendSubSection(name + " profile definition:");

		report.appendLine("Information Permission: " + profile.getInformationPermission());
		report.appendLine("Library Permission: " + profile.getLibraryPermission());
		report.appendLine("Directory Permission: " + profile.getDirectoryPermission());
		report.appendLine("Event Permission: " + profile.getEventPermission());
		report.appendLine("Newsgroup Permission: " + profile.getNewsgroupPermission());

		report.appendSubSection("");
	}

	public void visit(final Node node, final LibraryUserRights permissions)
	{
		if(permissions != null && notEmpty(permissions.getPermissions()))
		{
			report.appendSubSection("Permissions setted (inherited=" + permissions.isInherit() + "): ");

			for(final LibraryPermissionItem permissionItem : permissions.getPermissions())
			{
				if(permissionItem.getProfile() != null)
				{
					report.appendLine("The profile "  + permissionItem.getProfile() + " has right " + permissionItem.getRight());
				}
				else
				{
					report.appendLine("The user "  + permissionItem.getProfile() + " has right " + permissionItem.getRight());
				}
			}
		}
	}

	public void visit(final Node node, final NewsgroupUserRights permissions)
	{
		if(permissions != null && notEmpty(permissions.getPermissions()))
		{
			report.appendSubSection("Permissions setted (inherited=" + permissions.isInherit() + "): ");

			for(final NewsgroupPermissionItem permissionItem : permissions.getPermissions())
			{
				if(permissionItem.getProfile() != null)
				{
					report.appendLine("The profile "  + permissionItem.getProfile() + " has right " + permissionItem.getRight());
				}
				else
				{
					report.appendLine("The user "  + permissionItem.getProfile() + " has right " + permissionItem.getRight());
				}
			}
		}
	}

	public void visit(final InterestGroup interestGroup, final DynamicPropertyDefinitions dynamicPropertyDefinitions)
	{
		if(dynamicPropertyDefinitions != null && notEmpty(dynamicPropertyDefinitions.getDefinitions()))
		{
			final List<DynamicPropertyDefinition> definitions = dynamicPropertyDefinitions.getDefinitions();
			report.appendSubSection("Dynamic properties defined (" + definitions.size()+ " on " + DynamicPropertyService.MAX_PROPERTY_BY_IG_IN_CIRCA + " possible):");

			Object values = null;
			DynPropertyType type = null;
			for(final DynamicPropertyDefinition def : definitions)
			{
				values = (notEmpty(def.getI18NValues())) ? i18nValuesAsString(def.getI18NValues()) : def.getValue();
				type = def.getType();

				report.appendLine("Id: " + def.getId() + ". Value: " + values + ". Type: " + type + ((DynPropertyType.SELECTION.equals(type)) ? ". Selection: " + def.getSelectionCases() : "." ));
			}
		}
		else
		{
			report.appendSubSection("No Dynamic property setted");
		}
	}

	public void visit(final InterestGroup interestGroup, final LogoDefinitions logoDefinitions) throws Exception
	{
		if(logoDefinitions != null && notEmpty(logoDefinitions.getLogos()))
		{
			final List<Logo> logos = logoDefinitions.getLogos();
			report.appendSubSection("Logo defined (" + logos.size() + ") :");

			if(logoDefinitions.getDefaultLogo() != null)
			{
				report.appendLine("Default logo setted: " + logoDefinitions.getDefaultLogo());
			}
			else
			{
				report.appendLine("No default logo defined.");
			}


			for(final Logo logo : logos)
			{
				report.appendLine("Logo with name " + logo.getName() + " at " + logo.getUri());
			}
		}
		else
		{
			report.appendSubSection("No Logo definition setted");
		}
	}


	public void visit(final InterestGroup interestGroup, final KeywordDefinitions keywordDefinitions)
	{
		if(keywordDefinitions != null && notEmpty(keywordDefinitions.getDefinitions()))
		{
			final List<KeywordDefinition> definitions = keywordDefinitions.getDefinitions();
			report.appendSubSection("Keywords defined " + (definitions.size()) + ": ");

			Object values = null;
			for (final KeywordDefinition def : definitions)
			{
				values = (notEmpty(def.getI18NValues())) ? i18nValuesAsString(def.getI18NValues()) : def.getValue();
				report.appendLine("Id: " + def.getId() + ". Value: " + values);
			}
		}
		else
		{
			report.appendSubSection("No Keyword setted");
		}
	}

	public void visit(final Node node, final ExtendedProperty property)
	{
		// already logged
	}

	public void visit(final Node node, final KeywordReferences keywords)
	{
		if(keywords != null && notEmpty(keywords.getIds()))
		{
			report.appendSubSection("Keywords defined (Refer to the related Interest Group Keywords definition): ");
			for(final Integer id : keywords.getIds())
			{
				report.appendLine("Id: " + id);
			}
		}
	}

	public void visit(final Node node, final Notifications notifications)
	{
		if(notifications != null && notEmpty(notifications.getNotifications()))
		{
			report.appendSubSection("Notifications defined at this level: ");

			for(final NotificationItem item : notifications.getNotifications())
			{
				final boolean isProfile = item.getProfile() != null;

				report.appendLine(item.getStatus() + " setted to the " + (isProfile ? "Profile: " + item.getProfile() : "User: " + item.getUser()));
			}
		}
	}

	public void visit(final Node node, final TypedProperty property)
	{
		// already logged
	}

	public void visitLibrarytSection(final Meeting meeting, final String reference)
	{
		if(reference != null)
		{
			report.appendSubSection("The meeting refers to the space: " + reference);
		}
	}

	public void visitLinkTarget(final Link node, final String reference)
	{
		report.appendSubSection("The link refers to the node: " + reference);
	}

	public void visitSharedSpaceLinkTarget(SharedSpacelink node, String reference)
	{
		report.appendSubSection("The link refers to the shared space: " + reference);
	}

	public void visitLocation(final Node node,final  String uri)
	{
		report.appendSubSection("Content found at location: " + uri);
	}

	public void visit(Space node, Shared sharedProperties)
	{
		report.appendSubSection("Shared with interest group: " + sharedProperties.getIgReference() + " and permission: "+ sharedProperties.getPermission());
	}



	private boolean notEmpty(final Collection<? extends Object> collection)
	{
		return collection != null && collection.size() > 0;
	}

	/**
	 * @return the report
	 */
	public final Report getReport()
	{
		return report;
	}

	private final String i18nValuesAsString(final List<I18NProperty> values)
	{
		StringBuffer buff = new StringBuffer("");
		int count = 0;
		for(I18NProperty property: values)
		{
			if(count == 3)
			{
				buff.append(", ...");
				break;
			}
			if(count != 1)
			{
				buff.append(", ");
			}
			buff
				.append(property.getLang().getLanguage())
				.append(':')
				.append(property.getValue());

			++count;
		}


		return buff.toString();
	}

	public void setFailOnError(boolean failOnError)
	{
		// no relevant
	}
}
