/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.business.helper;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.repo.content.metadata.MetadataExtracter;
import org.alfresco.repo.content.metadata.MetadataExtracterRegistry;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Business service helper to manage server config, via the alfresco configuration service.
 *
 * @author Yanick Pignot
 */
public class MetadataManager {

    private static final Pattern NAME_REPLACEALL_PATTERN = Pattern
            .compile("\\\"|\\*|\\\\|\\>|\\<|\\?|\\/|\\:|\\|");

    private static final String UNIQUE_NAME = "{0}({1}){2}";
    private static final String INVALID_CAR_REPLACEMENT = "_";
    private static final char DOT = '.';

    private final Log logger = LogFactory.getLog(MetadataManager.class);

    private MetadataExtracterRegistry metadataExtracterRegistry;
    private MimetypeService mimetypeService;
    private NodeService nodeService;

    private ApplicationConfigManager configManager;
    private NodeTypeManager nodeTypeManager;

    //--------------
    //-- public methods

    public Map<QName, Serializable> extractContentMetadata(final ContentReader contentReader) {
        Assert.notNull(contentReader);

        final String mimetype = contentReader.getMimetype();
        Assert.hasText(mimetype, "Reader mimetype");

        // look for a transformer
        final MetadataExtracter extracter = getMetadataExtracterRegistry().getExtracter(mimetype);
        if (extracter == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No metadata extractor for mimetype: " + mimetype);
            }

            return Collections.emptyMap();
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Metadata extractor successfully found for mimetype '" + mimetype + "': " + extracter
                                .getClass());
            }

            final Map<QName, Serializable> properties = new HashMap<>(10);

            try {
                // we have a transformer, so do it
                extracter.extract(contentReader, properties);
            } catch (Throwable silentFailure) {
                // it failed
                logger.warn("Metadata extraction failed: \n" +
                        "   mimetype: " + mimetype + "\n" +
                        "   extracter: " + extracter.getClass());
            }

            return properties;
        }
    }

    public Boolean isInlineEditable(final String mimetype) {
        if (mimetype == null) {
            return Boolean.FALSE;
        } else {
            final List<String> editableMimetypes = getConfigManager().getInlineEditableMimeTypes();
            if (editableMimetypes == null) {
                return Boolean.FALSE;
            } else {
                return editableMimetypes.contains(mimetype);
            }

        }
    }

    public String guessMimetype(final String fileName) {
        Assert.notNull(fileName);

        // fall back to binary mimetype if no match found
        String mimetype = MimetypeMap.MIMETYPE_BINARY;
        final int extIndex = fileName.lastIndexOf('.');
        if (extIndex != -1) {
            String ext = fileName.substring(extIndex + 1).toLowerCase();
            String mt = mimetypeService.getMimetypesByExtension().get(ext);
            if (mt != null) {
                mimetype = mt;
            }
        }

        return mimetype;
    }

    public String guessEncoding(final InputStream inputStream, final String mimetype) {
        Assert.notNull(inputStream);

        final ContentCharsetFinder charsetFinder = getMimetypeService().getContentCharsetFinder();
        final Charset charset = charsetFinder.getCharset(inputStream, mimetype);

        return charset.name();
    }

    /**
     * Compute a valid an unique child filename for a given parent
     *
     * @param parent An existing parent
     * @param name   A filename name (not null)
     */
    public String getValidUniqueName(final NodeRef parent, final String name) {
        return getValidUniqueName(parent, ContentModel.ASSOC_CONTAINS, name);
    }

    /**
     * Compute a valid an unique child filename for a given parent
     *
     * @param parent An existing parent
     * @param name   A filename name (not null)
     */
    public String getValidUniqueName(final NodeRef parent, final QName assocQname,
                                     final String name) {
        String cleanName = getValidName(name);
        final Pair<String, String> split = splitFilename(cleanName);

        int counter = -1;
        while (exists(parent, assocQname, cleanName)) {
            cleanName = MessageFormat.format(UNIQUE_NAME, split.getFirst(), ++counter, split.getSecond());
        }

        if (logger.isDebugEnabled()) {
            if (cleanName.equals(name)) {
                logger.debug("Name " + name + " is already valid and unique.");
            } else {
                logger.debug("Name " + name + " has been replaced by " + cleanName);
            }
        }

        return cleanName;
    }

    /**
     * Compute a valid file name replacing unsuported caraters by -
     *
     * @param name The filname (not null)
     * @return A valid file name
     */
    public String getValidName(final String name) {
        return getValidName(name, INVALID_CAR_REPLACEMENT);
    }

    /**
     * Compute a valid file name replacing unsuported caraters by the given replacement String
     *
     * @param name        The clean filname (not null)
     * @param replacement The replacement string (not null)
     * @return A valid file name
     */
    public String getValidName(final String name, final String replacement) {
        Assert.notNull(name);
        Assert.notNull(replacement);

        final Matcher matcher = NAME_REPLACEALL_PATTERN.matcher(name.trim());

        return matcher.replaceAll(replacement);
    }

    /**
     * Return true if the current user is lock owner on a given working copy
     *
     * @param workingCopy The working copy
     * @return True is the nodeRef is a working copy and the user is the lock owner
     */
    public boolean isLockOwner(final NodeRef workingCopy) {
        if (getNodeTypeManager().isWorkingCopyDocument(workingCopy)) {
            final Serializable obj = getNodeService()
                    .getProperty(workingCopy, ContentModel.PROP_WORKING_COPY_OWNER);

            return obj != null && AuthenticationUtil.getFullyAuthenticatedUser().equals(obj);
        } else {
            return false;
        }
    }

    /**
     * Return the title of the node. If the title doesn't exists, return the name.
     *
     * <p>
     * For performance issues, if you already get the properties of the node, use
     * <i>computeTitle(Map<Qname, Serializable>)</i>
     * </p>
     *
     * @param nodeRef The node ref to compute the title
     * @return Never a null or empty string
     */
    public String computeTitle(final NodeRef nodeRef) {
        return computeTitle(getNodeService().getProperties(nodeRef));
    }

    /**
     * Return the title of the already retreived node properties. If the title doesn't exists, return
     * the name.
     *
     * <p>
     * For performance issues, use this method if the properties are already retreived from the
     * repository
     * </p>
     *
     * @param properties The node properties
     * @return Never a null or empty string
     */
    public String computeTitle(final Map<QName, Serializable> properties) {
        final String title = (String) properties.get(ContentModel.PROP_TITLE);

        if (title == null || title.trim().length() < 1) {
            return (String) properties.get(ContentModel.PROP_NAME);
        } else {
            return title;
        }
    }

    //--------------
    //-- private helpers


    private boolean exists(final NodeRef parent, final QName assocQname, final String name) {
        return getNodeService().getChildByName(parent, assocQname, name) != null;
    }

    private Pair<String, String> splitFilename(final String name) {
        final int dotPosition = name.lastIndexOf(DOT);
        if (dotPosition > -1) {
            return new Pair<>(
                    name.substring(0, dotPosition),
                    name.substring(dotPosition));
        } else {
            return new Pair<>(name, "");
        }

    }

    //	--------------
    //-- IOC

    /**
     * @return the metadataExtracterRegistry
     */
    protected final MetadataExtracterRegistry getMetadataExtracterRegistry() {
        return metadataExtracterRegistry;
    }

    /**
     * @param metadataExtracterRegistry the metadataExtracterRegistry to set
     */
    public final void setMetadataExtracterRegistry(
            MetadataExtracterRegistry metadataExtracterRegistry) {
        this.metadataExtracterRegistry = metadataExtracterRegistry;
    }

    /**
     * @return the mimetypeService
     */
    protected final MimetypeService getMimetypeService() {
        return mimetypeService;
    }

    /**
     * @param mimetypeService the mimetypeService to set
     */
    public final void setMimetypeService(MimetypeService mimetypeService) {
        this.mimetypeService = mimetypeService;
    }

    /**
     * @return the nodeService
     */
    protected final NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @return the configManager
     */
    protected final ApplicationConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * @param configManager the configManager to set
     */
    public final void setConfigManager(ApplicationConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * @return the nodeTypeManager
     */
    protected final NodeTypeManager getNodeTypeManager() {
        return nodeTypeManager;
    }

    /**
     * @param nodeTypeManager the nodeTypeManager to set
     */
    public final void setNodeTypeManager(NodeTypeManager nodeTypeManager) {
        this.nodeTypeManager = nodeTypeManager;
    }
}
