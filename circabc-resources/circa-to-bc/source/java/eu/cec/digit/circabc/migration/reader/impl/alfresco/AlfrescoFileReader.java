package eu.cec.digit.circabc.migration.reader.impl.alfresco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.VersionNumber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eu.cec.digit.circabc.migration.entities.ElementsHelper;
import eu.cec.digit.circabc.migration.entities.XMLNode;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Category;
import eu.cec.digit.circabc.migration.entities.generated.nodes.CategoryHeader;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Circabc;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Directory;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Events;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Information;
import eu.cec.digit.circabc.migration.entities.generated.nodes.InterestGroup;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Library;
import eu.cec.digit.circabc.migration.entities.generated.nodes.Newsgroups;
import eu.cec.digit.circabc.migration.reader.RemoteFileReader;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DossierModel;
import eu.cec.digit.circabc.model.SharedSpaceModel;
import eu.cec.digit.circabc.service.migration.ExportationException;
import eu.cec.digit.circabc.service.struct.ManagementService;

/**
 * RemoteFileReader implementation that reads from the CIRCABC Alfresco repository.
 * Uses NodeRef.toString() as the path convention.
 * Content URIs are generated as HTTP download URLs so they can be fetched
 * from another instance during import.
 */
public class AlfrescoFileReader implements RemoteFileReader {

    private static final Log logger = LogFactory.getLog(AlfrescoFileReader.class);
    private static final String PATH_SEPARATOR = "/";

    private NodeService nodeService;
    private VersionService versionService;
    private MultilingualContentService multilingualContentService;
    private ManagementService managementService;

    /** Base URL of the Alfresco instance, e.g. https://circabc.europa.eu/circabc */
    private String alfrescoBaseUrl = "";

    @Override
    public String getPathSeparator() {
        return PATH_SEPARATOR;
    }

    @Override
    public String generateResouceString(final String basePath) {
        // Generate an HTTP download URL for the node
        return buildContentUrl(basePath);
    }

    /**
     * Build an HTTP content download URL for a given NodeRef path.
     * Uses the Alfresco webscript API which supports alf_ticket authentication:
     * {baseUrl}/s/api/node/content/{storeType}/{storeId}/{nodeId}/{filename}
     *
     * Note: The /d/d/ download servlet does NOT support alf_ticket or Basic Auth
     * when the Global Authentication Filter is active (ECAS environments).
     */
    private String buildContentUrl(final String nodeRefStr) {
        try {
            final NodeRef ref = toNodeRef(nodeRefStr);
            if (!nodeService.exists(ref)) {
                return nodeRefStr;
            }
            final String name = (String) nodeService.getProperty(ref, ContentModel.PROP_NAME);
            final String safeName = (name != null) ? name.replace(" ", "%20") : "content";
            return alfrescoBaseUrl + "/s/api/node/content/"
                    + ref.getStoreRef().getProtocol() + "/"
                    + ref.getStoreRef().getIdentifier() + "/"
                    + ref.getId() + "/"
                    + safeName;
        } catch (Exception e) {
            logger.warn("Could not build content URL for " + nodeRefStr, e);
            return nodeRefStr;
        }
    }

    /**
     * Build an HTTP content download URL for a specific version of a node.
     * Uses the Alfresco webscript API which supports alf_ticket authentication:
     * {baseUrl}/s/api/node/content/{storeType}/{storeId}/{nodeId}/{filename}
     */
    private String buildVersionContentUrl(final NodeRef frozenRef, final String versionLabel, final NodeRef originalRef) {
        try {
            final String name = (String) nodeService.getProperty(originalRef, ContentModel.PROP_NAME);
            final String safeName = (name != null) ? name.replace(" ", "%20") : "content";
            // Use the frozen state NodeRef directly - it has its own content
            return alfrescoBaseUrl + "/s/api/node/content/"
                    + frozenRef.getStoreRef().getProtocol() + "/"
                    + frozenRef.getStoreRef().getIdentifier() + "/"
                    + frozenRef.getId() + "/"
                    + safeName;
        } catch (Exception e) {
            logger.warn("Could not build version content URL", e);
            return frozenRef.toString();
        }
    }

