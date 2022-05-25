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
package eu.cec.digit.circabc.web.wai.menu;

import org.alfresco.util.GUID;

import java.io.Serializable;

/**
 * Wrap an action link element in the left menu
 *
 * @author yanick pignot
 */
public class ActionWrapper implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3978709719598425383L;

    private PermissionEvaluatorWrapper permissionEvaluatorWrapper;
    private ActionLinkWrapper actionLinkWrapper;
    private ParameterWrapper parametersWrapper;


    /**
     * Wrap an action without paremeters
     */
    public ActionWrapper(String allowPermission, String linkValue, String action,
                         String actionListener, String tooltip) {
        this(allowPermission, linkValue, action, actionListener, tooltip, null, null);
    }

    /**
     * Wrap an action without paremeters and but with icon
     */
    public ActionWrapper(String allowPermission, String linkValue, String action,
                         String actionListener, String tooltip, String icon, boolean showLink) {
        this(allowPermission, linkValue, action, actionListener, tooltip, icon, showLink, null, null);
    }

    /**
     * Wrap an action with only one parameter.
     */
    public ActionWrapper(String allowPermission, String linkValue, String action,
                         String actionListener, String tooltip, String paramName, Serializable paramValue) {
        this(allowPermission, linkValue, action, actionListener, tooltip, null, true, paramName,
                paramValue);
    }

    /**
     * Wrap an action with only one parameter and add an icon to this action
     */
    public ActionWrapper(String allowPermission, String linkValue, String action,
                         String actionListener, String tooltip, String icon, boolean showLink, String paramName,
                         Serializable paramValue) {
        super();
        this.permissionEvaluatorWrapper = new PermissionEvaluatorWrapper(allowPermission);
        this.actionLinkWrapper = new ActionLinkWrapper(linkValue, action, actionListener, tooltip, icon,
                showLink);
        this.parametersWrapper = new ParameterWrapper(paramName, paramValue);
    }

    /**
     * @return the actionLinkWrapper
     */
    public ActionLinkWrapper getLink() {
        return actionLinkWrapper;
    }

    /**
     * @return the permissionEvaluatorWrapper
     */
    public PermissionEvaluatorWrapper getPermission() {
        return permissionEvaluatorWrapper;
    }

    /**
     * @return the parametersEvaluatorWrapper
     */
    public ParameterWrapper getParameter() {
        return parametersWrapper;
    }

    public boolean isOnlyActionListener() {
        return getLink().getAction() == null;
    }

    public boolean isNotOnlyActionListener() {
        return getLink().getAction() != null;
    }


    /**
     * Wrap the PermissionEvaluator values:
     * <p>
     * ie: <circa:permissionEvaluator value="#{backBean.node}" allow="#{actionWrapper.permission.allow}"
     * id="#{actionWrapper.permission.id}" />
     */
    public static class PermissionEvaluatorWrapper implements Serializable {

        private static final long serialVersionUID = -3086255650889990302L;
        private String allowPermission;
        private String id;

        /**
         * @param allowPermission
         */
        public PermissionEvaluatorWrapper(String allowPermission) {
            super();
            this.allowPermission = allowPermission;
            //this.id = allowPermission + System.currentTimeMillis();
            this.id = allowPermission + GUID.generate();
        }

        /**
         * @return the allowPermission
         */
        public String getAllow() {
            return allowPermission;
        }

        /**
         * @return the id
         */
        public String getId() {
            return id;
        }
    }

    /**
     * Wrap an action parameter unit Evaluator values:
     * <p>
     * ie: <circa:param id="actionWrapper.parameters.id" name="actionWrapper.parameters.name"
     * value="actionWrapper.parameters.vale" />
     */
    public static class ParameterWrapper implements Serializable {

        private static final long serialVersionUID = 4700922055967201996L;

        private String name;
        private Serializable value;
        private String id;

        /**
         * @param name
         * @param value
         */
        public ParameterWrapper(String name, Serializable value) {
            super();
            this.name = (name == null) ? "" : name;
            this.value = (value == null) ? "" : value;
            this.id = this.name + this.value.toString() + GUID.generate();
        }


        /**
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * @return the vale
         */
        public Serializable getValue() {
            return value;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

    }

    /**
     * Wrap the ActionLink values:
     * <p>
     * ie:	<circa:actionLink value="#{actionWrapper.link.value}" image="/images/icons/admin_console.gif"
     * showLink="false" action="#{actionWrapper.link.action}" actionListener="#{actionWrapper.link.actionListener}"
     * tooltip="#{actionWrapper.link.tooltip}" id="#{actionWrapper.link.id}" />
     */
    public static class ActionLinkWrapper implements Serializable {

        private static final long serialVersionUID = 4385375291158360372L;
        private final String value;
        private final String actionListener;
        private final String tooltip;
        private final String icon;
        private String action;
        private String id;
        private boolean showLink;

        /**
         * @param value
         * @param actionListener
         * @param tooltip
         */
        public ActionLinkWrapper(String value, String action, String actionListener, String tooltip,
                                 String icon, boolean showLink) {
            super();
            this.value = value;
            this.action = action;
            this.icon = icon;
            this.showLink = showLink;

            if (action == null || action.length() < 0) {
                this.action = "";
            } else {
                this.action = action;
            }
            if (actionListener == null || actionListener.length() < 0) {
                this.actionListener = "#{WaiLeftMenuBean.doNothingAction}";
            } else {
                this.actionListener = "#{" + actionListener + "}";
            }

            if (this.action == null && this.actionListener == null) {
                throw new IllegalArgumentException(
                        "At least an Action and/or an action listener must be setted .");
            }

            this.tooltip = tooltip;
            this.id = (actionListener != null) ? actionListener : action;

            this.id = "id-" + this.id.replace('.', '_').replace(':', '_') + GUID.generate();
        }

        /**
         * @return the actionListener
         */
        public String getActionListener() {
            return this.actionListener;
        }

        /**
         * @return the id
         */
        public String getId() {
            return this.id;
        }

        /**
         * @return the tooltip
         */
        public String getTooltip() {
            return this.tooltip;
        }

        /**
         * @return the value
         */
        public String getValue() {
            return this.value;
        }

        /**
         * @return the action
         */
        public String getAction() {
            return this.action;
        }

        /**
         * @return the icon
         */
        public String getIcon() {
            return icon;
        }

        /**
         * @return the showLink
         */
        public boolean isShowLink() {
            return showLink;
        }

    }


}
