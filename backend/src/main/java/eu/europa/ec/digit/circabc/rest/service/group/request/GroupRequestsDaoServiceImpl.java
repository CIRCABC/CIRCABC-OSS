/**
 *
 */
package eu.europa.ec.digit.circabc.rest.service.group.request;

import eu.europa.ec.digit.circabc.rest.service.user.LdapUserService;
import eu.europa.ec.digit.circabc.rest.service.user.UserService;
import io.swagger.api.UsersApi;
import io.swagger.model.CircabcUserDataBean;
import io.swagger.model.GroupCreationRequest;
import io.swagger.model.GroupDeletionRequest;
import io.swagger.model.User;
import io.swagger.model.db.GroupCreationRequestDAO;
import io.swagger.model.db.GroupDeletionRequestDAO;
import io.swagger.model.db.KeyValueString;
import java.util.*;
import java.util.Map.Entry;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * MyBatis-backed implementation of {@link GroupRequestsDaoService}.
 *
 * <p>This service persists and retrieves the two kinds of group workflow requests handled at the
 * category level:
 *
 * <ul>
 *   <li><b>Group creation requests</b> ({@link GroupCreationRequest}) — proposals to create a new
 *       Interest Group within a category, including the proposed name, localized titles and
 *       descriptions, justification, requested leaders and the reviewer's approval decision.
 *   <li><b>Group deletion requests</b> ({@link GroupDeletionRequest}) — requests to delete an
 *       existing group, including the target group id, justification and the reviewer's decision.
 * </ul>
 *
 * <p>All persistence operations are delegated to a {@link SqlSessionTemplate} using mapper
 * statements defined under the {@code GroupRequests} namespace. Database rows are represented by the
 * DAO beans {@link GroupCreationRequestDAO} and {@link GroupDeletionRequestDAO}, which this class
 * converts to and from the richer domain models. During conversion, user identifiers are resolved
 * into full {@link User} objects via {@link UsersApi}, and missing leader accounts are provisioned
 * from LDAP when a request is saved.
 *
 * @author beaurpi
 */
public class GroupRequestsDaoServiceImpl implements GroupRequestsDaoService {

  /** MyBatis parameter key / request property name for the {@code description} field. */
  private static final String DESCRIPTION = "description";

  /** MyBatis parameter key / request property name for the {@code title} field. */
  private static final String TITLE = "title";

  /** MyBatis parameter key for the reviewer (approver) user id. */
  private static final String REVIEWER = "reviewer";

  /** MyBatis parameter key for the approval decision (agreement) flag. */
  private static final String AGREEMENT = "agreement";

  /**
   * MyBatis parameter key used to indicate whether a title or a description is being updated
   * (holds either {@link #TITLE} or {@link #DESCRIPTION}).
   */
  private static final String PARAMETER = "parameter";

  /** MyBatis parameter key for the lower bound (offset) of a paged query. */
  private static final String LIMIT_MIN = "limitMin";

  /** MyBatis parameter key for the page size (row limit) of a query. */
  private static final String LIMIT = "limit";

  /** MyBatis parameter key for the category reference the request belongs to. */
  private static final String CATEGORY_REF = "categoryRef";

  /** MyBatis parameter key for the free-text search filter. */
  private static final String FILTER = "filter";

  /** MyBatis session template used to execute the {@code GroupRequests} mapper statements. */
  private SqlSessionTemplate sqlSessionTemplate = null;

  /** Logger for this service. */
  private static final Log logger = LogFactory.getLog(
    GroupRequestsDaoServiceImpl.class
  );

  /** API used to resolve user identifiers into full {@link User} objects. */
  @Autowired
  private UsersApi usersApi;

  /** Service used to provision Alfresco users (e.g. leaders that do not yet exist). */
  @Autowired
  private UserService userService;

  /** Alfresco service used to check whether a person already exists in the repository. */
  @Autowired
  private PersonService personService;

  /** Service used to fetch user data from LDAP when a leader account must be created. */
  @Autowired
  @Qualifier("ldapOrLuceneUserService")
  private LdapUserService ldapUserService;