    @Override
    public void setNodePath(final XMLNode node) {
        if (node instanceof Circabc) {
            ElementsHelper.setExportationPath(node, managementService.getCircabcNodeRef().toString());
        } else if (node instanceof CategoryHeader) {
            // CategoryHeader maps to the circabc root in Alfresco
            ElementsHelper.setExportationPath(node, managementService.getCircabcNodeRef().toString());
        } else if (node instanceof Category) {
            final String categoryName = ((Category) node).getName().getValue().toString();
            final NodeRef catRef = managementService.getCategory(categoryName);
            if (catRef != null) {
                ElementsHelper.setExportationPath(node, catRef.toString());
            } else {
                logger.warn("Category not found in repository: " + categoryName);
            }
        } else if (node instanceof InterestGroup) {
            final NodeRef parentCatRef = toNodeRef(ElementsHelper.getExportationPath((XMLNode) ElementsHelper.getParent(node)));
            final String igName = ((InterestGroup) node).getName().getValue().toString();
            final List<NodeRef> igs = managementService.getInterestGroups(parentCatRef);
            NodeRef foundRef = null;
            // First try exact name match
            for (final NodeRef igRef : igs) {
                final String name = (String) nodeService.getProperty(igRef, ContentModel.PROP_NAME);
                if (igName.equals(name)) {
                    foundRef = igRef;
                    break;
                }
            }
            // If not found by name, try matching by title (IG may have been renamed)
            if (foundRef == null) {
                logger.warn("IG not found by cm:name '" + igName + "', trying title match...");
                for (final NodeRef igRef : igs) {
                    final String title = (String) nodeService.getProperty(igRef, ContentModel.PROP_TITLE);
                    if (igName.equals(title)) {
                        foundRef = igRef;
                        logger.info("IG found by title match: " + igName + " -> " + igRef);
                        break;
                    }
                }
            }
            if (foundRef != null) {
                ElementsHelper.setExportationPath(node, foundRef.toString());
            } else {
                logger.error("Interest group NOT FOUND in repository by name or title: '" + igName
                    + "' in category " + parentCatRef + ". Available IGs:");
                for (final NodeRef igRef : igs) {
                    final String name = (String) nodeService.getProperty(igRef, ContentModel.PROP_NAME);
                    final String title = (String) nodeService.getProperty(igRef, ContentModel.PROP_TITLE);
                    logger.error("  - name='" + name + "' title='" + title + "' ref=" + igRef);
                }
            }
        } else if (node instanceof Library) {
            final String parentPath = ElementsHelper.getExportationPath((XMLNode) ElementsHelper.getParent(node));
            if (parentPath == null) {
                logger.warn("Cannot resolve Library path: parent IG has no exportation path set");
                return;
            }
            final NodeRef igRef = toNodeRef(parentPath);
            final NodeRef libRef = managementService.getCurrentLibrary(igRef);
            if (libRef != null) {
                ElementsHelper.setExportationPath(node, libRef.toString());
            }
        } else if (node instanceof Information) {
            final String parentPath = ElementsHelper.getExportationPath((XMLNode) ElementsHelper.getParent(node));
            if (parentPath == null) {
                logger.warn("Cannot resolve Information path: parent IG has no exportation path set");
                return;
            }
            final NodeRef igRef = toNodeRef(parentPath);
            final List<ChildAssociationRef> children = nodeService.getChildAssocs(igRef);
            for (final ChildAssociationRef child : children) {
                if (nodeService.hasAspect(child.getChildRef(), CircabcModel.ASPECT_INFORMATION_ROOT)) {
                    ElementsHelper.setExportationPath(node, child.getChildRef().toString());
                    break;
                }
            }
        } else if (node instanceof Newsgroups) {
            final String parentPath = ElementsHelper.getExportationPath((XMLNode) ElementsHelper.getParent(node));
            if (parentPath == null) {
                logger.warn("Cannot resolve Newsgroups path: parent IG has no exportation path set");
                return;
            }
            final NodeRef igRef = toNodeRef(parentPath);
            final NodeRef ngRef = managementService.getCurrentNewsGroup(igRef);
            if (ngRef != null) {
                ElementsHelper.setExportationPath(node, ngRef.toString());
            }
        } else if (node instanceof Events) {
            final String parentPath = ElementsHelper.getExportationPath((XMLNode) ElementsHelper.getParent(node));
            if (parentPath == null) {
                logger.warn("Cannot resolve Events path: parent IG has no exportation path set");
                return;
            }
            final NodeRef igRef = toNodeRef(parentPath);
            final List<ChildAssociationRef> children = nodeService.getChildAssocs(igRef);
            for (final ChildAssociationRef child : children) {
                if (nodeService.hasAspect(child.getChildRef(), CircabcModel.ASPECT_EVENT_ROOT)) {
                    ElementsHelper.setExportationPath(node, child.getChildRef().toString());
                    break;
                }
            }
        } else if (node instanceof Directory) {
            final String parentPath = ElementsHelper.getExportationPath((XMLNode) ElementsHelper.getParent(node));
            if (parentPath == null) {
                logger.warn("Cannot resolve Directory path: parent IG has no exportation path set");
                return;
            }
            final NodeRef igRef = toNodeRef(parentPath);
            final List<ChildAssociationRef> children = nodeService.getChildAssocs(igRef);
            for (final ChildAssociationRef child : children) {
                if (nodeService.getType(child.getChildRef()).equals(CircabcModel.TYPE_DIRECTORY_SERVICE)) {
                    ElementsHelper.setExportationPath(node, child.getChildRef().toString());
                    break;
                }
            }
        }
    }

