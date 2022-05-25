package eu.cec.digit.circabc.migration.archive;

import org.alfresco.service.cmr.repository.NodeRef;


public interface LockHandler
{

	/**
	 * Lock the migration process by locking a given node
	 *
	 * @param nodeRef					The noderef to lock
	 * @throws LockException
	 */
	public abstract void lock(NodeRef nodeRef) throws LockException;

	/**
	 * Unlock the migration process by unlocking a given node
	 *
	 * @param nodeRef					The noderef to unlock
	 * @throws LockException
	 */
	public abstract void unLock(NodeRef nodeRef) throws LockException;

}