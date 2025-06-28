package io.swagger.api;

import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.service.profile.CircabcRootProfileManagerService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import eu.cec.digit.circabc.service.user.UserService;
import java.util.List;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;

public class CircabcApiImpl implements CircabcApi {

  private PersonService personService;
  private UserService userService;
  private AuthorityService authorityService;
  private CircabcService circabcService;
  private ManagementService managementService;
  private CircabcRootProfileManagerService circabcRootProfileManagerService;

  public void setPersonService(PersonService personService) {
    this.personService = personService;
  }

  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  public void setAuthorityService(AuthorityService authorityService) {
    this.authorityService = authorityService;
  }

  public void setCircabcService(CircabcService circabcService) {
    this.circabcService = circabcService;
  }

  public void setManagementService(ManagementService managementService) {
    this.managementService = managementService;
  }

  public void setCircabcRootProfileManagerService(
    CircabcRootProfileManagerService circabcRootProfileManagerService
  ) {
    this.circabcRootProfileManagerService = circabcRootProfileManagerService;
  }

  @Override
  public void circabcAdminsPost(List<String> userIds) {
    for (String userAuthority : userIds) {
      if (personService.getPersonOrNull(userAuthority) == null) {
        userService.createUser(
          userService.getLDAPUserDataNoFilterByUid(userAuthority),
          true
        );
      }

      NodeRef circabcNodeRef = managementService.getCircabcNodeRef();
      String circabcAdminGroup = circabcRootProfileManagerService
        .getProfile(
          circabcNodeRef,
          CircabcRootProfileManagerService.Profiles.CIRCABC_ADMIN
        )
        .getPrefixedAlfrescoGroupName();

      authorityService.addAuthority(circabcAdminGroup, userAuthority);
      if (!circabcService.isUserExists(userAuthority)) {
        circabcService.addUser(userAuthority);
      }
      circabcService.addCircabcAdmin(userAuthority);
    }
  }
}
