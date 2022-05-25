package io.swagger.api;

import eu.cec.digit.circabc.model.CircabcModel;
import io.swagger.model.InformationPage;
import io.swagger.model.News;
import io.swagger.model.PagedNews;
import io.swagger.util.Converter;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.extensions.surf.util.URLEncoder;

import java.io.Serializable;
import java.util.*;

/**
 * @author beaurpi
 */
public class InformationApiImpl implements InformationApi {

    private static final String FILE_SEPARATOR = "/";
    private static final String INFORMATION = "Information";

    private NodeService secureNodeService;
    private FileFolderService fileFolderService;
    private NodesApi nodesApi;
    private SpacesApi spacesApi;
    private PermissionService permissionService;
    private AuthorityService authorityService;
    private AuthenticationService authenticationService;
    private String webRoolUrl;

    /*
     * (non-Javadoc)
     *
     * @see io.swagger.api.InformationApi#groupsIdInformationGet(java.lang.String)
     */
    @Override
    public InformationPage groupsIdInformationGet(String id) {
        InformationPage result = new InformationPage();

        NodeRef igRef = Converter.createNodeRefFromId(id);
        NodeRef infRef =
                secureNodeService.getChildByName(igRef, ContentModel.ASSOC_CONTAINS, INFORMATION);

        String indexPage =
                secureNodeService.getProperty(infRef, CircabcModel.PROP_INF_INDEX_PAGE).toString();
        if (!webRoolUrl.endsWith("/")) {
            webRoolUrl = webRoolUrl + "/";
        }

        if (indexFileFound(infRef, indexPage)) {

            if (!indexPage.contains("http")) {
                String igName = secureNodeService.getProperty(igRef, ContentModel.PROP_NAME).toString();
                NodeRef categRef = secureNodeService.getPrimaryParent(igRef).getParentRef();
                String categName =
                        secureNodeService.getProperty(categRef, ContentModel.PROP_NAME).toString();

                String webdavContext = "webdav/CircaBC/";
                String url =
                        webRoolUrl
                                + webdavContext
                                + URLEncoder.encode(categName)
                                + "/"
                                + URLEncoder.encode(igName)
                                + "/Information"
                                + (indexPage.startsWith("/") ? "" : "/")
                                + indexPage;

                result.setUrl(url);
            } else {
                result.setUrl(indexPage);
            }
        }

        Boolean adapt =
                Boolean.valueOf(
                        secureNodeService.getProperty(infRef, CircabcModel.PROP_INF_ADAPT).toString());
        result.setAdapt(adapt);

        Object displayOldInfo =
                secureNodeService.getProperty(infRef, CircabcModel.PROP_INF_DISPLAY_OLD_INFORMATION);
        if (displayOldInfo != null) {
            result.setDisplayOldInformation(Boolean.valueOf(displayOldInfo.toString()));
        }

        String currentAuthority = "";
        Map<String, String> permissions = new HashMap<>();
        for (org.alfresco.service.cmr.security.AccessPermission ac :
                permissionService.getPermissions(infRef)) {
            permissions.put(ac.getPermission(), ac.getAccessStatus().name());
            if (currentAuthority.equals("")) {
                currentAuthority = ac.getAuthority();
            }
        }

        result.setPermissions(permissions);

        return result;
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
     * @return the webRoolUrl
     */
    public String getWebRoolUrl() {
        return webRoolUrl;
    }

    /**
     * @param webRoolUrl the webRoolUrl to set
     */
    public void setWebRoolUrl(String webRoolUrl) {
        this.webRoolUrl = webRoolUrl;
    }

    private boolean indexFileFound(final NodeRef parent, final String indexPage) {
        if (indexPage == null || indexPage.trim().length() == 0) {
            return false;
        } else {
            NodeRef ref = parent;

            String[] split = indexPage.split("Information/");

            if (split.length == 0) {
                return false;
            }

            String path = "";

            if (split.length == 1) {
                path = split[0];
            } else if (split.length == 2) {
                path = split[1];
            }

            final StringTokenizer tokens = new StringTokenizer(path, FILE_SEPARATOR, false);

            boolean found = false;

            while (tokens.hasMoreTokens()) {

                String token = tokens.nextToken();

                ref = secureNodeService.getChildByName(ref, ContentModel.ASSOC_CONTAINS, token);
                found = ref != null;
            }

            return found;
        }
    }

    @Override
    public PagedNews groupsIdInformationNewsGet(String id, Integer limit, Integer page) {
        NodeRef infoRef = getInformationNodeRef(id);
        PagedNews result = new PagedNews();

        PagingRequest pr = new PagingRequest(page * limit, limit);
        List<Pair<QName, Boolean>> sortProps = new ArrayList<>(1);

        Pair<QName, Boolean> sortPair =
                new Pair<>(
                        QName.createQName(
                                NamespaceService.CONTENT_MODEL_1_0_URI, ContentModel.PROP_CREATED.getLocalName()),
                        false);
        sortProps.add(sortPair);

        Set<QName> ignoredQNames = new HashSet<>();
        ignoredQNames.add(ContentModel.TYPE_FOLDER);
        ignoredQNames.add(ContentModel.TYPE_CONTENT);

        final PagingResults<FileInfo> list =
                getFileFolderService().list(infoRef, false, true, ignoredQNames, sortProps, pr);

        for (FileInfo item : list.getPage()) {
            final NodeRef childRef = item.getNodeRef();
            if (secureNodeService.hasAspect(childRef, CircabcModel.ASPECT_INFORMATION_NEWS)) {
                News news = newsIdGetInternal(childRef);
                if (news != null) {
                    result.getData().add(news);
                }
            }
        }

        Set<QName> infoSet = new HashSet<>();
        infoSet.add(CircabcModel.TYPE_INFORMATION_NEWS);
        result.setTotal((long) secureNodeService.getChildAssocs(infoRef, infoSet).size());

        return result;
    }

    private News newsIdGetInternal(NodeRef childRef) {
        if (secureNodeService.hasAspect(childRef, CircabcModel.ASPECT_INFORMATION_NEWS)) {
            News news = new News();
            news.setContent(
                    secureNodeService.getProperty(childRef, CircabcModel.PROP_NEWS_CONTENT).toString());
            news.setId(childRef.getId());

            String pattern =
                    secureNodeService.getProperty(childRef, CircabcModel.PROP_NEWS_PATTERN).toString();
            news.setPattern(News.PatternEnum.fromValue(pattern));

            String layout =
                    secureNodeService.getProperty(childRef, CircabcModel.PROP_NEWS_LAYOUT).toString();
            news.setLayout(News.LayoutEnum.fromValue(layout));

            if (secureNodeService.getProperty(childRef, ContentModel.PROP_TITLE) instanceof String) {
                news.setTitle(
                        Converter.toI18NProperty(
                                (String) secureNodeService.getProperty(childRef, ContentModel.PROP_TITLE)));
            } else if (secureNodeService.getProperty(childRef, ContentModel.PROP_TITLE)
                    instanceof MLText) {
                news.setTitle(
                        Converter.toI18NProperty(
                                (MLText) secureNodeService.getProperty(childRef, ContentModel.PROP_TITLE)));
            }

            Date date = (Date) secureNodeService.getProperty(childRef, ContentModel.PROP_MODIFIED);
            news.setModified(new DateTime(date));

            Date dateCreated = (Date) secureNodeService.getProperty(childRef, ContentModel.PROP_CREATED);
            news.setCreated(new DateTime(dateCreated));

            news.setSize(
                    Integer.parseInt(
                            secureNodeService.getProperty(childRef, CircabcModel.PROP_NEWS_SIZE).toString()));

            news.setModifier(
                    secureNodeService.getProperty(childRef, ContentModel.PROP_MODIFIER).toString());

            news.setCreator(
                    secureNodeService.getProperty(childRef, ContentModel.PROP_CREATOR).toString());

            if (News.PatternEnum.DOCUMENT.equals(News.PatternEnum.fromValue(pattern))
                    || News.PatternEnum.IMAGE.equals(News.PatternEnum.fromValue(pattern))) {
                news.setFiles(spacesApi.spaceGetChildren(childRef.getId(), false));
            } else if (News.PatternEnum.DATE.equals(News.PatternEnum.fromValue(pattern))) {
                Date newsDate = (Date) secureNodeService.getProperty(childRef, CircabcModel.PROP_NEWS_DATE);
                news.setDate(new LocalDate(newsDate));
            }

            Map<String, String> permissions = new HashMap<>();

            String userName = AuthenticationUtil.getRunAsUser();
            Set<String> authorities = authorityService.getAuthorities();
            for (org.alfresco.service.cmr.security.AccessPermission ac :
                    permissionService.getAllSetPermissions(childRef)) {
                if (ac.getAuthorityType() == AuthorityType.USER) {
                    if (ac.getAuthority().equals(userName)) {
                        permissions.put(ac.getPermission(), ac.getAccessStatus().name());
                    }
                } else if (ac.getAuthorityType() == AuthorityType.GROUP) {
                    if (authorities.contains(ac.getAuthority())) {
                        permissions.put(ac.getPermission(), ac.getAccessStatus().name());
                    }
                }
            }

            news.setPermissions(permissions);

            Map<String, String> properties = new HashMap<>();
            Serializable ownerObj = secureNodeService.getProperty(childRef, ContentModel.PROP_OWNER);
            if (ownerObj != null) {
                properties.put("owner", ownerObj.toString());
            }

            news.setProperties(properties);

            Serializable url = secureNodeService.getProperty(childRef, CircabcModel.PROP_NEWS_URL);
            if (url != null) {
                news.setUrl(url.toString());
            }

            return news;
        }

        return null;
    }

    private NodeRef getInformationNodeRef(String id) {
        NodeRef groupRef = Converter.createNodeRefFromId(id);
        return secureNodeService.getChildByName(groupRef, ContentModel.ASSOC_CONTAINS, INFORMATION);
    }

    @Override
    public News groupsIdInformationNewsPost(String id, News news) {
        NodeRef infoRef = getInformationNodeRef(id);

        String uid = UUID.randomUUID().toString();
        FileInfo newCard = fileFolderService.create(infoRef, uid, CircabcModel.TYPE_INFORMATION_NEWS);
        NodeRef newCardRef = newCard.getNodeRef();
        secureNodeService.setProperty(newCardRef, ContentModel.PROP_NAME, "news_" + newCardRef.getId());
        secureNodeService.setProperty(
                newCardRef, ContentModel.PROP_TITLE, Converter.toMLText(news.getTitle()));
        news.setId(newCardRef.getId());

        Map<QName, Serializable> props = new HashMap<>();
        props.put(CircabcModel.PROP_NEWS_CONTENT, news.getContent());
        props.put(CircabcModel.PROP_NEWS_PATTERN, news.getPattern().toString());
        props.put(CircabcModel.PROP_NEWS_SIZE, news.getSize());
        props.put(CircabcModel.PROP_NEWS_LAYOUT, news.getLayout().toString());

        if (news.getPattern().equals(News.PatternEnum.DATE)) {
            props.put(CircabcModel.PROP_NEWS_DATE, news.getDate().toDate());
        }

        if (news.getPattern().equals(News.PatternEnum.IFRAME)) {
            props.put(CircabcModel.PROP_NEWS_URL, news.getUrl());
        }

        secureNodeService.addAspect(newCardRef, CircabcModel.ASPECT_INFORMATION_NEWS, props);

        Map<QName, Serializable> propsOwner = new HashMap<>();
        propsOwner.put(ContentModel.PROP_OWNER, authenticationService.getCurrentUserName());
        secureNodeService.addAspect(newCardRef, ContentModel.ASPECT_OWNABLE, propsOwner);

        news.setModifier(
                secureNodeService.getProperty(newCardRef, ContentModel.PROP_MODIFIER).toString());
        news.setModified(
                new DateTime(secureNodeService.getProperty(newCardRef, ContentModel.PROP_MODIFIED)));

        return news;
    }

    /**
     * @return the fileFolderService
     */
    public FileFolderService getFileFolderService() {
        return fileFolderService;
    }

    /**
     * @param fileFolderService the fileFolderService to set
     */
    public void setFileFolderService(FileFolderService fileFolderService) {
        this.fileFolderService = fileFolderService;
    }

    public AuthorityService getAuthorityService() {
        return authorityService;
    }

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public NodesApi getNodesApi() {
        return nodesApi;
    }

    public void setNodesApi(NodesApi nodesApi) {
        this.nodesApi = nodesApi;
    }

    public SpacesApi getSpacesApi() {
        return spacesApi;
    }

    public void setSpacesApi(SpacesApi spacesApi) {
        this.spacesApi = spacesApi;
    }

    @Override
    public void newsIdDelete(String id) {
        NodeRef newsRef = Converter.createNodeRefFromId(id);
        secureNodeService.deleteNode(newsRef);
    }

    public PermissionService getPermissionService() {
        return permissionService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public News newsIdGet(String id) {
        NodeRef newsRef = Converter.createNodeRefFromId(id);
        return newsIdGetInternal(newsRef);
    }

    @Override
    public News newsIdPut(String id, News news) {
        NodeRef newsRef = Converter.createNodeRefFromId(id);

        secureNodeService.setProperty(
                newsRef, ContentModel.PROP_TITLE, Converter.toMLText(news.getTitle()));
        secureNodeService.setProperty(newsRef, CircabcModel.PROP_NEWS_CONTENT, news.getContent());
        secureNodeService.setProperty(
                newsRef, CircabcModel.PROP_NEWS_PATTERN, news.getPattern().toString());
        secureNodeService.setProperty(newsRef, CircabcModel.PROP_NEWS_SIZE, news.getSize());
        secureNodeService.setProperty(
                newsRef, CircabcModel.PROP_NEWS_LAYOUT, news.getLayout().toString());

        if (news.getPattern().equals(News.PatternEnum.IFRAME)) {
            secureNodeService.setProperty(newsRef, CircabcModel.PROP_NEWS_URL, news.getUrl());
        }

        if (news.getPattern().equals(News.PatternEnum.DATE)) {
            secureNodeService.setProperty(newsRef, CircabcModel.PROP_NEWS_DATE, news.getDate().toDate());
        }

        return newsIdGetInternal(newsRef);
    }

    @Override
    public void groupsIdInformationPut(String id, InformationPage body) {
        NodeRef igRef = Converter.createNodeRefFromId(id);
        NodeRef informationRef =
                secureNodeService.getChildByName(igRef, ContentModel.ASSOC_CONTAINS, INFORMATION);

        if (body.getAdapt() != null) {
            secureNodeService.setProperty(informationRef, CircabcModel.PROP_INF_ADAPT, body.getAdapt());
        }

        if (body.getDisplayOldInformation() != null) {
            secureNodeService.setProperty(
                    informationRef,
                    CircabcModel.PROP_INF_DISPLAY_OLD_INFORMATION,
                    body.getDisplayOldInformation());
        }
    }
}
