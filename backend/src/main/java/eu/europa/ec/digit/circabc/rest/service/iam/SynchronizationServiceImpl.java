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
package eu.europa.ec.digit.circabc.rest.service.iam;

import eu.europa.ec.digit.circabc.rest.service.mail.MailService;
import io.swagger.model.alfresco.CircabcModel;
import jakarta.mail.MessagingException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;

/**
 * Default implementation of {@link SynchronizationService} that propagates CIRCABC membership
 * changes to the external IAM (Identity and Access Management) system.
 *
 * <p>Role grant/revoke operations are dispatched asynchronously on a dedicated
 * {@link TaskExecutor} and executed through the {@link IamWSClient} web-service client. Each call is
 * wrapped in a bounded retry loop; if all attempts fail, a notification email is sent to a
 * configured administrator address so the operation can be completed manually. The mapping between
 * an interest group and its eCORDA theme identifiers is resolved through the eCORDA DAO.
 *
 * @author Slobodan Filipovic
 */
public class SynchronizationServiceImpl implements SynchronizationService {

  /** {@link String#format} template for the subject of the failure notification email. */
  private static final String SUBJECT_MAIL_TEMPLATE =
    "Did not succeed to synchronize CIRCABC with IAM operation : %1$s ,userID : %2$s , themaID : %3$s , roleID: %4$s";

  /** {@link String#format} template for the body of the failure notification email. */
  private static final String BODY_MAIL_TEMPLATE =
    "Did not succeed to synchronize CIRCABC with IAM operation : %1$s ,userID : %2$s , themaID : %3$s , roleID:  %4$s . %n Please do it manualy. %n Exception details : %5$s ";

  private static final Log logger = LogFactory.getLog(
    SynchronizationServiceImpl.class
  );

  /** Maximum number of attempts made for a single IAM synchronization operation before giving up. */
  private static final int MAX_RETRIES = 5;

  /** Pause, in milliseconds, between two consecutive retry attempts. */
  private static final int RETRY_PAUSE_IN_MILISECONDS = 120000;

  @Autowired
  private NodeService nodeService;

  @Qualifier("circabcMailService")
  @Autowired
  private MailService mailService;

  /** Executor used to run IAM synchronization operations asynchronously off the request thread. */
  @Autowired
  @Qualifier("synchronizationTaskExecutor")
  private TaskExecutor taskExecutor;

  /** Web-service client used to perform the actual grant/revoke calls against the IAM system. */
  @Autowired
  private IamWSClient iamWSClient;

  /** DAO providing the mapping between CIRCABC interest groups and their eCORDA theme identifiers. */
  @Autowired
  private EcordaDaoServiceImpl ecordaDaoServiceImpl;

  /** Recipient address for failure notification emails, injected from the {@code iam.error.email} property. */
  @Value("${iam.error.email}")
  private String emailErrorAdress;

  /**
   * {@inheritDoc}
   *
   * <p>The grant is submitted to the synchronization {@link TaskExecutor} and performed
   * asynchronously with retry handling.
   */
  @Override
  public void grantThemeRole(String userName, String themeID, String profile) {
    taskExecutor.execute(() ->
      executeWithRetry(
        () -> iamWSClient.grantThemeRole(userName, themeID, profile),
        userName,
        themeID,
        profile,
        "grantThemeRole"
      )
    );
  }

  /**
   * {@inheritDoc}
   *
   * <p>The revoke is submitted to the synchronization {@link TaskExecutor} and performed
   * asynchronously with retry handling.
   */
  @Override
  public void revokeThemeRole(String userName, String themeID, String profile) {
    taskExecutor.execute(() ->
      executeWithRetry(
        () -> iamWSClient.revokeThemeRole(userName, themeID, profile),
        userName,
        themeID,
        profile,
        "revokeThemeRole"
      )
    );
  }

