/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.service.migration;

import java.io.Serializable;


/**
 * Base interface of the statistics of a single interesgroup
 *
 * @author Yanick Pignot
 */
public interface InterestGroupStat extends Serializable
{

	/**
	 * @return the calendarEvents
	 */
	public abstract int getCalendarEvents();

	/**
	 * @return the calendarMeetings
	 */
	public abstract int getCalendarMeetings();

	/**
	 * @return the igAverageDocSize
	 */
	public abstract double getIgAverageDocSize();

	/**
	 * @return the igAverageSpaceDeep
	 */
	public abstract double getIgAverageSpaceDeep();

	/**
	 * @return the igAverageSpaceSize
	 */
	public abstract double getIgAverageSpaceSize();

	/**
	 * @return the igExportedProfiles
	 */
	public abstract int getIgExportedProfiles();

	/**
	 * @return the igExportedSpace
	 */
	public abstract int getIgExportedSpaces();

	/**
	 * @return the igImportedProfiles
	 */
	public abstract int getIgImportedProfiles();

	/**
	 * @return the igImportedSpace
	 */
	public abstract int getIgImportedSpaces();

	/**
	 * @return the igMaxDocSize
	 */
	public abstract long getIgMaxDocSize();

	/**
	 * @return the igMaxSpaceDeep
	 */
	public abstract int getIgMaxSpaceDeep();

	/**
	 * @return the igMaxSpaceSize
	 */
	public abstract int getIgMaxSpaceSize();

	/**
	 * @return the igMinDocSize
	 */
	public abstract long getIgMinDocSize();

	/**
	 * @return the igMinSpaceDeep
	 */
	public abstract int getIgMinSpaceDeep();

	/**
	 * @return the igMinSpaceSize
	 */
	public abstract int getIgMinSpaceSize();

	/**
	 * @return the igName
	 */
	public abstract String getIgName();

	/**
	 * @return the igProfiles
	 */
	public abstract int getIgProfiles();

	/**
	 * @return the igTotalDocSize
	 */
	public abstract long getIgTotalDocSize();

	/**
	 * @return the igUsers
	 */
	public abstract int getIgUsers();

	/**
	 * @return the informationDocs
	 */
	public abstract int getInformationDocs();

	/**
	 * @return the informationLinks
	 */
	public abstract int getInformationLinks();

	/**
	 * @return the informationMLContents
	 */
	public abstract int getInformationMLContents();

	/**
	 * @return the informationOldVersions
	 */
	public abstract int getInformationOldVersions();

	/**
	 * @return the informationSpaces
	 */
	public abstract int getInformationSpaces();

	/**
	 * @return the pair
	 */
	public abstract String getInterestGroupName();

	/**
	 * @return the libraryDiscussionAttach
	 */
	public abstract int getLibraryDiscussionAttachements();

	/**
	 * @return the libraryDiscussionMessages
	 */
	public abstract int getLibraryDiscussionMessages();

	/**
	 * @return the libraryDiscussions
	 */
	public abstract int getLibraryDiscussions();

	/**
	 * @return the libraryDiscussionTopics
	 */
	public abstract int getLibraryDiscussionTopics();

	/**
	 * @return the libraryDocs
	 */
	public abstract int getLibraryDocs();

	/**
	 * @return the libraryDossiers
	 */
	public abstract int getLibraryDossiers();

	/**
	 * @return the libraryLinks
	 */
	public abstract int getLibraryLinks();

	/**
	 * @return the libraryMLContent
	 */
	public abstract int getLibraryMLContents();

	/**
	 * @return the libraryOldVersions
	 */
	public abstract int getLibraryOldVersions();

	/**
	 * @return the librarySpaces
	 */
	public abstract int getLibrarySpaces();

	/**
	 * @return the libraryUrls
	 */
	public abstract int getLibraryUrls();

	/**
	 * @return the newsgroupForums
	 */
	public abstract int getNewsgroupForums();

	/**
	 * @return the newsgroupMessages
	 */
	public abstract int getNewsgroupMessages();

	/**
	 * @return the newsgroupMessagesAttach
	 */
	public abstract int getNewsgroupMessagesAttachements();

	/**
	 * @return the newsgroupModeratedForums
	 */
	public abstract int getNewsgroupModeratedForums();

	/**
	 * @return the newsgroupTopics
	 */
	public abstract int getNewsgroupTopics();

	/**
	 * @return the guestVisible
	 */
	public abstract boolean isGuestVisible();

	/**
	 * @return the registredVisible
	 */
	public abstract boolean isRegistredVisible();
}