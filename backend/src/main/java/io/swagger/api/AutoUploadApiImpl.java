package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.service.auto.upload.AutoUploadManagementService;
import eu.europa.ec.digit.circabc.rest.service.ftp.FtpDestinationValidator;
import io.swagger.exception.SwaggerRuntimeException;
import io.swagger.model.Configuration;
import io.swagger.model.PagedAutoUploadConfiguration;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link AutoUploadApi}, providing the business logic
 * for managing "auto upload" configurations of an Interest Group (IG).
 *
 * <p>An auto-upload configuration describes how content is periodically fetched
 * from a remote FTP source and uploaded into a target location (file or parent
 * folder) within the Alfresco repository, according to a schedule expressed as
 * a cron expression. This class delegates persistence operations to the
 * {@link AutoUploadManagementService} and uses the Alfresco {@link NodeService}
 * to resolve and validate node references and to build human-readable paths and
 * titles for the configured nodes.</p>
 *
 * @author schwerr
 */
public class AutoUploadApiImpl implements AutoUploadApi {

  /** Prefix identifying node references stored in the Alfresco workspace SpacesStore. */
  public static final String WORKSPACE_SPACES_STORE =
    "workspace://SpacesStore/";
  /** Reusable message suffix appended when a supplied node reference is invalid. */
  private static final String INVALID_NODE_REF_MSG =
    "' must be a valid node reference.";
  /** Logger for this class. */
  private static final Log logger = LogFactory.getLog(AutoUploadApiImpl.class);

  /** Service handling persistence and retrieval of auto-upload configurations. */
  @Autowired
  private AutoUploadManagementService autoUploadManagementService;

  /** Alfresco node service used to resolve, validate and read repository nodes. */
  @Autowired
  private NodeService nodeService;

  /** Shared helper used to resolve the Interest Group that contains a node. */
  @Autowired
  private ApiToolBox apiToolBox;

  /**
   * Retrieves a paged list of auto-upload configurations for the given Interest
   * Group.
   *
   * <p>For each configuration, the stored file and parent node references are
   * replaced with user-friendly values when the nodes still exist: the file
   * reference is replaced by the file name and the parent reference by a
   * readable breadcrumb-style path. Paging is applied on the resulting list;
   * an {@code amount} of {@code 0} returns all items.</p>
   *
   * @param igId the node id of the Interest Group whose configurations are listed
   * @param startItem the zero-based index of the first configuration to return
   * @param amount the maximum number of configurations to return, or {@code 0}
   *               to return all of them
   * @return a {@link PagedAutoUploadConfiguration} holding the requested page of
   *         configurations together with the total result size
   * @throws IllegalArgumentException if no IG exists for the given {@code igId}
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
            nodeService
              .getProperty(igNodeRef, ContentModel.PROP_NAME)
              .toString(),
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
   * Resolves the {@link NodeRef} for the given Interest Group id and verifies it
   * exists in the repository.
   *
   * @param id the node id of the Interest Group
   * @return the resolved node reference
   * @throws IllegalArgumentException if no node exists for the given id
   */
  private NodeRef getIGNodeRef(String id) {
    NodeRef igNodeRef = Converter.createNodeRefFromId(id);

    if (
      !nodeService.exists(igNodeRef) ||
      !nodeService.hasAspect(igNodeRef, CircabcModel.ASPECT_IGROOT)
    ) {
      throw new IllegalArgumentException(
        "The IG with id '" + id + "' does not exist."
      );
    }

    return igNodeRef;
  }

  /**
   * Verifies that the given node belongs to the authorized Interest Group, i.e. its enclosing IG
   * root (resolved via {@link ApiToolBox#getCurrentInterestGroup(NodeRef)}) is {@code igNodeRef},
   * rejecting the request with {@link AccessDeniedException} otherwise.
   *
   * @param igNodeRef the authorized Interest Group root node
   * @param nodeRef the node to validate
   */
  private void requireNodeInInterestGroup(NodeRef igNodeRef, NodeRef nodeRef) {
    NodeRef nodeInterestGroup = apiToolBox.getCurrentInterestGroup(nodeRef);
    if (nodeInterestGroup == null || !igNodeRef.equals(nodeInterestGroup)) {
      throw new AccessDeniedException(
        "The selected node does not belong to the authorized IG."
      );
    }
  }

