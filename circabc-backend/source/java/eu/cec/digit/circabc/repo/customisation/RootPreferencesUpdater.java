/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.repo.customisation;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.profile.CircabcRootProfileManagerService;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.service.cmr.dictionary.InvalidAspectException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.extensions.config.*;

import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Class in charge to synchronize a folder with a war / ear resource list.
 *
 * @author Yanick Pignot
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 Config was moved to Spring. ConfigElement was moved to
 * Spring. ConfigException was moved to Spring. ConfigLookupContext was moved to Spring.
 * ConfigService was moved to Spring. This class seems to be developed for CircaBC
 */
public class RootPreferencesUpdater {

    private static final String CONFIG_AREA = "space-contents";
    private static final String ELEM_SPACE = "space";
    private static final String ELEM_ROOT = "root";
    private static final String ELEM_FILE = "file";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_VERSIONABLE = "versionable";
    private static final String ATTR_EDITONLINE = "editOnline";
    private static final String ATTR_FORCEUPDATE = "forceUpdate";
    private static final String ATTR_REVISION = "revison";
    private static final String ATTR_GUEST_PERM = "guestPerm";
    private static final String ATTR_REGISTRED_PERM = "registredPerm";

    private static final Log logger = LogFactory.getLog(RootPreferencesUpdater.class);

    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private ContentService contentService;
    private MimetypeService mimetypeService;
    private ConfigService configService;
    private PermissionService permissionService;

    private String configCondition;

    /*package*/ void updateSpace(final NodeRef rootSpace) {
        ParameterCheck.mandatory("The root folder", rootSpace);
        ParameterCheck.mandatoryString(
                "The config condition (evaluator='string-compare' condition='??')", configCondition);

        try {
            final ConfigLookupContext clContext = new ConfigLookupContext(CONFIG_AREA);
            final Config configConditions = configService.getConfig(configCondition, clContext);

            final ConfigElement configElements = configConditions.getConfigElement(ELEM_ROOT);

            createNodes(rootSpace, configElements.getChildren());
        } catch (Exception e) {
            throw new ConfigException(
                    "Impossible to update space: "
                            + rootSpace
                            + " with the config condition "
                            + configCondition
                            + ". "
                            + e.getMessage(),
                    e);
        }
    }

