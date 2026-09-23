/**
 *
 */
package eu.europa.ec.digit.circabc.rest.service.app.message;

import io.swagger.model.db.AppMessageDAO;
import io.swagger.model.db.DistributionEmailDAO;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mybatis.spring.SqlSessionTemplate;

/**
 * Default MyBatis-backed implementation of {@link AppMessageDaoService}.
 *
 * <p>This service is the data-access layer for the application-wide broadcast messages
 * (the "app messages" shown to users, e.g. maintenance banners) and for the distribution
 * e-mail addresses used to notify recipients. All persistence operations are delegated to
 * a configured {@link SqlSessionTemplate}, invoking the mapped SQL statements declared under
 * the {@code AppMessage} MyBatis namespace (for example {@code AppMessage.select_app_messages}
 * or {@code AppMessage.insert_distribution_email}).
 *
 * <p>The {@link SqlSessionTemplate} dependency is injected via its setter, following the
 * XML-based Spring wiring used throughout the module.
 *
 * @author beaurpi
 */
public class AppMessageDaoServiceImpl implements AppMessageDaoService {

  /** MyBatis parameter key holding a distribution e-mail address. */
  private static final String EMAIL = "email";

  /** MyBatis parameter key holding a (lower-cased) free-text search term. */
  private static final String SEARCH = "search";

  /** Spring-managed MyBatis session template used to execute all mapped SQL statements. */
  private SqlSessionTemplate sqlSessionTemplate = null;

  /**
   * Retrieves a page of application message templates ordered as defined by the underlying
   * SQL statement.
   *
   * @param page the 1-based page number; converted to the 0-based offset expected by the
   *     query. Values of {@code 0} or below leave the offset unset.
   * @param limit the maximum number of rows to return; ignored when {@code 0} or below
   * @return the list of {@link AppMessageDAO} for the requested page (possibly empty)
   */
  @Override
  public List<AppMessageDAO> selectAppMessageTemplates(int page, int limit) {
    Map<String, Object> props = new HashMap<>();

    // 0 based system
    if (page > 0) {
      props.put("page", page - 1);
    }

    if (limit > 0) {
      props.put("limit", limit);
    }

    // for mysql
    props.put("limitMin", (page - 1) * limit);

    return sqlSessionTemplate.selectList(
      "AppMessage.select_app_messages",
      props
    );
  }

  /**
   * Inserts a new application message template.
   *
   * @param content the message body/content to display
   * @param dateClosure the date after which the message should no longer be shown
   * @param level the severity/display level of the message (e.g. info, warning)
   * @param displayTime the duration, in the unit expected by the application, for which the
   *     message is displayed
   * @param enabled {@code true} to make the message active, {@code false} otherwise
   */
  @Override
  public void addAppMessageTemplate(
    String content,
    Date dateClosure,
    String level,
    Integer displayTime,
    Boolean enabled
  ) {
    Map<String, Object> props = new HashMap<>();
    props.put("dateClosure", dateClosure);
    props.put("displayTime", displayTime);
    props.put("content", content);
    props.put("level", level);
    props.put("enabled", enabled);

    sqlSessionTemplate.insert("AppMessage.insert_app_message", props);
  }

  /**
   * Updates an existing application message template identified by its id.
   *
   * @param id the identifier of the message template to update
   * @param content the new message body/content
   * @param dateClosure the new date after which the message should no longer be shown
   * @param level the new severity/display level of the message
   * @param displayTime the new duration for which the message is displayed
   * @param enabled {@code true} to make the message active, {@code false} otherwise
   */
  @Override
  public void updateAppMessageTemplate(
    Integer id,
    String content,
    Date dateClosure,
    String level,
    Integer displayTime,
    Boolean enabled
  ) {
    Map<String, Object> props = new HashMap<>();
    props.put("dateClosure", dateClosure);
    props.put("displayTime", displayTime);
    props.put("content", content);
    props.put("level", level);
    props.put("enabled", enabled);
    props.put("id", id);

    sqlSessionTemplate.update("AppMessage.update_app_message", props);
  }

  /**
   * Deletes the application message template with the given id.
   *
   * @param id the identifier of the message template to delete
   */
  @Override
  public void deleteAppMessageTemplate(Integer id) {
    Map<String, Object> props = new HashMap<>();
    props.put("id", id);

    sqlSessionTemplate.delete("AppMessage.delete_app_message", props);
  }

  /** @return the sqlSessionTemplate */
  public SqlSessionTemplate getSqlSessionTemplate() {
    return sqlSessionTemplate;
  }

