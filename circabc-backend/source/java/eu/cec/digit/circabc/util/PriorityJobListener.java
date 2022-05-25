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
package eu.cec.digit.circabc.util;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

/**
 * Listener used to change the priority of the worker thread that executes the listened job.
 *
 * @author schwerr
 */
public class PriorityJobListener implements JobListener {

    private String name = null;
    private int priority = Thread.NORM_PRIORITY;
    private int originalPriority = Thread.NORM_PRIORITY;

    public PriorityJobListener(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    /**
     * @see org.quartz.JobListener#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @see org.quartz.JobListener#jobExecutionVetoed(org.quartz.JobExecutionContext)
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
    }

    /**
     * @see org.quartz.JobListener#jobToBeExecuted(org.quartz.JobExecutionContext)
     */
    @Override
    public synchronized void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        originalPriority = Thread.currentThread().getPriority();
        Thread.currentThread().setPriority(priority);
    }

    /**
     * @see org.quartz.JobListener#jobWasExecuted(org.quartz.JobExecutionContext,
     * org.quartz.JobExecutionException)
     */
    @Override
    public synchronized void jobWasExecuted(JobExecutionContext jobExecutionContext,
                                            JobExecutionException jobExecutionException) {
        Thread.currentThread().setPriority(originalPriority);
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }
}