  /**
   * Verifies that the given auto-upload configuration belongs to the authorized Interest Group,
   * rejecting the request with {@link AccessDeniedException} otherwise.
   *
   * @param igNodeRef the authorized Interest Group root node
   * @param configuration the configuration to validate
   */
  private void requireConfigurationInInterestGroup(
    NodeRef igNodeRef,
    Configuration configuration
  ) {
    if (configuration == null) {
      throw new IllegalArgumentException(
        "The auto-upload configuration could not be found."
      );
    }

    String igName = configuration.getIgName();
    NodeRef configurationIgNodeRef = null;
    if (igName != null && !igName.isEmpty()) {
      try {
        configurationIgNodeRef = Converter.createNodeRefFromId(
          Converter.extractNodeRefId(igName)
        );
      } catch (MalformedNodeRefException e) {
        configurationIgNodeRef = null;
      }
    }

    if (
      configurationIgNodeRef == null ||
      !igNodeRef.equals(configurationIgNodeRef)
    ) {
      throw new AccessDeniedException(
        "The auto-upload configuration does not belong to the authorized IG."
      );
    }
  }

  /**
   * Loads the auto-upload configuration by id and verifies it belongs to the authorized Interest
   * Group.
   *
   * @param igNodeRef the authorized Interest Group root node
   * @param configurationId the configuration id
   * @return the validated configuration
   * @throws SQLException if the configuration cannot be read
   */
  private Configuration getConfigurationForInterestGroup(
    NodeRef igNodeRef,
    long configurationId
  ) throws SQLException {
    Configuration configuration =
      autoUploadManagementService.getConfigurationById((int) configurationId);
    requireConfigurationInInterestGroup(igNodeRef, configuration);
    return configuration;
  }

  /**
   * Builds a human-readable breadcrumb-style path (segments separated by
   * {@code " > "}) for the given content node by walking up its parent
   * associations until a library or information root is reached.
   *
   * <p>Each segment uses the node title when available, falling back to the node
   * name otherwise. When the starting node is a folder, its own segment is
   * included; for other node types the walk starts at the parent.</p>
   *
   * @param nodeRef the content node whose path is resolved
   * @return the assembled path string
   */
  private String getPathFromContentNode(NodeRef nodeRef) {
    StringBuilder path = new StringBuilder();
    NodeRef tmpNodeRef = nodeRef;
    boolean firstLoop = true;

    boolean done = false;
    while (!done) {
      List<ChildAssociationRef> parent = nodeService.getParentAssocs(
        tmpNodeRef
      );
      if (parent.isEmpty()) {
        done = true;
        continue;
      }

      if (
        !firstLoop ||
        !nodeService.getType(nodeRef).equals(ContentModel.TYPE_FOLDER)
      ) {
        tmpNodeRef = parent.get(0).getParentRef();
      }

      String title = resolveNodeTitle(tmpNodeRef);
      boolean isRoot =
        nodeService.hasAspect(
          tmpNodeRef,
          CircabcModel.ASPECT_INFORMATION_ROOT
        ) ||
        nodeService.hasAspect(tmpNodeRef, CircabcModel.ASPECT_LIBRARY_ROOT);

      String segment = title.isEmpty()
        ? nodeService.getProperty(tmpNodeRef, ContentModel.PROP_NAME).toString()
        : title;

      if (firstLoop) {
        path = new StringBuilder(segment);
      } else {
        path.insert(0, segment + " > ");
      }

      if (isRoot) {
        done = true;
      }

      firstLoop = false;
    }

    return path.toString();
  }

  /**
   * Returns the title of the given node, handling both plain {@link String} and
   * {@link MLText} title properties.
   *
   * @param nodeRef the node whose title is resolved
   * @return the node title, or an empty string when no usable title is present
   */
  private String resolveNodeTitle(NodeRef nodeRef) {
    Serializable titleSerialized = nodeService.getProperty(
      nodeRef,
      ContentModel.PROP_TITLE
    );
    if (titleSerialized instanceof String s) {
      return s;
    } else if (titleSerialized instanceof MLText mlText) {
      return mlText.getDefaultValue();
    }
    return "";
  }

