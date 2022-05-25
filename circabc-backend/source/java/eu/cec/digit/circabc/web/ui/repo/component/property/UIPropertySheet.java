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
package eu.cec.digit.circabc.web.ui.repo.component.property;

import eu.cec.digit.circabc.web.bean.navigation.AspectResolver;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNode;
import eu.cec.digit.circabc.web.bean.navigation.NavigableNodeType;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.config.PropertySheetConfigElement;
import org.alfresco.web.config.PropertySheetConfigElement.*;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIAssociation;
import org.alfresco.web.ui.repo.component.property.UIChildAssociation;
import org.alfresco.web.ui.repo.component.property.UIProperty;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigLookupContext;
import org.springframework.extensions.config.ConfigService;

import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * OVERRIDE to take a WAI UIProperty. Component that represents the properties of a Node.
 *
 * @author patrice.coppens@trasys.lu
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 Config was moved to Spring.
 */
public class UIPropertySheet extends org.alfresco.web.ui.repo.component.property.UIPropertySheet {

    public static final String PROPERTY_RENDERER = "eu.cec.digit.circabc.faces.PropertyRenderer";
    public static final String VIEW_MODE = "view";
    public static final String EDIT_MODE = "edit";

    private static final Log logger = LogFactory.getLog(UIPropertySheet.class);
    private static final String DEFAULT_VAR_NAME = "node";
    private static final String PROP_ID_PREFIX = "prop_";
    private static final String ASSOC_ID_PREFIX = "assoc_";
    private static final String SEP_ID_PREFIX = "sep_";

    /**
     * the circabc message bundle name
     */
    private static final String CIRCABC_MESSAGE_BUNDLE = "alfresco.extension.webclient";

    /**
     * Default constructor
     */
    public UIPropertySheet() {
        // set the default renderer for a property sheet
        setRendererType(ComponentConstants.JAVAX_FACES_GRID);
    }

    /**
     * @see javax.faces.component.UIComponent#encodeBegin(javax.faces.context.FacesContext)
     */
    @SuppressWarnings("unchecked")
    public void encodeBegin(FacesContext context) throws IOException {
        int howManyChildren = getChildren().size();
        Boolean externalConfig = (Boolean) getAttributes().get("externalConfig");

        // generate a variable name to use if necessary
        if (getVar() == null) {
            setVar(DEFAULT_VAR_NAME);
        }

        // force retrieval of node info
        Node node = getNode();

        if (howManyChildren == 0) {
            if (externalConfig != null && externalConfig) {
                // configure the component using the config service
                if (logger.isDebugEnabled()) {
                    logger.debug("Configuring property sheet using ConfigService");
                }

                // get the properties to display
                ConfigService configSvc = Application.getConfigService(FacesContext.getCurrentInstance());
                Config configProps = null;
                if (getConfigArea() == null) {
                    configProps = configSvc.getConfig(node);
                } else {
                    // only look within the given area
                    configProps = configSvc.getConfig(node, new ConfigLookupContext(getConfigArea()));
                }

                PropertySheetConfigElement itemsToDisplay = (PropertySheetConfigElement) configProps.
                        getConfigElement("property-sheet");

                if (itemsToDisplay != null) {
                    Collection<ItemConfig> itemsToRender = null;

                    if (this.getMode().equalsIgnoreCase(EDIT_MODE)) {
                        itemsToRender = itemsToDisplay.getEditableItemsToShow().values();

                        if (logger.isDebugEnabled()) {
                            logger.debug("Items to render: " + itemsToDisplay.getEditableItemNamesToShow());
                        }
                    } else {
                        itemsToRender = itemsToDisplay.getItemsToShow().values();

                        if (logger.isDebugEnabled()) {
                            logger.debug("Items to render: " + itemsToDisplay.getItemNamesToShow());
                        }
                    }

                    createComponentsFromConfig(context, itemsToRender, isNodeNameSecured(node));
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("There are no items to render!");
                    }
                }
            } else {
                // show all the properties for the current node
                if (logger.isDebugEnabled()) {
                    logger.debug("Configuring property sheet using node's current state");
                }

                createComponentsFromNode(context, node);
            }
        }

