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


import eu.cec.digit.circabc.web.bean.navigation.AspectResolver;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.config.ActionsConfigElement;
import org.alfresco.web.config.ActionsConfigElement.ActionDefinition;
import org.alfresco.web.config.ActionsConfigElement.ActionGroup;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.repo.component.evaluator.ActionInstanceEvaluator;
import org.alfresco.web.ui.repo.component.evaluator.PermissionEvaluator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_impl.taglib.UIComponentTagUtils;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.webscripts.ui.common.ConstantMethodBinding;

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author pignot yanick
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 Config was moved to Spring. ConstantMethodBinding was moved
 * to Spring. This class seems to be developed for CircaBC
 */
public class UIActionsExtended extends org.alfresco.web.ui.repo.component.UIActions {

    // ------------------------------------------------------------------------------
    // Component implementation

    private static final String ATTR_VALUE = "value";

    private static final Log logger = LogFactory.getLog(UIActions.class);

    private static final String ATTR_SHOWLINK = "showLink";
    private static final String ATTR_STYLECLASS = "styleClass";
    private static final String ATTR_STYLE = "style";
    private static final String ACTION_CONTEXT = "actionContext";


    private static final String CONTEXTID_DEFAULT = "_default";
    private static short id = 0;

    /**
     * @return a unique ID for a JSF component
     */
    private static String createUniqueId() {
        return "id_" + Short.toString(++id);
    }

    protected String comptuteType(Node node) {
        NavigableNodeType cType = AspectResolver.resolveType(node);
        if (cType != null) {
            if (cType.isIGServiceOrAbove()) {
                return cType.getComparatorQName().toString();
            } else {
                return NavigableNodeType.CIRCABC_CHILD.getComparatorQName().toString();
            }
        } else {
            return node.getType().toString();
        }

    }

    /**
     * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
     */
    @SuppressWarnings("unchecked")
    public void encodeBegin(FacesContext context) throws IOException {
        if (isRendered() == false) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("encodeBegin() for <r:actions/> Id: " + getId() + " groupId: " + getValue());
        }

        // put the context object into the requestMap so it is accessable
        // by any child component value binding expressions
        Object actionContext = getContext();
        Map requestMap = getFacesContext().getExternalContext().getRequestMap();
        requestMap.put(ACTION_CONTEXT, actionContext);

        String contextId;
        if (actionContext instanceof Node) {
            contextId = comptuteType((Node) actionContext);
        } else {
            contextId = CONTEXTID_DEFAULT;
        }

