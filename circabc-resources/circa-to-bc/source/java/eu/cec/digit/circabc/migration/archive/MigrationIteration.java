/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.archive;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Contains all data to identify a migration iteration.
 *
 * An iteration is composed of a serie of transformation and import processes.
 *
 *
 * @author Yanick Pignot
 */
public interface MigrationIteration extends Serializable
{

	/**
	 * @return			The unique identifier of the iteration
	 */
	public abstract String getIdentifier();

	/**
	 * @return			The noderef of the starting point of the iteration
	 */
	public abstract NodeRef getOriginalFileNodeRef();

	/**
	 * @return			The noderef of the ig log files
	 */
	public abstract NodeRef getOriginalIgLogsFileNodeRef();

	/**
	 * @return			The description of the iteration
	 */
	public abstract String getDescription();

	/**
	 * @return			The start date of the iteration
	 */
	public abstract Date getIterationStartDate();

	/**
	 * @return			The creator of the iteration
	 */
	public abstract String getCreator();

	/**
	 * @return			All transormation iteration process dates
	 */
	public abstract Map<NodeRef, Date> getTransformationDates();

	/**
	 * @return			All importation iteration process dates
	 */
	public abstract Map<NodeRef, Date> getImportedDates();

	/**
	 * @return			All importation iteration process dates
	 */
	public abstract Map<NodeRef, Date> getFailedImportation();


	/**
	 * @return			The space wher all iteration files is stored.
	 */
	public NodeRef getIterationRootSpace();

}