  /**
   * Deletes the auto-upload configuration identified by the given id, provided it belongs to the
   * authorized Interest Group.
   *
   * @param igId the identifier of the authorized Interest Group
   * @param configurationId the id of the configuration to remove
   * @throws SwaggerRuntimeException if the deletion fails at the persistence layer
   * @see io.swagger.api.AutoUploadApi#removeAutoUploadEntry(java.lang.String, long)
   */
  @Override
  public void removeAutoUploadEntry(String igId, long configurationId) {
    try {
      NodeRef igNodeRef = getIGNodeRef(igId);
      Configuration conf = getConfigurationForInterestGroup(
        igNodeRef,
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
   * Enables or disables the auto-upload configuration identified by the given id
   * by updating its status flag, provided it belongs to the authorized Interest Group.
   *
   * @param igId the identifier of the authorized Interest Group
   * @param configurationId the id of the configuration to toggle
   * @param enable {@code true} to enable the configuration, {@code false} to
   *               disable it
   * @throws SwaggerRuntimeException if reading or updating the configuration
   *         fails at the persistence layer
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
   * Retrieves the auto-upload configuration associated with the given node, provided both the node
   * and the returned configuration belong to the authorized Interest Group.
   *
   * @param igId the identifier of the authorized Interest Group
   * @param nodeId the node id whose auto-upload configuration is requested
   * @return the matching {@link Configuration}
   * @throws IllegalArgumentException if no node exists for the given id
   * @throws SwaggerRuntimeException if the lookup fails at the persistence layer
   * @see io.swagger.api.AutoUploadApi#getAutoUploadEntry(java.lang.String, java.lang.String)
   */
  @Override
  public Configuration getAutoUploadEntry(String igId, String nodeId) {
    NodeRef igNodeRef = getIGNodeRef(igId);
    NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);

    if (!nodeService.exists(nodeRef)) {
      throw new IllegalArgumentException(
        "The node with id " + nodeId + " could not be found."
      );
    }

    requireNodeInInterestGroup(igNodeRef, nodeRef);

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
   * Creates or updates an auto-upload configuration from its JSON representation, bound to the
   * authorized Interest Group.
   *
   * <p>The JSON body is parsed and validated into a {@link Configuration}. When
   * the parsed configuration has an id of {@code 0} a new configuration is
   * registered, otherwise the existing configuration is updated after verifying it belongs to the
   * authorized Interest Group.</p>
   *
   * @param igId the identifier of the authorized Interest Group
   * @param autoUploadConfigurationJson the JSON body describing the configuration
   *        to add or update
   * @throws IllegalArgumentException if the body is empty or contains invalid data
   * @throws SwaggerRuntimeException if persisting the configuration fails
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

  /**
   * Parses and validates the JSON body of an auto-upload configuration request
   * into a fully populated {@link Configuration}.
   *
   * <p>Validation covers the IG reference, the target file or parent node, the
   * FTP connection settings, the schedule (day/hour translated into a cron
   * expression) and the remaining boolean and identifier fields. The resulting
   * configuration is marked with an active status.</p>
   *
   * @param autoUploadConfigurationJson the raw JSON body
   * @return the parsed and validated configuration
   * @throws IllegalArgumentException if the body is empty, malformed, or
   *         contains invalid field values
   */
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

    String igName = parseAndValidateNodeRef(
      json,
      "igName",
      CircabcModel.ASPECT_IGROOT,
      "'igName' must be a valid IG id"
    );
    NodeRef igNameNodeRef = Converter.createNodeRefFromId(
      Converter.extractNodeRefId(igName)
    );
    if (!igNodeRef.equals(igNameNodeRef)) {
      throw new AccessDeniedException(
        "'igName' does not belong to the authorized IG."
      );
    }
    configuration.setIgName(igName);
    parseFileOrParentId(igNodeRef, json, configuration);
    parseFtpSettings(json, configuration);
    parseDateRestriction(json, configuration);
    parseBooleanAndRemainingFields(json, configuration);

    configuration.setStatus(1);

    return configuration;
  }

  /**
   * Reads a node-reference field from the JSON body, normalises it with the
   * workspace prefix, and validates that the referenced node exists and (when
   * required) carries a given aspect.
   *
   * @param json the JSON body
   * @param field the name of the field to read
   * @param requiredAspect an aspect the node must have, or {@code null} to skip
   *        the aspect check
   * @param errorMsg the message used when the node is missing or lacks the aspect
   * @return the normalised node reference value
   * @throws IllegalArgumentException if the field is missing/empty, the node is
   *         invalid, or the required aspect is absent
   */
  private String parseAndValidateNodeRef(
    JSONObject json,
    String field,
    org.alfresco.service.namespace.QName requiredAspect,
    String errorMsg
  ) {
    String value = (String) json.get(field);
    if (value == null || value.isEmpty()) {
      throw new IllegalArgumentException(
        "The interest group id cannot be null or empty (" + field + ")"
      );
    }
    if (!value.startsWith(WORKSPACE_SPACES_STORE)) {
      value = WORKSPACE_SPACES_STORE + value;
    }
    try {
      NodeRef nodeRef = Converter.createNodeRefFromId(
        Converter.extractNodeRefId(value)
      );
      if (
        !nodeService.exists(nodeRef) ||
        (requiredAspect != null &&
          !nodeService.hasAspect(nodeRef, requiredAspect))
      ) {
        throw new IllegalArgumentException(errorMsg);
      }
    } catch (MalformedNodeRefException e) {
      throw new IllegalArgumentException("'" + field + INVALID_NODE_REF_MSG, e);
    }
    return value;
  }

  /**
   * Determines and validates the upload target of the configuration from the
   * JSON body.
   *
   * <p>When a {@code fileId} is provided it is validated and the configuration's
   * parent is set to that file's parent folder. Otherwise a {@code parentId}
   * must be provided and validated. The file node reference is set to the
   * supplied file id, or {@code null} when only a parent is given.</p>
   *
   * @param json the JSON body
   * @param configuration the configuration to populate
   * @throws IllegalArgumentException if neither a valid file nor a valid parent
   *         reference can be resolved
   */
  private void parseFileOrParentId(
    NodeRef igNodeRef,
    JSONObject json,
    Configuration configuration
  ) {
    String fileId = (String) json.get("fileId");

    if (fileId != null && !fileId.isEmpty()) {
      fileId = ensureWorkspacePrefix(fileId);
      NodeRef fileNodeRef = validateNodeRef(fileId, "fileId");
      requireNodeInInterestGroup(igNodeRef, fileNodeRef);
      NodeRef parentNodeRef = nodeService
        .getParentAssocs(fileNodeRef)
        .get(0)
        .getParentRef();
      requireNodeInInterestGroup(igNodeRef, parentNodeRef);
      configuration.setParentNodeRef(parentNodeRef.toString());
    } else {
      fileId = null;
      String parentId = (String) json.get("parentId");
      if (parentId == null || parentId.isEmpty()) {
        throw new IllegalArgumentException(
          "The parent id cannot be null or empty (parentId)"
        );
      }
      parentId = ensureWorkspacePrefix(parentId);
      NodeRef parentNodeRef = validateNodeRef(parentId, "parentId");
      requireNodeInInterestGroup(igNodeRef, parentNodeRef);
      configuration.setParentNodeRef(parentId);
    }

    configuration.setFileNodeRef(fileId);
  }

  /**
   * Ensures the given node id is prefixed with the workspace SpacesStore prefix,
   * adding it when absent.
   *
   * @param id the node id, with or without the workspace prefix
   * @return the id guaranteed to start with {@link #WORKSPACE_SPACES_STORE}
   */
  private String ensureWorkspacePrefix(String id) {
    return id.startsWith(WORKSPACE_SPACES_STORE)
      ? id
      : WORKSPACE_SPACES_STORE + id;
  }

  /**
   * Converts the given id into a {@link NodeRef} and verifies that the node
   * exists.
   *
   * @param id the node id to resolve
   * @param fieldName the name of the originating field, used in error messages
   * @return the resolved and existing node reference
   * @throws IllegalArgumentException if the id is malformed or the node does not
   *         exist
   */
  private NodeRef validateNodeRef(String id, String fieldName) {
    try {
      NodeRef nodeRef = Converter.createNodeRefFromId(
        Converter.extractNodeRefId(id)
      );
      if (!nodeService.exists(nodeRef)) {
        throw new IllegalArgumentException(
          "'" + fieldName + INVALID_NODE_REF_MSG
        );
      }
      return nodeRef;
    } catch (MalformedNodeRefException e) {
      throw new IllegalArgumentException(
        "'" + fieldName + INVALID_NODE_REF_MSG,
        e
      );
    }
  }

  /**
   * Reads and validates the FTP connection settings ({@code ftpHost},
   * {@code ftpPort}, {@code ftpPath}, {@code ftpUsername}, {@code ftpPassword})
   * from the JSON body and populates the configuration. Optional string fields
   * default to an empty string when absent.
   *
   * @param json the JSON body
   * @param configuration the configuration to populate
   * @throws IllegalArgumentException if the host is empty or the port is missing,
   *         non-numeric, or outside the range {@code [1..65535]}
   */
  private void parseFtpSettings(JSONObject json, Configuration configuration) {
    String ftpHost = (String) json.get("ftpHost");
    if (ftpHost == null || ftpHost.isEmpty()) {
      throw new IllegalArgumentException("'ftpHost' cannot be empty.");
    }
    configuration.setFtpHost(ftpHost);

    int ftpPort = parseIntField(
      json,
      "ftpPort",
      "Invalid 'ftpPort' format. Must be a number in the range [1..65535]"
    );
    if (ftpPort < 1 || ftpPort > 65535) {
      throw new IllegalArgumentException(
        "Invalid 'ftpPort'. Must be a number in the range [1..65535]"
      );
    }

    FtpDestinationValidator.validateAndResolveHost(ftpHost, ftpPort);

    configuration.setFtpPort(ftpPort);

    String ftpPath = (String) json.get("ftpPath");
    configuration.setFtpPath(ftpPath == null ? "" : ftpPath);
    String ftpUsername = (String) json.get("ftpUsername");
    configuration.setFtpUsername(ftpUsername == null ? "" : ftpUsername);
    String ftpPassword = (String) json.get("ftpPassword");
    configuration.setFtpPassword(ftpPassword == null ? "" : ftpPassword);
  }

  /**
   * Reads the schedule fields ({@code dayChoice} and {@code hourChoice}) from the
   * JSON body, validates their ranges, and stores the resulting cron expression
   * as the configuration's date restriction.
   *
   * <p>{@code dayChoice} accepts {@code -1} (every day) or {@code 1..7}
   * (Monday..Sunday); {@code hourChoice} accepts {@code -1} (every hour) or
   * {@code 0..23}.</p>
   *
   * @param json the JSON body
   * @param configuration the configuration to populate
   * @throws IllegalArgumentException if the day or hour values are out of range
   */
  private void parseDateRestriction(
    JSONObject json,
    Configuration configuration
  ) {
    int dayChoice = parseIntField(
      json,
      "dayChoice",
      "Invalid day format. Must be a number in the range [-1 = Every day, 1 = Monday .. 7 = Sunday]"
    );
    if (dayChoice < -1 || dayChoice > 7 || dayChoice == 0) {
      throw new IllegalArgumentException(
        "Invalid day. Must be a number in the range [-1 = Every day, 1 = Monday .. 7 = Sunday]"
      );
    }

    int hourChoice = parseIntField(
      json,
      "hourChoice",
      "Invalid hour format. Must be a number in the range [-1 = Every hour, 0..23]"
    );
    if (hourChoice < -1 || hourChoice > 23) {
      throw new IllegalArgumentException(
        "Invalid hour. Must be a number in the range [-1 = Every hour, 0..23]"
      );
    }

    configuration.setDateRestriction(
      buildCronExpression(String.valueOf(dayChoice), String.valueOf(hourChoice))
    );
  }

  /**
   * Reads an integer field from the JSON body, returning {@code 0} when the
   * field is absent.
   *
   * @param json the JSON body
   * @param field the name of the field to read
   * @param errorMsg the message used when the value cannot be parsed as an integer
   * @return the parsed integer value, or {@code 0} when the field is missing
   * @throws IllegalArgumentException if the value cannot be parsed as an integer
   */
  private int parseIntField(JSONObject json, String field, String errorMsg) {
    int value = 0;
    try {
      Object obj = json.get(field);
      if (obj != null) {
        value = Integer.parseInt(obj.toString().trim());
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(errorMsg, e);
    }
    return value;
  }

  /**
   * Reads the remaining boolean and identifier fields ({@code autoExtract},
   * {@code jobNotifications}, {@code emails}, {@code idConfiguration}) from the
   * JSON body and populates the configuration.
   *
   * @param json the JSON body
   * @param configuration the configuration to populate
   * @throws IllegalArgumentException if a boolean field is not a valid boolean
   *         or {@code idConfiguration} is not a valid number
   */
  private void parseBooleanAndRemainingFields(
    JSONObject json,
    Configuration configuration
  ) {
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

    String emails = (String) json.get("emails");
    configuration.setEmails(emails);

    long idConfiguration = 0;
    try {
      Object idConfigurationObj = json.get("idConfiguration");
      if (idConfigurationObj != null) {
        idConfiguration = Long.parseLong(idConfigurationObj.toString().trim());
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(
        "Invalid 'idConfiguration' format. Must be a number",
        e
      );
    }
    configuration.setIdConfiguration(idConfiguration);
  }

  /**
   * Builds a Quartz-style cron expression from the given day and hour choices.
   *
   * <p>A value of {@code -1} for either choice is translated into a wildcard
   * ({@code *}), meaning "every day" or "every hour" respectively; otherwise the
   * concrete value is used. The returned expression follows the pattern
   * {@code "* * <hour> ? * <day>"}.</p>
   *
   * @param dayChoice the day selector: {@code -1} (every day) or {@code 1..7}
   * @param hourChoice the hour selector: {@code -1} (every hour) or {@code 0..23}
   * @return the assembled cron expression
   * @throws IllegalArgumentException if either value is non-numeric or out of range
   */
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
}