    @Override
    public String getCircabcPath() {
        return managementService.getCircabcNodeRef().toString();
    }

    @Override
    public String getCategoryPathWithName(final String categoryName) throws ExportationException {
        final NodeRef catRef = managementService.getCategory(categoryName);
        if (catRef == null) {
            throw new ExportationException("Category not found: " + categoryName);
        }
        return catRef.toString();
    }

    @Override
    public List<String> getCategoriesPath() throws ExportationException {
        final List<NodeRef> categories = managementService.getCategories();
        final List<String> paths = new ArrayList<>(categories.size());
        for (final NodeRef cat : categories) {
            paths.add(cat.toString());
        }
        return paths;
    }

    @Override
    public List<String> getInterestGroupsPath(final String categoryPath) throws ExportationException {
        final NodeRef catRef = toNodeRef(categoryPath);
        final List<NodeRef> igs = managementService.getInterestGroups(catRef);
        final List<String> paths = new ArrayList<>(igs.size());
        for (final NodeRef ig : igs) {
            paths.add(ig.toString());
        }
        return paths;
    }

    @Override
    public String getInterestGroupPathWithNames(final String categoryName, final String igName) throws ExportationException {
        final NodeRef catRef = toNodeRef(getCategoryPathWithName(categoryName));
        final List<NodeRef> igs = managementService.getInterestGroups(catRef);
        for (final NodeRef ig : igs) {
            final String name = (String) nodeService.getProperty(ig, ContentModel.PROP_NAME);
            if (igName.equals(name)) {
                return ig.toString();
            }
        }
        throw new ExportationException("Interest group not found: " + categoryName + "/" + igName);
    }

    @Override
    public String getCategoryHeaderName(final String categoryName) throws ExportationException {
        return getCategoryHeaderName(categoryName, "Default");
    }

    @Override
    public Set<String> getAllCategoryHeaders() throws ExportationException {
        final List<NodeRef> headers = managementService.getExistingCategoryHeaders();
        final java.util.Set<String> names = new java.util.LinkedHashSet<>();
        for (final NodeRef h : headers) {
            names.add((String) nodeService.getProperty(h, ContentModel.PROP_NAME));
        }
        return names;
    }

    @Override
    public String getCategoryHeaderName(final String categoryName, final String defaultCategoryName) throws ExportationException {
        final NodeRef catRef = toNodeRef(getCategoryPathWithName(categoryName));
        final List<NodeRef> headers = managementService.getCategoryHeaders(catRef);
        if (headers != null && !headers.isEmpty()) {
            return (String) nodeService.getProperty(headers.get(0), ContentModel.PROP_NAME);
        }
        return defaultCategoryName;
    }

    @Override
    public List<String> listChidrenPath(final String parentPath) throws ExportationException {
        final NodeRef parentRef = toNodeRef(parentPath);
        final List<ChildAssociationRef> children = nodeService.getChildAssocs(parentRef);
        final List<String> paths = new ArrayList<>(children.size());
        for (final ChildAssociationRef child : children) {
            final NodeRef childRef = child.getChildRef();
            // Skip system nodes (profiles, customization, etc.)
            final QName type = nodeService.getType(childRef);
            if (isExportableType(type, childRef)) {
                paths.add(childRef.toString());
            }
        }
        return paths;
    }