    private void createNodes(
            final NodeRef parentSpace, final Collection<ConfigElement> configElements) throws Exception {
        if (configElements == null) {
            return;
        }

        for (final ConfigElement element : configElements) {
            final String elementName = element.getName();
            switch (elementName) {
                case ELEM_SPACE: {
                    final String name = element.getAttribute(ATTR_NAME);
                    if (isNameValid(name)) {
                        NodeRef spaceRef =
                                nodeService.getChildByName(parentSpace, ContentModel.ASSOC_CONTAINS, name);

                        if (spaceRef == null) {
                            final FileInfo fileInfo =
                                    fileFolderService.create(parentSpace, name, ContentModel.TYPE_FOLDER);
                            spaceRef = fileInfo.getNodeRef();

                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        "Space '"
                                                + name
                                                + "' successfully create under "
                                                + nodeService.getProperty(parentSpace, ContentModel.PROP_NAME)
                                                + ". With the reference "
                                                + spaceRef);
                            }
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        "Space '"
                                                + name
                                                + "' successfully found under "
                                                + nodeService.getProperty(parentSpace, ContentModel.PROP_NAME));
                            }
                        }

                        // continue recursion
                        createNodes(spaceRef, element.getChildren());

                    } else {
                        throw new ConfigException("The space name attribute is mandatory");
                    }
                    break;
                }
                case ELEM_FILE: {
                    final String name = element.getAttribute(ATTR_NAME);
                    if (isNameValid(name)) {
                        NodeRef contentRef =
                                nodeService.getChildByName(parentSpace, ContentModel.ASSOC_CONTAINS, name);
                        final boolean newContent = contentRef == null;
                        if (newContent) {
                            final FileInfo fileInfo =
                                    fileFolderService.create(parentSpace, name, ContentModel.TYPE_CONTENT);
                            contentRef = fileInfo.getNodeRef();

                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        "Content '"
                                                + name
                                                + "' successfully create under "
                                                + nodeService.getProperty(parentSpace, ContentModel.PROP_NAME)
                                                + ". With the reference "
                                                + contentRef);
                            }
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                        "Content '"
                                                + name
                                                + "' successfully found under "
                                                + nodeService.getProperty(parentSpace, ContentModel.PROP_NAME));
                            }
                        }

                        final Integer revision = getInteger(element.getAttribute(ATTR_REVISION));
                        boolean forceUpdate = isTrue(element.getAttribute(ATTR_FORCEUPDATE));

                        if (forceUpdate == false) {
                            // if forceUpdate = false, the revision is mandatory

                            if (revision == null) {
                                throw new ConfigException(
                                        "In force update is null or false, a revision number is mandatory (attribute 'revision')");
                            }
                            if (revision < 1) {
                                throw new ConfigException(
                                        "The revision number is invalid. Found: "
                                                + revision
                                                + " and any strictly positive number expected.");
                            }
                        }

                        if (newContent || forceUpdate || updateContent(contentRef, revision)) {
                            final String guestPerm = element.getAttribute(ATTR_GUEST_PERM);
                            final String registredPerm = element.getAttribute(ATTR_REGISTRED_PERM);

                            final InputStream inputStream = getInpuStreamFromResource(element.getValue());

                            final ContentWriter writer =
                                    contentService.getWriter(contentRef, ContentModel.PROP_CONTENT, true);
                            final String mimetype = getMimeTypeForFileName(name);
                            writer.setMimetype(mimetype);
                            writer.setEncoding(guessEncoding(inputStream, mimetype));
                            writer.putContent(inputStream);

                            if (logger.isDebugEnabled()) {
                                logger.debug("Content '" + name + "' successfully updated. ");
                            }

                            updateRevision(revision, name, contentRef);

                            if (isNameValid(guestPerm)) {
                                permissionService.setPermission(
                                        contentRef, CircabcConstant.GUEST_AUTHORITY, guestPerm, true);
                            }

                            if (isNameValid(registredPerm)) {
                                permissionService.setPermission(
                                        contentRef,
                                        CircabcRootProfileManagerService.ALL_CIRCA_USERS_AUTHORITY,
                                        registredPerm,
                                        true);
                            }
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Content '" + name + "' doesn't need to be updated");
                            }
                        }

                        applyVersionable(element, name, contentRef);

                        applyInlineEditable(element, name, contentRef);
                    } else {
                        throw new ConfigException("The content name attribute is mandatory");
                    }
                    break;
                }
                default:
                    throw new ConfigException("Unknow element name " + elementName);
            }
        }
    }

    private void updateRevision(Integer revision, String name, NodeRef contentRef) {
        if (revision != null) {
            final Map<QName, Serializable> revisionProps =
                    Collections.singletonMap(CircabcModel.PROP_REVISION_NUMBER, (Serializable) revision);

            if (nodeService.hasAspect(contentRef, CircabcModel.ASPECT_REVISIONABLE)) {
                nodeService.addProperties(contentRef, revisionProps);

                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Content '"
                                    + name
                                    + "' is already revisionable, the revision number is updated: "
                                    + revision);
                }
            } else {
                nodeService.addAspect(contentRef, CircabcModel.ASPECT_REVISIONABLE, revisionProps);

                if (logger.isDebugEnabled()) {
                    logger.debug(
                            "Content '"
                                    + name
                                    + "' successfully setted as revisionable with the revision number: "
                                    + revision);
                }
            }
        }
    }

    private boolean updateContent(final NodeRef contentRef, final Integer revision) {
        if (revision == null
                || revision < 1
                || nodeService.hasAspect(contentRef, CircabcModel.ASPECT_REVISIONABLE) == false) {
            return true;
        } else {
            final Integer oldRevision =
                    (Integer) nodeService.getProperty(contentRef, CircabcModel.PROP_REVISION_NUMBER);

            return oldRevision == null || revision > oldRevision;
        }
    }

    /**
     * @param element
     * @param name
     * @param contentRef
     * @throws InvalidNodeRefException
     * @throws InvalidAspectException
     */
    private void applyInlineEditable(
            final ConfigElement element, final String name, NodeRef contentRef)
            throws InvalidNodeRefException, InvalidAspectException {
        if (isTrue(element.getAttribute(ATTR_EDITONLINE))) {
            if (nodeService.hasAspect(contentRef, ApplicationModel.ASPECT_INLINEEDITABLE)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Content '" + name + "' is already inline editable");
                }
            } else {
                nodeService.addAspect(
                        contentRef,
                        ApplicationModel.ASPECT_INLINEEDITABLE,
                        Collections.singletonMap(
                                ApplicationModel.PROP_EDITINLINE, (Serializable) Boolean.TRUE));

                if (logger.isDebugEnabled()) {
                    logger.debug("Content '" + name + "' is made inline editable.");
                }
            }
        }
    }

    /**
     * @param element
     * @param name
     * @param contentRef
     * @throws InvalidNodeRefException
     * @throws InvalidAspectException
     */
    private void applyVersionable(final ConfigElement element, final String name, NodeRef contentRef)
            throws InvalidNodeRefException, InvalidAspectException {
        if (isTrue(element.getAttribute(ATTR_VERSIONABLE))) {
            if (nodeService.hasAspect(contentRef, ContentModel.ASPECT_VERSIONABLE)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Content '" + name + "' is already versionable");
                }
            } else {
                nodeService.addAspect(
                        contentRef,
                        ContentModel.ASPECT_VERSIONABLE,
                        Collections.singletonMap(ContentModel.PROP_AUTO_VERSION, (Serializable) Boolean.TRUE));

                if (logger.isDebugEnabled()) {
                    logger.debug("Content '" + name + "' is made versionable");
                }
            }
        }
    }

    /**
     * @param name
     * @return
     */
    private boolean isNameValid(final String name) {
        return name != null && name.trim().length() > 0;
    }

    /**
     * @param booleanStr
     * @return
     */
    private boolean isTrue(final String booleanStr) {
        return booleanStr != null && Boolean.parseBoolean(booleanStr);
    }

    /**
     * @param intStr
     * @return
     */
    private Integer getInteger(final String intStr) {
        return intStr == null ? null : Integer.parseInt(intStr);
    }

    /* TODO Refractor: Should be in a business layer !!*/
    private String getMimeTypeForFileName(final String filename) {
        // fall back to binary mimetype if no match found
        String mimetype = MimetypeMap.MIMETYPE_BINARY;
        int extIndex = filename.lastIndexOf('.');
        if (extIndex != -1) {
            String ext = filename.substring(extIndex + 1).toLowerCase();
            String mt = mimetypeService.getMimetypesByExtension().get(ext);
            if (mt != null) {
                mimetype = mt;
            }
        }

        return mimetype;
    }

    /* TODO Refractor: Should be in a business layer !!*/
    private String guessEncoding(InputStream is, String mimetype) {
        try {
            final ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
            final Charset charset = charsetFinder.getCharset(is, mimetype);
            return charset.name();
        } catch (Throwable t) {
            return Charset.defaultCharset().name();
        }
    }

    private InputStream getInpuStreamFromResource(final String contentUri) throws Exception {
        final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        final Resource resource = resolver.getResource(contentUri);
        return resource.getInputStream();
    }

    public final String getConfigCondition() {
        return configCondition;
    }

    public final void setConfigCondition(String configCondition) {
        this.configCondition = configCondition;
    }

    public final void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public final void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public final void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    public final void setMimetypeService(MimetypeService mimetypeService) {
        this.mimetypeService = mimetypeService;
    }

    public final void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public final void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }
}
