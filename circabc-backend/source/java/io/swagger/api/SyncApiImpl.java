package io.swagger.api;

import eu.cec.digit.circabc.repo.app.CircabcDaoServiceImpl;
import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.service.struct.ManagementService;
import io.swagger.util.Converter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SyncApiImpl implements SyncApi {

  private static final Log logger = LogFactory.getLog(SyncApiImpl.class);

  private CircabcService circabcService;
  private CircabcDaoServiceImpl circabcDaoService;
  private NodeService nodeService;
  private PersonService personService;
  private ManagementService managementService;

  @Override
  public void syncAll() {
    logger.info("Admin sync: starting full resync (deleteAll + loadModel)");
    AuthenticationUtil.runAs(
      new AuthenticationUtil.RunAsWork<Void>() {
        public Void doWork() {
          circabcService.resyncAll();
          return null;
        }
      },
      AuthenticationUtil.getSystemUserName()
    );
    logger.info("Admin sync: full resync completed");
  }

  @Override
  public void syncUsers() {
    logger.info("Admin sync: starting users resync");
    AuthenticationUtil.runAs(
      new AuthenticationUtil.RunAsWork<Void>() {
        public Void doWork() {
          circabcService.resyncUsers();
          return null;
        }
      },
      AuthenticationUtil.getSystemUserName()
    );
    logger.info("Admin sync: users resync completed");
  }

  @Override
  public void syncGroup(String groupId) {
    logger.info("Admin sync: starting resync for group " + groupId);
    final NodeRef igNodeRef = Converter.createNodeRefFromId(groupId);
    AuthenticationUtil.runAs(
      new AuthenticationUtil.RunAsWork<Void>() {
        public Void doWork() {
          circabcService.resyncInterestGroup(igNodeRef);
          return null;
        }
      },
      AuthenticationUtil.getSystemUserName()
    );
    logger.info("Admin sync: resync completed for group " + groupId);
  }

  @Override
  public void syncCategory(String categoryId) {
    logger.info("Admin sync: starting resync for category " + categoryId);
    final NodeRef catNodeRef = Converter.createNodeRefFromId(categoryId);
    AuthenticationUtil.runAs(
      new AuthenticationUtil.RunAsWork<Void>() {
        public Void doWork() {
          circabcService.resyncCategory(catNodeRef);
          return null;
        }
      },
      AuthenticationUtil.getSystemUserName()
    );
    logger.info("Admin sync: resync completed for category " + categoryId);
  }

  @Override
  public void syncAdmins() {
    logger.info("Admin sync: starting admins resync");
    AuthenticationUtil.runAs(
      new AuthenticationUtil.RunAsWork<Void>() {
        public Void doWork() {
          circabcService.resyncCircabcAdmins();
          return null;
        }
      },
      AuthenticationUtil.getSystemUserName()
    );
    logger.info("Admin sync: admins resync completed");
  }

  @Override
  public Map<String, Object> getSyncStatus() {
    return AuthenticationUtil.runAs(
      new AuthenticationUtil.RunAsWork<Map<String, Object>>() {
        public Map<String, Object> doWork() {
          return buildSyncStatus();
        }
      },
      AuthenticationUtil.getSystemUserName()
    );
  }

  private Map<String, Object> buildSyncStatus() {
    Map<String, Object> status = new LinkedHashMap<>();

    // Count users in Alfresco
    int alfUserCount = personService.getAllPeople().size();
    // Count users in CBC
    List<String> cbcUserNodeRefs = circabcDaoService.selectAllUserNodeRef();
    int cbcUserCount = cbcUserNodeRefs != null ? cbcUserNodeRefs.size() : 0;

    Map<String, Object> users = new LinkedHashMap<>();
    users.put("alfrescoCount", alfUserCount);
    users.put("cbcCount", cbcUserCount);
    users.put("delta", alfUserCount - cbcUserCount);
    status.put("users", users);

    // Count Interest Groups in CBC
    List<?> cbcGroups = circabcDaoService.getAllInterestGroups();
    int cbcGroupCount = cbcGroups != null ? cbcGroups.size() : 0;

    // Count Interest Groups in Alfresco via management service
    int alfGroupCount = 0;
    try {
      Map<NodeRef, List<NodeRef>> categoryMap =
        managementService.getCategoryMap();
      for (NodeRef catRef : categoryMap.keySet()) {
        List<org.alfresco.service.cmr.repository.ChildAssociationRef> children =
          nodeService.getChildAssocs(catRef);
        for (org.alfresco.service.cmr.repository.ChildAssociationRef child : children) {
          if (
            nodeService.hasAspect(
              child.getChildRef(),
              eu.cec.digit.circabc.model.CircabcModel.ASPECT_IGROOT
            )
          ) {
            alfGroupCount++;
          }
        }
      }
    } catch (Exception e) {
      logger.error("Error counting Alfresco IGs for sync status", e);
      alfGroupCount = -1;
    }

    Map<String, Object> groups = new LinkedHashMap<>();
    groups.put("alfrescoCount", alfGroupCount);
    groups.put("cbcCount", cbcGroupCount);
    groups.put(
      "delta",
      alfGroupCount >= 0 ? alfGroupCount - cbcGroupCount : "unknown"
    );
    status.put("interestGroups", groups);

    // Sync enabled flag
    status.put("syncEnabled", circabcService.syncEnabled());
    status.put("readFromDatabase", circabcService.readFromDatabase());

    return status;
  }

  // Setters for Spring DI

  public void setCircabcService(CircabcService circabcService) {
    this.circabcService = circabcService;
  }

  public void setCircabcDaoService(CircabcDaoServiceImpl circabcDaoService) {
    this.circabcDaoService = circabcDaoService;
  }

  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  public void setPersonService(PersonService personService) {
    this.personService = personService;
  }

  public void setManagementService(ManagementService managementService) {
    this.managementService = managementService;
  }
}