    @Override
    public boolean isSpace(final String path) throws ExportationException {
        final NodeRef ref = toNodeRef(path);
        final QName type = nodeService.getType(ref);
        return ContentModel.TYPE_FOLDER.equals(type) &&
               !nodeService.hasAspect(ref, CircabcModel.ASPECT_LIBRARY_ROOT) &&
               !nodeService.hasAspect(ref, CircabcModel.ASPECT_NEWSGROUP_ROOT) &&
               !nodeService.hasAspect(ref, CircabcModel.ASPECT_INFORMATION_ROOT) &&
               !nodeService.hasAspect(ref, CircabcModel.ASPECT_EVENT_ROOT) &&
               !nodeService.hasAspect(ref, CircabcModel.ASPECT_IGROOT) &&
               !nodeService.hasAspect(ref, CircabcModel.ASPECT_CATEGORY) &&
               !DossierModel.TYPE_DOSSIER_SPACE.equals(type) &&
               !CircabcModel.TYPE_INFORMATION_NEWS.equals(type);
    }

    @Override
    public boolean isNews(final String path) throws ExportationException {
        final NodeRef ref = toNodeRef(path);
        return CircabcModel.TYPE_INFORMATION_NEWS.equals(nodeService.getType(ref)) ||
               nodeService.hasAspect(ref, CircabcModel.ASPECT_INFORMATION_NEWS);
    }

    @Override
    public boolean isDossier(final String path) throws ExportationException {
        final NodeRef ref = toNodeRef(path);
        return DossierModel.TYPE_DOSSIER_SPACE.equals(nodeService.getType(ref));
    }

    @Override
    public boolean isDocument(final String path) throws ExportationException {
        final NodeRef ref = toNodeRef(path);
        final QName type = nodeService.getType(ref);
        return ContentModel.TYPE_CONTENT.equals(type) || nodeService.getType(ref).getLocalName().contains("content");
    }

    @Override
    public long getFileSize(final String path) throws ExportationException {
        final NodeRef ref = toNodeRef(path);
        final ContentData contentData = (ContentData) nodeService.getProperty(ref, ContentModel.PROP_CONTENT);
        if (contentData != null) {
            return contentData.getSize();
        }
        return -1;
    }

    @Override
    public boolean isUrl(final String path) throws ExportationException {
        final NodeRef ref = toNodeRef(path);
        return ContentModel.TYPE_LINK.equals(nodeService.getType(ref)) &&
               nodeService.getProperty(ref, ContentModel.PROP_LINK_DESTINATION) == null;
    }

    @Override
    public boolean isSharedSpaceLink(final String path) throws ExportationException {
        final NodeRef ref = toNodeRef(path);
        return nodeService.hasAspect(ref, CircabcModel.ASPECT_SHARED_SPACE) ||
               SharedSpaceModel.TYPE_INVITED_INTEREST_GROUP.equals(nodeService.getType(ref));
    }

    @Override
    public boolean isLink(final String path) throws ExportationException {
        final NodeRef ref = toNodeRef(path);
        final QName type = nodeService.getType(ref);
        return ContentModel.TYPE_LINK.equals(type) &&
               nodeService.getProperty(ref, ContentModel.PROP_LINK_DESTINATION) != null;
    }

    @Override
    public List<Locale> getContentTranslations(final String path) throws ExportationException {
        final NodeRef ref = toNodeRef(path);
        if (nodeService.hasAspect(ref, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)) {
            try {
                final Map<Locale, NodeRef> translations = multilingualContentService.getTranslations(ref);
                return new ArrayList<>(translations.keySet());
            } catch (Exception e) {
                logger.warn("Error getting translations for " + path, e);
            }
        }
        // Single language - get locale from content property
        final Locale locale = (Locale) nodeService.getProperty(ref, ContentModel.PROP_LOCALE);
        return Collections.singletonList(locale != null ? locale : Locale.ENGLISH);
    }

