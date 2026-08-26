package io.swagger.api;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.config.auto.upload.Configuration;
import eu.cec.digit.circabc.service.config.auto.upload.AutoUploadManagementService;
import eu.cec.digit.circabc.service.ftp.FtpDestinationValidator;
import io.swagger.exception.SwaggerRuntimeException;
import io.swagger.model.PagedAutoUploadConfiguration;
import io.swagger.util.Converter;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * @author schwerr
 */
public class AutoUploadApiImpl implements AutoUploadApi {

  public static final String WORKSPACE_SPACES_STORE =
    "workspace://SpacesStore/";
  private static final Log logger = LogFactory.getLog(AutoUploadApiImpl.class);
  private AutoUploadManagementService autoUploadManagementService = null;
  private NodeService nodeService = null;

  /**
   * @see io.swagger.api.AutoUploadApi#getAutoUploadEntries(java.lang.String, int, int)
   */
  @Override
  public PagedAutoUploadConfiguration getAutoUploadEntries(
    String igId,
    int startItem,
    int amount
  ) {
    NodeRef igNodeRef = getIGNodeRef(igId);

    List<Configuration> listOfConfigurations = Collections.emptyList();

    try {
      listOfConfigurations = autoUploadManagementService.listConfigurations(
        igNodeRef.toString()
      );

      for (Configuration listOfConfiguration : listOfConfigurations) {
        requireConfigurationInInterestGroup(igNodeRef, listOfConfiguration);

        if (
          listOfConfiguration.getFileNodeRef() != null &&
          nodeService.exists(
            Converter.createNodeRefFromId(
              Converter.extractNodeRefId(listOfConfiguration.getFileNodeRef())
            )
          )
        ) {
          listOfConfiguration.setFileNodeRef(
            nodeService
              .getProperty(
                Converter.createNodeRefFromId(
                  Converter.extractNodeRefId(
                    listOfConfiguration.getFileNodeRef()
                  )
                ),
                ContentModel.PROP_NAME
              )
              .toString()
          );
        }
        if (
          listOfConfiguration.getParentNodeRef() != null &&
          nodeService.exists(
            Converter.createNodeRefFromId(
              Converter.extractNodeRefId(listOfConfiguration.getParentNodeRef())
            )
          )
        ) {
          listOfConfiguration.setParentNodeRef(
            getPathFromContentNode(
              Converter.createNodeRefFromId(
                Converter.extractNodeRefId(
                  listOfConfiguration.getParentNodeRef()
                )
              )
            )
          );
        }
      }
    } catch (SQLException e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Error during listing all auto upload configurations of IG: " +
          nodeService.getProperty(igNodeRef, ContentModel.PROP_NAME).toString(),
          e
        );
      }
    }

    int resultSize = listOfConfigurations.size();

    List<Configuration> pagedConfigurations;

    if (amount == 0) {
      // amount == 0 means that we want all items
      pagedConfigurations = listOfConfigurations;
    } else {
      pagedConfigurations = new ArrayList<>();

      int endItem = Math.min(startItem + amount, resultSize);

      for (int index = startItem; index < endItem; index++) {
        pagedConfigurations.add(listOfConfigurations.get(index));
      }
    }

    return new PagedAutoUploadConfiguration(pagedConfigurations, resultSize);
  }

  /**
   * Gets the nodeRef with the id of the given IG
   */
  private NodeRef getIGNodeRef(String id) {
    NodeRef igNodeRef = getExistingNodeRef(id, "IG id");

    if (!nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)) {
      throw new IllegalArgumentException(
        "The IG with id '" + id + "' does not exist."
      );
    }

    return igNodeRef;
  }

  private NodeRef getExistingNodeRef(String id, String selectorName) {
    NodeRef nodeRef = createNodeRef(id, selectorName);

    if (!nodeService.exists(nodeRef)) {
      throw new IllegalArgumentException(
        "The node selected by '" + selectorName + "' does not exist."
      );
    }

    return nodeRef;
  }

  private NodeRef createNodeRef(String id, String selectorName) {
    if (id == null || id.isEmpty()) {
      throw new IllegalArgumentException(
        "'" + selectorName + "' cannot be null or empty."
      );
    }

    String nodeId = id;
    if (id.startsWith(WORKSPACE_SPACES_STORE)) {
      nodeId = Converter.extractNodeRefId(id);
    } else if (id.contains("://")) {
      throw new IllegalArgumentException(
        "'" + selectorName + "' must reference the workspace store."
      );
    }

    try {
      return Converter.createNodeRefFromId(nodeId);
    } catch (MalformedNodeRefException e) {
      throw new IllegalArgumentException(
        "'" + selectorName + "' must be a valid node reference.",
        e
      );
    }
  }

  private void requireNodeInInterestGroup(
    NodeRef igNodeRef,
    NodeRef nodeRef,
    String selectorName
  ) {
    if (!isNodeInInterestGroup(igNodeRef, nodeRef)) {
      throw new AccessDeniedException(
        "The node selected by '" +
        selectorName +
        "' does not belong to the authorized IG."
      );
    }
  }

  private boolean isNodeInInterestGroup(NodeRef igNodeRef, NodeRef nodeRef) {
    ArrayDeque<NodeRef> pending = new ArrayDeque<>();
    Set<NodeRef> visited = new HashSet<>();
    pending.add(nodeRef);

    while (!pending.isEmpty()) {
      NodeRef current = pending.removeFirst();

      if (!visited.add(current)) {
        continue;
      }

      if (igNodeRef.equals(current)) {
        return true;
      }

      for (ChildAssociationRef parentAssoc : nodeService.getParentAssocs(
        current
      )) {
        pending.addLast(parentAssoc.getParentRef());
      }
    }

    return false;
  }

  private void requireConfigurationInInterestGroup(
    NodeRef igNodeRef,
    Configuration configuration
  ) {
    if (configuration == null) {
      throw new IllegalArgumentException(
        "The auto-upload configuration could not be found."
      );
    }

    NodeRef configurationIgNodeRef = getExistingNodeRef(
      configuration.getIgName(),
      "configuration IG"
    );

    if (!igNodeRef.equals(configurationIgNodeRef)) {
      throw new AccessDeniedException(
        "The auto-upload configuration does not belong to the authorized IG."
      );
    }

    requireStoredNodeInInterestGroup(
      igNodeRef,
      configuration.getFileNodeRef(),
      "fileId"
    );
    requireStoredNodeInInterestGroup(
      igNodeRef,
      configuration.getParentNodeRef(),
      "parentId"
    );
  }

  private void requireStoredNodeInInterestGroup(
    NodeRef igNodeRef,
    String nodeId,
    String selectorName
  ) {
    if (nodeId == null || nodeId.isEmpty()) {
      return;
    }

    NodeRef nodeRef = createNodeRef(nodeId, selectorName);
    if (nodeService.exists(nodeRef)) {
      requireNodeInInterestGroup(igNodeRef, nodeRef, selectorName);
    }
  }

  private Configuration getConfigurationForInterestGroup(
    NodeRef igNodeRef,
    long configurationId
  )
    throws SQLException {
    if (configurationId < 1 || configurationId > Integer.MAX_VALUE) {
      throw new IllegalArgumentException(
        "The auto-upload configuration id is invalid."
      );
    }

    Configuration configuration =
      autoUploadManagementService.getConfigurationById((int) configurationId);
    requireConfigurationInInterestGroup(igNodeRef, configuration);
    return configuration;
  }

  private String getPathFromContentNode(NodeRef nodeRef) {
    StringBuilder path = new StringBuilder();
    NodeRef tmpNodeRef = nodeRef;
    boolean stop = false;
    boolean firstLoop = true;

    while (!stop) {
      List<ChildAssociationRef> parent;
      parent = nodeService.getParentAssocs(tmpNodeRef);

      if (parent.isEmpty()) {
        stop = true;
      } else {
        if (
          !firstLoop ||
          !nodeService.getType(nodeRef).equals(ContentModel.TYPE_FOLDER)
        ) {
          tmpNodeRef = parent.get(0).getParentRef();
        }

        Serializable titleSerialized = nodeService.getProperty(
          tmpNodeRef,
          ContentModel.PROP_TITLE
        );
        String title = "";
        if (titleSerialized instanceof String) {
          title = (String) titleSerialized;
        } else if (titleSerialized instanceof MLText) {
          title = ((MLText) titleSerialized).getDefaultValue();
        }
        /*
         * reached library root
         */
        if (
          nodeService.hasAspect(
            tmpNodeRef,
            CircabcModel.ASPECT_INFORMATION_ROOT
          ) ||
          nodeService.hasAspect(tmpNodeRef, CircabcModel.ASPECT_LIBRARY_ROOT)
        ) {
          stop = true;
          if (firstLoop) {
            path = new StringBuilder(title);
          } else {
            path.insert(0, title + " > ");
          }
        } else if (title.isEmpty()) {
          path.insert(
            0,
            nodeService
              .getProperty(tmpNodeRef, ContentModel.PROP_NAME)
              .toString() +
            " > "
          );
        } else {
          if (firstLoop) {
            path = new StringBuilder(title);
          } else {
            path.insert(0, title + " > ");
          }
        }
      }

      firstLoop = false;
    }

    return path.toString();
  }

  /**
   * @see io.swagger.api.AutoUploadApi#removeAutoUploadEntry(java.lang.String, long)
   */
  @Override
  public void removeAutoUploadEntry(String igId, long configurationId) {
    try {
      Configuration conf = getConfigurationForInterestGroup(
        getIGNodeRef(igId),
        configurationId
      );
      autoUploadManagementService.deleteConfiguration(conf);
    } catch (SQLException e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Error when deleting an auto upload configuration, id: " +
          configurationId,
          e
        );
      }
      throw new SwaggerRuntimeException(e);
    }
  }

  /**
   * @see io.swagger.api.AutoUploadApi#toggleAutoUploadEntry(java.lang.String, long, boolean)
   */
  @Override
  public void toggleAutoUploadEntry(
    String igId,
    long configurationId,
    boolean enable
  ) {
    try {
      Configuration conf = getConfigurationForInterestGroup(
        getIGNodeRef(igId),
        configurationId
      );
      conf.setStatus(enable ? 1 : 0);
      autoUploadManagementService.updateConfiguration(conf);
    } catch (SQLException e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Error during toggling the status of an auto upload configuration, id: " +
          configurationId,
          e
        );
      }
      throw new SwaggerRuntimeException(e);
    }
  }

  /**
   * @see io.swagger.api.AutoUploadApi#getAutoUploadEntry(java.lang.String, java.lang.String)
   */
  @Override
  public Configuration getAutoUploadEntry(String igId, String nodeId) {
    NodeRef igNodeRef = getIGNodeRef(igId);
    NodeRef nodeRef = getExistingNodeRef(nodeId, "nodeId");
    requireNodeInInterestGroup(igNodeRef, nodeRef, "nodeId");

    try {
      Configuration configuration =
        autoUploadManagementService.getConfigurationByNodeRef(nodeRef);
      if (configuration != null) {
        requireConfigurationInInterestGroup(igNodeRef, configuration);
      }
      return configuration;
    } catch (SQLException e) {
      if (logger.isErrorEnabled()) {
        logger.error(
          "Error when getting an auto upload configuration, id: " + nodeId,
          e
        );
      }
      throw new SwaggerRuntimeException(e);
    }
  }

  /**
   * @see io.swagger.api.AutoUploadApi#addAutoUploadEntry(java.lang.String, java.lang.String)
   */
  @Override
  public void addAutoUploadEntry(
    String igId,
    String autoUploadConfigurationJson
  ) {
    NodeRef igNodeRef = getIGNodeRef(igId);
    Configuration autoUploadConfiguration = parseBodyJSON(
      igNodeRef,
      autoUploadConfigurationJson
    );

    try {
      if (autoUploadConfiguration.getIdConfiguration() == 0) {
        autoUploadManagementService.registerConfiguration(
          autoUploadConfiguration
        );
      } else {
        getConfigurationForInterestGroup(
          igNodeRef,
          autoUploadConfiguration.getIdConfiguration()
        );
        autoUploadManagementService.updateConfiguration(
          autoUploadConfiguration
        );
      }
    } catch (SQLException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error during an auto upload configuration update.", e);
      }
      throw new SwaggerRuntimeException(e);
    }
  }

  private Configuration parseBodyJSON(
    NodeRef igNodeRef,
    String autoUploadConfigurationJson
  ) {
    if (
      autoUploadConfigurationJson == null ||
      autoUploadConfigurationJson.isEmpty()
    ) {
      throw new IllegalArgumentException(
        "The body (auto-upload configuration) cannot be empty. It must contain the configuration data to add/update."
      );
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Provided JSON String: " + autoUploadConfigurationJson);
    }

    JSONParser parser = new JSONParser();

    JSONObject json;

    try {
      json = (JSONObject) parser.parse(autoUploadConfigurationJson);
    } catch (ParseException e) {
      throw new IllegalArgumentException(
        "Error when parsing the body (auto-upload configuration).",
        e
      );
    }

    Configuration configuration = new Configuration();

    // igName
    String igName = (String) json.get("igName");

    if (igName == null || igName.isEmpty()) {
      throw new IllegalArgumentException(
        "The interest group id cannot be null or empty (igName)"
      );
    }

    NodeRef igNameNodeRef = getExistingNodeRef(igName, "igName");
    if (!nodeService.hasAspect(igNameNodeRef, CircabcModel.ASPECT_IGROOT)) {
      throw new IllegalArgumentException("'igName' must be a valid IG id");
    }

    if (!igNodeRef.equals(igNameNodeRef)) {
      throw new AccessDeniedException(
        "'igName' does not belong to the authorized IG."
      );
    }

    configuration.setIgName(igNameNodeRef.toString());

    // fileId
    String fileId = (String) json.get("fileId");

    if (fileId != null && !fileId.isEmpty()) {
      // in case the configuration is defined at the file level

      NodeRef fileNodeRef = getExistingNodeRef(fileId, "fileId");
      requireNodeInInterestGroup(igNodeRef, fileNodeRef, "fileId");

      List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(
        fileNodeRef
      );
      if (parentAssocs.isEmpty()) {
        throw new IllegalArgumentException(
          "'fileId' must have a valid parent node."
        );
      }

      NodeRef parentNodeRef = parentAssocs.get(0).getParentRef();
      requireNodeInInterestGroup(igNodeRef, parentNodeRef, "parentId");
      configuration.setParentNodeRef(parentNodeRef.toString());
      fileId = fileNodeRef.toString();
    } else {
      // in case the configuration is defined at the admin level
      fileId = null;

      // parentId
      String parentId = (String) json.get("parentId");

      if (parentId == null || parentId.isEmpty()) {
        throw new IllegalArgumentException(
          "The parent id cannot be null or empty (parentId)"
        );
      }

      NodeRef parentNodeRef = getExistingNodeRef(parentId, "parentId");
      requireNodeInInterestGroup(igNodeRef, parentNodeRef, "parentId");
      configuration.setParentNodeRef(parentNodeRef.toString());
    }

    configuration.setFileNodeRef(fileId);

    // ftp host
    String ftpHost = (String) json.get("ftpHost");
    if (ftpHost == null || ftpHost.isEmpty()) {
      throw new IllegalArgumentException("'ftpHost' cannot be empty.");
    }
    configuration.setFtpHost(ftpHost);

    int ftpPort = 0;
    try {
      Object ftpPortObj = json.get("ftpPort");
      if (ftpPortObj != null) {
        ftpPort = Integer.parseInt(ftpPortObj.toString().trim());
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(
        "Invalid 'ftpPort' format. Must be a number in the range [1..65535]",
        e
      );
    }
    if (ftpPort < 1 || ftpPort > 65535) {
      throw new IllegalArgumentException(
        "Invalid 'ftpPort'. Must be a number in the range [1..65535]"
      );
    }

    FtpDestinationValidator.validateAndResolveHost(ftpHost, ftpPort);

    configuration.setFtpPort(ftpPort);

    // path, username and password
    String ftpPath = (String) json.get("ftpPath");
    configuration.setFtpPath(ftpPath == null ? "" : ftpPath);
    String ftpUsername = (String) json.get("ftpUsername");
    configuration.setFtpUsername(ftpUsername == null ? "" : ftpUsername);
    String ftpPassword = (String) json.get("ftpPassword");
    configuration.setFtpPassword(ftpPassword == null ? "" : ftpPassword);

    // date restriction
    int dayChoice = 0;
    try {
      Object dayChoiceObj = json.get("dayChoice");
      if (dayChoiceObj != null) {
        dayChoice = Integer.parseInt(dayChoiceObj.toString().trim());
      }
    } catch (Exception e1) {
      throw new IllegalArgumentException(
        "Invalid day format. Must be a number in the range [-1 = Every day, 1 = Monday .. 7 = Sunday]",
        e1
      );
    }
    if (dayChoice < -1 || dayChoice > 7 || dayChoice == 0) {
      throw new IllegalArgumentException(
        "Invalid day. Must be a number in the range [-1 = Every day, 1 = Monday .. 7 = Sunday]"
      );
    }
    int hourChoice = -2;
    try {
      Object hourChoiceObj = json.get("hourChoice");
      if (hourChoiceObj != null) {
        hourChoice = Integer.parseInt(hourChoiceObj.toString().trim());
      }
    } catch (Exception e1) {
      throw new IllegalArgumentException(
        "Invalid hour format. Must be a number in the range [-1 = Every hour, 0..23]",
        e1
      );
    }
    if (hourChoice < -1 || hourChoice > 23) {
      throw new IllegalArgumentException(
        "Invalid hour. Must be a number in the range [-1 = Every hour, 0..23]"
      );
    }

    configuration.setDateRestriction(
      buildCronExpression(String.valueOf(dayChoice), String.valueOf(hourChoice))
    );

    // auto extract zip
    boolean autoExtract;
    try {
      autoExtract = (Boolean) json.get("autoExtract");
    } catch (Exception e) {
      throw new IllegalArgumentException(
        "'autoExtract' must be 'true' or 'false'.",
        e
      );
    }
    configuration.setAutoExtract(autoExtract);

    // job notifications
    boolean jobNotifications;
    try {
      jobNotifications = (Boolean) json.get("jobNotifications");
    } catch (Exception e) {
      throw new IllegalArgumentException(
        "'jobNotifications' must be 'true' or 'false'.",
        e
      );
    }
    configuration.setJobNotifications(jobNotifications);

    // emails
    String emails = (String) json.get("emails");
    configuration.setEmails(emails);

    // idConfiguration
    long idConfiguration = 0;
    try {
      Object idConfigurationObj = json.get("idConfiguration");
      if (idConfigurationObj != null) {
        idConfiguration = Long.parseLong(idConfigurationObj.toString().trim());
      }
    } catch (Exception e1) {
      throw new IllegalArgumentException(
        "Invalid 'idConfiguration' format. Must be a number",
        e1
      );
    }
    configuration.setIdConfiguration(idConfiguration);

    configuration.setStatus(0);

    return configuration;
  }

  private String buildCronExpression(String dayChoice, String hourChoice) {
    String cronHour = "*";
    String cronDay = "*";

    try {
      int dayChoiceInt = Integer.parseInt(dayChoice);

      if (dayChoiceInt != -1) {
        if (dayChoiceInt < 1 || dayChoiceInt > 7) {
          throw new IllegalArgumentException(
            "'dayChoice' must be a number in the range [1..7]"
          );
        }

        cronDay = dayChoice;
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
        "'dayChoice' must be a number in the range [1..7]",
        e
      );
    }

    try {
      int hourChoiceInt = Integer.parseInt(hourChoice);

      if (hourChoiceInt != -1) {
        if (hourChoiceInt < 0 || hourChoiceInt > 23) {
          throw new IllegalArgumentException(
            "'hourChoice' must be a number in the range [0..23]"
          );
        }

        cronHour = hourChoice;
      }
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
        "'hourChoice' must be a number in the range [0..23]",
        e
      );
    }

    return "* * " + cronHour + " ? * " + cronDay;
  }

  /**
   * @param autoUploadManagementService the autoUploadManagementService to set
   */
  public void setAutoUploadManagementService(
    AutoUploadManagementService autoUploadManagementService
  ) {
    this.autoUploadManagementService = autoUploadManagementService;
  }

  /**
   * @param nodeService the nodeService to set
   */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }
}
