/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/

package eu.cec.digit.circabc.migration.archive.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

import eu.cec.digit.circabc.migration.archive.MigrationIteration;

/**
 * Concrete implementation of a migration iteration
 *
 * @author Yanick Pignot
 */
final class MigrationIterationImpl implements MigrationIteration
{
	/** */
	private static final long serialVersionUID = -6488354669342919436L;
	private final String identifier;
	private final String description;
	private final String creator;
	private final Date date;
	private NodeRef startFileRef;
	private NodeRef originalIgLogsFileNodeRef;
	private Map<NodeRef, Date> importedDates;
	private Map<NodeRef, Date> transformationDates;
	private Map<NodeRef, Date> failedImportation;
	public NodeRef iterationRootSpace;

	/**
	 * @param identifier
	 * @param description
	 * @param date
	 */
	/*package*/ MigrationIterationImpl(final String identifier, final String description, final Date date, final String creator)
	{
		super();
		this.identifier = identifier;
		this.description = description;
		this.date = date;
		this.creator = creator;
	}


	/*package*/ void withFailedImportation(final NodeRef node, final Date date)
	{
		getFailedImportation().put(node, date);
	}


	/*package*/ void withImportedDate(final NodeRef node, final Date date)
	{
		getImportedDates().put(node, date);
	}

	/*package*/ void withTransformationDate(final NodeRef node, final Date date)
	{
		getTransformationDates().put(node, date);
	}


	/*package*/ void  setOriginalFileNodeRef(final NodeRef ref)
	{
		startFileRef = ref;
	}

	/*package*/ final void setIterationRootSpace(NodeRef iterationRootSpace)
	{
		this.iterationRootSpace = iterationRootSpace;
	}

	/*package*/ final void setOriginalIgLogsFileNodeRef(NodeRef originalIgLogsFileNodeRef)
	{
		this.originalIgLogsFileNodeRef = originalIgLogsFileNodeRef;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.migration.archive.MigrationIteration#getDescription()
	 */
	public String getDescription()
	{
		return description;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.migration.archive.MigrationIteration#getIdentifier()
	 */
	public String getIdentifier()
	{
		return identifier;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.migration.archive.MigrationIteration#getIterationRootSpace()
	 */
	public final NodeRef getIterationRootSpace()
	{
		return iterationRootSpace;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.migration.archive.MigrationIteration#getOriginalFileNodeRef()
	 */
	public NodeRef getOriginalFileNodeRef()
	{
		return startFileRef;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.migration.archive.MigrationIteration#getOriginalIgLogsFileNodeRef()
	 */
	public final NodeRef getOriginalIgLogsFileNodeRef()
	{
		return originalIgLogsFileNodeRef;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.migration.archive.MigrationIteration#getIterationStartDate()
	 */
	public Date getIterationStartDate()
	{
		return date;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.migration.archive.MigrationIteration#getCreator()
	 */
	public String getCreator()
	{
		return creator;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.migration.archive.MigrationIteration#getImportedDates()
	 */
	public Map<NodeRef, Date> getImportedDates()
	{
		if(importedDates == null)
		{
			importedDates = new HashMap<NodeRef, Date>();
		}
		return importedDates;
	}

	public Map<NodeRef, Date> getFailedImportation()
	{
		if(failedImportation == null)
		{
			failedImportation = new HashMap<NodeRef, Date>();
		}
		return failedImportation;
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.migration.archive.MigrationIteration#getTransformationDates()
	 */
	public Map<NodeRef, Date> getTransformationDates()
	{
		if(transformationDates == null)
		{
			transformationDates = new HashMap<NodeRef, Date>();
		}
		return transformationDates;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "MigrationIteration(" + date + "|" + identifier + "|" + description + "|tranformed " + getTransformationDates().size() + " times|imported " + getImportedDates().size() + "times)" ;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((date == null) ? 0 : date.hashCode());
		result = PRIME * result + ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MigrationIterationImpl other = (MigrationIterationImpl) obj;
		if (date == null)
		{
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (identifier == null)
		{
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		return true;
	}

}