        // put the node in the session if it is not there already
        Map sessionMap = getFacesContext().getExternalContext().getSessionMap();
        sessionMap.put(getVar(), node);

        if (logger.isDebugEnabled()) {
            logger.debug("Put node into session with key '" + getVar() + "': " + node);
        }

        super.encodeBegin(context);
    }


    /**
     * Workaround, since the configuration is bugged and we can't set the name of certain kind of node
     * being read only, we hardcoded this config.
     */
    private boolean isNodeNameSecured(Node node) {
        NavigableNodeType type = null;

        if (node instanceof NavigableNode) {
            type = ((NavigableNode) node).getNavigableNodeType();
        } else {
            type = AspectResolver.resolveType(node);
        }

        return type != null && (type.isIGServiceOrAbove() && !type.equals(NavigableNodeType.IG_ROOT)
                && !type.equals(NavigableNodeType.CATEGORY_HEADER));
    }

    /**
     * Creates all the property components required to display the properties held by the node.
     *
     * @param context JSF context
     * @param node    The Node to show all the properties for
     */
    @SuppressWarnings("unchecked")
    private void createComponentsFromNode(FacesContext context, Node node)
            throws IOException {
        // add all the properties of the node to the UI
        Map<String, Object> props = node.getProperties();
        for (String propertyName : props.keySet()) {
            // create the property component
            UIProperty propComp = (UIProperty) context.getApplication().
                    createComponent(RepoConstants.ALFRESCO_FACES_PROPERTY);
            propComp.setRendererType(PROPERTY_RENDERER);

            // get the property name in it's prefix form
            QName qname = QName.createQName(propertyName);
            String prefixPropName = qname.toPrefixString();

            FacesHelper.setupComponentId(context, propComp, PROP_ID_PREFIX + prefixPropName);
            propComp.setName(prefixPropName);

            // if this property sheet is set as read only, set all properties to read only
            if (isReadOnly()) {
                propComp.setReadOnly(true);
            }

            // NOTE: we don't know what the display label is so don't set it

            this.getChildren().add(propComp);

            if (logger.isDebugEnabled()) {
                logger.debug("Created property component " + propComp + "(" +
                        propComp.getClientId(context) +
                        ") for '" + prefixPropName +
                        "' and added it to property sheet " + this);
            }
        }

        // add all the associations of the node to the UI
        Map associations = node.getAssociations();
        Iterator iter = associations.keySet().iterator();
        while (iter.hasNext()) {
            String assocName = (String) iter.next();
            UIAssociation assocComp = (UIAssociation) context.getApplication().
                    createComponent(RepoConstants.ALFRESCO_FACES_ASSOCIATION);
            assocComp.setRendererType(PROPERTY_RENDERER);

            // get the association name in it's prefix form
            QName qname = QName.createQName(assocName);
            String prefixAssocName = qname.toPrefixString();

            FacesHelper.setupComponentId(context, assocComp, ASSOC_ID_PREFIX + prefixAssocName);
            assocComp.setName(prefixAssocName);

            // if this property sheet is set as read only, set all properties to read only
            if (isReadOnly()) {
                assocComp.setReadOnly(true);
            }

            // NOTE: we don't know what the display label is so don't set it

            this.getChildren().add(assocComp);

            if (logger.isDebugEnabled()) {
                logger.debug("Created association component " + assocComp + "(" +
                        assocComp.getClientId(context) +
                        ") for '" + prefixAssocName +
                        "' and added it to property sheet " + this);
            }
        }

        // add all the child associations of the node to the UI
        Map childAssociations = node.getChildAssociations();
        iter = childAssociations.keySet().iterator();
        while (iter.hasNext()) {
            String assocName = (String) iter.next();
            UIChildAssociation childAssocComp = (UIChildAssociation) context.getApplication().
                    createComponent(RepoConstants.ALFRESCO_FACES_CHILD_ASSOCIATION);
            FacesHelper.setupComponentId(context, childAssocComp, ASSOC_ID_PREFIX + assocName);
            childAssocComp.setName(assocName);
            childAssocComp.setRendererType(PROPERTY_RENDERER);

            // if this property sheet is set as read only, set all properties to read only
            if (isReadOnly()) {
                childAssocComp.setReadOnly(true);
            }

            // NOTE: we don't know what the display label is so don't set it

            this.getChildren().add(childAssocComp);

            if (logger.isDebugEnabled()) {
                logger.debug("Created child association component " + childAssocComp + "(" +
                        childAssocComp.getClientId(context) +
                        ") for '" + assocName +
                        "' and added it to property sheet " + this);
            }
        }
    }

    /**
     * Creates all the property components required to display the properties specified in an external
     * config file.
     *
     * @param context JSF context
     */
    @SuppressWarnings({"unchecked", "static-access"})
    private void createComponentsFromConfig(FacesContext context, Collection<ItemConfig> items,
                                            boolean nameReadOnly)
            throws IOException {
        for (ItemConfig item : items) {
            String id = null;
            PropertySheetItem propSheetItem = null;

            // create the appropriate component
            if (item instanceof PropertyConfig) {
                id = PROP_ID_PREFIX + item.getName();
                propSheetItem = (PropertySheetItem) context.getApplication().
                        createComponent(RepoConstants.ALFRESCO_FACES_PROPERTY);
            } else if (item instanceof AssociationConfig) {
                id = ASSOC_ID_PREFIX + item.getName();
                propSheetItem = (PropertySheetItem) context.getApplication().
                        createComponent(
                                eu.cec.digit.circabc.web.ui.repo.RepoConstants.CIRCAB_FACES_ASSOCIATION);

            } else if (item instanceof ChildAssociationConfig) {
                id = ASSOC_ID_PREFIX + item.getName();
                propSheetItem = (PropertySheetItem) context.getApplication().
                        createComponent(RepoConstants.ALFRESCO_FACES_CHILD_ASSOCIATION);
            } else if (item instanceof SeparatorConfig) {
                id = SEP_ID_PREFIX + item.getName();
                propSheetItem = (PropertySheetItem) context.getApplication().
                        createComponent(RepoConstants.ALFRESCO_FACES_SEPARATOR);
            }

            // now setup the common stuff across all component types
            if (propSheetItem != null) {
                FacesHelper.setupComponentId(context, propSheetItem, id);
                propSheetItem.setRendererType(PROPERTY_RENDERER);
                propSheetItem.setName(item.getName());
                propSheetItem.setConverter(item.getConverter());
                propSheetItem.setComponentGenerator(item.getComponentGenerator());
                propSheetItem.setIgnoreIfMissing(item.getIgnoreIfMissing());
                String displayLabel = item.getDisplayLabel();

                String shortName = item.getName();
                int idx = shortName.indexOf(':');
                if (idx >= 0) {
                    shortName = shortName.substring(idx + 1);
                }
                final String labelId =
                        item.getDisplayLabelId() == null ? shortName : item.getDisplayLabelId();
                final ResourceBundle circabcWebBundle = Application.getBundle(context)
                        .getBundle(CIRCABC_MESSAGE_BUNDLE);
                String circabcLabel = null;

                try {
                    circabcLabel = circabcWebBundle.getString(labelId.toLowerCase());
                } catch (java.util.MissingResourceException ex) {
                    // just log
                    if (logger.isInfoEnabled()) {
                        logger.info(
                                "Can't find resource for bundle java.util.PropertyResourceBundle, key: " + labelId);
                    }
                }

                if (circabcLabel != null) {
                    displayLabel = circabcLabel;
                } else if (item.getDisplayLabelId() != null) {
                    String label = Application.getMessage(context, item.getDisplayLabelId());
                    if (label != null) {
                        displayLabel = label;
                    }
                }

                propSheetItem.setDisplayLabel(displayLabel);

                // if this property sheet is set as read only or the config says the property
                // should be read only set it as such
                if (isReadOnly() || item.isReadOnly() || (nameReadOnly && item.getName().equals("name"))) {
                    propSheetItem.setReadOnly(true);
                }

                this.getChildren().add(propSheetItem);

                if (logger.isDebugEnabled()) {
                    logger.debug("Created property sheet item component " + propSheetItem + "(" +
                            propSheetItem.getClientId(context) +
                            ") for '" + item.getName() +
                            "' and added it to property sheet " + this);
                }
            }
        }
    }


}
