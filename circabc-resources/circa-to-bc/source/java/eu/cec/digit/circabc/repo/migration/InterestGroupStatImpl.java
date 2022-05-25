/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.repo.migration;

import java.io.Serializable;

import eu.cec.digit.circabc.service.migration.InterestGroupStat;

/**
 * Concrete implementation of statistics of a single interest group
 *
 * @author Yanick Pignot
 */
public class InterestGroupStatImpl implements InterestGroupStat, Serializable
{
	/** */
	private static final long serialVersionUID = 8499389416849562885L;

	private final String igName;

	private long igTotalDocSize = 0;
	private long igMinDocSize = -1;
	private long igMaxDocSize = 0;

	private long igTotalSpaceSize = 0;
	private int igMinSpaceSize = -1;
	private int igMaxSpaceSize = 0;

	private long igTotalSpaceDeep = 0;
	private int igMinSpaceDeep = -1;
	private int igMaxSpaceDeep = 0;

	private int igUsers = 0;
	private int igProfiles = 0;
	private int igImportedProfiles = 0;
	private int igExportedProfiles = 0;
	private Boolean guestVisible = false;
	private Boolean registredVisible = false;

	private int igImportedSpaces = 0;
	private int igExportedSpaces = 0;

	private int librarySpaces = 0;
	private int libraryDocs = 0;
	private int libraryUrls = 0;
	private int libraryDossiers = 0;

	private int libraryLinks = 0;
	private int libraryMLContents = 0;
	private int libraryOldVersions = 0;
	private int libraryDiscussions = 0;
	private int libraryDiscussionTopics = 0;
	private int libraryDiscussionMessages = 0;
	private int libraryDiscussionAttachements = 0;

	private int informationSpaces = 0;
	private int informationDocs = 0;
	private int informationLinks = 0;
	private int informationMLContents = 0;
	private int informationOldVersions = 0;

	private int newsgroupModeratedForums = 0;
	private int newsgroupForums = 0;
	private int newsgroupTopics = 0;
	private int newsgroupMessages = 0;
	private int newsgroupMessagesAttachements = 0;

	private int calendarEvents = 0;
	private int calendarMeetings = 0;