  /**
   * Runs the given IAM action, retrying up to {@link #MAX_RETRIES} times with a pause between
   * attempts. If every attempt fails, a failure notification email is sent describing the operation
   * and the last encountered exception.
   *
   * @param action the IAM operation to execute
   * @param userName the user the operation applies to
   * @param themeID the eCORDA theme identifier
   * @param profile the CIRCABC profile / role involved
   * @param operation a human-readable name of the operation, used for logging and notification
   */
  private void executeWithRetry(
    Runnable action,
    String userName,
    String themeID,
    String profile,
    String operation
  ) {
    Exception lastException = null;
    for (int i = 0; i < MAX_RETRIES; i++) {
      try {
        action.run();
        return;
      } catch (Exception e) {
        lastException = e;
        sleepWithInterruptHandling();
      }
    }
    if (lastException != null) {
      sendEmail(userName, themeID, profile, operation, lastException);
    }
  }

  /**
   * Pauses the current thread for {@link #RETRY_PAUSE_IN_MILISECONDS} milliseconds between retry
   * attempts, restoring the thread's interrupt status if the sleep is interrupted.
   */
  private void sleepWithInterruptHandling() {
    try {
      Thread.sleep(RETRY_PAUSE_IN_MILISECONDS);
    } catch (InterruptedException e) {
      if (logger.isWarnEnabled()) logger.warn("Interrupted!", e);
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Sends a notification email to the configured administrator address reporting that an IAM
   * synchronization operation failed after all retries, so it can be handled manually. Any
   * {@link MessagingException} raised while sending is logged and swallowed.
   *
   * @param userName the user the failed operation applied to
   * @param themeID the eCORDA theme identifier
   * @param roleID the CIRCABC profile / role involved
   * @param operation a human-readable name of the failed operation
   * @param lastException the last exception encountered during the retry attempts, whose message is
   *     included in the email body
   */
  protected void sendEmail(
    String userName,
    String themeID,
    String roleID,
    String operation,
    Exception lastException
  ) {
    String from = mailService.getNoReplyEmailAddress();
    String subject = String.format(
      SUBJECT_MAIL_TEMPLATE,
      operation,
      userName,
      themeID,
      roleID
    );
    String body = String.format(
      BODY_MAIL_TEMPLATE,
      operation,
      userName,
      themeID,
      roleID,
      lastException.getMessage()
    );
    try {
      mailService.send(
        from,
        emailErrorAdress,
        null,
        subject,
        body,
        false,
        false
      );
    } catch (MessagingException e) {
      if (logger.isErrorEnabled()) logger.error("Error when sending email", e);
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Validates that the supplied node is an interest group root before delegating to the eCORDA
   * DAO. Any error raised while resolving the theme identifiers is logged and results in an empty
   * list.
   *
   * @throws IllegalArgumentException if {@code interestGroup} is not an interest group node (i.e.
   *     does not carry the {@link CircabcModel#ASPECT_IGROOT} aspect)
   */
  @Override
  public List<String> getEcordaThemeIds(NodeRef interestGroup) {
    if (!nodeService.hasAspect(interestGroup, CircabcModel.ASPECT_IGROOT)) {
      throw new IllegalArgumentException(
        "Not a interest group node " + interestGroup
      );
    }
    try {
      List<String> result = ecordaDaoServiceImpl.getEcordaThemaID(
        interestGroup.toString()
      );
      return result != null ? result : Collections.emptyList();
    } catch (Exception e) {
      if (logger.isErrorEnabled()) logger.error(
        "Error when getting ECORDA thema id for interest group: " +
          interestGroup,
        e
      );
      return Collections.emptyList();
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Iterates over the supplied users and grants the theme role to each one individually via
   * {@link #grantThemeRole(String, String, String)}.
   */
  @Override
  public void grantThemeRoles(
    Set<String> userName,
    String themeID,
    String profile
  ) {
    for (String userID : userName) {
      grantThemeRole(userID, themeID, profile);
    }
  }
}
