package io.swagger.api;

import eu.cec.digit.circabc.service.user.BulkImportUserData;
import io.swagger.model.*;
import org.joda.time.LocalDate;
import org.springframework.extensions.webscripts.WebScriptResponse;

import java.io.InputStream;
import java.util.List;

// the users API
public interface UsersApi {

    // retrieve the dashboard of a user, we need to provide a date restriction
    // it order to load not all information
    // GET /users/{userId}/dashboard
    UserDashboard getUserDashboard(String userId, LocalDate to, String language);

    // Retrieve the list of all users / Alfresco accounts If a parameter is
    // provided, then the list will only return the list based on the query
    // parameters
    // /users
    List<User> usersGet(String query, boolean filter);

    // creates a new user
    // POST /users
    User usersPost(User body);

    // Delete one user
    // DELETE /users/{userId}",
    void usersUserIdDelete(String userId);

    // Retrieve one user
    // GET /users/{userId}
    User usersUserIdGet(String userId);

    // retrieve user details from the user db
    // GET /users/{userId}
    User usersUserIdGetFromLdap(String userId);

    // Update one user
    // PUT /users/{userId}
    User usersUserIdPut(String userId, User body);

    List<InterestGroupProfile> getUserMembership(String userId);

    List<InterestGroupProfile> getUserMembership(String userId, Boolean lightMode);

    List<Category> getUserCategories(String userId);

    // Delete the users avatar
    // DELETE /users/{userId}/avatar
    void removeAvatar(String userId);

    // Update the users avatar
    // PUT /users/{userId}/avatar
    void updateAvatar(String userId, InputStream imageInputStream, String fileName);

    void writeBulkInviteTemplate(WebScriptResponse response);

    List<Category> getBulkInviteCategories(String username);

    List<IGData> getBulkInviteIGs(String categoryId, String currentIgId);

    List<BulkImportUserData> getBulkInviteMembers(List<String> igIds, String destinationIGId);

    void bulkInviteUsers(
            String bulkInviteDataJson, String igId, boolean createNewProfiles, boolean notifyUsers);

    List<BulkImportUserData> bulkInviteUsersDigestFile(
            String igId, InputStream inputStream, String fileName);

    List<User> retrieveUserList(List<User> users);

    void saveUserPreferenceConfiguration(String username, String preference);

    PreferenceConfiguration getUserPreference(String username);
}
