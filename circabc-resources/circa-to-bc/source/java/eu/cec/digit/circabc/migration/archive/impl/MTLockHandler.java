/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.migration.archive.impl;

import org.alfresco.service.cmr.repository.NodeRef;

import eu.cec.digit.circabc.migration.archive.LockException;
import eu.cec.digit.circabc.migration.archive.LockHandler;

/**
 * Allow all processe for multi threading purposes.
 *
 * @author Yanick Pignot
 */
public class MTLockHandler implements LockHandler
{

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.migration.archive.LockHandler#lock(org.alfresco.service.cmr.repository.NodeRef)
	 */
	public void lock(final NodeRef nodeRef) throws LockException
	{
		// allow multi threading
	}

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.migration.archive.LockHandler#unLock(org.alfresco.service.cmr.repository.NodeRef)
	 */
	public void unLock(final NodeRef nodeRef) throws LockException
	{
		// allow multi threading
	}
}
