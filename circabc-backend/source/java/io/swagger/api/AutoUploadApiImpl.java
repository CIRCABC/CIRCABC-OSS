package io.swagger.api;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.config.auto.upload.Configuration;
import eu.cec.digit.circabc.service.config.auto.upload.AutoUploadManagementService;
import io.swagger.exception.SwaggerRuntimeException;
import io.swagger.model.PagedAutoUploadConfiguration;
import io.swagger.util.Converter;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author schwerr
 */
public class AutoUploadApiImpl implements AutoUploadApi {

    public static final String WORKSPACE_SPACES_STORE = "workspace://SpacesStore/";
    private static final Log logger = LogFactory.getLog(AutoUploadApiImpl.class);
    private AutoUploadManagementService autoUploadManagementService = null;
    private NodeService nodeService = null;

    /**
     * @see io.swagger.api.AutoUploadApi#getAutoUploadEntries(java.lang.String, int, int)
     */
    @Override
    public PagedAutoUploadConfiguration getAutoUploadEntries(String igId, int startItem, int amount) {

        NodeRef igNodeRef = getIGNodeRef(igId);

        List<Configuration> listOfConfigurations = Collections.emptyList();

        try {

            listOfConfigurations = autoUploadManagementService.listConfigurations(igNodeRef.toString());

            for (Configuration listOfConfiguration : listOfConfigurations) {
                if (listOfConfiguration.getFileNodeRef() != null
                        && nodeService.exists(
                        Converter.createNodeRefFromId(
                                Converter.extractNodeRefId(listOfConfiguration.getFileNodeRef())))) {
                    listOfConfiguration.setFileNodeRef(
                            nodeService
                                    .getProperty(
                                            Converter.createNodeRefFromId(
                                                    Converter.extractNodeRefId(listOfConfiguration.getFileNodeRef())),
                                            ContentModel.PROP_NAME)
                                    .toString());
                }
                if (listOfConfiguration.getParentNodeRef() != null
                        && nodeService.exists(
                        Converter.createNodeRefFromId(
                                Converter.extractNodeRefId(listOfConfiguration.getParentNodeRef())))) {
                    listOfConfiguration.setParentNodeRef(
                            getPathFromContentNode(
                                    Converter.createNodeRefFromId(
                                            Converter.extractNodeRefId(listOfConfiguration.getParentNodeRef()))));
                }
            }
        } catch (SQLException e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error during listing all auto upload configurations of IG: "
                                + nodeService.getProperty(igNodeRef, ContentModel.PROP_NAME).toString(),
                        e);
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

        NodeRef igNodeRef = Converter.createNodeRefFromId(id);

        if (!nodeService.exists(igNodeRef)) {
            throw new IllegalArgumentException("The IG with id '" + id + "' does not exist.");
        }

        return igNodeRef;
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

                if (!firstLoop || !nodeService.getType(nodeRef).equals(ContentModel.TYPE_FOLDER)) {
                    tmpNodeRef = parent.get(0).getParentRef();
                }

                Serializable titleSerialized = nodeService.getProperty(tmpNodeRef, ContentModel.PROP_TITLE);
                String title = "";
                if (titleSerialized instanceof String) {
                    title = (String) titleSerialized;
                } else if (titleSerialized instanceof MLText) {
                    title = ((MLText) titleSerialized).getDefaultValue();
                }
                /*
                 * reached library root
                 */
                if (nodeService.hasAspect(tmpNodeRef, CircabcModel.ASPECT_INFORMATION_ROOT)
                        || nodeService.hasAspect(tmpNodeRef, CircabcModel.ASPECT_LIBRARY_ROOT)) {
                    stop = true;
                    if (firstLoop) {
                        path = new StringBuilder(title);
                    } else {
                        path.insert(0, title + " > ");
                    }

                } else if (title.isEmpty()) {

                    path.insert(
                            0, nodeService.getProperty(tmpNodeRef, ContentModel.PROP_NAME).toString() + " > ");
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
     * @see io.swagger.api.AutoUploadApi#removeAutoUploadEntry(long)
     */
    @Override
    public void removeAutoUploadEntry(long configurationId) {

        Configuration conf = new Configuration();
        conf.setIdConfiguration(configurationId);

        try {
            autoUploadManagementService.deleteConfiguration(conf);
        } catch (SQLException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when deleting an auto upload configuration, id: " + configurationId, e);
            }
            throw new SwaggerRuntimeException(e);
        }
    }

    /**
     * @see io.swagger.api.AutoUploadApi#toggleAutoUploadEntry(long, boolean)
     */
    @Override
    public void toggleAutoUploadEntry(long configurationId, boolean enable) {

        try {
            Configuration conf = autoUploadManagementService.getConfigurationById((int) configurationId);
            conf.setStatus(enable ? 1 : 0);
            autoUploadManagementService.updateConfiguration(conf);
        } catch (SQLException e) {
            if (logger.isErrorEnabled()) {
                logger.error(
                        "Error during toggling the status of an auto upload configuration, id: "
                                + configurationId,
                        e);
            }
            throw new SwaggerRuntimeException(e);
        }
    }

    /**
     * @see io.swagger.api.AutoUploadApi#getAutoUploadEntry(java.lang.String)
     */
    @Override
    public Configuration getAutoUploadEntry(String nodeId) {

        NodeRef nodeRef = Converter.createNodeRefFromId(nodeId);

        if (!nodeService.exists(nodeRef)) {
            throw new IllegalArgumentException("The node with id " + nodeId + " could not be found.");
        }

        try {
            return autoUploadManagementService.getConfigurationByNodeRef(nodeRef);
        } catch (SQLException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when getting an auto upload configuration, id: " + nodeId, e);
            }
            throw new SwaggerRuntimeException(e);
        }
    }

