package io.swagger.api;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.repo.app.CircabcDaoServiceImpl;
import eu.cec.digit.circabc.service.app.CircabcService;
import io.swagger.model.Category;
import io.swagger.model.Header;
import io.swagger.model.I18nProperty;
import io.swagger.model.User;
import io.swagger.util.Converter;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.CategoryService;
import org.alfresco.service.cmr.search.CategoryService.Depth;
import org.alfresco.service.cmr.search.CategoryService.Mode;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class HeadersApiImpl implements HeadersApi {

    public static final String NOT_A_HEADER = "Not a header";
    private final Log logger = LogFactory.getLog(HeadersApiImpl.class);
    private CategoryService categoryService;
    private NodeService nodeService;
    private NodeService secureNodeService;
    private NodeRef circabcCategoryRoot;
    private CircabcService circabcService;
    private CircabcDaoServiceImpl circabcDaoServiceImpl;
    private UsersApi usersApi;

    @Override
    public void deleteHeader(String id) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        boolean isHeaderNode = isHeader(nodeRef);
        if (isHeaderNode) {
            if (categoryService.getChildren(nodeRef, Mode.MEMBERS, Depth.IMMEDIATE).isEmpty()) {
                categoryService.deleteCategory(nodeRef);
            } else {
                throw new IllegalArgumentException("Header is not empty");
            }
        } else {
            throw new IllegalArgumentException(NOT_A_HEADER);
        }
    }

    @Override
    public List<Category> getCategoriesByHeaderId(String id, String language, Boolean guest) {
        List<Category> result = new ArrayList<>();
        if (circabcService.readFromDatabase()) {
            NodeRef headerRef = Converter.createNodeRefFromId(id);
            Long headerId = (Long) nodeService.getProperty(headerRef, ContentModel.PROP_NODE_DBID);
            String userName = AuthenticationUtil.getFullyAuthenticatedUser();
            User user = usersApi.usersUserIdGet(userName);
            Long localeId = circabcService.getUserLocaleID(userName);
            try {
                List<eu.cec.digit.circabc.repo.app.model.Category> categories =
                        circabcDaoServiceImpl.selectCategoriesByHeaderLocale(headerId, localeId);
                for (eu.cec.digit.circabc.repo.app.model.Category category : categories) {
                    Category cat = Converter.toCategory(category, user.getUiLang());
                    NodeRef categRef = Converter.createNodeRefFromId(cat.getId());
                    Map<String, String> title = circabcService.getCategoryTitle(categRef);
                    cat.setTitle(Converter.convertMlToI18nProperty(title));
                    result.add(cat);
                }

            } catch (SQLException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Error in getCategoriesByHeaderId", e);
                }
            }

        } else {
            initCircabcCategoryRoot();
            setCategoriesByHeader(id, result);
        }

        return result;
    }

    private void setCategoriesByHeader(String headerId, List<Category> categories) {
        NodeRef headerNodeRef = Converter.createNodeRefFromId(headerId);
        final Collection<ChildAssociationRef> children =
                categoryService.getChildren(headerNodeRef, Mode.MEMBERS, Depth.ANY);
        for (ChildAssociationRef child : children) {
            if (nodeService.hasAspect(child.getChildRef(), CircabcModel.ASPECT_CATEGORY)) {
                Category category = new Category();
                category.setName(
                        (String) nodeService.getProperty(child.getChildRef(), ContentModel.PROP_NAME));
                category.setId(child.getChildRef().getId());
                final Serializable property =
                        nodeService.getProperty(child.getChildRef(), ContentModel.PROP_TITLE);
                if (property instanceof MLText) {
                    category.setTitle(Converter.toI18NProperty((MLText) property));
                } else if (property instanceof String) {
                    category.setTitle(Converter.toI18NProperty((String) property));
                }
                categories.add(category);
            }
        }
    }

    @Override
    public Header getHeader(String id) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        boolean isHeaderNode = isHeader(nodeRef);
        if (isHeaderNode) {
            Header header = new Header();
            header.setName((String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
            header.setId(nodeRef.getId());
            final Serializable property = nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION);
            setHaederDescription(header, property);
            header.setCategories(getCategoriesByHeaderId(id, null, null));
            return header;
        } else {
            throw new IllegalArgumentException(NOT_A_HEADER);
        }
    }

    private void setHaederDescription(Header header, Serializable property) {
        I18nProperty description = null;
        if (property instanceof MLText) {
            description = Converter.toI18NProperty((MLText) property);
        } else if (property instanceof String) {
            description = Converter.toI18NProperty((String) property);
        }
        header.setDescription(description);
    }

    @Override
    public List<Header> getHeaders(String language, Boolean guest) {
        List<Header> headers = new ArrayList<>();
        if (circabcService.readFromDatabase()) {
            try {
                List<eu.cec.digit.circabc.repo.app.model.Header> headerList =
                        circabcDaoServiceImpl.selectHeaders();
                for (eu.cec.digit.circabc.repo.app.model.Header header : headerList) {
                    Header hd = Converter.toHeader(header);
                    headers.add(hd);
                }
            } catch (SQLException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Error in getHeaders", e);
                }
            }
        } else {
            initCircabcCategoryRoot();
            setCategoryHeaders(headers);
        }
        return headers;
    }

    private void setCategoryHeaders(List<Header> headers) {
        Collection<ChildAssociationRef> ciracbcCategoryHeaders =
                categoryService.getChildren(circabcCategoryRoot, Mode.SUB_CATEGORIES, Depth.IMMEDIATE);
        for (ChildAssociationRef child : ciracbcCategoryHeaders) {
            Header h = new Header();
            h.setName((String) nodeService.getProperty(child.getChildRef(), ContentModel.PROP_NAME));
            h.setId(child.getChildRef().getId());
            final Serializable property =
                    nodeService.getProperty(child.getChildRef(), ContentModel.PROP_DESCRIPTION);

            setHaederDescription(h, property);
            h.setCategories(getCategoriesByHeaderId(child.getChildRef().getId(), null, null));
            headers.add(h);
        }
    }

    @Override
    public Category headersIdCategoriesPost(String id, Category body) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Header postHeader(Header body) {
        initCircabcCategoryRoot();
        final NodeRef headerNodeRef =
                categoryService.createCategory(circabcCategoryRoot, body.getName());
        secureNodeService.setProperty(
                headerNodeRef, ContentModel.PROP_DESCRIPTION, Converter.toMLText(body.getDescription()));
        Header result = new Header();
        result.setId(headerNodeRef.getId());
        result.setName(body.getName());
        result.setDescription(body.getDescription());
        return result;
    }

    @Override
    public Header putHeader(String id, Header body) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        boolean isHeaderNode = isHeader(nodeRef);
        if (isHeaderNode) {
            secureNodeService.setProperty(
                    nodeRef, ContentModel.PROP_DESCRIPTION, Converter.toMLText(body.getDescription()));
            secureNodeService.setProperty(nodeRef, ContentModel.PROP_NAME, body.getName());
            Header result = new Header();
            result.setId(id);
            result.setName(body.getName());
            result.setDescription(body.getDescription());
            return result;
        } else {
            throw new IllegalArgumentException(NOT_A_HEADER);
        }
    }

    private boolean isHeader(NodeRef nodeRef) {
        initCircabcCategoryRoot();
        return nodeService.getPrimaryParent(nodeRef).getParentRef().equals(circabcCategoryRoot);
    }

    private void initCircabcCategoryRoot() {
        if (circabcCategoryRoot == null) {
            setCircabcCategoryRoot();
        }
    }

    private void setCircabcCategoryRoot() {
        Collection<ChildAssociationRef> rootCategories =
                categoryService.getCategories(
                        Repository.getStoreRef(), ContentModel.ASPECT_GEN_CLASSIFIABLE, Depth.IMMEDIATE);
        for (ChildAssociationRef child : rootCategories) {

            if (nodeService
                    .getProperty(child.getChildRef(), ContentModel.PROP_NAME)
                    .equals("CircaBCHeader")) {
                circabcCategoryRoot = child.getChildRef();
            }
        }
    }

    public CategoryService getCategoryService() {
        return categoryService;
    }

    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public NodeService getSecureNodeService() {
        return secureNodeService;
    }

    public void setSecureNodeService(NodeService secureNodeService) {
        this.secureNodeService = secureNodeService;
    }

    public CircabcService getCircabcService() {
        return circabcService;
    }

    public void setCircabcService(CircabcService circabcService) {
        this.circabcService = circabcService;
    }

    public CircabcDaoServiceImpl getCircabcDaoServiceImpl() {
        return circabcDaoServiceImpl;
    }

    public void setCircabcDaoServiceImpl(CircabcDaoServiceImpl circabcDaoServiceImpl) {
        this.circabcDaoServiceImpl = circabcDaoServiceImpl;
    }

    public void setUsersApi(UsersApi usersApi) {
        this.usersApi = usersApi;
    }

    @Override
    public Header getHeaderByCategory(String id) {
        NodeRef categoryRef = Converter.createNodeRefFromId(id);

        Header result = null;

        List<NodeRef> categories =
                (List<NodeRef>) nodeService.getProperty(categoryRef, ContentModel.PROP_CATEGORIES);
        if (categories != null && categories.size() == 1) {
            result = getHeader(categories.get(0).getId());
        }

        return result;
    }
}