  /**
   * Returns the number of group creation requests for a category, honouring an optional filter.
   *
   * @param categRef the category reference to count requests for
   * @param filter an optional free-text filter applied to the requests; may be {@code null}
   * @return the matching request count
   */
  @Override
  public Integer getCountCategoryGroupCreationRequests(
    String categRef,
    String filter
  ) {
    Map<String, Object> props = new HashMap<>();
    props.put("categRef", categRef);
    props.put(FILTER, filter);
    return (Integer) sqlSessionTemplate.selectOne(
      "GroupRequests.select_count_category_group_requests",
      props
    );
  }

  /**
   * Retrieves a page of group creation requests for a category.
   *
   * <p>Each matching database row is converted into a fully populated {@link GroupCreationRequest},
   * including resolved user objects and localized titles/descriptions.
   *
   * @param categRef the category reference to fetch requests for
   * @param limit the maximum number of requests to return (page size)
   * @param page the 1-based page number; values greater than 0 are translated to a 0-based offset
   * @param filter an optional free-text filter applied to the requests; may be {@code null}
   * @return the list of matching creation requests, never {@code null}
   */
  public List<GroupCreationRequest> getCategoryGroupCreationRequests(
    String categRef,
    int limit,
    int page,
    String filter
  ) {
    Map<String, Object> props = new HashMap<>();

    props.put(CATEGORY_REF, categRef);
    props.put(LIMIT, limit);
    // 0 based system
    if (page > 0) {
      props.put("page", page - 1);
    }
    props.put(FILTER, filter);
    // for mysql
    props.put(LIMIT_MIN, (page - 1) * limit);

    List<GroupCreationRequestDAO> requests = sqlSessionTemplate.selectList(
      "GroupRequests.select_category_group_requests",
      props
    );

    List<GroupCreationRequest> result = new ArrayList<>();

    if (!requests.isEmpty()) {
      for (int i = 0; i < requests.size(); i++) {
        GroupCreationRequest req = convertToGroupCreationRequest(
          requests.get(i)
        );
        result.add(req);
      }
    }

    return result;
  }

  /**
   * Converts a persisted {@link GroupCreationRequestDAO} row into a domain
   * {@link GroupCreationRequest}, resolving the requester, reviewer and leader user ids into full
   * {@link User} objects and populating the localized titles/descriptions.
   *
   * @param groupCreationRequestDAO the database row to convert
   * @return the populated domain request
   */
  private GroupCreationRequest convertToGroupCreationRequest(
    GroupCreationRequestDAO groupCreationRequestDAO
  ) {
    GroupCreationRequest result = new GroupCreationRequest();
    result.setId(groupCreationRequestDAO.getId());
    result.setProposedName(groupCreationRequestDAO.getProposedName());
    result.setJustification(groupCreationRequestDAO.getJustification());
    result.setRequestDate(
      new DateTime(groupCreationRequestDAO.getRequestDate())
    );
    result.setAgreement(groupCreationRequestDAO.getAgreement());
    result.setArgument(groupCreationRequestDAO.getArgument());
    result.setAgreementDate(
      new DateTime(groupCreationRequestDAO.getAgreementDate())
    );
    result.setCategoryRef(groupCreationRequestDAO.getCategoryReference());

    User user = usersApi.usersUserIdGet(
      groupCreationRequestDAO.getFromUsername()
    );
    result.setFrom(user);

    if (
      groupCreationRequestDAO.getReviewer() != null &&
      !"".equals(groupCreationRequestDAO.getReviewer())
    ) {
      User reviewser = usersApi.usersUserIdGet(
        groupCreationRequestDAO.getReviewer()
      );
      result.setReviewer(reviewser);
    }

    if (
      groupCreationRequestDAO.getLeaders() != null &&
      !"".equals(groupCreationRequestDAO.getLeaders())
    ) {
      String[] leaders = groupCreationRequestDAO.getLeaders().split(";");
      for (int i = 0; i < leaders.length; i++) {
        result.getLeaders().add(usersApi.usersUserIdGet(leaders[i]));
      }
    }

    fillGroupRequestI18nProperty(result);

    return result;
  }