    /**
     * @see io.swagger.api.AutoUploadApi#addAutoUploadEntry(java.lang.String)
     */
    @Override
    public void addAutoUploadEntry(String autoUploadConfigurationJson) {

        Configuration autoUploadConfiguration = parseBodyJSON(autoUploadConfigurationJson);

        try {
            if (autoUploadConfiguration.getIdConfiguration() == 0) {
                autoUploadManagementService.registerConfiguration(autoUploadConfiguration);
            } else {
                autoUploadManagementService.updateConfiguration(autoUploadConfiguration);
            }
        } catch (SQLException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error during an auto upload configuration update.", e);
            }
            throw new SwaggerRuntimeException(e);
        }
    }

    private Configuration parseBodyJSON(String autoUploadConfigurationJson) {

        if (autoUploadConfigurationJson == null || autoUploadConfigurationJson.isEmpty()) {
            throw new IllegalArgumentException(
                    "The body (auto-upload configuration) cannot be empty. It must contain the configuration data to add/update.");
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
                    "Error when parsing the body (auto-upload configuration).", e);
        }

        Configuration configuration = new Configuration();

        // igName
        String igName = (String) json.get("igName");

        if (igName == null || igName.isEmpty()) {
            throw new IllegalArgumentException("The interest group id cannot be null or empty (igName)");
        }

        if (!igName.startsWith(WORKSPACE_SPACES_STORE)) {
            igName = WORKSPACE_SPACES_STORE + igName;
        }

        // check if the node exists
        try {
            NodeRef igNameNodeRef = Converter.createNodeRefFromId(Converter.extractNodeRefId(igName));

            if (!nodeService.exists(igNameNodeRef)
                    || !nodeService.hasAspect(igNameNodeRef, CircabcModel.ASPECT_IGROOT)) {
                throw new IllegalArgumentException("'igName' must be a valid IG id");
            }
        } catch (MalformedNodeRefException e) {
            throw new IllegalArgumentException("'igName' must be a valid node reference.", e);
        }

        configuration.setIgName(igName);

        // fileId
        String fileId = (String) json.get("fileId");

        if (fileId != null && !fileId.isEmpty()) {

            // in case the configuration is defined at the file level

            if (!fileId.startsWith(WORKSPACE_SPACES_STORE)) {
                fileId = WORKSPACE_SPACES_STORE + fileId;
            }

            NodeRef fileNodeRef;

            // check if the node exists
            try {
                fileNodeRef = Converter.createNodeRefFromId(Converter.extractNodeRefId(fileId));

                if (!nodeService.exists(fileNodeRef)) {
                    throw new IllegalArgumentException("'fileId' must be a valid node reference.");
                }
            } catch (MalformedNodeRefException e) {
                throw new IllegalArgumentException("'fileId' must be a valid node reference.", e);
            }

            configuration.setParentNodeRef(
                    nodeService.getParentAssocs(fileNodeRef).get(0).getParentRef().toString());
        } else {

            // in case the configuration is defined at the admin level
            fileId = null;

            // parentId
            String parentId = (String) json.get("parentId");

            if (parentId == null || parentId.isEmpty()) {
                throw new IllegalArgumentException("The parent id cannot be null or empty (parentId)");
            }

            if (!parentId.startsWith(WORKSPACE_SPACES_STORE)) {
                parentId = WORKSPACE_SPACES_STORE + parentId;
            }

            // check if the node exists
            try {
                NodeRef parentNodeRef = Converter.createNodeRefFromId(Converter.extractNodeRefId(parentId));

                if (!nodeService.exists(parentNodeRef)) {
                    throw new IllegalArgumentException("'parentId' must be a valid node reference.");
                }
            } catch (MalformedNodeRefException e) {
                throw new IllegalArgumentException("'parentId' must be a valid node reference.", e);
            }

            configuration.setParentNodeRef(parentId);
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
                    "Invalid 'ftpPort' format. Must be a number in the range [1..65535]", e);
        }
        if (ftpPort < 1 || ftpPort > 65535) {
            throw new IllegalArgumentException(
                    "Invalid 'ftpPort'. Must be a number in the range [1..65535]");
        }

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
                    e1);
        }
        if (dayChoice < -1 || dayChoice > 7 || dayChoice == 0) {
            throw new IllegalArgumentException(
                    "Invalid day. Must be a number in the range [-1 = Every day, 1 = Monday .. 7 = Sunday]");
        }
        int hourChoice = -2;
        try {
            Object hourChoiceObj = json.get("hourChoice");
            if (hourChoiceObj != null) {
                hourChoice = Integer.parseInt(hourChoiceObj.toString().trim());
            }
        } catch (Exception e1) {
            throw new IllegalArgumentException(
                    "Invalid hour format. Must be a number in the range [-1 = Every hour, 0..23]", e1);
        }
        if (hourChoice < -1 || hourChoice > 23) {
            throw new IllegalArgumentException(
                    "Invalid hour. Must be a number in the range [-1 = Every hour, 0..23]");
        }

        configuration.setDateRestriction(
                buildCronExpression(String.valueOf(dayChoice), String.valueOf(hourChoice)));

        // auto extract zip
        boolean autoExtract;
        try {
            autoExtract = (Boolean) json.get("autoExtract");
        } catch (Exception e) {
            throw new IllegalArgumentException("'autoExtract' must be 'true' or 'false'.", e);
        }
        configuration.setAutoExtract(autoExtract);

        // job notifications
        boolean jobNotifications;
        try {
            jobNotifications = (Boolean) json.get("jobNotifications");
        } catch (Exception e) {
            throw new IllegalArgumentException("'jobNotifications' must be 'true' or 'false'.", e);
        }
        configuration.setJobNotifications(jobNotifications);

        // emails
        String emails = (String) json.get("emails");
        configuration.setEmails(emails);

        // idConfiguration
        long idConfiguration = (long) 0;
        try {
            Object idConfigurationObj = json.get("idConfiguration");
            if (idConfigurationObj != null) {
                idConfiguration = Long.parseLong(idConfigurationObj.toString().trim());
            }
        } catch (Exception e1) {
            throw new IllegalArgumentException("Invalid 'idConfiguration' format. Must be a number", e1);
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
                    throw new IllegalArgumentException("'dayChoice' must be a number in the range [1..7]");
                }

                cronDay = dayChoice;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'dayChoice' must be a number in the range [1..7]", e);
        }

        try {
            int hourChoiceInt = Integer.parseInt(hourChoice);

            if (hourChoiceInt != -1) {

                if (hourChoiceInt < 0 || hourChoiceInt > 23) {
                    throw new IllegalArgumentException("'hourChoice' must be a number in the range [0..23]");
                }

                cronHour = hourChoice;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'hourChoice' must be a number in the range [0..23]", e);
        }

        return "* * " + cronHour + " ? * " + cronDay;
    }

    /**
     * @param autoUploadManagementService the autoUploadManagementService to set
     */
    public void setAutoUploadManagementService(
            AutoUploadManagementService autoUploadManagementService) {
        this.autoUploadManagementService = autoUploadManagementService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
