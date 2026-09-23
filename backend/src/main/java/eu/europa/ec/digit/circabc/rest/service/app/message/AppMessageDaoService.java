/**
 *
 */
package eu.europa.ec.digit.circabc.rest.service.app.message;

import io.swagger.model.db.AppMessageDAO;
import io.swagger.model.db.DistributionEmailDAO;
import java.util.Date;
import java.util.List;

/**
 * Data access service for application-wide messages (banners/notifications) and their email
 * distribution list.
 *
 * <p>This DAO layer abstracts the persistence operations for two related concerns:
 *
 * <ul>
 *   <li><b>App message templates</b> ({@link AppMessageDAO}) &ndash; the reusable definitions of
 *       application messages, including their content, severity level, display duration, closure
 *       date and enabled state.
 *   <li><b>Distribution emails</b> ({@link DistributionEmailDAO}) &ndash; the list of email
 *       addresses to which app messages may be distributed.
 * </ul>
 *
 * <p>Implementations are responsible for the actual read/write operations against the underlying
 * data store.
 *
 * @author beaurpi
 */
public interface AppMessageDaoService {
  /**
   * Returns a paginated list of app message templates.
   *
   * @param page the zero/one-based page index to retrieve (as interpreted by the implementation)
   * @param limit the maximum number of templates to return per page
   * @return the list of app message templates for the requested page
   */
  List<AppMessageDAO> selectAppMessageTemplates(int page, int limit);

  /**
   * Inserts a new app message template.
   *
   * @param content the message body/content
   * @param dateClosure the date on which the message expires or is closed
   * @param level the severity level of the message (e.g. info, warning, error)
   * @param displayTime the duration, in the implementation-defined unit, the message is displayed
   * @param enabled whether the template is enabled
   */
  void addAppMessageTemplate(
    String content,
    Date dateClosure,
    String level,
    Integer displayTime,
    Boolean enabled
  );

  /**
   * Updates an existing app message template.
   *
   * @param id the identifier of the template to update
   * @param content the new message body/content
   * @param dateClosure the new closure/expiry date
   * @param level the new severity level (e.g. info, warning, error)
   * @param displayTime the new display duration, in the implementation-defined unit
   * @param enabled whether the template is enabled
   */
  void updateAppMessageTemplate(
    Integer id,
    String content,
    Date dateClosure,
    String level,
    Integer displayTime,
    Boolean enabled
  );

  /**
   * Deletes the app message template with the given identifier.
   *
   * @param id the identifier of the template to delete
   */
  void deleteAppMessageTemplate(Integer id);

  /**
   * Retrieves a single app message template by its identifier.
   *
   * @param id the identifier of the template to retrieve
   * @return the matching template, or {@code null} if none exists
   */
  AppMessageDAO getMessageTemplate(Integer id);

  /**
   * Counts all app message templates, regardless of whether they are enabled or disabled.
   *
   * @return the total number of templates
   */
  Integer countAppMessageTemplates();

  /**
   * Returns a paginated, optionally filtered list of distribution emails.
   *
   * @param page the zero/one-based page index to retrieve (as interpreted by the implementation)
   * @param limit the maximum number of emails to return per page
   * @param search an optional search term used to filter the email addresses
   * @return the list of distribution emails for the requested page
   */
  List<DistributionEmailDAO> selectDistributionEmails(
    int page,
    int limit,
    String search
  );

  /**
   * Counts the distribution emails matching the given search query.
   *
   * @param query the search term used to filter the email addresses
   * @return the number of matching distribution emails
   */
  Long countDistributionEmails(String query);

  /**
   * Inserts a new distribution email entry.
   *
   * @param distribEmail the distribution email to persist
   */
  void insertEmail(DistributionEmailDAO distribEmail);

  /**
   * Indicates whether a distribution email matching the given query exists.
   *
   * @param query the email address (or search term) to look up
   * @return a non-zero value if a matching distribution email exists, otherwise {@code 0}
   */
  int hasDistributionEmail(String query);

  /**
   * Deletes the distribution email with the given identifier.
   *
   * @param id the identifier of the distribution email to delete
   */
  void deleteDistributionEmail(Integer id);

  /**
   * Retrieves a distribution email by its email address.
   *
   * @param email the email address to look up
   * @return the matching distribution email, or {@code null} if none exists
   */
  DistributionEmailDAO getDistributionEmail(String email);

  /**
   * Retrieves a distribution email by its identifier.
   *
   * @param id the identifier of the distribution email to retrieve
   * @return the matching distribution email, or {@code null} if none exists
   */
  DistributionEmailDAO getDistributionEmailById(Integer id);
}
