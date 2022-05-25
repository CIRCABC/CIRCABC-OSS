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
package eu.cec.digit.circabc.web.ui.repo.component;

import eu.cec.digit.circabc.util.PathUtils;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.repo.WebResources;
import org.alfresco.web.ui.repo.component.AbstractItemSelector;

import javax.faces.component.EditableValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.transaction.UserTransaction;
import java.io.IOException;
import java.util.*;

/**
 * @author Slobodan Filipovic
 * <p>
 * Add new parameter rootNode so user can browse only nodes bellow rootNode
 */
public class UINodeSelector extends AbstractItemSelector {

    private static final String SELECT_SPACE_PROMPT = "select_space_prompt";

    private static final String EU_CEC_DIGIT_CIRCABC_FACES_SPACE_SELECTOR = "eu.cec.digit.circabc.faces.NodeSelector";
    protected String rootNode = Application.getCompanyRootId();
    protected Boolean showContents = Boolean.FALSE;

    // ------------------------------------------------------------------------------
    // Component Impl
    protected String pathLabel = null;
    protected String pathErrorMessage = null;
    private String submitedNoScriptValue;

    /**
     * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext,
     * java.lang.Object)
     */
    @Override
    public void restoreState(final FacesContext context, final Object state) {
        final Object values[] = (Object[]) state;
        // standard component attributes are restored by the super class
        super.restoreState(context, values[0]);
        this.rootNode = (String) values[1];
        this.showContents = (Boolean) values[2];
        this.pathLabel = (String) values[3];
        this.pathErrorMessage = (String) values[4];

    }

    /**
     * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
     */
    @Override
    public Object saveState(final FacesContext context) {
        final Object values[] = new Object[5];
        // other component attributes are saved by the super class
        values[0] = super.saveState(context);
        values[1] = this.rootNode;
        values[2] = this.showContents;
        values[3] = this.pathLabel;
        values[4] = this.pathErrorMessage;
        return (values);
    }

    @Override
    public String getFamily() {
        return EU_CEC_DIGIT_CIRCABC_FACES_SPACE_SELECTOR;
    }

    @Override
    public String getDefaultLabel() {
        return Application.getMessage(FacesContext.getCurrentInstance(), SELECT_SPACE_PROMPT);
    }

    public String getParentNodeId(final FacesContext context) {
        String id = null;

        if (this.navigationId != null && this.navigationId.equals(getRootNode()) == false) {
            try {
                final ChildAssociationRef parentRef = getFastNodeService(context)
                        .getPrimaryParent(new NodeRef(Repository.getStoreRef(), this.navigationId));
                id = parentRef.getParentRef().getId();
            } catch (final AccessDeniedException accessErr) {
                // cannot navigate to parent id will be null
            }
        }

        return id;
    }

    public Collection<NodeRef> getChildrenForNode(final FacesContext context) {
        final NodeRef nodeRef = new NodeRef(Repository.getStoreRef(), this.navigationId);
        final List<ChildAssociationRef> allKids = getNodeService(context)
                .getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        final NodeService service = getFastNodeService(context);
        // filter out those children that are not spaces
        final List<NodeRef> spaceKids = new ArrayList<>();
        QName type;
        for (final ChildAssociationRef ref : allKids) {
            type = service.getType(ref.getChildRef());
            if (isShowContents() || !ContentModel.TYPE_CONTENT.equals(type)) {
                spaceKids.add(ref.getChildRef());
            }
        }

        return spaceKids;
    }

    @Override
    public Collection<NodeRef> getRootChildren(FacesContext context) {
        NodeRef rootRef = getRootNodeRef();

        // get a child association reference back from the parent node to satisfy
        // the generic API we have in the abstract super class
        PermissionService ps = Repository.getServiceRegistry(context).getPermissionService();
        if (ps.hasPermission(rootRef, PermissionService.READ) != AccessStatus.ALLOWED) {
            // get the root space from the current user home instead
            String homeId = Application.getCurrentUser(context).getHomeSpaceId();
            rootRef = new NodeRef(Repository.getStoreRef(), homeId);
        }
        List<NodeRef> roots = new ArrayList<>(1);
        roots.add(rootRef);

        return roots;
    }

    /**
     * @see javax.faces.component.UIComponentBase#decode(javax.faces.context.FacesContext)
     */
    @Override
    public void decode(FacesContext context) {

        Map requestMap = context.getExternalContext().getRequestParameterMap();
        String fieldId = getNoScriptFieldName();
        submitedNoScriptValue = (String) requestMap.get(fieldId);
        if (submitedNoScriptValue != null) {
            if (submitedNoScriptValue.trim().equalsIgnoreCase("")) {
                ((EditableValueHolder) this).setSubmittedValue(null);
                this.setValue(null);
            } else {
                final NodeRef nodeRefFromPath = getNodeRefFromPath(context, submitedNoScriptValue);
                if (nodeRefFromPath == null) {
                    setValid(false);
                } else {
                    ((EditableValueHolder) this).setSubmittedValue(nodeRefFromPath);
                }
            }
        } else {
            super.decode(context);
        }

    }

    private NodeRef getNodeRefFromPath(FacesContext context, String pathString) {
        final StringTokenizer tokens = new StringTokenizer(pathString, "/", false);
        NodeRef iterNodeRef = null;
        String iterName = null;
        NodeService nodeService = getNodeService(context);
        if (tokens.hasMoreTokens()) //root node
        {
            iterNodeRef = getRootNodeRef();
            iterName = tokens.nextToken();
            final String rootName = nodeService.getProperty(iterNodeRef, ContentModel.PROP_NAME)
                    .toString();
            if (!rootName.equalsIgnoreCase(iterName)) {
                return null;
            }
        }
        while (tokens.hasMoreTokens()) {
            iterName = tokens.nextToken();
            iterNodeRef = nodeService.getChildByName(iterNodeRef, ContentModel.ASSOC_CONTAINS, iterName);
            if (iterNodeRef == null) {
                break;
            }
        }

        return iterNodeRef;
    }

