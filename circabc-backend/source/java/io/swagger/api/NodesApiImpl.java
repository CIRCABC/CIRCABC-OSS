package io.swagger.api;

import eu.cec.digit.circabc.business.api.content.CociContentBusinessSrv;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.profile.permissions.DirectoryPermissions;
import eu.cec.digit.circabc.service.profile.permissions.InformationPermissions;
import eu.cec.digit.circabc.service.profile.permissions.LibraryPermissions;
import eu.cec.digit.circabc.service.profile.permissions.NewsGroupPermissions;
import eu.cec.digit.circabc.service.profile.permissions.VisibilityPermissions;
import io.swagger.model.CircabcServiceName;
import io.swagger.model.InterestGroup;
import io.swagger.model.Node;
import io.swagger.util.ApiToolBox;
import io.swagger.util.Converter;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.cmr.favourites.FavouritesService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.repository.Path.Element;
import org.alfresco.service.cmr.security.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author beaurpi
 */
public class NodesApiImpl implements NodesApi {

    public static final QName PROP_DESTINATION = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
            "destination");
    public static final String NOTIFICATION_STATUS = "NotificationStatus";
    public static final String ALLOWED = "ALLOWED";
    private static final Log logger = LogFactory.getLog(NodesApiImpl.class);
    private NodeService secureNodeService;
    // used for the path calculation
    private NodeService nodeService;
    private MimetypeService mimetypeService;
    private PermissionService permissionService;
    private OwnableService ownableService;
    private LockService lockService = null;
    private CociContentBusinessSrv cociContentBusinessSrv = null;
    private FavouritesService favouritesService;
    private AuthenticationService authenticationService;
    private MultilingualContentService multilingualContentService;
    private AuthorityService authorityService;
    private ApiToolBox apiToolBox;
    private GroupsApi groupsApi;
    private final List<String> noAccessStrings = Arrays.asList(DirectoryPermissions.DIRNOACCESS.toString(),
            InformationPermissions.INFNOACCESS.toString(), LibraryPermissions.LIBNOACCESS.toString(),
            NewsGroupPermissions.NWSNOACCESS.toString(), VisibilityPermissions.NOVISIBILITY.toString());

    /*
     * (non-Javadoc)
     *
     * @see io.swagger.api.NodesApi#getNodeById(java.lang.String)
     */
    @Override
    public Node getNodeById(String id) {
        Node result;
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        result = getNode(nodeRef);
        return result;
    }

    public GroupsApi getGroupsApi() {
        return groupsApi;
    }

    public void setGroupsApi(GroupsApi groupsApi) {
        this.groupsApi = groupsApi;
    }

    /*
     * (non-Javadoc)
     *
     * @see io.swagger.api.NodesApi#getPathByNode(java.lang.String)
     */
    @Override
    public List<Node> getPathByNode(String id) {

        List<Node> result = new ArrayList<>();
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        if (secureNodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)
                || secureNodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION)
                || secureNodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)) {

            Path path = secureNodeService.getPath(nodeRef);

            int cursor = path.size() - 1;
            boolean stop = false;

            while (!stop) {
                Element element = path.get(cursor);

                if (element instanceof Path.ChildAssocElement) {
                    NodeRef elementRef = ((Path.ChildAssocElement) element).getRef().getChildRef();

                    if (secureNodeService.hasAspect(elementRef, CircabcModel.ASPECT_IGROOT) || cursor == 0) {
                        stop = true;
                    } else if (permissionService
                            .hasPermission(elementRef, PermissionService.READ)
                            .equals(AccessStatus.ALLOWED)) {
                        Node node = getNode(elementRef);
                        setI18nProperties(node, elementRef);
                        result.add(node);

                        cursor--;
                    }
                }
            }
        }

        Collections.reverse(result);

        return result;
    }

    private void setI18nProperties(Node node, NodeRef tmpNodeRef) {

        Serializable serializable = secureNodeService.getProperty(tmpNodeRef, ContentModel.PROP_TITLE);

        if (serializable != null) {
            Map<String, String> ml = fixMLTextProperty(serializable);
            node.setTitle(Converter.convertMlToI18nProperty(ml));
        }

        serializable = secureNodeService.getProperty(tmpNodeRef, ContentModel.PROP_DESCRIPTION);

        if (serializable != null) {
            Map<String, String> ml = fixMLTextProperty(serializable);
            node.setDescription(Converter.convertMlToI18nProperty(ml));
        }
    }

    /**
     * @param nodeRef
     */
    public Node getNode(final NodeRef nodeRef) {
        return getNode(nodeRef, new Node());
    }

    /**
     * @see io.swagger.api.NodesApi#getNode(org.alfresco.service.cmr.repository.NodeRef,
     *      io.swagger.model.Node)
     */
    @Override
    public Node getNode(NodeRef nodeRef, Node node) {

        node.setId(nodeRef.getId());
        node.setName((String) secureNodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
        final QName type = secureNodeService.getType(nodeRef);
        node.setType(type.toString());

        boolean isLibRootAdmin = false;
        boolean isNewsRootAdmin = false;
        boolean hasGuestAccess = false;
        if (secureNodeService.hasAspect(nodeRef, CircabcModel.ASPECT_INFORMATION)) {
            node.setService(CircabcServiceName.INFORMATION);
        } else if (secureNodeService.hasAspect(nodeRef, CircabcModel.ASPECT_LIBRARY)) {
            node.setService(CircabcServiceName.LIBRARY);
            NodeRef libRef = apiToolBox.getCurrentLibraryRoot(nodeRef);
            if (libRef != null) {
                isLibRootAdmin = permissionService.hasPermission(libRef, "LibAdmin").equals(AccessStatus.ALLOWED);
            }
        } else if (secureNodeService.hasAspect(nodeRef, CircabcModel.ASPECT_EVENT)) {
            node.setService(CircabcServiceName.EVENTS);
        } else if (secureNodeService.hasAspect(nodeRef, CircabcModel.ASPECT_NEWSGROUP)) {
            node.setService(CircabcServiceName.NEWSGROUPS);
            NodeRef newsRef = apiToolBox.getCurrentNewsgroupRoot(nodeRef);
            if (newsRef != null) {
                isNewsRootAdmin = permissionService.hasPermission(newsRef, "NwsAdmin").equals(AccessStatus.ALLOWED);
            }
        }

        node.setParentId(secureNodeService.getPrimaryParent(nodeRef).getParentRef().getId());

        final Map<String, String> properties = new HashMap<>();
        boolean isMLAware = MLPropertyInterceptor.isMLAware();
        MLPropertyInterceptor.setMLAware(true);
        for (Entry<QName, Serializable> property : secureNodeService.getProperties(nodeRef).entrySet()) {
            if (property != null && property.getValue() != null) {

                Object value = "";

                if (property.getValue().getClass().equals(Date.class)) {

                    value = Converter.convertDateToString((Date) property.getValue());
                } else if (property.getKey().equals(ContentModel.PROP_TITLE)) {
                    Map<String, String> ml = fixMLTextProperty(property.getValue());
                    node.setTitle(Converter.convertMlToI18nProperty(ml));
                } else if (property.getKey().equals(ContentModel.PROP_DESCRIPTION)) {
                    Map<String, String> ml = fixMLTextProperty(property.getValue());
                    node.setDescription(Converter.convertMlToI18nProperty(ml));
                } else {
                    value = property.getValue();
                }
                properties.put(property.getKey().getLocalName(), value.toString());
            } else {
                if (property != null) {
                    properties.put(property.getKey().getLocalName(), "");
                }
            }
        }
        MLPropertyInterceptor.setMLAware(isMLAware);

        // add owner
        if (this.ownableService.hasOwner(nodeRef)) {
            properties.put("owner", this.ownableService.getOwner(nodeRef));
        }
        // add is version information
        boolean isVersion = nodeRef.getStoreRef().getIdentifier().startsWith("version");
        properties.put("isVersion", isVersion ? "true" : "false");

        properties.put(
                "originalContainerId", secureNodeService.getPrimaryParent(nodeRef).getParentRef().getId());

        if (secureNodeService.getType(nodeRef).equals(ContentModel.TYPE_CONTENT)) {
            ContentData cData = (ContentData) secureNodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
            long size;
            if (cData != null) {
                size = cData.getSize();
                properties.put("size", Long.toString(size));
                properties.put("mimetype", cData.getMimetype());
                properties.put(
                        "mimetypeName", mimetypeService.getDisplaysByMimetype().get(cData.getMimetype()));
                properties.put("encoding", cData.getEncoding());
            }

            boolean isMultilingual = secureNodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);

            properties.put("multilingual", String.valueOf(isMultilingual));

            if (isMultilingual) {
                Map<Locale, NodeRef> modelTrans = multilingualContentService.getTranslations(nodeRef);
                properties.put("translations", String.valueOf(modelTrans.size()));
            } else {
                properties.put("translations", "0");
            }

            // for checkout/checkin
            boolean isLocked = lockService.getLockType(nodeRef) != null;
            properties.put("locked", String.valueOf(isLocked));
            boolean isWorkingCopy = secureNodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY);
            properties.put("workingCopy", String.valueOf(isWorkingCopy));
            if (isWorkingCopy && StoreRef.STORE_REF_WORKSPACE_SPACESSTORE.equals(nodeRef.getStoreRef())) {
                try {
                    properties.put(
                            "originalNodeId", cociContentBusinessSrv.getWorkingCopyOf(nodeRef).getId());
                } catch (Exception e) {
                    logger.error(
                            "Could not get original node from the reported working copy (working copy aspect present): "
                                    + nodeRef.toString(),
                            e);
                    throw e;
                }
            }
            boolean currentUserHasAccess = true;
            try {
                lockService.checkForLock(nodeRef);
            } catch (NodeLockedException e) {
                currentUserHasAccess = false;
            }
            if (isWorkingCopy && currentUserHasAccess) {

                String workingCopyOwner = (String) secureNodeService.getProperty(nodeRef,
                        ContentModel.PROP_WORKING_COPY_OWNER);
                currentUserHasAccess = workingCopyOwner.equals(AuthenticationUtil.getRunAsUser());
            }
            properties.put("currentUserHasAccess", String.valueOf(currentUserHasAccess));
            if (isLocked) {
                NodeRef workingCopyNodeRef = cociContentBusinessSrv.getWorkingCopy(nodeRef);
                if (workingCopyNodeRef != null) {
                    properties.put("workingCopyId", workingCopyNodeRef.getId());
                }
            }
        }

        // add the IG id in case the node is of type
        // {http://www.alfresco.org/model/application/1.0}folderlink
        if (secureNodeService.getType(nodeRef).equals(ApplicationModel.TYPE_FOLDERLINK)) {
            NodeRef destination = (NodeRef) secureNodeService.getProperty(nodeRef, PROP_DESTINATION);
            if (destination != null) {
                NodeRef destinationIgNodeRef = apiToolBox.getCurrentInterestGroup(destination);
                if (destinationIgNodeRef != null) {
                    properties.put("destinationIgId", destinationIgNodeRef.getId());
                    properties.put("destinationId", destination.getId());
                }
            }
            NodeRef originIgNodeRef = apiToolBox.getCurrentInterestGroup(nodeRef);
            if (originIgNodeRef != null) {
                properties.put("originIgId", originIgNodeRef.getId());
            }
        }

        // is it a URL?
        if (secureNodeService.hasAspect(nodeRef, DocumentModel.ASPECT_URLABLE)) {
            properties.put("isUrl", "true");
        }

        node.setProperties(properties);

        Map<String, String> permissions = new HashMap<>();

        String userName = AuthenticationUtil.getRunAsUser();
        Set<String> authorities = authorityService.getAuthorities();
        for (org.alfresco.service.cmr.security.AccessPermission ac : permissionService.getAllSetPermissions(nodeRef)) {
            String permission = ac.getPermission();
            if (!hasGuestAccess && ac.getAuthority().equals("guest") && !noAccessStrings.contains(permission)) {
                hasGuestAccess = true;
            }

            if (ac.getAuthorityType() == AuthorityType.USER) {
                if (ac.getAuthority().equals(userName)) {

                    /*
                     * special case when dealing with the notifications, because DENIED prevails on
                     * ALLOWED
                     *
                     */
                    if (permission.equals(NOTIFICATION_STATUS)
                            && ac.getAccessStatus().name().equals(ALLOWED)) {
                        if (!"DENIED".equals(permissions.get(NOTIFICATION_STATUS))) {
                            permissions.put(permission, ac.getAccessStatus().name());
                        }
                    } else {
                        permissions.put(permission, ac.getAccessStatus().name());
                    }
                }
            } else if (ac.getAuthorityType() == AuthorityType.GROUP
                    && authorities.contains(ac.getAuthority())) {
                permissions.put(permission, ac.getAccessStatus().name());
            }
        }
        node.setHasGuestAccess(hasGuestAccess);
        if (permissions.containsKey(NOTIFICATION_STATUS)) {
            node.setNotifications(permissions.get(NOTIFICATION_STATUS));
            permissions.remove(NOTIFICATION_STATUS);
        } else {
            node.setNotifications("inherit");
        }

        // if the user is service admin,
        // we must keep all the rights in beneath nodes
        if (isLibRootAdmin) {
            permissions.put("LibAdmin", ALLOWED);
        } else if (isNewsRootAdmin) {
            permissions.put("NwsAdmin", ALLOWED);
        }

        node.setPermissions(permissions);

        if ((secureNodeService.getType(nodeRef).equals(ContentModel.TYPE_CONTENT)
                || secureNodeService.getType(nodeRef).equals(ContentModel.TYPE_FOLDER))
                && !secureNodeService.hasAspect(nodeRef, ContentModel.ASPECT_ARCHIVED)
                && !AuthenticationUtil.getSystemUserName().equals(userName)) {
            node.setFavourite(favouritesService.isFavourite(userName, nodeRef));
        } else {
            node.setFavourite(false);
        }

        if (secureNodeService.getType(nodeRef).equals(ContentModel.TYPE_FOLDER)) {
            Set<QName> typeFolder = new HashSet<>();
            typeFolder.add(ContentModel.TYPE_FOLDER);
            Boolean hasFolders = !nodeRef.toString().startsWith("versionStore")
                    && !secureNodeService.getChildAssocs(nodeRef, typeFolder).isEmpty();
            node.setHasSubFolders(hasFolders);
        } else if (secureNodeService.getType(nodeRef).equals(ForumModel.TYPE_FORUM)) {
            Set<QName> typeForum = new HashSet<>();
            typeForum.add(ForumModel.TYPE_FORUM);
            Boolean hasFolders = !secureNodeService.getChildAssocs(nodeRef, typeForum).isEmpty();
            node.setHasSubFolders(hasFolders);
        }

        return node;
    }

    private Map<String, String> fixMLTextProperty(Serializable property) {
        Map<String, String> ml = new HashMap<>();
        if (property instanceof MLText) {
            for (Entry<Locale, String> item : ((MLText) property).entrySet()) {
                if (item.getValue() == null) {
                    ml.put(item.getKey().toString(), "");
                } else {
                    ml.put(item.getKey().toString(), item.getValue());
                }
            }
        } else if (property instanceof String) {
            ml.put("en", property.toString());
        }
        return ml;
    }

    /**
     * @return the secureNodeService
     */
    public NodeService getSecureNodeService() {
        return secureNodeService;
    }

    /**
     * @param secureNodeService the secureNodeService to set
     */
    public void setSecureNodeService(NodeService secureNodeService) {
        this.secureNodeService = secureNodeService;
    }

    /**
     * @return the mimetypeService
     */
    public MimetypeService getMimetypeService() {
        return mimetypeService;
    }

    /**
     * @param mimetypeService the mimetypeService to set
     */
    public void setMimetypeService(MimetypeService mimetypeService) {
        this.mimetypeService = mimetypeService;
    }

    /**
     * @return the permissionService
     */
    public PermissionService getPermissionService() {
        return permissionService;
    }

    /**
     * @param permissionService the permissionService to set
     */
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @return the ownableService
     */
    public OwnableService getOwnableService() {
        return ownableService;
    }

    /**
     * @param ownableService the ownableService to set
     */
    public void setOwnableService(OwnableService ownableService) {
        this.ownableService = ownableService;
    }

    /**
     * @param apiToolBox the apiToolBox to set
     */
    public void setApiToolBox(ApiToolBox apiToolBox) {
        this.apiToolBox = apiToolBox;
    }

    @Override
    public Node nodesIdOwnershipPut(String id) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        ownableService.takeOwnership(nodeRef);
        return this.getNode(nodeRef);
    }

    // check permission and if OK return group
    //
    @Override
    public InterestGroup nodesIdGroupGet(String id) {
        try {
            AuthenticationUtil.setRunAsUser(AuthenticationUtil.getSystemUserName());
            NodeRef nodeRef = Converter.createNodeRefFromId(id);
            NodeRef igNodeRef = apiToolBox.getCurrentInterestGroup(nodeRef);
            return groupsApi.getInterestGroup(igNodeRef.getId());
        } catch (Exception e) {
            logger.error("Error in nodesIdGroupGet", e);

            throw new InvalidArgumentException("invalid bode id " + id);
        } finally {
            AuthenticationUtil.clearCurrentSecurityContext();
        }

    }

    /**
     * @param lockService the lockService to set
     */
    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }

    /**
     * @param cociContentBusinessSrv the cociContentBusinessSrv to set
     */
    public void setCociContentBusinessSrv(CociContentBusinessSrv cociContentBusinessSrv) {
        this.cociContentBusinessSrv = cociContentBusinessSrv;
    }

    public String generateUniqueName(final NodeRef parent, final String candidateName) {
        //
        if (secureNodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, candidateName) == null) {
            return candidateName;
        } else {
            final String extension = getFileNameExtension(candidateName);
            final String name = removeFileNameExtension(candidateName);
            String uniqueName;
            int tries = 0;

            do {
                uniqueName = name + " (" + (++tries) + ")" + ((extension == null) ? "" : "." + extension);
            } while (secureNodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, uniqueName) != null);

            return uniqueName;
        }
    }

    public String getFileNameExtension(final String fileName) {
        int extIndex = fileName.lastIndexOf('.');
        if (extIndex != -1) {
            return fileName.substring(extIndex + 1).toLowerCase();
        } else {
            return null;
        }
    }

    public String removeFileNameExtension(final String fileName) {
        int extIndex = fileName.lastIndexOf('.');
        if (extIndex != -1) {
            return fileName.substring(0, extIndex).toLowerCase();
        } else {
            return fileName;
        }
    }

    /**
     * @return the favouritesService
     */
    public FavouritesService getFavouritesService() {
        return favouritesService;
    }

    /**
     * @param favouritesService the favouritesService to set
     */
    public void setFavouritesService(FavouritesService favouritesService) {
        this.favouritesService = favouritesService;
    }

    /**
     * @return the authenticationService
     */
    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    /**
     * @param authenticationService the authenticationService to set
     */
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public AuthorityService getAuthorityService() {
        return authorityService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    /**
     * @param multilingualContentService the multilingualContentService to set
     */
    public void setMultilingualContentService(MultilingualContentService multilingualContentService) {
        this.multilingualContentService = multilingualContentService;
    }

    /**
     * @return the nodeService
     */
    public NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
