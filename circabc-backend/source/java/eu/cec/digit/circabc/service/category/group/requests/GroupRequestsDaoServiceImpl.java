/**
 *
 */
package eu.cec.digit.circabc.service.category.group.requests;

import eu.cec.digit.circabc.repo.app.model.KeyValueString;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.user.LdapUserService;
import eu.cec.digit.circabc.service.user.UserService;
import eu.cec.digit.circabc.util.CircabcUserDataBean;
import io.swagger.api.UsersApi;
import io.swagger.model.GroupCreationRequest;
import io.swagger.model.User;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.mybatis.spring.SqlSessionTemplate;

import java.util.*;
import java.util.Map.Entry;

/** @author beaurpi */
public class GroupRequestsDaoServiceImpl implements GroupRequestsDaoService {

    private static final Log logger = LogFactory.getLog(GroupRequestsDaoServiceImpl.class);

    private SqlSessionTemplate sqlSessionTemplate = null;

    private UsersApi usersApi;
    private UserService userService;
    private PersonService personService;
    private LdapUserService ldapUserService;
    private ManagementService managementService;

    @Override
    public Integer getCountCategoryGroupCreationRequests(String categRef, String filter) {
        Map<String, Object> props = new HashMap<>();
        props.put("categRef", categRef);
        props.put("filter", filter);
        Integer result =
                (Integer)
                        sqlSessionTemplate.selectOne(
                                "GroupRequests.select_count_category_group_requests", props);

        return result;
    }

    public List<GroupCreationRequest> getCategoryGroupCreationRequests(
            String categRef, int limit, int page, String filter) {
        Map<String, Object> props = new HashMap<>();

        props.put("categoryRef", categRef);
        props.put("limit", limit);
        // 0 based system
        if (page > 0) {
            props.put("page", page - 1);
        }
        props.put("filter", filter);
        // for mysql
        props.put("limitMin", (page - 1) * limit);

        List<GroupCreationRequestDAO> requests =
                (List<GroupCreationRequestDAO>)
                        sqlSessionTemplate.selectList("GroupRequests.select_category_group_requests", props);

        List<GroupCreationRequest> result = new ArrayList<>();

        if (requests.size() > 0) {
            for (int i = 0; i < requests.size(); i++) {
                GroupCreationRequest req = convertToGroupCreationRequest(requests.get(i));
                result.add(req);
            }
        }

        return result;
    }

    private GroupCreationRequest convertToGroupCreationRequest(
            GroupCreationRequestDAO groupCreationRequestDAO) {
        GroupCreationRequest result = new GroupCreationRequest();
        result.setId(groupCreationRequestDAO.getId());
        result.setProposedName(groupCreationRequestDAO.getProposedName());
        result.setJustification(groupCreationRequestDAO.getJustification());
        result.setRequestDate(new DateTime(groupCreationRequestDAO.getRequestDate()));
        result.setAgreement(groupCreationRequestDAO.getAgreement());
        result.setArgument(groupCreationRequestDAO.getArgument());
        result.setAgreementDate(new DateTime(groupCreationRequestDAO.getAgreementDate()));
        result.setCategoryRef(groupCreationRequestDAO.getCategoryReference());

        User user = usersApi.usersUserIdGet(groupCreationRequestDAO.getFromUsername());
        result.setFrom(user);

        if (groupCreationRequestDAO.getReviewer() != null
                && !"".equals(groupCreationRequestDAO.getReviewer())) {
            User reviewser = usersApi.usersUserIdGet(groupCreationRequestDAO.getReviewer());
            result.setReviewer(reviewser);
        }

        if (groupCreationRequestDAO.getLeaders() != null
                && !"".equals(groupCreationRequestDAO.getLeaders())) {
            String[] leaders = groupCreationRequestDAO.getLeaders().split(";");
            for (int i = 0; i < leaders.length; i++) {
                result.getLeaders().add(usersApi.usersUserIdGet(leaders[i]));
            }
        }

        fillGroupRequestI18nProperty(result);

        return result;
    }

    private void fillGroupRequestI18nProperty(GroupCreationRequest req) {
        Map<String, Object> props = new HashMap<>();
        props.put("reqId", req.getId());
        List<KeyValueString> titles =
                (List<KeyValueString>)
                        sqlSessionTemplate.selectList("select_category_group_requests_titles", props);

        for (KeyValueString title : titles) {
            if (title.getValue() != null) {
                req.getProposedTitle()
                        .put(title.getKey().toString().substring(0, 2), title.getValue().toString());
            }
        }

        List<KeyValueString> descriptions =
                (List<KeyValueString>)
                        sqlSessionTemplate.selectList("select_category_group_requests_descriptions", props);

        for (KeyValueString description : descriptions) {
            if (description.getValue() != null) {
                req.getProposedDescription()
                        .put(
                                description.getKey().toString().substring(0, 2), description.getValue().toString());
            }
        }
    }