  /** @param sqlSessionTemplate the sqlSessionTemplate to set */
  public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }

  /**
   * Retrieves a single application message template by its id.
   *
   * @param id the identifier of the message template
   * @return the matching {@link AppMessageDAO}, or {@code null} if none exists
   */
  @Override
  public AppMessageDAO getMessageTemplate(Integer id) {
    Map<String, Object> props = new HashMap<>();
    props.put("id", id);
    return (AppMessageDAO) sqlSessionTemplate.selectOne(
      "AppMessage.select_app_message",
      props
    );
  }

  /**
   * Counts the total number of application message templates.
   *
   * @return the total count of message templates
   */
  @Override
  public Integer countAppMessageTemplates() {
    return (Integer) sqlSessionTemplate.selectOne(
      "AppMessage.count_app_messages"
    );
  }

  /**
   * Retrieves a page of distribution e-mail addresses, optionally filtered by a search term.
   *
   * @param page the 1-based page number; converted to the 0-based offset expected by the
   *     query. Values of {@code 0} or below leave the offset unset.
   * @param limit the maximum number of rows to return
   * @param search an optional case-insensitive search term; a {@code null} value or the
   *     literal string {@code "null"} is treated as no filter
   * @return the list of matching {@link DistributionEmailDAO} for the requested page
   *     (possibly empty)
   */
  @Override
  public List<DistributionEmailDAO> selectDistributionEmails(
    int page,
    int limit,
    String search
  ) {
    Map<String, Object> props = new HashMap<>();
    // 0 based system
    if (page > 0) {
      props.put("page", page - 1);
    }
    props.put("limit", limit);

    // for mysql
    props.put("limitMin", (page - 1) * limit);

    if (search != null && !"null".equals(search)) {
      props.put(SEARCH, search.toLowerCase());
    } else {
      props.put(SEARCH, "");
    }

    return sqlSessionTemplate.selectList(
      "AppMessage.select_distribution_emails",
      props
    );
  }

  /**
   * Counts the distribution e-mail addresses matching an optional search term.
   *
   * @param query an optional case-insensitive search term; a {@code null} value or the
   *     literal string {@code "null"} is treated as no filter
   * @return the number of matching distribution e-mail addresses
   */
  @Override
  public Long countDistributionEmails(String query) {
    Map<String, Object> props = new HashMap<>();
    if (query != null && !"null".equals(query)) {
      props.put(SEARCH, query.toLowerCase());
    } else {
      props.put(SEARCH, "");
    }

    return Long.parseLong(
      sqlSessionTemplate
        .selectOne("AppMessage.count_distribution_emails", props)
        .toString()
    );
  }

  /**
   * Inserts a new distribution e-mail address.
   *
   * @param distribEmail the distribution e-mail entity whose address is persisted
   */
  @Override
  public void insertEmail(DistributionEmailDAO distribEmail) {
    Map<String, Object> props = new HashMap<>();
    props.put(EMAIL, distribEmail.getEmailAddress());
    sqlSessionTemplate.insert("AppMessage.insert_distribution_email", props);
  }

  /**
   * Checks whether a given distribution e-mail address already exists.
   *
   * @param query the e-mail address to look up; matched case-insensitively
   * @return the number of stored rows matching the address (typically {@code 0} or {@code 1})
   */
  @Override
  public int hasDistributionEmail(String query) {
    Map<String, Object> props = new HashMap<>();
    props.put(EMAIL, query.toLowerCase());
    return (int) sqlSessionTemplate.selectOne(
      "AppMessage.count_distribution_email",
      props
    );
  }

  /**
   * Deletes the distribution e-mail address with the given id.
   *
   * @param id the identifier of the distribution e-mail to delete
   */
  @Override
  public void deleteDistributionEmail(Integer id) {
    Map<String, Object> props = new HashMap<>();
    props.put("id", id);
    sqlSessionTemplate.delete("AppMessage.delete_distribution_email", props);
  }

  /**
   * Retrieves a distribution e-mail entity by its address.
   *
   * @param email the e-mail address to look up; matched case-insensitively
   * @return the matching {@link DistributionEmailDAO}, or {@code null} if none exists
   */
  @Override
  public DistributionEmailDAO getDistributionEmail(String email) {
    Map<String, Object> props = new HashMap<>();
    props.put(EMAIL, email.toLowerCase());
    return (DistributionEmailDAO) sqlSessionTemplate.selectOne(
      "AppMessage.get_distribution_email",
      props
    );
  }

  /**
   * Retrieves a distribution e-mail entity by its id.
   *
   * @param id the identifier of the distribution e-mail
   * @return the matching {@link DistributionEmailDAO}, or {@code null} if none exists
   */
  @Override
  public DistributionEmailDAO getDistributionEmailById(Integer id) {
    Map<String, Object> props = new HashMap<>();
    props.put("id", id);
    return (DistributionEmailDAO) sqlSessionTemplate.selectOne(
      "AppMessage.get_distribution_email_by_id",
      props
    );
  }
}