        String groupId = getValue();
        if (groupId != null && groupId.length() != 0) {
            Config config;
            if (actionContext instanceof Node) {
                config = Application.getConfigService(context).getConfig(actionContext);
            } else {
                config = Application.getConfigService(context).getGlobalConfig();
            }
            if (config != null) {
                // find the Actions specific config element
                ActionsConfigElement actionConfig =
                        (ActionsConfigElement) config.getConfigElement(ActionsConfigElement.CONFIG_ELEMENT_ID);
                if (actionConfig != null) {
                    // and lookup our ActionGroup by Id
                    ActionGroup actionGroup = actionConfig.getActionGroup(groupId);
                    if (actionGroup != null) {
                        // render the action group component tree
                        if (logger.isDebugEnabled()) {
                            logger
                                    .debug("-constructing ActionGroup: " + groupId + " for ContextId: " + contextId);
                        }
                        buildActionGroup(context, actionConfig, actionGroup, contextId);
                    } else {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Unable to find specified Action Group config ID: " + groupId);
                        }
                    }
                }
            }
        }
    }

    /**
     * @see javax.faces.component.UIComponentBase#encodeChildren(javax.faces.context.FacesContext)
     */
    public void encodeChildren(FacesContext context) throws IOException {
        ResponseWriter out = context.getResponseWriter();
        int verticalSpacing = getVerticalSpacing();
        if (verticalSpacing != 0) {
            out.write("<table cellspacing='");
            out.write(verticalSpacing);
            out.write("'");
            if (getAttributes().get(ATTR_STYLE) != null) {
                outputAttribute(out, getAttributes().get(ATTR_STYLE), ATTR_STYLE);
            }
            if (getAttributes().get(ATTR_STYLECLASS) != null) {
                outputAttribute(out, getAttributes().get(ATTR_STYLECLASS), "class");
            }
            out.write(">");
        }

        // use the current context Id to find the correct component group to render
        Map requestMap = getFacesContext().getExternalContext().getRequestMap();
        Object actionContext = requestMap.get(ACTION_CONTEXT);
        String contextId = CONTEXTID_DEFAULT;
        if (actionContext instanceof Node) {
            contextId = comptuteType((Node) actionContext);
        }

        for (Object o : getChildren()) {
            UIComponent child = (UIComponent) o;
            if (contextId.equals(child.getAttributes().get("contextId"))) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Rendering actions group for contextId: " + contextId);
                }
                Utils.encodeRecursive(context, child);
                break;
            }
        }

        if (verticalSpacing != 0) {
            out.write("</table>");
        }
    }

    // ------------------------------------------------------------------------------
    // Private data

    /**
     * Build an action group as reusable UIActionLink components.
     */
    @SuppressWarnings("unchecked")
    private void buildActionGroup(
            FacesContext context, ActionsConfigElement config, ActionGroup actionGroup, String contextId)
            throws IOException {
        javax.faces.application.Application facesApp = context.getApplication();
        ResourceBundle messages = Application.getBundle(context);

        // get overriding display attributes
        String style = (String) getAttributes().get(ATTR_STYLE);
        String styleClass = (String) getAttributes().get(ATTR_STYLECLASS);
        Boolean showLink = null;
        if (getAttributes().get(ATTR_SHOWLINK) != null) {
            showLink = (Boolean) getAttributes().get(ATTR_SHOWLINK);
        }

        // build parent wrapper component
        HtmlPanelGroup wrapper = (HtmlPanelGroup) facesApp
                .createComponent(ComponentConstants.JAVAX_FACES_PANELGROUP);
        wrapper.setId(createUniqueId());
        wrapper.getAttributes().put("contextId", contextId);
        this.getChildren().add(wrapper);
        //this.groups.add(contextId);

        // process each ActionDefinition in the order they were defined
        for (String actionId : actionGroup) {
            if (logger.isDebugEnabled()) {
                logger.debug("---processing ActionDefinition: " + actionId);
            }

            ActionDefinition actionDef = config.getActionDefinition(actionId);
            if (actionDef == null) {
                throw new AlfrescoRuntimeException(
                        "Unable to find configured ActionDefinition Id: " + actionId);
            }

            UIComponent currentParent = wrapper;

            // build a permissions evaluator component to wrap the actionlink
            PermissionEvaluator permEval = null;
            List<String> allow = actionDef.getAllowPermissions();
            if (allow != null && allow.size() != 0) {
                // found some permissions to test
                permEval = (PermissionEvaluator) facesApp.createComponent(COMPONENT_PERMISSIONEVAL);
                StringBuilder condition = new StringBuilder(allow.get(0));
                if (allow.size() != 1) {
                    for (int i = 1; i < allow.size(); i++) {
                        condition.append(",").append(allow.get(i));
                    }
                }
                permEval.setAllow(condition.toString());
            }
            List<String> deny = actionDef.getDenyPermissions();
            if (deny != null && deny.size() != 0) {
                if (permEval == null) {
                    permEval = (PermissionEvaluator) facesApp.createComponent(COMPONENT_PERMISSIONEVAL);
                }
                StringBuilder condition = new StringBuilder(128);
                condition.append(deny.get(0));
                if (deny.size() != 1) {
                    for (int i = 1; i < deny.size(); i++) {
                        condition.append(",");
                        condition.append(deny.get(i));
                    }
                }
                permEval.setDeny(condition.toString());
            }
            if (permEval != null) {
                // add the permission evaluator component and walk down the hierarchy
                permEval.setId(createUniqueId());
                permEval
                        .setValueBinding(ATTR_VALUE, facesApp.createValueBinding("#{" + ACTION_CONTEXT + "}"));
                if (logger.isDebugEnabled()) {
                    logger.debug("-----adding PermissionEvaluator to action");
                }
                currentParent.getChildren().add(permEval);
                currentParent = permEval;
            }

            // now prepare any code based evaluators that may be present
            if (actionDef.Evaluator != null) {
                ActionInstanceEvaluator evaluator =
                        (ActionInstanceEvaluator) facesApp.createComponent(COMPONENT_ACTIONEVAL);
                evaluator.setId(createUniqueId());
                evaluator.setEvaluator(actionDef.Evaluator);
                evaluator
                        .setValueBinding(ATTR_VALUE, facesApp.createValueBinding("#{" + ACTION_CONTEXT + "}"));

                // add the action evaluator component and walk down the hiearchy
                if (logger.isDebugEnabled()) {
                    logger.debug("-----adding ActionEvaluator to action");
                }
                currentParent.getChildren().add(evaluator);
                currentParent = evaluator;
            }

            // now build the UIActionLink component for this action
            UIActionLink control = (UIActionLink) facesApp.createComponent(COMPONENT_ACTIONLINK);

            control.setRendererType(RENDERER_ACTIONLINK);
            control.setId(actionDef.getId() + createUniqueId());

            if (actionDef.Action != null) {
                if (UIComponentTagUtils.isValueReference(actionDef.Action)) {
                    control.setAction(facesApp.createMethodBinding(actionDef.Action, null));
                } else {
                    control.setAction(new ConstantMethodBinding(actionDef.Action));
                }
            }
            if (actionDef.ActionListener != null) {
                control.setActionListener(
                        facesApp.createMethodBinding(actionDef.ActionListener, ACTION_CLASS_ARGS));
            }

            if (style != null) {
                control.getAttributes().put(ATTR_STYLE, style);
            } else if (actionDef.Style != null) {
                control.getAttributes().put(ATTR_STYLE, actionDef.Style);
            }
            if (styleClass != null) {
                control.getAttributes().put(ATTR_STYLECLASS, styleClass);
            } else if (actionDef.StyleClass != null) {
                control.getAttributes().put(ATTR_STYLECLASS, actionDef.StyleClass);
            }
            if (showLink != null) {
                control.setShowLink(showLink);
            } else {
                control.setShowLink(actionDef.ShowLink);
            }

            if (actionDef.Onclick != null) {
                if (UIComponentTagUtils.isValueReference(actionDef.Onclick)) {
                    control.setValueBinding("onclick", facesApp.createValueBinding(actionDef.Onclick));
                } else {
                    control.setOnclick(actionDef.Onclick);
                }
            }

            if (actionDef.Href != null) {
                if (UIComponentTagUtils.isValueReference(actionDef.Href)) {
                    control.setValueBinding("href", facesApp.createValueBinding(actionDef.Href));
                } else {
                    control.setHref(actionDef.Href);
                }
            } else if (actionDef.Script != null && actionDef.Script.length() != 0) {
                // found a script reference - may be a Path or a NodeRef
                StringBuilder scriptHref = new StringBuilder(100);
                scriptHref.append("/command/script/execute");
                if (actionDef.Script.charAt(0) == '/') {
                    // found a Path - encode it as a URL argument
                    scriptHref.append("?scriptPath=");
                    scriptHref
                            .append(Utils.replace(URLEncoder.encode(actionDef.Script, "UTF-8"), "+", "%20"));
                } else {
                    // found a NodeRef string, encode as URL elements
                    NodeRef ref = new NodeRef(actionDef.Script);
                    scriptHref.append('/').append(ref.getStoreRef().getProtocol())
                            .append('/').append(ref.getStoreRef().getIdentifier())
                            .append('/').append(ref.getId());
                }
                // set the full script execution URL as the href for the control
                control.setHref(scriptHref.toString());
            }

            control.setTarget(actionDef.Target);
            control.setImage(actionDef.Image);

            if (actionDef.TooltipMsg != null) {
                control.setTooltip(messages.getString(actionDef.TooltipMsg));
            } else if (actionDef.Tooltip != null) {
                if (UIComponentTagUtils.isValueReference(actionDef.Tooltip)) {
                    control.setValueBinding("tooltip", facesApp.createValueBinding(actionDef.Tooltip));
                } else {
                    control.setValue(actionDef.Tooltip);
                }
            }
            if (actionDef.LabelMsg != null) {
                control.setValue(messages.getString(actionDef.LabelMsg));
            } else if (actionDef.Label != null) {
                if (UIComponentTagUtils.isValueReference(actionDef.Label)) {
                    control.setValueBinding("value", facesApp.createValueBinding(actionDef.Label));
                } else {
                    control.setValue(actionDef.Label);
                }
            }

            // build any child params <f:param> components that are needed.
            Map<String, String> params = actionDef.getParams();
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    UIParameter param =
                            (UIParameter) facesApp.createComponent(ComponentConstants.JAVAX_FACES_PARAMETER);
                    param.setId(createUniqueId());
                    param.setName(entry.getKey());
                    String value = entry.getValue();
                    if (UIComponentTagUtils.isValueReference(value)) {
                        param.setValueBinding(ATTR_VALUE, facesApp.createValueBinding(value));
                    } else {
                        param.setValue(value);
                    }
                    control.getChildren().add(param);
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("-----adding UIActionLink component for: " + actionId);
            }
            currentParent.getChildren().add(control);
        }
    }
}