  /**
   * Loads the localized (i18n) proposed titles and descriptions for a creation request and copies
   * them into the given request, keyed by the two-letter language code.
   *
   * @param req the creation request to enrich; its id is used to look up the localized values
   */
  private void fillGroupRequestI18nProperty(GroupCreationRequest req) {
    Map<String, Object> props = new HashMap<>();
    props.put("reqId", req.getId());
    List<KeyValueString> titles = sqlSessionTemplate.selectList(
      "select_category_group_requests_titles",
      props
    );

    for (KeyValueString title : titles) {
      if (title.getValue() != null) {
        req
          .getProposedTitle()
          .put(title.getKey().substring(0, 2), title.getValue());
      }
    }

    List<KeyValueString> descriptions = sqlSessionTemplate.selectList(
      "select_category_group_requests_descriptions",
      props
    );

    for (KeyValueString description : descriptions) {
      if (description.getValue() != null) {
        req
          .getProposedDescription()
          .put(description.getKey().substring(0, 2), description.getValue());
      }
    }
  }

  /**
   * Returns the API used to resolve user identifiers.
   *
   * @return the configured {@link UsersApi}
   */
  public UsersApi getUsersApi() {
    return usersApi;
  }

  /**
   * Sets the API used to resolve user identifiers.
   *
   * @param usersApi the {@link UsersApi} to use
   */
  public void setUsersApi(UsersApi usersApi) {
    this.usersApi = usersApi;
  }

  /**
   * Returns the MyBatis session template used for persistence.
   *
   * @return the configured {@link SqlSessionTemplate}
   */
  public SqlSessionTemplate getSqlSessionTemplate() {
    return sqlSessionTemplate;
  }

