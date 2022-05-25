/*--+
 |     Copyright European Community 2006 - Licensed under the EUPL V.1.0
 |
 |          http://ec.europa.eu/idabc/en/document/6523
 |
 +--*/
package eu.cec.digit.circabc.migration.archive.impl;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.lock.UnableToAquireLockException;
import org.alfresco.service.cmr.lock.UnableToReleaseLockException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.migration.archive.LockException;
import eu.cec.digit.circabc.migration.archive.LockHandler;

/**
 * Handle the lock / unlock process for a migration file.
 *
 * <b>It's important to prevent potential conflicts when migartion starts on several cluster nodes on a same time.</b>
 *
 * @author Yanick Pignot
 */
public class SpaceLockHandler implements LockHandler
{
	/* Expiration time of the lock */
	private int lockExpiration;
	/* The lock Service */
	private LockService lockService;
	/* The transaction Service */
	private TransactionService transactionService;

	private static final Log logger = LogFactory.getLog(SpaceLockHandler.class);

	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.migration.archive.LockHandler#lock(org.alfresco.service.cmr.repository.NodeRef)
	 */
	public void lock(final NodeRef nodeRef) throws LockException
	{
		final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
		final RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
		{
			public String execute() throws Throwable
			{
				if(logger.isDebugEnabled())
				{
					logger.debug("Start to lock the migration ... ");
				}

				final LockStatus lockStatus = lockService.getLockStatus(nodeRef);

				if(LockStatus.LOCKED.equals(lockStatus))
				{
					throw new LockException("A migration process is still running. If the problem persists, connect with the admin user and manually unlock node: " + nodeRef);
				}
				else
				{
					try
					{
						lockService.lock(nodeRef, LockType.READ_ONLY_LOCK, lockExpiration);
					}
					catch(UnableToAquireLockException ex)
					{
						throw new LockException(ex);
					}
				}
				return null;
			}
		};
		txnHelper.doInTransaction(callback, false, true);
	}
	/* (non-Javadoc)
	 * @see eu.cec.digit.circabc.migration.archive.LockHandler#unLock(org.alfresco.service.cmr.repository.NodeRef)
	 */
	public void unLock(final NodeRef nodeRef) throws LockException
	{
		final RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
		final RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
		{
			public String execute() throws Throwable
			{
				if(logger.isDebugEnabled())
				{
					logger.debug("Start to lock the migration ... ");
				}

				try
				{
					lockService.unlock(nodeRef);
				}
				catch(UnableToReleaseLockException ex)
				{
					throw new LockException(ex);
				}

				return null;
			}
		};
		txnHelper.doInTransaction(callback, false, true);
	}

	/**
	 * @param lockExpiration the lockExpiration to set
	 */
	public final void setLockExpiration(String lockExpiration)
	{
		this.lockExpiration = Integer.parseInt(lockExpiration);
	}

	/**
	 * @param lockService the lockService to set
	 */
	public final void setLockService(LockService lockService)
	{
		this.lockService = lockService;
	}
	/**
	 * @param transactionService the transactionService to set
	 */
	public final void setTransactionService(TransactionService transactionService)
	{
		this.transactionService = transactionService;
	}

}