    /**
     * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
     */
    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (isRendered() == false) {
            return;
        }

        context.getResponseWriter().write("<div id=\"script\" style=\"visibility:hidden\" >");
        super.encodeBegin(context);
        context.getResponseWriter().write("</div>");

        String selectedSpacePath = getNodePath(context);

        {
            StringBuilder bufferNoScript = new StringBuilder(512);

            bufferNoScript.append("<div id=\"noscript\">");
            bufferNoScript.append(getPathLabel());
            bufferNoScript.append("<input id=\"");
            bufferNoScript.append(getNoScriptFieldName());
            bufferNoScript.append("\" type=\"text\" value=\"");
            bufferNoScript.append(Utils.encode(selectedSpacePath));
            bufferNoScript.append("\" name=\"");
            bufferNoScript.append(getNoScriptFieldName());
            bufferNoScript.append("\"/>");
            if (!isValid()) {
                bufferNoScript.append("<div cla8ss =\"messageError\"  >");
                bufferNoScript.append("<span class=\"messageError\">");
                bufferNoScript.append(getPathErrorMessage());
                bufferNoScript.append(this.submitedNoScriptValue);
                bufferNoScript.append("</span>");
                bufferNoScript.append("</div>");
            }
            bufferNoScript.append("</div>");
            context.getResponseWriter().write(bufferNoScript.toString());
        }

    }

    /**
     * @param context
     * @return
     */
    private String getNodePath(FacesContext context) {
        String result = "";
        if (!isValid()) {
            result = submitedNoScriptValue;
        } else if (this.getValue() != null) {

            NodeService nodeService = getNodeService(context);
            UserTransaction tx = null;
            try {
                tx = Repository.getUserTransaction(context, true);
                tx.begin();
                final Path path;
                path = nodeService.getPath((NodeRef) this.getValue());
                Path rootPath = nodeService.getPath(getRootNodeRef());
                int startLevel = rootPath.size() - 1;
                result = PathUtils.getPath(path, startLevel, false);
                // commit the transaction
                tx.commit();
            } catch (Exception ex) {
                try {
                    if (tx != null) {
                        tx.rollback();
                    }
                } catch (Exception tex) {
                }
            }

        }

        return result;
    }

    private NodeRef getRootNodeRef() {
        return new NodeRef(Repository.getStoreRef(), getRootNode());
    }

    /* (non-Javadoc)
     * @see org.alfresco.web.ui.repo.component.AbstractItemSelector#getItemIcon(javax.faces.context.FacesContext, org.alfresco.service.cmr.repository.NodeRef)
     */
    public String getItemIcon(FacesContext context, NodeRef ref) {
        final NodeService fastNodeService = getFastNodeService(context);
        final DictionaryService dictionaryService = getDictionaryService(context);

        final QName type = fastNodeService.getType(ref);

        String icon = (String) fastNodeService.getProperty(ref, ApplicationModel.PROP_ICON);

        if (icon != null) {
            icon = "/images/icons/" + icon + "-16.gif";
        } else if (type.equals(ContentModel.TYPE_FOLDER) || dictionaryService
                .isSubClass(type, ContentModel.TYPE_FOLDER)) {
            icon = WebResources.IMAGE_SPACE;
        } else {
            final String contentName = (String) fastNodeService.getProperty(ref, ContentModel.PROP_NAME);
            icon = FileTypeImageUtils.getFileTypeImage(context, contentName, true);
        }
        return icon;
    }

    public String getRootNode() {
        ValueBinding vb = getValueBinding("rootNode");
        if (vb != null) {
            this.rootNode = (String) vb.getValue(getFacesContext());
        }
        return this.rootNode;
    }

    public void setRootNode(String rootNode) {
        this.rootNode = rootNode;
    }

    public boolean isShowContents() {
        return this.showContents;
    }

    public void setShowContents(boolean showContents) {
        this.showContents = showContents;
    }

    /**
     * We use a unique noscript field name based on our client Id (client ID concatenate with word
     * "noscript"). This is on the assumption that there won't be many selectors on screen at once!
     * Also means we have less values to decode on submit.
     *
     * @return noscript field name
     */
    public String getNoScriptFieldName() {
        return getHiddenFieldName() + "-noscript";
    }

    /**
     * @return the pathLabel
     */
    public String getPathLabel() {
        ValueBinding vb = getValueBinding("pathLabel");
        if (vb != null) {
            this.pathLabel = (String) vb.getValue(getFacesContext());
        }
        return this.pathLabel;
    }

    /**
     * @param pathLabel the pathLabel to set
     */
    public void setPathLabel(String pathLabel) {
        this.pathLabel = pathLabel;
    }

    /**
     * @return the pathErrorMessage
     */
    public String getPathErrorMessage() {
        ValueBinding vb = getValueBinding("pathErrorMessage");
        if (vb != null) {
            this.pathErrorMessage = (String) vb.getValue(getFacesContext());
        }
        return this.pathErrorMessage;
    }

    /**
     * @param pathErrorMessage the pathErrorMessage to set
     */
    public void setPathErrorMessage(String pathErrorMessage) {
        this.pathErrorMessage = pathErrorMessage;
    }
}