    @Override
    public Map<VersionNumber, String> getContentVersions(final String path, final Locale locale) throws ExportationException {
        NodeRef ref = toNodeRef(path);

        // If multilingual, get the translation for the specific locale
        if (nodeService.hasAspect(ref, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT)) {
            try {
                final Map<Locale, NodeRef> translations = multilingualContentService.getTranslations(ref);
                if (translations.containsKey(locale)) {
                    ref = translations.get(locale);
                }
            } catch (Exception e) {
                logger.warn("Error getting translation for locale " + locale, e);
            }
        }

        final NodeRef liveRef = ref;
        final Map<VersionNumber, String> versions = new HashMap<>();
        final VersionHistory history = versionService.getVersionHistory(ref);
        if (history != null) {
            // getAllVersions() returns newest first — first entry is the current/latest
            boolean first = true;
            for (final Version version : history.getAllVersions()) {
                final String label = version.getVersionLabel();
                try {
                    if (first) {
                        // Current version: use the live NodeRef (frozen nodes in version2Store
                        // don't expose cm:name and cm:content via nodeService.getProperties)
                        versions.put(new VersionNumber(label), liveRef.toString());
                        first = false;
                    } else {
                        versions.put(new VersionNumber(label), version.getFrozenStateNodeRef().toString());
                    }
                } catch (Exception e) {
                    if (!versions.containsKey(new VersionNumber("1.0"))) {
                        versions.put(new VersionNumber("1.0"), liveRef.toString());
                    }
                }
            }
        }
        if (versions.isEmpty()) {
            versions.put(new VersionNumber("1.0"), liveRef.toString());
        }
        return versions;
    }

    @Override
    public List<String> getContents(final String path) throws ExportationException {
        final List<String> contents = new ArrayList<>();
        final List<Locale> translations = getContentTranslations(path);
        for (final Locale locale : translations) {
            final Map<VersionNumber, String> versions = getContentVersions(path, locale);
            contents.addAll(versions.values());
        }
        return contents;
    }

    @Override
    public List<String> getDossierTranslations(final String path) throws ExportationException {
        // CIRCABC doesn't support dossier translations natively
        return Collections.singletonList(path);
    }

    @Override
    public List<String> getUrlTranslations(final String path) throws ExportationException {
        return Collections.singletonList(path);
    }

    @Override
    public boolean exists(final String path) throws ExportationException {
        try {
            final NodeRef ref = toNodeRef(path);
            return nodeService.exists(ref);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isExportableType(final QName type, final NodeRef ref) {
        // Skip profile nodes, customization nodes, directory service nodes
        if (CircabcModel.TYPE_DIRECTORY_SERVICE.equals(type) ||
            CircabcModel.TYPE_CIRCABC_ROOT_PROFILE.equals(type) ||
            CircabcModel.TYPE_CATEGORY_PROFILE.equals(type) ||
            CircabcModel.TYPE_INTEREST_GROUP_PROFILE.equals(type) ||
            CircabcModel.TYPE_CUSTOMIZATION_CONTAINER.equals(type) ||
            CircabcModel.TYPE_CUSTOMIZATION_CONTENT.equals(type) ||
            CircabcModel.TYPE_CUSTOMIZATION_FOLDER.equals(type) ||
            CircabcModel.TYPE_IGLOOKANDFEEL_CONTAINER.equals(type)) {
            return false;
        }
        // Skip nodes with service root aspects
        if (nodeService.hasAspect(ref, CircabcModel.ASPECT_LIBRARY_ROOT) ||
            nodeService.hasAspect(ref, CircabcModel.ASPECT_NEWSGROUP_ROOT) ||
            nodeService.hasAspect(ref, CircabcModel.ASPECT_INFORMATION_ROOT) ||
            nodeService.hasAspect(ref, CircabcModel.ASPECT_EVENT_ROOT) ||
            nodeService.hasAspect(ref, CircabcModel.ASPECT_SURVEY_ROOT)) {
            return false;
        }
        return true;
    }

    private NodeRef toNodeRef(final String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Cannot create NodeRef from null or empty path");
        }
        return new NodeRef(path);
    }

    // --- Setters for Spring injection ---

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setVersionService(VersionService versionService) {
        this.versionService = versionService;
    }

    public void setMultilingualContentService(MultilingualContentService multilingualContentService) {
        this.multilingualContentService = multilingualContentService;
    }

    public void setManagementService(ManagementService managementService) {
        this.managementService = managementService;
    }

    public void setAlfrescoBaseUrl(String alfrescoBaseUrl) {
        this.alfrescoBaseUrl = alfrescoBaseUrl;
    }
}