  /**
   * Sets the MyBatis session template used for persistence.
   *
   * @param sqlSessionTemplate the {@link SqlSessionTemplate} to use
   */
  public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }

  /**
   * Persists a new group creation request together with its localized titles and descriptions.
   *
   * <p>The requester is always added as a leader. Any requested leader that does not yet exist as
   * an Alfresco person is provisioned from LDAP before being recorded. Non-empty localized titles
   * and descriptions are stored as separate rows.
   *
   * @param body the creation request to persist
   */
  @Override
  public void saveRequest(GroupCreationRequest body) {
    Map<String, Object> props = new HashMap<>();
    props.put(CATEGORY_REF, body.getCategoryRef());
    props.put("proposedName", body.getProposedName());
    props.put("fromUsername", body.getFrom().getUserId());
    props.put("justification", body.getJustification());
    props.put("id", null);

    Set<String> leaders = new HashSet<>();
    leaders.add(body.getFrom().getUserId());
    for (User leader : body.getLeaders()) {
      if (!personService.personExists(leader.getUserId())) {
        CircabcUserDataBean userDataBean = ldapUserService.getLDAPUserDataByUid(
          leader.getUserId()
        );

        userService.createUser(userDataBean, true);
      }
      leaders.add(leader.getUserId());
    }

    props.put(
      "leaders",
      leaders.toString().replace("[", "").replace("]", "").replace(", ", ";")
    );

    sqlSessionTemplate.insert(
      "GroupRequests.insert_category_group_request",
      props
    );
    Integer id = null;
    if (props.get("id") instanceof Integer intId) {
      id = intId;
    } else if (props.get("id") instanceof Long longId) {
      id = longId.intValue();
    }

    for (Entry<String, String> title : body.getProposedTitle().entrySet()) {
      if (!"null".equals(title.getValue()) && !"".equals(title.getValue())) {
        Map<String, Object> propsTitle = new HashMap<>();
        propsTitle.put("id", id);
        propsTitle.put(PARAMETER, TITLE);
        propsTitle.put("value", title.getValue());
        propsTitle.put("locale", title.getKey());
        sqlSessionTemplate.insert(
          "GroupRequests.insert_category_group_request_title-description",
          propsTitle
        );
      }
    }

    for (Entry<String, String> description : body
      .getProposedDescription()
      .entrySet()) {
      if (
        !"null".equals(description.getValue()) &&
        !"".equals(description.getValue())
      ) {
        Map<String, Object> propsDescription = new HashMap<>();
        propsDescription.put("id", id);
        propsDescription.put(PARAMETER, DESCRIPTION);
        propsDescription.put("value", description.getValue());
        propsDescription.put("locale", description.getKey());
        sqlSessionTemplate.insert(
          "GroupRequests.insert_category_group_request_title-description",
          propsDescription
        );
      }
    }
  }

  /**
   * Records a reviewer's approval decision on a group creation request.
   *
   * @param username the reviewer's user id
   * @param id the identifier of the creation request being reviewed
   * @param agreement the approval decision flag (e.g. approved/rejected)
   * @param argument the reviewer's justification for the decision
   */
  @Override
  public void updateGroupCreationRequestApproval(
    String username,
    long id,
    int agreement,
    String argument
  ) {
    Map<String, Object> props = new HashMap<>();
    props.put("id", id);
    props.put("argument", argument);
    props.put(AGREEMENT, agreement);
    props.put(REVIEWER, username);
    sqlSessionTemplate.update("update_category_group_request_approval", props);
  }

  /**
   * Retrieves a single group creation request by its identifier.
   *
   * @param requestId the request identifier as a string; parsed as an integer
   * @return the matching {@link GroupCreationRequest}, or {@code null} if none exists
   * @throws NumberFormatException if {@code requestId} is not a valid integer
   */
  @Override
  public GroupCreationRequest getCategoryGroupCreationRequests(
    String requestId
  ) {
    Map<String, Object> props = new HashMap<>();

    props.put("requestId", Integer.parseInt(requestId));

    GroupCreationRequestDAO request =
      (GroupCreationRequestDAO) sqlSessionTemplate.selectOne(
        "GroupRequests.select_category_group_request",
        props
      );
    GroupCreationRequest req = null;

    if (request != null) {
      req = convertToGroupCreationRequest(request);
    }
    return req;
  }

  /**
   * Updates the proposed name and the English title/description of an existing group creation
   * request.
   *
   * @param requestId the request identifier as a string; parsed as an integer
   * @param body the request carrying the updated proposed name, titles and descriptions
   * @throws NumberFormatException if {@code requestId} is not a valid integer
   */
  @Override
  public void putCategoryGroupCreationRequest(
    String requestId,
    GroupCreationRequest body
  ) {
    Map<String, Object> props = new HashMap<>();

    props.put("id", Integer.valueOf(requestId));
    props.put("proposedName", body.getProposedName());
    if (body.getProposedTitle().containsKey("en")) {
      props.put("proposedTitle", body.getProposedTitle().get("en"));
    }
    if (body.getProposedDescription().containsKey("en")) {
      props.put("ProposedDescription", body.getProposedDescription().get("en"));
    }

    sqlSessionTemplate.update(
      "GroupRequests.update_category_group_request_name",
      props
    );

    props.put(PARAMETER, TITLE);
    sqlSessionTemplate.update(
      "GroupRequests.update_category_group_request_title-or-description",
      props
    );

    props.put(PARAMETER, DESCRIPTION);
    sqlSessionTemplate.update(
      "GroupRequests.update_category_group_request_title-or-description",
      props
    );
  }

  /**
   * Persists a new group deletion request.
   *
   * <p>Any failure is logged and swallowed so that callers receive a status code rather than an
   * exception.
   *
   * @param body the deletion request to persist
   * @return {@code 1} if the request was inserted successfully, {@code 0} if an error occurred
   */
  @Override
  public int saveRequestDeletion(GroupDeletionRequest body) {
    Map<String, Object> props = new HashMap<>();
    try {
      props.put("id", null);
      props.put("from_username", body.getFrom().getUserId());
      props.put("request_date", body.getRequestDate().toDate());
      props.put("category_ref", body.getCategoryRef());
      props.put(AGREEMENT, body.getAgreement());
      props.put(REVIEWER, null);
      props.put("justification", body.getJustification());
      props.put("agreement_date", null);
      props.put("group_id", body.getGroupId());
      props.put(TITLE, body.getTitle());
      props.put("name", body.getName());
      props.put(DESCRIPTION, body.getDescription());

      Set<String> leaders = new HashSet<>();
      if (body.getLeaders() != null) {
        for (User leader : body.getLeaders()) {
          leaders.add(leader.getUserId());
        }
      }
      props.put(
        "leaders",
        leaders.toString().replace("[", "").replace("]", "").replace(", ", ";")
      );

      sqlSessionTemplate.insert(
        "GroupRequests.insert_category_group_request_deletion",
        props
      );

      return 1;
    } catch (Exception e) {
      if (logger.isWarnEnabled()) {
        logger.warn(
          "Error while inserting group deletion request: " + e.getMessage(),
          e
        );
      }
      return 0;
    }
  }

  /**
   * Indicates whether a pending deletion request already exists for the given group.
   *
   * @param groupId the group identifier to check
   * @return {@code true} if at least one deletion request exists for the group, {@code false}
   *     otherwise
   */
  @Override
  public boolean existsGroupDeleteRequest(String groupId) {
    Map<String, Object> props = new HashMap<>();
    props.put("groupId", groupId);
    int result = (int) sqlSessionTemplate.selectOne(
      "GroupRequests.select_count_group_delete_requests",
      props
    );
    return result != 0;
  }

  /**
   * Retrieves a page of group deletion requests for a category.
   *
   * <p>When {@code limit} is {@code null} or non-positive, all matching requests are returned. The
   * filter, when provided, is lower-cased before being applied.
   *
   * @param categoryRef the category reference to fetch deletion requests for
   * @param limit the maximum number of requests to return; {@code null} or {@code <= 0} means no
   *     limit
   * @param page the 1-based page number used to compute the query offset; may be {@code null}
   * @param filter an optional free-text filter; may be {@code null}
   * @return the list of matching deletion requests, never {@code null}
   */
  @Override
  public List<GroupDeletionRequest> getCategoryGroupDeletionRequests(
    String categoryRef,
    Integer limit,
    Integer page,
    String filter
  ) {
    Map<String, Object> props = new HashMap<>();

    props.put(CATEGORY_REF, categoryRef);

    if (limit != null && limit > 0) {
      int limitMin = (page != null && page > 0) ? (page - 1) * limit : 0;
      props.put(LIMIT_MIN, limitMin);
      props.put(LIMIT, limit);
    } else {
      props.put(LIMIT_MIN, 0);
      props.put(LIMIT, Integer.MAX_VALUE);
    }

    // Filter conditions
    if (filter != null) {
      props.put(FILTER, filter.toLowerCase());
    } else {
      props.put(FILTER, ""); // Default to no filter if none provided
    }

    // Fetch the list from the database
    List<GroupDeletionRequestDAO> request = sqlSessionTemplate.selectList(
      "GroupRequests.select_category_group_delete_requests",
      props
    );

    List<GroupDeletionRequest> result = new ArrayList<>();

    if (!request.isEmpty()) {
      for (int i = 0; i < request.size(); i++) {
        GroupDeletionRequest req = convertToGroupDeletionRequest(
          request.get(i)
        );
        result.add(req);
      }
    }

    return result;
  }

  /**
   * Converts a persisted {@link GroupDeletionRequestDAO} row into a domain
   * {@link GroupDeletionRequest}, resolving the requester, reviewer and leader user ids into full
   * {@link User} objects.
   *
   * @param groupDeletionRequestDAO the database row to convert
   * @return the populated domain deletion request
   */
  private GroupDeletionRequest convertToGroupDeletionRequest(
    GroupDeletionRequestDAO groupDeletionRequestDAO
  ) {
    GroupDeletionRequest result = new GroupDeletionRequest();
    result.setId((long) groupDeletionRequestDAO.getId());

    User user = usersApi.usersUserIdGet(
      groupDeletionRequestDAO.getFromUsername()
    );
    result.setFrom(user);

    result.setRequestDate(
      new DateTime(groupDeletionRequestDAO.getRequestDate())
    );

    result.setCategoryRef(groupDeletionRequestDAO.getCategoryRef());

    result.setAgreementDate(
      new DateTime(groupDeletionRequestDAO.getAgreementDate())
    );

    if (
      groupDeletionRequestDAO.getReviewer() != null &&
      !"".equals(groupDeletionRequestDAO.getReviewer())
    ) {
      User reviewser = usersApi.usersUserIdGet(
        groupDeletionRequestDAO.getReviewer()
      );
      result.setReviewer(reviewser);
    }

    if (
      groupDeletionRequestDAO.getJustification() != null &&
      !"".equals(groupDeletionRequestDAO.getJustification())
    ) {
      result.setJustification(groupDeletionRequestDAO.getJustification());
    }
    if (
      groupDeletionRequestDAO.getGroupId() != null &&
      !"".equals(groupDeletionRequestDAO.getGroupId())
    ) {
      result.setGroupId(groupDeletionRequestDAO.getGroupId());
    }

    result.setAgreement(groupDeletionRequestDAO.getAgreement());

    result.setTitle(groupDeletionRequestDAO.getTitle());
    result.setName(groupDeletionRequestDAO.getName());
    result.setDescription(groupDeletionRequestDAO.getDescription());

    if (
      groupDeletionRequestDAO.getLeaders() != null &&
      !"".equals(groupDeletionRequestDAO.getLeaders())
    ) {
      String[] leaders = groupDeletionRequestDAO.getLeaders().split(";");
      for (int i = 0; i < leaders.length; i++) {
        result.getLeaders().add(usersApi.usersUserIdGet(leaders[i]));
      }
    }

    return result;
  }

  /**
   * Returns the number of group deletion requests for a category, honouring an optional filter.
   *
   * @param categoryRef the category reference to count deletion requests for
   * @param filter an optional free-text filter; may be {@code null}
   * @return the matching request count
   */
  @Override
  public Long getCountCategoryGroupDeletionRequests(
    String categoryRef,
    String filter
  ) {
    Map<String, Object> props = new HashMap<>();
    props.put("categRef", categoryRef);
    props.put(FILTER, filter);
    int result = (int) sqlSessionTemplate.selectOne(
      "GroupRequests.select_count_category_group_delete_requests",
      props
    );

    return (long) result;
  }

  /**
   * Retrieves a single group deletion request by its identifier.
   *
   * @param requestId the request identifier as a string; parsed as an integer
   * @return the matching {@link GroupDeletionRequest}, or {@code null} if none exists
   * @throws NumberFormatException if {@code requestId} is not a valid integer
   */
  @Override
  public GroupDeletionRequest getCategoryGroupDeletionRequests(
    String requestId
  ) {
    Map<String, Object> props = new HashMap<>();
    props.put("requestId", Integer.parseInt(requestId));

    GroupDeletionRequestDAO request =
      (GroupDeletionRequestDAO) sqlSessionTemplate.selectOne(
        "GroupRequests.select_group_request_delete_by_id",
        props
      );

    GroupDeletionRequest req = null;

    if (request != null) {
      req = convertToGroupDeletionRequest(request);
    }
    return req;
  }

  /**
   * Records a reviewer's decision on a group deletion request.
   *
   * <p>When the request is rejected (agreement of {@code -1}), the rejection message is also stored.
   * Any failure is logged and swallowed.
   *
   * @param body the deletion request carrying the id, agreement and optional rejection message
   * @param reviewer the reviewer's user id
   */
  @Override
  public void updateRequestDeletion(
    GroupDeletionRequest body,
    String reviewer
  ) {
    Map<String, Object> props = new HashMap<>();
    try {
      // update reviewer, agrementDate and agrement
      props.put("id", body.getId());
      props.put(REVIEWER, reviewer);
      props.put(AGREEMENT, body.getAgreement());

      if (body.getAgreement() == -1) {
        props.put("rejected_message", body.getRejectedMessage());
      }

      sqlSessionTemplate.update(
        "GroupRequests.update_category_group_request_deletion",
        props
      );
    } catch (Exception e) {
      if (logger.isWarnEnabled()) {
        logger.warn(
          "Error while updating group deletion request: " + e.getMessage(),
          e
        );
      }
    }
  }

  /**
   * Deletes any deletion request(s) associated with the given group.
   *
   * @param groupId the group identifier whose deletion request should be removed
   */
  @Override
  public void deleteRequestDeletion(String groupId) {
    Map<String, Object> props = new HashMap<>();
    props.put("group_id", groupId);
    sqlSessionTemplate.delete(
      "GroupRequests.delete_group_request_delete",
      props
    );
  }
}