	/*package*/ InterestGroupStatImpl(final String igName)
	{
		super();
		this. igName = igName;
	}


	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getIgExportedSpace()
	 */
	public final int getIgExportedSpaces()
	{
		return igExportedSpaces;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getIgImportedProfiles()
	 */
	public final int getIgImportedProfiles()
	{
		return igImportedProfiles;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getIgImportedSpace()
	 */
	public final int getIgImportedSpaces()
	{
		return igImportedSpaces;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getIgProfiles()
	 */
	public final int getIgProfiles()
	{
		return igProfiles;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getIgUsers()
	 */
	public final int getIgUsers()
	{
		return igUsers;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getInformationDocs()
	 */
	public final int getInformationDocs()
	{
		return informationDocs;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getInformationSpaces()
	 */
	public final int getInformationSpaces()
	{
		return informationSpaces;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getLibraryDocs()
	 */
	public final int getLibraryDocs()
	{
		return libraryDocs;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getLibraryDossiers()
	 */
	public final int getLibraryDossiers()
	{
		return libraryDossiers;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getLibrarySpaces()
	 */
	public final int getLibrarySpaces()
	{
		return librarySpaces;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getLibraryUrls()
	 */
	public final int getLibraryUrls()
	{
		return libraryUrls;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getNewsgroupForums()
	 */
	public final int getNewsgroupForums()
	{
		return newsgroupForums;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getNewsgroupMessages()
	 */
	public final int getNewsgroupMessages()
	{
		return newsgroupMessages;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getNewsgroupModeratedForums()
	 */
	public final int getNewsgroupModeratedForums()
	{
		return newsgroupModeratedForums;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getNewsgroupTopics()
	 */
	public final int getNewsgroupTopics()
	{
		return newsgroupTopics;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getInterestGroup()
	 */
	public final String getInterestGroupName()
	{
		return igName;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#isGuestVisible()
	 */
	public final boolean isGuestVisible()
	{
		return guestVisible;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getIgExportedProfiles()
	 */
	public final int getIgExportedProfiles()
	{
		return igExportedProfiles;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#isRegistredVisible()
	 */
	public final boolean isRegistredVisible()
	{
		return registredVisible;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getLibraryDiscussionTopics()
	 */
	public final int getLibraryDiscussionTopics()
	{
		return libraryDiscussionTopics;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getLibraryDiscussions()
	 */
	public final int getLibraryDiscussions()
	{
		return libraryDiscussions;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getLibraryDiscussionMessages()
	 */
	public final int getLibraryDiscussionMessages()
	{
		return libraryDiscussionMessages;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getInformationLinks()
	 */
	public final int getInformationLinks()
	{
		return informationLinks;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getInformationMLContents()
	 */
	public final int getInformationMLContents()
	{
		return informationMLContents;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getLibraryLinks()
	 */
	public final int getLibraryLinks()
	{
		return libraryLinks;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getLibraryMLContents()
	 */
	public final int getLibraryMLContents()
	{
		return libraryMLContents;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getInformationOldVersions()
	 */
	public final int getInformationOldVersions()
	{
		return informationOldVersions;
	}


	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getLibraryOldVersions()
	 */
	public final int getLibraryOldVersions()
	{
		return libraryOldVersions;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getLibraryDiscussionAttachements()
	 */
	public final int getLibraryDiscussionAttachements()
	{
		return libraryDiscussionAttachements;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getNewsgroupMessagesAttachements()
	 */
	public final int getNewsgroupMessagesAttachements()
	{
		return newsgroupMessagesAttachements;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getCalendarEvents()
	 */
	public final int getCalendarEvents()
	{
		return calendarEvents;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.repo.migration.InterestGroupStat#getCalendarMeetings()
	 */
	public final int getCalendarMeetings()
	{
		return calendarMeetings;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.service.migration.InterestGroupStat#getIgMaxDocSize()
	 */
	public final long getIgMaxDocSize()
	{
		return igMaxDocSize;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.service.migration.InterestGroupStat#getIgMaxSpaceDeep()
	 */
	public final int getIgMaxSpaceDeep()
	{
		return igMaxSpaceDeep;
	}


	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.service.migration.InterestGroupStat#getIgMaxSpaceSize()
	 */
	public final int getIgMaxSpaceSize()
	{
		return igMaxSpaceSize;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.service.migration.InterestGroupStat#getIgMinDocSize()
	 */
	public final long getIgMinDocSize()
	{
		return igMinDocSize;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.service.migration.InterestGroupStat#getIgMinSpaceDeep()
	 */
	public final int getIgMinSpaceDeep()
	{
		return igMinSpaceDeep;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.service.migration.InterestGroupStat#getIgMinSpaceSize()
	 */
	public final int getIgMinSpaceSize()
	{
		return igMinSpaceSize;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.service.migration.InterestGroupStat#getIgName()
	 */
	public final String getIgName()
	{
		return igName;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.service.migration.InterestGroupStat#getIgTotalDocSize()
	 */
	public final long getIgTotalDocSize()
	{
		return igTotalDocSize;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.service.migration.InterestGroupStat#getIgAverageSpaceDeep()
	 */
	public final double getIgAverageSpaceDeep()
	{
		final int totalContainer = getLibrarySpaces() + getLibraryDossiers() + getInformationSpaces();
		return totalContainer < 1 ? 0 : (double) igTotalSpaceDeep / (double) totalContainer;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.service.migration.InterestGroupStat#getIgAverageSpaceSize()
	 */
	public final double getIgAverageSpaceSize()
	{
		final int totalContainer = getLibrarySpaces() + getLibraryDossiers() + getInformationSpaces();
		return totalContainer < 1 ? 0 : (double) igTotalSpaceSize / (double) totalContainer;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.service.migration.InterestGroupStat#getIgAverageDocSize()
	 */
	public final double getIgAverageDocSize()
	{
		final int totalDoc = getLibraryDocs() + getInformationDocs();
		return totalDoc < 1 ? 0 : (double) getIgTotalDocSize() / (double) totalDoc;
	}


	/**
	 * @param igExportedSpace the igExportedSpace to set
	 */
	/*package*/ final void setIgExportedSpaces(int igExportedSpace)
	{
		this.igExportedSpaces = igExportedSpace;
	}

	/**
	 * @param igImportedProfiles the igImportedProfiles to set
	 */
	/*package*/ final void setIgImportedProfiles(int igImportedProfiles)
	{
		this.igImportedProfiles = igImportedProfiles;
	}

	/**
	 * @param igImportedSpace the igImportedSpace to set
	 */
	/*package*/ final void setIgImportedSpaces(int igImportedSpace)
	{
		this.igImportedSpaces = igImportedSpace;
	}

	/**
	 * @param igProfiles the igProfiles to set
	 */
	/*package*/ final void setIgProfiles(int igProfiles)
	{
		this.igProfiles = igProfiles;
	}

	/**
	 * @param igUsers the igUsers to set
	 */
	/*package*/ final void setIgUsers(int igUsers)
	{
		this.igUsers = igUsers;
	}

	/**
	 * @param informationDocs the informationDocs to set
	 */
	/*package*/ final void setInformationDocs(int informationDocs)
	{
		this.informationDocs = informationDocs;
	}

	/**
	 * @param informationSpaces the informationSpaces to set
	 */
	/*package*/ final void setInformationSpaces(int informationSpaces)
	{
		this.informationSpaces = informationSpaces;
	}

	/**
	 * @param libraryDocs the libraryDocs to set
	 */
	/*package*/ final void setLibraryDocs(int libraryDocs)
	{
		this.libraryDocs = libraryDocs;
	}

	/**
	 * @param libraryDossiers the libraryDossiers to set
	 */
	/*package*/ final void setLibraryDossiers(int libraryDossiers)
	{
		this.libraryDossiers = libraryDossiers;
	}

	/**
	 * @param librarySpaces the librarySpaces to set
	 */
	/*package*/ final void setLibrarySpaces(int librarySpaces)
	{
		this.librarySpaces = librarySpaces;
	}

	/**
	 * @param libraryUrls the libraryUrls to set
	 */
	/*package*/ final void setLibraryUrls(int libraryUrls)
	{
		this.libraryUrls = libraryUrls;
	}

	/**
	 * @param newsgroupForums the newsgroupForums to set
	 */
	/*package*/ final void setNewsgroupForums(int newsgroupForums)
	{
		this.newsgroupForums = newsgroupForums;
	}

	/**
	 * @param newsgroupMessages the newsgroupMessages to set
	 */
	/*package*/ final void setNewsgroupMessages(int newsgroupMessages)
	{
		this.newsgroupMessages = newsgroupMessages;
	}

	/**
	 * @param newsgroupModeratedForums the newsgroupModeratedForums to set
	 */
	/*package*/ final void setNewsgroupModeratedForums(int newsgroupModeratedForums)
	{
		this.newsgroupModeratedForums = newsgroupModeratedForums;
	}

	/**
	 * @param newsgroupTopics the newsgroupTopics to set
	 */
	/*package*/ final void setNewsgroupTopics(int newsgroupTopics)
	{
		this.newsgroupTopics = newsgroupTopics;
	}

	/**
	 * @param guestVisible the guestVisible to set
	 */
	/*package*/  final void setGuestVisible(boolean guestVisible)
	{
		this.guestVisible = guestVisible;
	}

	/**
	 * @param igExportedProfiles the igExportedProfiles to set
	 */
	/*package*/  final void setIgExportedProfiles(int igExportedProfiles)
	{
		this.igExportedProfiles = igExportedProfiles;
	}


	/**
	 * @param registredVisible the registredVisible to set
	 */
	/*package*/  final void setRegistredVisible(boolean registredVisible)
	{
		this.registredVisible = registredVisible;
	}

	/**
	 * @param libraryDiscussionMessages the libraryDiscussionMessages to set
	 */
	/*package*/   final void setLibraryDiscussionMessages(int libraryDiscussionMessages)
	{
		this.libraryDiscussionMessages = libraryDiscussionMessages;
	}

	/**
	 * @param libraryDiscussions the libraryDiscussions to set
	 */
	/*package*/   final void setLibraryDiscussions(int libraryDiscussions)
	{
		this.libraryDiscussions = libraryDiscussions;
	}

	/**
	 * @param libraryDiscussionTopics the libraryDiscussionTopics to set
	 */
	/*package*/  final void setLibraryDiscussionTopics(int libraryDiscussionTopics)
	{
		this.libraryDiscussionTopics = libraryDiscussionTopics;
	}

	/**
	 * @param informationLinks the informationLinks to set
	 */
	/*package*/ final void setInformationLinks(int informationLinks)
	{
		this.informationLinks = informationLinks;
	}

	/**
	 * @param informationMLContents the informationMLContents to set
	 */
	/*package*/ final void setInformationMLContents(int informationMLContents)
	{
		this.informationMLContents = informationMLContents;
	}

	/**
	 * @param libraryLinks the libraryLinks to set
	 */
	/*package*/ final void setLibraryLinks(int libraryLinks)
	{
		this.libraryLinks = libraryLinks;
	}

	/**
	 * @param libraryMLContent the libraryMLContent to set
	 */
	/*package*/ final void setLibraryMLContents(int libraryMLContent)
	{
		this.libraryMLContents = libraryMLContent;
	}

	/**
	 * @param libraryOldVersions the libraryOldVersions to set
	 */
	/*package*/ final void setLibraryOldVersions(int libraryOldVersions)
	{
		this.libraryOldVersions = libraryOldVersions;
	}

	/**
	 * @param informationOldVersions the informationOldVersions to set
	 */
	/*package*/ final void setInformationOldVersions(int informationOldVersions)
	{
		this.informationOldVersions = informationOldVersions;
	}

	/**
	 * @param libraryDiscussionAttach the libraryDiscussionAttach to set
	 */
	/*package*/ final void setLibraryDiscussionAttachements(int libraryDiscussionAttach)
	{
		this.libraryDiscussionAttachements = libraryDiscussionAttach;
	}

	/**
	 * @param newsgroupMessagesAttach the newsgroupMessagesAttach to set
	 */
	/*package*/ final void setNewsgroupMessagesAttachements(int newsgroupMessagesAttach)
	{
		this.newsgroupMessagesAttachements = newsgroupMessagesAttach;
	}

	/**
	 * @param calendarEvents the calendarEvents to set
	 */
	/*package*/ final void setCalendarEvents(int calendarEvents)
	{
		this.calendarEvents = calendarEvents;
	}

	/**
	 * @param calendarMeetings the calendarMeetings to set
	 */
	/*package*/ final void setCalendarMeetings(int calendarMeetings)
	{
		this.calendarMeetings = calendarMeetings;
	}

	/**
	 * @param igExportedSpace the igExportedSpace to set
	 */
	/*package*/ final void addIgExportedSpace()
	{
		++ this.igExportedSpaces;
	}

	/**
	 * @param igImportedProfiles the igImportedProfiles to set
	 */
	/*package*/ final void addIgImportedProfile()
	{
		++ this.igImportedProfiles;
	}

	/**
	 * @param igImportedSpace the igImportedSpace to set
	 */
	/*package*/ final void addIgImportedSpace()
	{
		++ this.igImportedSpaces;
	}

	/**
	 * @param igProfiles the igProfiles to set
	 */
	/*package*/ final void addIgProfile()
	{
		++ this.igProfiles;
	}

	/**
	 * @param igUsers the igUsers to set
	 */
	/*package*/ final void addIgUser()
	{
		++ this.igUsers;
	}

	/**
	 * @param informationDocs the informationDocs to set
	 */
	/*package*/ final void addInformationDoc()
	{
		++ this.informationDocs;
	}

	/**
	 * @param
	 */
	/*package*/ final void addDocumentSize(final long size)
	{
		if(size > 0)
		{
			this.igTotalDocSize += size;

			if(igMinDocSize == -1 || size < igMinDocSize)
			{
				igMinDocSize = size;
			}
			if(size > igMaxDocSize)
			{
				igMaxDocSize = size;
			}
		}
	}

	/**
	 * @param informationSpaces the informationSpaces to set
	 */
	/*package*/ final void addInformationSpace()
	{
		++ this.informationSpaces ;
	}

	/**
	 * @param libraryDocs the libraryDocs to set
	 */
	/*package*/ final void addLibraryDoc()
	{
		++ this.libraryDocs;
	}

	/**
	 * @param libraryDossiers the libraryDossiers to set
	 */
	/*package*/ final void addLibraryDossier()
	{
		++ this.libraryDossiers ;
	}

	/**
	 * @param librarySpaces the librarySpaces to set
	 */
	/*package*/ final void addLibrarySpace()
	{
		++ this.librarySpaces;
	}

	/**
	 * @param libraryUrls the libraryUrls to set
	 */
	/*package*/ final void addLibraryUrl()
	{
		++ this.libraryUrls;
	}

	/**
	 * @param newsgroupForums the newsgroupForums to set
	 */
	/*package*/ final void addNewsgroupForum()
	{
		++ this.newsgroupForums;
	}

	/**
	 * @param newsgroupMessages the newsgroupMessages to set
	 */
	/*package*/ final void addNewsgroupMessage()
	{
		++ this.newsgroupMessages;
	}

	/**
	 * @param newsgroupModeratedForums the newsgroupModeratedForums to set
	 */
	/*package*/ final void addNewsgroupModeratedForum()
	{
		++ this.newsgroupModeratedForums;
	}

	/**
	 * @param newsgroupTopics the newsgroupTopics to set
	 */
	/*package*/ final void addNewsgroupTopic()
	{
		++ this.newsgroupTopics;
	}

	/**
	 * @param igExportedProfiles the igExportedProfiles to set
	 */
	/*package*/  final void addIgExportedProfile()
	{
		++ this.igExportedProfiles;
	}

	/**
	 * @param libraryDiscussionMessages the libraryDiscussionMessages to set
	 */
	/*package*/   final void addLibraryDiscussionMessage()
	{
		++ this.libraryDiscussionMessages;
	}

	/**
	 * @param libraryDiscussions the libraryDiscussions to set
	 */
	/*package*/   final void addLibraryDiscussion()
	{
		++ this.libraryDiscussions;
	}

	/**
	 * @param libraryDiscussionTopics the libraryDiscussionTopics to set
	 */
	/*package*/ final void addLibraryDiscussionTopic()
	{
		++ this.libraryDiscussionTopics;
	}

	/**
	 * @param informationLinks the informationLinks to set
	 */
	/*package*/ final void addInformationLink()
	{
		++ this.informationLinks ;
	}

	/**
	 * @param informationMLContents the informationMLContents to set
	 */
	/*package*/ final void addInformationMLContent()
	{
		++ this.informationMLContents;
	}

	/**
	 * @param libraryLinks the libraryLinks to set
	 */
	/*package*/ final void addLibraryLink()
	{
		++ this.libraryLinks;
	}

	/**
	 * @param libraryMLContent the libraryMLContent to set
	 */
	/*package*/ final void addLibraryMLContent()
	{
		++ this.libraryMLContents;
	}

	/**
	 * @param libraryOldVersions the libraryOldVersions to set
	 */
	/*package*/ final void addLibraryOldVersion()
	{
		++ this.libraryOldVersions;
	}

	/**
	 * @param informationOldVersions the informationOldVersions to set
	 */
	/*package*/ final void addInformationOldVersion()
	{
		++ this.informationOldVersions;
	}

	/**
	 * @param libraryDiscussionAttach the libraryDiscussionAttach to set
	 */
	/*package*/ final void addLibraryDiscussionAttachement()
	{
		++ libraryDiscussionAttachements;
	}

	/**
	 * @param newsgroupMessagesAttach the newsgroupMessagesAttach to set
	 */
	/*package*/ final void addNewsgroupMessagesAttachement()
	{
		++ newsgroupMessagesAttachements;
	}

	/**
	 * @param calendarEvents the calendarEvents to set
	 */
	/*package*/  final void addCalendarEvent()
	{
		++ this.calendarEvents;
	}

	/**
	 * @param calendarMeetings the calendarMeetings to set
	 */
	/*package*/  final void addCalendarMeeting()
	{
		++ this.calendarMeetings;
	}

	/**
	 * @param libraryDeepestSpace the libraryDeepestSpace to set
	 */
	/*package*/ final void addSpaceDeep(int deep)
	{
		if(deep > 0)
		{
			igTotalSpaceDeep += deep;

			if(igMinSpaceDeep == -1 || deep < igMinSpaceDeep)
			{
				igMinSpaceDeep = deep;
			}
			if(deep > igMaxSpaceDeep)
			{
				igMaxSpaceDeep = deep;
			}
		}
	}

	/**
	 * @param libraryWidestSpace the libraryWidestSpace to set
	 */
	/*package*/ final void addSpaceSize(int spaceSize)
	{
		if(spaceSize > 0)
		{
			igTotalSpaceSize += spaceSize;

			if(igMinSpaceSize == -1 || spaceSize < igMinSpaceSize)
			{
				igMinSpaceSize = spaceSize;
			}
			if(spaceSize > igMaxSpaceSize)
			{
				igMaxSpaceSize = spaceSize;
			}
		}
	}

}