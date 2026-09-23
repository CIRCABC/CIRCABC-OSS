package io.swagger.api;

import io.swagger.model.*;
import io.swagger.model.db.DistributionEmailDAO;
import java.util.List;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Business API contract for managing application-wide messages (banners/announcements) and their
 * associated distribution mailing list.
 *
 * <p>Implementations expose the operations backing the CIRCABC "app message" feature, covering:
 *
 * <ul>
 *   <li>retrieving the currently enabled app messages shown to users;
 *   <li>full CRUD management of app message templates;
 *   <li>configuration of the legacy ("old") system message (display/enable flags and content);
 *   <li>management of the distribution email list used to notify subscribers, including export to
 *       an Excel workbook.
 * </ul>
 *
 * @author beaurpi
 */
public interface AppMessageApi {
  /**
   * Gets the list of app messages that are currently enabled.
   *
   * @return the enabled {@link AppMessage} instances
   */
  List<AppMessage> getAppMessages();

  /**
   * Gets the app message templates defined in the application, regardless of whether each message
   * is enabled, in a paginated form.
   *
   * @param page the zero/one-based page index to retrieve
   * @param limit the maximum number of templates to return per page
   * @return a {@link PagedAppMessages} page of templates
   */
  PagedAppMessages getAppMessageTemplates(int page, int limit);

  /**
   * Inserts a new app message template into the system. This does not persist it as an active
   * legacy system message.
   *
   * @param template the template to add
   */
  void addAppMessageTemplate(AppMessage template);

  /**
   * Updates an existing app message template.
   *
   * @param template the template carrying the updated values (identified by its id)
   */
  void updateAppMessageTemplate(AppMessage template);

  /**
   * Deletes an app message template.
   *
   * @param id the identifier of the template to delete
   */
  void deleteAppMessageTemplate(Integer id);

  /**
   * Gets a single app message template by its identifier.
   *
   * @param id the identifier of the template to retrieve
   * @return the matching {@link AppMessage} template
   */
  AppMessage getAppMessageTemplate(Integer id);

  /**
   * Gets the configuration flag indicating whether the legacy ("old") system message is displayed.
   *
   * @return the current display configuration for the legacy message
   */
  DisplayConfiguration getDisplayOldMessage();

  /**
   * Sets whether the legacy ("old") system message should be displayed.
   *
   * @param displayOld {@code true} to display the legacy message, {@code false} otherwise
   */
  void setDisplayOldMessage(Boolean displayOld);

  /**
   * Gets the configuration flag indicating whether the legacy ("old") system message is enabled.
   *
   * @return the current enable configuration for the legacy message
   */
  EnableConfiguration getEnableOldMessage();

  /**
   * Sets whether the legacy ("old") system message should be enabled.
   *
   * @param enableOld {@code true} to enable the legacy message, {@code false} otherwise
   */
  void setEnableOldMessage(Boolean enableOld);

  /**
   * Updates the content of the legacy ("old") system message from the given template.
   *
   * @param template the template carrying the updated legacy message content
   */
  void udpateOldAppMessage(AppMessage template);

  /**
   * Gets a paginated list of the distribution email subscriptions, optionally filtered by a query.
   *
   * @param page the zero/one-based page index to retrieve
   * @param limit the maximum number of entries to return per page
   * @param query an optional filter applied to the email entries
   * @return a {@link PagedEmails} page of distribution email subscriptions
   */
  PagedEmails getAppDistributionEmails(int page, int limit, String query);

  /**
   * Adds the given entries to the distribution email list.
   *
   * @param list the distribution email entries to add
   */
  void addAppDistributionPostEmails(List<DistributionEmailDAO> list);

  /**
   * Checks whether an email subscription exists for the given query.
   *
   * @param query the email (or lookup key) to check
   * @return {@code true} if a matching subscription exists, {@code false} otherwise
   */
  Boolean isSubscribedDistributionEmail(String query);

  /**
   * Removes an entry from the distribution email list.
   *
   * @param id the identifier of the distribution email entry to remove
   */
  void removeAppDistributionPostEmails(Integer id);

  /**
   * Exports the distribution email list as an Excel workbook.
   *
   * @return a {@link Workbook} containing the distribution list
   */
  Workbook getdistributionListAsExcel();

  /**
   * Gets the distribution email subscription for a given user.
   *
   * @param userId the identifier of the user
   * @return the matching {@link DistributionEmailDAO}, or {@code null} if none exists
   */
  DistributionEmailDAO getSubscribedDistributionEmail(String userId);

  /**
   * Notifies the distribution list subscribers using the given template.
   *
   * @param template the app message template to send as a notification
   */
  void notifyTemplate(AppMessage template);

  /**
   * Gets a distribution email subscription by its identifier.
   *
   * @param idInt the identifier of the distribution email entry
   * @return the matching {@link DistributionEmailDAO}, or {@code null} if none exists
   */
  DistributionEmailDAO getSubscribedDistributionEmailById(Integer idInt);
}