    public UsersApi getUsersApi() {
        return usersApi;
    }

    public void setUsersApi(UsersApi usersApi) {
        this.usersApi = usersApi;
    }

    public SqlSessionTemplate getSqlSessionTemplate() {
        return sqlSessionTemplate;
    }

    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public LdapUserService getLdapUserService() {
        return ldapUserService;
    }

    public void setLdapUserService(LdapUserService ldapUserService) {
        this.ldapUserService = ldapUserService;
    }

    public ManagementService getManagementService() {
        return managementService;
    }

    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    @Override
    public int saveRequest(GroupCreationRequest body) {
        Map<String, Object> props = new HashMap<>();
        props.put("categoryRef", body.getCategoryRef());
        props.put("proposedName", body.getProposedName());
        props.put("fromUsername", body.getFrom().getUserId());
        props.put("justification", body.getJustification());
        props.put("id", null);

        Set<String> leaders = new HashSet<>();
        leaders.add(body.getFrom().getUserId());
        for (User leader : body.getLeaders()) {
            if (!personService.personExists(leader.getUserId())) {
                CircabcUserDataBean userDataBean = ldapUserService.getLDAPUserDataByUid(leader.getUserId());
                userDataBean.setHomeSpaceNodeRef(managementService.getGuestHomeNodeRef());
                userService.createUser(userDataBean, false);
            }
            leaders.add(leader.getUserId());
        }

        props.put("leaders", leaders.toString().replace("[", "").replace("]", "").replace(", ", ";"));

        sqlSessionTemplate.insert("GroupRequests.insert_category_group_request", props);
        Integer id = null;
        if (props.get("id") instanceof Integer) {
            id = (Integer) props.get("id");
        } else if (props.get("id") instanceof Long) {
            id = ((Long) props.get("id")).intValue();
        }

        for (Entry<String, String> title : body.getProposedTitle().entrySet()) {
            if (!"null".equals(title.getValue()) && !"".equals(title.getValue())) {
                Map<String, Object> propsTitle = new HashMap<>();
                propsTitle.put("id", id);
                propsTitle.put("parameter", "title");
                propsTitle.put("value", title.getValue());
                propsTitle.put("locale", title.getKey());
                sqlSessionTemplate.insert(
                        "GroupRequests.insert_category_group_request_title-description", propsTitle);
            }
        }

        for (Entry<String, String> description : body.getProposedDescription().entrySet()) {
            if (!"null".equals(description.getValue()) && !"".equals(description.getValue())) {
                Map<String, Object> propsDescription = new HashMap<>();
                propsDescription.put("id", id);
                propsDescription.put("parameter", "description");
                propsDescription.put("value", description.getValue());
                propsDescription.put("locale", description.getKey());
                sqlSessionTemplate.insert(
                        "GroupRequests.insert_category_group_request_title-description", propsDescription);
            }
        }

        return id;
    }

    @Override
    public void updateGroupCreationRequestApproval(
            String username, long id, int agreement, String argument) {
        Map<String, Object> props = new HashMap<>();
        props.put("id", id);
        props.put("argument", argument);
        props.put("agreement", agreement);
        props.put("reviewer", username);
        sqlSessionTemplate.update("update_category_group_request_approval", props);
    }

    @Override
    public GroupCreationRequest getCategoryGroupCreationRequests(String requestId) {

        Map<String, Object> props = new HashMap<>();

        props.put("requestId", requestId);

        GroupCreationRequestDAO request =
                (GroupCreationRequestDAO)
                        sqlSessionTemplate.selectOne("GroupRequests.select_category_group_request", props);
        GroupCreationRequest req = null;

        if (request != null) {
            req = convertToGroupCreationRequest(request);
        }
        return req;
    }

    @Override
    public void putCategoryGroupCreationRequest(String requestId, GroupCreationRequest body) {
        Map<String, Object> props = new HashMap<>();

        props.put("id", requestId);
        props.put("proposedName", body.getProposedName());
        if (body.getProposedTitle().containsKey("en")) {
            props.put("proposedTitle", body.getProposedTitle().get("en"));
        }
        if (body.getProposedDescription().containsKey("en")) {
            props.put("ProposedDescription", body.getProposedDescription().get("en"));
        }

        sqlSessionTemplate.update("GroupRequests.update_category_group_request_name", props);

        props.put("parameter", "title");
        sqlSessionTemplate.update(
                "GroupRequests.update_category_group_request_title-or-description", props);

        props.put("parameter", "description");
        sqlSessionTemplate.update(
                "GroupRequests.update_category_group_request_title-or-description", props);
    }
}
