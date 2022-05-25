/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.reader;

import java.util.Set;

import eu.cec.digit.circabc.migration.entities.XMLNode;
import eu.cec.digit.circabc.migration.entities.generated.ImportRoot;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Space;
import eu.cec.digit.circabc.service.migration.CategoryInterestGroupPair;
import eu.cec.digit.circabc.service.migration.ExportationException;

/**
 * Base interface for security settings reader
 *
 * @author Yanick Pignot
 */
public interface SecurityReader
{

	/**
	 * Set profile definition for a given Interest Group
	 *
	 * @param interestGroup
	 */
	public abstract void setProfileDefinition(final InterestGroup interestGroup) throws ExportationException;

	/**
	 * Set the application for membership defined fot thr given interest group
	 *
	 * @param root							Root must be used to add persons in the list.
	 * @param igRoot
	 * @throws ExportationException
	 */
	public abstract void setApplicants(final ImportRoot root, final InterestGroup igRoot) throws ExportationException;

	/**
	 * Set shared definition for a given space
	 *
	 * @param space
	 */
	public abstract void setSharedDefinition(final Space space) throws ExportationException;

	/**
	 * Set permissions for a given node
	 *
	 * @param node
	 */
	public abstract void setPermission(final XMLNode node) throws ExportationException;

	/**
	 * Set notifcation statuses for a given node
	 *
	 * @param node
	 */
	public abstract void setNotification(final XMLNode node) throws ExportationException;

	/**
	 * get all ig that define a shared space used in a given interest group
	 *
	 * @param pair
	 * @return
	 */
	public abstract Set<CategoryInterestGroupPair> getAllSharedLinkTarget(final CategoryInterestGroupPair pair) throws ExportationException;

	/**
	 * get all ig that define a profile used in a given interest group
	 *
	 * @param pair
	 * @return
	 */
	public abstract Set<CategoryInterestGroupPair> getAllImportedProfileTarget(final CategoryInterestGroupPair pair) throws ExportationException;


}