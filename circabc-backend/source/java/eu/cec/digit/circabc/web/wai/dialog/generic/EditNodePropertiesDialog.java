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
package eu.cec.digit.circabc.web.wai.dialog.generic;

import eu.cec.digit.circabc.aspect.ContentNotifyAspect;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.app.CircabcService;
import eu.cec.digit.circabc.service.notification.NotifiableUser;
import eu.cec.digit.circabc.service.notification.NotificationService;
import eu.cec.digit.circabc.service.notification.NotificationSubscriptionService;
import eu.cec.digit.circabc.web.Beans;
import eu.cec.digit.circabc.web.Services;
import eu.cec.digit.circabc.web.app.CircabcNavigationHandler;
import eu.cec.digit.circabc.web.bean.override.CircabcBrowseBean;
import eu.cec.digit.circabc.web.validator.URLValidator;
import eu.cec.digit.circabc.web.wai.bean.navigation.CategoryHeadersBeanData;
import eu.cec.digit.circabc.web.wai.dialog.BaseWaiDialog;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIListItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.surf.util.I18NUtil;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import java.io.Serializable;
import java.util.*;

/**
 * Bean to handle the generic edit node properties process on the WAI part.
 *
 * @author yanick pignot
 * <p>
 * Migration 3.1 -> 3.4.6 - 02/12/2011 I18NUtil was moved to Spring. Config was moved to Spring.
 * This class seems to be developed for CircaBC
 */
public class EditNodePropertiesDialog extends BaseWaiDialog {

    public static final String BEAN_NAME = "EditNodePropertiesDialog";
    public static final String DIALOG_NAME = "editNodePropertiesWai";
    public static final String DIALOG_CALL = CircabcNavigationHandler.WAI_DIALOG_PREFIX + DIALOG_NAME;
    public static final String DEFAULT_SPACE_ICON_NAME = "space-icon-default";
    public static final String DEFAULT_SPACE_LINK_ICON_NAME = "space-icon-link";
    public static final String DEFAULT_SPACE_ICON_PATH = "";
    public static final String DEFAULT_SPACE_TYPE_ICON_PATH = "/images/icons/space.gif";
    public static final String NOTIFY_AFTER_SETTING_PROPERTIES = "notify";
    protected static final String TEMP_PROP_MIMETYPE = "mimetype";
    protected static final String TEMP_PROP_ENCODING = "encoding";
    protected static final Map<ManagedNodes, String> ICONS_CONFIG = new HashMap<>(10);
    protected static final String DEFAULT_ID_SUFFIX = "node-props";
    protected static final Map<ManagedNodes, String> ID_PREFIX = new HashMap<>(5);
    protected static final String FILENAME_REGEX = "(.*[\\\"\\*\\\\\\>\\<\\?\\/\\:\\|]+.*)|(.*[\\.]?.*[\\.]+$)|(.*[ ]+$)";
    private static final long serialVersionUID = -1111740078894071999L;
    private static final Log logger = LogFactory.getLog(EditNodePropertiesDialog.class);
    //	bugfix DIGIT-CIRCABC-352
    private static final int MAX_LEN = 1000000000;

    static {
        ICONS_CONFIG.put(ManagedNodes.DOSSIER, "cm:folder icons");
        ICONS_CONFIG.put(ManagedNodes.SPACE, "cm:folder icons");
        ICONS_CONFIG.put(ManagedNodes.LINK, "app:space link icons");
        ICONS_CONFIG.put(ManagedNodes.FORUMS, "fm:forums icons");
        ICONS_CONFIG.put(ManagedNodes.FORUM, "fm:forum icons");
        ICONS_CONFIG.put(ManagedNodes.TOPIC, "fm:topic icons");
    }

    static {
        ID_PREFIX.put(ManagedNodes.CONTENT, "content-props");
        ID_PREFIX.put(ManagedNodes.LINK, "content-props");
        ID_PREFIX.put(ManagedNodes.TRANSLATION, "content-props");
        ID_PREFIX.put(ManagedNodes.EMPTY_TRANSLATION, "content-props");
        ID_PREFIX.put(ManagedNodes.ML_CONTAINER, "content-props");
        ID_PREFIX.put(ManagedNodes.SPACE, "space-props");
        ID_PREFIX.put(ManagedNodes.DOSSIER, "space-props");
    }

