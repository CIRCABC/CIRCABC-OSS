/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.service.app;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.service.customisation.mail.MailTemplate;
import eu.cec.digit.circabc.service.lock.LockService;
import eu.cec.digit.circabc.service.notification.NotificationService;
import io.swagger.api.AppMessageApi;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DistributionListSenderJob implements Job {

    private static final String SEND_DISTRIBUTION_LIST = "SEND_DISTRIBUTION_LIST";

    private static Log logger =
            LogFactory.getLog(eu.cec.digit.circabc.service.app.DistributionListSenderJob.class);

    private LockService lockService;
    private AppMessageApi appMessageApi;
    private NotificationService notificationService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        initialize(context);
        try {
            AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
            initialize(context);
            boolean isLocked = false;
            if (!lockService.isLocked(SEND_DISTRIBUTION_LIST)) {

                ByteArrayOutputStream bos = null;
                InputStream is = null;
                File tempFile = null;

                try {
                    lockService.lock(SEND_DISTRIBUTION_LIST);
                    isLocked = true;
                    Workbook wb = appMessageApi.getdistributionListAsExcel();

                    bos = new ByteArrayOutputStream();
                    wb.write(bos);
                    is = new ByteArrayInputStream(bos.toByteArray());
                    tempFile = TempFileProvider.createTempFile(is, "distrib-mail-", ".xlsx");
                    List<File> files = new ArrayList<>();
                    files.add(tempFile);
                    List<String> mails = new ArrayList<>();
                    mails.add(CircabcConfiguration.getProperty("mail.from.circabc.support"));
                    notificationService.notify(null, mails, MailTemplate.ADMIN_DISTRIBUTION_LIST, files);

                } catch (Exception e) {
                    logger.error("Exception when running the SEND_DISTRIBUTION_LIST job.", e);
                } finally {
                    if (isLocked) {
                        lockService.unlock(SEND_DISTRIBUTION_LIST);
                    }

                    if (bos != null) {
                        bos.close();
                    }

                    if (is != null) {
                        is.close();
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Can not run job ExpiredMembersJob", e);
        } finally {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }

    /**
     * @return the lockService
     */
    public LockService getLockService() {
        return lockService;
    }

    /**
     * @param lockService the lockService to set
     */
    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }

    private void initialize(final JobExecutionContext context) {
        setNotificationService(
                (NotificationService) context.getMergedJobDataMap().get("notificationService"));
        setAppMessageApi((AppMessageApi) context.getMergedJobDataMap().get("appMessageApi"));
        setLockService((LockService) context.getMergedJobDataMap().get("lockService"));
    }

    public AppMessageApi getAppMessageApi() {
        return appMessageApi;
    }

    public void setAppMessageApi(AppMessageApi appMessageApi) {
        this.appMessageApi = appMessageApi;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
