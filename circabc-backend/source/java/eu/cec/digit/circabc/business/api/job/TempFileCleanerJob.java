/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.business.api.job;

import eu.cec.digit.circabc.business.helper.TemporaryFileManager;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.util.Assert;

/**
 * Job that check if new users are to be imported into circabc
 *
 * @author Yanick Pignot
 */
public class TempFileCleanerJob implements Job {

    private static Log logger = LogFactory.getLog(TempFileCleanerJob.class);

    private TemporaryFileManager temporaryFileManager;
    private TransactionService transactionService;

    public void execute(final JobExecutionContext context) throws JobExecutionException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Start job .... ");
            }

            final RetryingTransactionHelper helper = getTransactionService(context)
                    .getRetryingTransactionHelper();
            helper.doInTransaction(new RetryingTransactionCallback<Object>() {

                                       public Object execute() throws Throwable {

                                           try {
                                               AuthenticationUtil.setRunAsUserSystem();
                                               getTemporaryFileManager(context).removeTempFiles();
                                           } finally {
                                               AuthenticationUtil.clearCurrentSecurityContext();
                                           }
                                           return null;
                                       }
                                   }
                    , false, true);


        } catch (final Throwable e) {
            logger.error("Can not run job TempFileCleanerJob", e);
        }
    }


    private synchronized void initialize(JobExecutionContext context) {
        final JobDataMap jobData = context.getJobDetail().getJobDataMap();

        temporaryFileManager = (TemporaryFileManager) jobData.get("temporaryFileManager");
        transactionService = (TransactionService) jobData.get("transactionService");

        Assert.notNull(temporaryFileManager);
        Assert.notNull(transactionService);
    }

    private TemporaryFileManager getTemporaryFileManager(final JobExecutionContext context) {
        if (this.temporaryFileManager == null) {
            initialize(context);
        }

        return this.temporaryFileManager;
    }

    private TransactionService getTransactionService(final JobExecutionContext context) {
        if (this.transactionService == null) {
            initialize(context);
        }

        return this.transactionService;
    }
}