    protected String icon;
    boolean wasNotifyEnable = false;
    private boolean notify = false;
    private NotificationService notificationService;
    private NotificationSubscriptionService notificationSubscriptionService;
    private Node editableNode;
    private ManagedNodes actionNodeType;
    private Boolean titleChanged;
    private NodeRef igNodeRef;
    private NodeRef catNodeRef;
    private CircabcService circabcService;
    private BehaviourFilter policyBehaviourFilter;

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);

        titleChanged = false;

        igNodeRef = null;
        catNodeRef = null;

        if (parameters != null) {
            actionNodeType = null;
            icon = null;
            editableNode = null;

            if (getActionNode() == null) {
                throw new NullPointerException("The node id is a mandatory parameter");
            }

            editableNode = new Node(getActionNode().getNodeRef());

            actionNodeType = ManagedNodes.resolve(getActionNode());

            if (actionNodeType == null) {
                throw new IllegalArgumentException(
                        "This kind of node is not managed by this dialog. Node Type : " + getActionNode()
                                .getType().getLocalName() + " . Expected: " + Arrays
                                .toString(ManagedNodes.values()));
            }

            if (ManagedNodes.EMPTY_TRANSLATION.equals(actionNodeType) || ManagedNodes.TRANSLATION
                    .equals(actionNodeType)) {
                // let the translations to be managed as a content
                actionNodeType = ManagedNodes.CONTENT;
            }

            icon = (String) editableNode.getProperties().get("icon");

            // fix for DIGIT-CIRCABC-2139
            super.getNodeService()
                    .setProperty(getActionNode().getNodeRef(), ContentModel.PROP_AUTO_VERSION_PROPS, false);

            //special case for Mimetype - since this is a sub-property of the ContentData object
            // we must extract it so it can be edited in the client, then we check for it later
            // and create a new ContentData object to wrap it and it's associated URL
            setMimeTypeAndEncoding();

            final String propertyNotifyAsString = parameters.get(NOTIFY_AFTER_SETTING_PROPERTIES);
            if (propertyNotifyAsString != null) {
                notify = Boolean.valueOf(propertyNotifyAsString);
            }

            setServiceAndActivity();

        }
    }

    private void setMimeTypeAndEncoding() {
        ContentData content = (ContentData) this.editableNode.getProperties()
                .get(ContentModel.PROP_CONTENT.toString());
        if (content != null) {
            this.editableNode.getProperties().put(TEMP_PROP_MIMETYPE, content.getMimetype());
            this.editableNode.getProperties().put(TEMP_PROP_ENCODING, content.getEncoding());
        }
    }

    public void restored() {
        super.restored();
        setMimeTypeAndEncoding();
    }

    public BehaviourFilter getPolicyBehaviourFilter() {
        if (policyBehaviourFilter == null) {
            policyBehaviourFilter = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getPolicyBehaviourFilter();
        }
        return policyBehaviourFilter;
    }

    @Override
    protected String finishImpl(final FacesContext context, final String outcome) throws Exception {
        wasNotifyEnable = !getPolicyBehaviourFilter().isEnabled();
        getPolicyBehaviourFilter().disableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);

        NodeRef nodeRef = getActionNode().getNodeRef();
        boolean isIGRoot = getNodeService().hasAspect(nodeRef, CircabcModel.ASPECT_IGROOT);

        if (!isIGRoot && !isNameValid()) {
            // Found fordidden character
            Utils.addErrorMessage(translate("add_content_error_filename_invalid"));
            this.isFinished = false;
            return null;
        }

        if (!isIGRoot && !isCleanNameHTML()) {
            // Found fordidden character
            Utils.addErrorMessage(translate(MSG_ERR_INVALID_VALUE_FOR, translate("name")));
            this.isFinished = false;
            return null;
        }

        if (!isIGRoot && !isCleanDescriptionHTML()) {

            Utils.addErrorMessage(translate(MSG_ERR_INVALID_VALUE_FOR, translate("description")));
            this.isFinished = false;
            return null;
        }

        if (!isIGRoot && !isCleanTitleHTML()) {
            Utils.addErrorMessage(translate(MSG_ERR_INVALID_VALUE_FOR, translate("title")));
            this.isFinished = false;
            return null;
        }

        if (!isIGRoot && !isCleanContactInfoHTML()) {
            Utils.addErrorMessage(translate(MSG_ERR_INVALID_VALUE_FOR, translate("contact")));
            this.isFinished = false;
            return null;
        }

        //check mandatory fields
        if (!fieldMandatoryIsNotMissing(context)) {
            return null;
        }

        // check text length
        // bugfix DIGIT-CIRCABC-352
        if (!isIGRoot && hasTextTooLong(context)) {
            return null;
        }
        final QName type = getActionNode().getType();
        if (ContentModel.TYPE_CONTENT.equals(type) || getDictionaryService()
                .isSubClass(type, ContentModel.TYPE_CONTENT)) {
            editContentProperties();
        } else {
            try {
                editContainerProperties(isIGRoot);
            } catch (LightDescriptionSizeExceedException e) {
                Utils.addErrorMessage(translate("lightDescription_limit_exceed"));

                return null;
            }

        }

        if ((getNodeService()
                .hasAspect(this.getActionNode().getNodeRef(), CircabcModel.ASPECT_CATEGORY))) {
            catNodeRef = this.getActionNode().getNodeRef();
        } else if ((getNodeService()
                .hasAspect(this.getActionNode().getNodeRef(), CircabcModel.ASPECT_IGROOT))) {
            igNodeRef = this.getActionNode().getNodeRef();
        }

        if ((getNodeService()
                .hasAspect(this.getActionNode().getNodeRef(), CircabcModel.ASPECT_CATEGORY))
                && titleChanged) {
            final CategoryHeadersBeanData categoryHeadersBeanData = (CategoryHeadersBeanData) Beans
                    .getBean("CategoryHeadersBeanData");
            categoryHeadersBeanData.reset();
            titleChanged = false;

        }

        return CircabcNavigationHandler.CLOSE_WAI_DIALOG_OUTCOME
                + CircabcNavigationHandler.OUTCOME_SEPARATOR
                + CircabcBrowseBean.PREFIXED_WAI_BROWSE_OUTCOME;
    }

    private boolean isCleanTitleHTML() {
        return isClean(ContentModel.PROP_TITLE.toString());
    }

    private boolean isCleanContactInfoHTML() {
        return isClean(CircabcModel.PROP_CONTACT_INFORMATION.toString());
    }

    private boolean isCleanDescriptionHTML() {
        return isClean(ContentModel.PROP_DESCRIPTION.toString());
    }

    private boolean isCleanNameHTML() {
        return isClean(ContentModel.PROP_NAME.toString());
    }

    private boolean isClean(String qnameString) {
        final String value = (String) this.editableNode.getProperties().get(qnameString);
        return (value == null) || (value != null && isCleanHTML(value, false));
    }

    public void setPropetyDefinedOutside(final QName propertyDef) {
        final Serializable newValue = getNodeService()
                .getProperty(editableNode.getNodeRef(), propertyDef);

        editableNode.getProperties().put(propertyDef.toString(), newValue);
    }

    protected void editContentProperties() throws Exception {
        NodeRef nodeRef = this.editableNode.getNodeRef();
        Map<String, Object> editedProps = this.editableNode.getProperties();
        Map<QName, Serializable> repoProps = this.getNodeService().getProperties(nodeRef);

        // get the name and move the node as necessary
        String name = (String) editedProps.get(ContentModel.PROP_NAME.toString());
        if (name != null) {
            getFileFolderService().rename(nodeRef, name);
        }

        // we need to put all the properties from the editable bag back into
        // the format expected by the repository

        logRecordSetInfo(repoProps, editedProps);

        // Extract and deal with the special mimetype property for ContentData
        String mimetype = (String) editedProps.get(TEMP_PROP_MIMETYPE);
        if (mimetype != null) {
            // remove temporary prop from list so it isn't saved with the others
            editedProps.remove(TEMP_PROP_MIMETYPE);
            ContentData contentData = (ContentData) editedProps.get(ContentModel.PROP_CONTENT.toString());
            if (contentData != null) {
                contentData = ContentData.setMimetype(contentData, mimetype);
                editedProps.put(ContentModel.PROP_CONTENT.toString(), contentData);
            }
        }
        // Extract and deal with the special encoding property for ContentData
        String encoding = (String) editedProps.get(TEMP_PROP_ENCODING);
        if (encoding != null) {
            // remove temporary prop from list so it isn't saved with the others
            editedProps.remove(TEMP_PROP_ENCODING);
            ContentData contentData = (ContentData) editedProps.get(ContentModel.PROP_CONTENT.toString());
            if (contentData != null) {
                contentData = ContentData.setEncoding(contentData, encoding);
                editedProps.put(ContentModel.PROP_CONTENT.toString(), contentData);
            }
        }

        // add the "author" aspect if required, properties will get set below
        if (this.getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_AUTHOR) == false) {
            this.getNodeService().addAspect(nodeRef, ContentModel.ASPECT_AUTHOR, null);
        }

        // add the "titled" aspect if required, properties will get set below
        if (this.getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_TITLED) == false) {
            getNodeService().addAspect(nodeRef, ContentModel.ASPECT_TITLED, null);
        }

        // add the remaining properties

        addRemainingProperties(editedProps, repoProps);

        // send the properties back to the repository
        this.getNodeService().setProperties(nodeRef, repoProps);

        // we also need to persist any association changes that may have been made

        // add any associations added in the UI
        Map<String, Map<String, AssociationRef>> addedAssocs = this.editableNode.getAddedAssociations();
        for (Map<String, AssociationRef> typedAssoc : addedAssocs.values()) {
            for (AssociationRef assoc : typedAssoc.values()) {
                this.getNodeService()
                        .createAssociation(assoc.getSourceRef(), assoc.getTargetRef(), assoc.getTypeQName());
            }
        }

        // remove any association removed in the UI
        Map<String, Map<String, AssociationRef>> removedAssocs = this.editableNode
                .getRemovedAssociations();
        for (Map<String, AssociationRef> typedAssoc : removedAssocs.values()) {
            for (AssociationRef assoc : typedAssoc.values()) {
                this.getNodeService()
                        .removeAssociation(assoc.getSourceRef(), assoc.getTargetRef(), assoc.getTypeQName());
            }
        }

        // add any child associations added in the UI
        Map<String, Map<String, ChildAssociationRef>> addedChildAssocs = this.editableNode
                .getAddedChildAssociations();
        for (Map<String, ChildAssociationRef> typedAssoc : addedChildAssocs.values()) {
            for (ChildAssociationRef assoc : typedAssoc.values()) {
                this.getNodeService()
                        .addChild(assoc.getParentRef(), assoc.getChildRef(), assoc.getTypeQName(),
                                assoc.getTypeQName());
            }
        }

        // remove any child association removed in the UI
        Map<String, Map<String, ChildAssociationRef>> removedChildAssocs = this.editableNode
                .getRemovedChildAssociations();
        for (Map<String, ChildAssociationRef> typedAssoc : removedChildAssocs.values()) {
            for (ChildAssociationRef assoc : typedAssoc.values()) {
                this.getNodeService().removeChild(assoc.getParentRef(), assoc.getChildRef());
            }
        }
    }


    private void addRemainingProperties(Map<String, Object> editedProps,
                                        Map<QName, Serializable> repoProps) {

        for (Map.Entry<String, Object> item : editedProps.entrySet()) {
            String propName = item.getKey();
            QName qname = QName.createQName(propName);

            // make sure the property is represented correctly
            Serializable propValue = (Serializable) item.getValue();

            // check for empty strings when using number types, set to null in this case
            if (propValue instanceof String) {
                PropertyDefinition propDef = this.getDictionaryService().getProperty(qname);

                if (((String) propValue).length() == 0) {
                    if (propDef != null) {
                        if (propDef.getDataType().getName().equals(DataTypeDefinition.DOUBLE) ||
                                propDef.getDataType().getName().equals(DataTypeDefinition.FLOAT) ||
                                propDef.getDataType().getName().equals(DataTypeDefinition.INT) ||
                                propDef.getDataType().getName().equals(DataTypeDefinition.LONG)) {
                            propValue = null;
                        }
                    }
                }
                // handle locale strings to Locale objects
                else if (propDef != null && propDef.getDataType().getName()
                        .equals(DataTypeDefinition.LOCALE)) {
                    propValue = I18NUtil.parseLocale((String) propValue);
                }
            }

            repoProps.put(qname, propValue);

        }
    }

    private void logRecordSetInfo(Map<QName, Serializable> repoProps,
                                  Map<String, Object> editableProperties) {
        final Set<QName> auditPropeties = new HashSet<>();
        auditPropeties.add(ContentModel.PROP_CREATED);
        auditPropeties.add(ContentModel.PROP_CREATOR);
        auditPropeties.add(ContentModel.PROP_MODIFIED);
        auditPropeties.add(ContentModel.PROP_MODIFIER);
        auditPropeties.add(ContentModel.PROP_ACCESSED);

        logRecord.setInfo("");
        String newLine = Character.toString('\n');
        for (Map.Entry<QName, Serializable> prop : repoProps.entrySet()) {
            final QName propKey = prop.getKey();
            final String key = propKey.toString();
            if (auditPropeties.contains(propKey)) {
                continue;
            }

            if (editableProperties.containsKey(key)) {
                Object repoValue = prop.getValue();
                Object editValue = editableProperties.get(key);
                if (!isSameValue(repoValue, editValue)) {
                    int beginIndex = key.indexOf('}');
                    logRecord.addInfo(key.substring(beginIndex + 1));
                    if (repoValue != null) {
                        logRecord.addInfo(" old value: ");
                        logRecord.addInfo(repoValue.toString());
                    }
                    if (editValue != null) {
                        logRecord.addInfo(" new value: ");
                        logRecord.addInfo(editValue.toString());
                    }
                    logRecord.addInfo(newLine);
                }

            }
        }
    }

    private boolean isSameValue(Object firstValue, Object secondValue) {
        boolean result;

        if (firstValue != null) {
            result = firstValue.equals(secondValue);
        } else if (secondValue != null) {
            result = false;
        } else {
            result = true;
        }
        return result;
    }

    protected void editContainerProperties(boolean isIGRoot) throws Exception {
        // update the existing node in the repository
        NodeRef nodeRef = this.editableNode.getNodeRef();
        Map<String, Object> editedProps = this.editableNode.getProperties();
        Map<QName, Serializable> repoProps = this.getNodeService().getProperties(nodeRef);

        Serializable oldTitles = repoProps.get(ContentModel.PROP_TITLE);

        // handle the name property separately, perform a rename in case it changed
        String name = (String) editedProps.get(ContentModel.PROP_NAME.toString());
        if (name != null) {
            if (ManagedNodes.ML_CONTAINER.equals(actionNodeType) || ManagedNodes.CATEGORY_HEADER_NODE
                    .equals(actionNodeType)) {

                // the ml container can bve renamed via file folder service.
                this.getNodeService().setProperty(nodeRef, ContentModel.PROP_NAME, name);
                String oldName = (String) editedProps.get(ContentModel.PROP_NAME.toString());
                repoProps.put(ContentModel.PROP_NAME, oldName);
                if (ManagedNodes.ML_CONTAINER.equals(actionNodeType)) {
                    logRecord.setActivity("Modify multilingual content properties");
                }

            } else {
                String oldName = this.getFileFolderService().getFileInfo(nodeRef).getName();
                repoProps.put(ContentModel.PROP_NAME, oldName);
                this.getFileFolderService().rename(nodeRef, name);
            }
        }

        // get the current set of properties from the repository

        logRecordSetInfo(repoProps, editedProps);

        // add the "uifacets" aspect if required, properties will get set below
        if (this.getNodeService().hasAspect(nodeRef, ApplicationModel.ASPECT_UIFACETS) == false) {
            this.getNodeService().addAspect(nodeRef, ApplicationModel.ASPECT_UIFACETS, null);
        }

        //    overwrite the current properties with the edited ones
        for (Map.Entry<String, Object> item : editedProps.entrySet()) {
            String propName = item.getKey();
            QName qname = QName.createQName(propName);

            // make sure the property is represented correctly
            Serializable propValue = (Serializable) item.getValue();

            if ((getNodeService()
                    .hasAspect(this.getActionNode().getNodeRef(), CircabcModel.ASPECT_IGROOT)) && qname
                    .equals(CircabcModel.PROP_NB_DAY_WHATS_NEW)) {
                if (item.getValue() != null && !item.getValue().equals("")) {
                    Integer nbDays = Integer.parseInt(item.getValue().toString());
                    if (nbDays > WhatsNewDaysExceedException.MAX_DAYS
                            || nbDays < WhatsNewDaysExceedException.MIN_DAYS) {
                        throw new WhatsNewDaysExceedException(
                                translate("range_nbDaysWhatsNew_exception", WhatsNewDaysExceedException.MIN_DAYS,
                                        WhatsNewDaysExceedException.MAX_DAYS));
                    }
                }
            }

            if (qname.equals(CircabcModel.PROP_LIGHT_DESCRIPTION)) {
                String finalValue = Jsoup.parse(item.getValue().toString()).text();

                if (!isIGRoot
                        && finalValue.length() > LightDescriptionSizeExceedException.MAX_CHARACTER_LIMIT) {
                    throw new LightDescriptionSizeExceedException(translate("lightDescription_limit_exceed"));
                }

                propValue = finalValue;
            }

            // check for empty strings when using number types, set to null in this case
            if ((propValue != null) && (propValue instanceof String) &&
                    (propValue.toString().length() == 0)) {
                PropertyDefinition propDef = this.getDictionaryService().getProperty(qname);
                if (propDef != null) {
                    final QName propDefDataTypeName = propDef.getDataType().getName();
                    if (propDefDataTypeName.equals(DataTypeDefinition.DOUBLE) ||
                            propDefDataTypeName.equals(DataTypeDefinition.FLOAT) ||
                            propDefDataTypeName.equals(DataTypeDefinition.INT) ||
                            propDefDataTypeName.equals(DataTypeDefinition.LONG)) {
                        propValue = null;
                    }
                }
            }

            repoProps.put(qname, propValue);

            if (qname.equals(ContentModel.PROP_TITLE)) {
                if (oldTitles != null) {
                    if (!oldTitles.equals(propValue)) {
                        this.titleChanged = true;
                    }
                }
            }
        }

        // send the properties back to the repository
        this.getNodeService().setProperties(nodeRef, repoProps);

        // we also need to persist any association changes that may have been made

        // add any associations added in the UI
        Map<String, Map<String, AssociationRef>> addedAssocs = this.editableNode.getAddedAssociations();
        for (Map<String, AssociationRef> typedAssoc : addedAssocs.values()) {
            for (AssociationRef assoc : typedAssoc.values()) {
                this.getNodeService()
                        .createAssociation(assoc.getSourceRef(), assoc.getTargetRef(), assoc.getTypeQName());
            }
        }

        // remove any association removed in the UI
        Map<String, Map<String, AssociationRef>> removedAssocs = this.editableNode
                .getRemovedAssociations();
        for (Map<String, AssociationRef> typedAssoc : removedAssocs.values()) {
            for (AssociationRef assoc : typedAssoc.values()) {
                this.getNodeService()
                        .removeAssociation(assoc.getSourceRef(), assoc.getTargetRef(), assoc.getTypeQName());
            }
        }

        // add any child associations added in the UI
        Map<String, Map<String, ChildAssociationRef>> addedChildAssocs = this.editableNode
                .getAddedChildAssociations();
        for (Map<String, ChildAssociationRef> typedAssoc : addedChildAssocs.values()) {
            for (ChildAssociationRef assoc : typedAssoc.values()) {
                this.getNodeService()
                        .addChild(assoc.getParentRef(), assoc.getChildRef(), assoc.getTypeQName(),
                                assoc.getTypeQName());
            }
        }

        // remove any child association removed in the UI
        Map<String, Map<String, ChildAssociationRef>> removedChildAssocs = this.editableNode
                .getRemovedChildAssociations();
        for (Map<String, ChildAssociationRef> typedAssoc : removedChildAssocs.values()) {
            for (ChildAssociationRef assoc : typedAssoc.values()) {
                this.getNodeService().removeChild(assoc.getParentRef(), assoc.getChildRef());
            }
        }
    }

    protected boolean isNameValid() {
        final String name = (String) this.editableNode.getProperties()
                .get(ContentModel.PROP_NAME.toString());
        return name != null && name.trim().length() > 0 && !name.matches(FILENAME_REGEX);
    }

    protected boolean fieldMandatoryIsNotMissing(FacesContext context) {
        final String name = (String) this.editableNode.getProperties()
                .get(ContentModel.PROP_NAME.toString());

        if (name == null || name.length() < 1) {
            Utils.addErrorMessage(translate("edit_space_error_required_field_fileName"));
            this.isFinished = false;
            return false;
        }

        //	no field status for a file link
        if (editableNode.hasAspect(DocumentModel.ASPECT_CPROPERTIES)) {
            final String status = (String) this.editableNode.getProperties()
                    .get(DocumentModel.PROP_STATUS.toString());
            if (status == null || status.length() < 1) {
                Utils.addErrorMessage(translate("edit_content_error_required_field_status"));
                this.isFinished = false;
                return false;
            }
        }

        //	check URL field for URLs
        if (editableNode.hasAspect(DocumentModel.ASPECT_URLABLE)) {
            final String url = (String) this.editableNode.getProperties()
                    .get(DocumentModel.PROP_URL.toString());
            try {
                URLValidator.evaluate(url);
            } catch (ValidatorException ex) {
                FacesMessage msg = ex.getFacesMessage();

                Utils.addErrorMessage(translate(msg.getSummary()));
                this.isFinished = false;
                return false;
            }
        }

        return true;
    }

    //bugfix DIGIT-CIRCABC-352
    private boolean hasTextTooLong(FacesContext context) {
        final String name = (String) this.editableNode.getProperties()
                .get(ContentModel.PROP_NAME.toString());
        final String title = (String) this.editableNode.getProperties()
                .get(ContentModel.PROP_TITLE.toString());
        final String desc = (String) this.editableNode.getProperties()
                .get(ContentModel.PROP_DESCRIPTION.toString());
        final String author = (String) this.editableNode.getProperties()
                .get(ContentModel.PROP_AUTHOR.toString());
        final String ref = (String) this.editableNode.getProperties()
                .get(DocumentModel.PROP_REFERENCE.toString());

        List<String> errorMessages = new ArrayList<>(8);

        if (name.length() > MAX_LEN) {
            errorMessages.add("edit_content_error_maxlen_fileName");
        }

        if (title != null && title.length() > MAX_LEN) {
            errorMessages.add("edit_content_error_maxlen_title");
        }

        if (desc != null && desc.length() > MAX_LEN) {
            errorMessages.add("edit_content_error_maxlen_desc");
        }

        if (author != null && author.length() > MAX_LEN) {
            errorMessages.add("edit_content_error_maxlen_author");
        }

        if (ref != null && ref.length() > MAX_LEN) {
            errorMessages.add("edit_content_error_maxlen_ref");
        }

        if (errorMessages.size() > 0) {
            for (String msg : errorMessages) {
                Utils.addErrorMessage(translate(msg));
            }
            this.isFinished = false;
            return true;
        } else {
            return false;
        }
    }

    public boolean isLocked() {
        return getActionNode().isLocked();
    }

    /**
     * Migration 3.1 -> 3.4.6 - 20/12/2011 - Added transaction wrapping necessary in Alfresco 3.4.6
     */
    @Override
    protected String doPostCommitProcessing(final FacesContext context, final String outcome) {

        if (getCircabcService().syncEnabled()) {
            if (igNodeRef != null) {
                getCircabcService().updateIntestGroupProperties(igNodeRef);
            } else if (catNodeRef != null) {
                getCircabcService().updateCategoryProperties(catNodeRef);
            }
        }
        RetryingTransactionHelper txnHelper = Repository.getRetryingTransactionHelper(context);
        RetryingTransactionCallback<String> callback = new RetryingTransactionCallback<String>() {
            public String execute() throws Throwable {
                return doPostCommitProcessingIntern(context, outcome);
            }
        };

        return txnHelper.doInTransaction(callback, false, true);
    }

    protected String doPostCommitProcessingIntern(FacesContext context, String outcome) {
//			for(AuthorityNotification an : notifications.keySet()) {
//				getNotificationSubscriptionService().setNotificationStatus(this.editableNode.getNodeRef(), an.getAuthority(), notifications.get(an));			
//			}

        if (wasNotifyEnable) {
            getPolicyBehaviourFilter().enableBehaviour(ContentNotifyAspect.ASPECT_CONTENT_NOTIFY);
        }

        if (notify) {
            final Set<NotifiableUser> users = getNotificationSubscriptionService()
                    .getNotifiableUsers(getActionNode().getNodeRef());
            try {
                getNotificationService().notify(getActionNode().getNodeRef(), users);
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Error when notifying usere ", e);
                }
            }
        }
        super.doPostCommitProcessing(context, outcome);
        if (getBrowseBean().getActionSpace() != null) {
            getBrowseBean().getActionSpace().reset();
        }

        if (getBrowseBean().getDocument() != null) {
            getBrowseBean().getDocument().reset();
        }

        if (getNavigator().getCurrentNode() != null) {
            getNavigator().getCurrentNode().reset();
        }

        return outcome;
    }

    /**
     * Returns a list of icons to allow the user to select from. The list can change according to the
     * type of space being created.
     *
     * @return A list of icons
     */
    public List<UIListItem> getIcons() {
        // NOTE: we can't cache this list as it depends on the space type
        //       which the user can change during the advanced space wizard

        final String iconConfigName = ICONS_CONFIG.get(actionNodeType);

        if (iconConfigName == null) {
            return null;
        }

        List<UIListItem> icons = null;
        List<String> iconNames = new ArrayList<>(8);

        Config config = Application.getConfigService(FacesContext.getCurrentInstance())
                .getConfig(iconConfigName);
        if (config != null) {
            ConfigElement iconsCfg = config.getConfigElement("icons");
            if (iconsCfg != null) {
                boolean first = true;
                for (ConfigElement icon : iconsCfg.getChildren()) {
                    String iconName = icon.getAttribute("name");
                    String iconPath = icon.getAttribute("path");

                    if (iconName != null && iconPath != null) {
                        if (first) {
                            // if this is the first icon create the list and make
                            // the first icon in the list the default

                            icons = new ArrayList<>(iconsCfg.getChildCount());
                            if (this.icon == null) {
                                // set the default if it is not already
                                this.icon = iconName;
                            }
                            first = false;
                        }

                        UIListItem item = new UIListItem();
                        item.setValue(iconName);
                        item.setImage(iconPath);
                        icons.add(item);
                        iconNames.add(iconName);
                    }
                }
            }
        }

        // if we didn't find any icons display one default choice
        if (icons == null) {
            icons = new ArrayList<>(1);

            if (ManagedNodes.LINK.equals(actionNodeType)) {
                this.icon = DEFAULT_SPACE_LINK_ICON_NAME;
            } else {
                this.icon = DEFAULT_SPACE_ICON_NAME;
            }

            UIListItem item = new UIListItem();
            item.setValue(this.icon);
            item.setImage("/images/icons/" + this.icon + ".gif");
            icons.add(item);
            iconNames.add(this.icon);
        }

        // make sure the current value for the icon is valid for the
        // current list of icons about to be displayed
        if (iconNames.contains(this.icon) == false) {
            this.icon = iconNames.get(0);
        }

        return icons;
    }

    public String getTranslationNote() {
        return translate("translation_note",
                "<b>" + I18NUtil.getContentLocale().getDisplayLanguage() + "</b>");
    }

    @Override
    public String getCancelButtonLabel() {
        return translate("close");
    }

    public Node getEditableNode() {
        return editableNode;
    }

    public void setEditableNode(Node editableNode) {
        this.editableNode = editableNode;
    }

    public String getIdPrefix() {
        final String prefix = ID_PREFIX.get(actionNodeType);

        return (prefix != null) ? prefix : DEFAULT_ID_SUFFIX;
    }

    public String getBrowserTitle() {
        return translate("edit_" + actionNodeType.toString() + "_properties_dialog_browser_title");
    }

    public String getPageIconAltText() {
        return translate("edit_" + actionNodeType.toString() + "_properties_dialog_icon_tooltip");
    }

    @Override
    public String getContainerTitle() {
        return translate("edit_" + actionNodeType.toString() + "_properties_dialog_title");
    }

    @Override
    public String getContainerDescription() {
        return translate("edit_" + actionNodeType.toString() + "_properties_dialog_description");
    }

    private void setServiceAndActivity() {
        String activity = null;
        String service = null;
        switch (actionNodeType) {
            case CONTENT:
                service = super.getServiceFromActionNode();
                activity = "Edit document";
                break;
            case DOSSIER:
                service = "Library";
                activity = "Edit dossier";
                break;
            case EMPTY_TRANSLATION:
                service = "Library";
                activity = "Edit empty translation";
                break;
            case FORUM:
                service = super.getServiceFromActionNode();
                activity = "Edit forum";
                break;
            case FORUMS:
                service = super.getServiceFromActionNode();
                activity = "Edit forums";
                break;
            case LINK:
                service = super.getServiceFromActionNode();
                activity = "Edit link";
                break;
            case ML_CONTAINER:
                service = "Library";
                activity = "Edit multi lingual container Link";
                break;
            case POST:
                service = super.getServiceFromActionNode();
                activity = "Edit  post";
                break;
            case SPACE:
                service = super.getServiceFromActionNode();
                activity = "Edit space";
                break;
            case TOPIC:
                service = super.getServiceFromActionNode();
                activity = "Edit topic";
                break;
            case TRANSLATION:
                service = "Library";
                activity = "Edit translation";
                break;
            case EXTERNAL_LINK:
                service = "Library";
                activity = "Edit external link";
                break;
            case IG_NODE:
                service = "Administration";
                activity = "Edit interest group";
                break;
            case CATEGORY_NODE:
                service = "Administration";
                activity = "Edit category";
                break;
            case CATEGORY_HEADER_NODE:
                service = "Administration";
                activity = "Edit category header";
                break;
            case CIRCABC_NODE:
                service = "Administration";
                activity = "Edit CIRCABC";
                break;
            case EVENTS_NODE:
                service = "Events";
                activity = "Edit event";
                break;
            case SURVEY_NODE:
                service = "Survey";
                activity = "Edit syrvey";
                break;
            case INFO_SERVICE_NODE:
                service = "Administration";
                activity = "Edit information";
                break;
            case LIBRARY_NODE:
                service = "Administration";
                activity = "Edit library";
                break;
            case NEWSGROUP_NODE:
                service = "Administration";
                activity = "Edit newsgroup";
                break;
            default:
                if (logger.isWarnEnabled()) {
                    logger.warn(
                            "Not defined logging activity and service for Action Node Type " + actionNodeType
                                    .toString());
                }
        }
        logRecord.setActivity(activity);
        logRecord.setService(service);
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public NotificationSubscriptionService getNotificationSubscriptionService() {
        return notificationSubscriptionService;
    }

    public void setNotificationSubscriptionService(
            NotificationSubscriptionService notificationSubscriptionService) {
        this.notificationSubscriptionService = notificationSubscriptionService;
    }

    public CircabcService getCircabcService() {
        if (circabcService == null) {
            circabcService = Services.getCircabcServiceRegistry(FacesContext.getCurrentInstance())
                    .getCircabcService();
        }
        return circabcService;
    }

    public void setCircabcService(CircabcService circabcService) {
        this.circabcService = circabcService;
    }


}
