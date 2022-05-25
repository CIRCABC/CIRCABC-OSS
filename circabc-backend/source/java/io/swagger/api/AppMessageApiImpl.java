/**
 *
 */
package io.swagger.api;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.SystemMessageModel;
import eu.cec.digit.circabc.service.app.message.AppMessageDAO;
import eu.cec.digit.circabc.service.app.message.AppMessageDaoService;
import eu.cec.digit.circabc.service.app.message.DistributionEmailDAO;
import eu.cec.digit.circabc.service.notification.NotificationService;
import io.swagger.model.*;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** @author beaurpi */
public class AppMessageApiImpl implements AppMessageApi {

    private NodeService nodeService;

    private PersonService personService;

    private NotificationService notificationService;

    private AppMessageDaoService appMessageDaoService;

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /** @return the personService */
    public PersonService getPersonService() {
        return personService;
    }

    /** @param personService the personService to set */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /** @return the notificationService */
    public NotificationService getNotificationService() {
        return notificationService;
    }

    /** @param notificationService the notificationService to set */
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /** @return the appMessageDaoService */
    public AppMessageDaoService getAppMessageDaoService() {
        return appMessageDaoService;
    }

    /** @param appMessageDaoService the appMessageDaoService to set */
    public void setAppMessageDaoService(AppMessageDaoService appMessageDaoService) {
        this.appMessageDaoService = appMessageDaoService;
    }

    /*
     * (non-Javadoc)
     *
     * @see io.swagger.api.AppMessageApi#getAppMessages()
     */
    @Override
    public List<AppMessage> getAppMessages() {

        List<AppMessage> result = new ArrayList<>();

        if (Boolean.TRUE.equals(getDisplayOldMessage().getDisplay())) {
            // old system message compatibility

            NodeRef messageRef = getOldMessageRef();
            if (messageRef != null) {
                AppMessage message = new AppMessage();
                message.setId(-1);

                Serializable content =
                        nodeService.getProperty(messageRef, SystemMessageModel.PROP_SYSTEMMESSAGE_TEXT);
                if (content != null) {
                    message.setContent(content.toString());
                } else {
                    message.setContent("");
                }

                message.setEnabled(
                        (Boolean)
                                nodeService.getProperty(
                                        messageRef, SystemMessageModel.PROP_IS_SYSTEMMESSAGE_ENABLED));
                result.add(message);
            }
        }

        List<AppMessageDAO> allTemplates = appMessageDaoService.selectAppMessageTemplates(-1, -1);
        for (AppMessageDAO template : allTemplates) {
            if (Boolean.TRUE.equals(template.getShowMessage())) {
                AppMessage message = convertToAppMessage(template);
                result.add(message);
            }
        }

        return result;
    }

    private AppMessage convertToAppMessage(AppMessageDAO template) {
        AppMessage result = new AppMessage();
        result.setId(template.getId());
        result.setContent(template.getMessageContent());

        if (template.getDateClosure() != null) {
            result.setDateClosure(new DateTime(template.getDateClosure()));
        }

        result.setDisplayTime(template.getDisplayTime());
        result.setEnabled(template.getShowMessage());
        result.setLevel(template.getMessageLevel());
        return result;
    }

    private NodeRef getCircabcNode() {
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        NodeRef appRootNodeRef = nodeService.getRootNode(storeRef);
        NodeRef circabcRootNodeRef = null;
        for (ChildAssociationRef assoc : nodeService.getChildAssocs(appRootNodeRef)) {
            NodeRef childRef = assoc.getChildRef();
            if (nodeService
                    .getProperty(childRef, ContentModel.PROP_NAME)
                    .toString()
                    .equals("Company Home")) {
                circabcRootNodeRef =
                        nodeService.getChildByName(
                                childRef,
                                ContentModel.ASSOC_CONTAINS,
                                CircabcConfiguration.getProperty(
                                        CircabcConfiguration.CIRCABC_ROOT_NODE_NAME_PROPERTIES));
            }
        }
        return circabcRootNodeRef;
    }

    private NodeRef getOldMessageRef() {
        NodeRef circabcNodeRef = getCircabcNode();
        List<ChildAssociationRef> listOfMessages =
                nodeService.getChildAssocs(
                        circabcNodeRef,
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(
                                SystemMessageModel.CIRCABC_SYSTEMMESSAGE_MODEL_1_0_URI, "System Message"));

        NodeRef result = null;

        for (ChildAssociationRef childAssoc : listOfMessages) {
            result = childAssoc.getChildRef();
        }

        return result;
    }

    @Override
    public PagedAppMessages getAppMessageTemplates(int page, int limit) {
        PagedAppMessages result = new PagedAppMessages();
        List<AppMessageDAO> allTemplates = appMessageDaoService.selectAppMessageTemplates(page, limit);
        for (AppMessageDAO template : allTemplates) {
            AppMessage message = convertToAppMessage(template);
            result.getData().add(message);
        }

        Integer total = appMessageDaoService.countAppMessageTemplates();
        result.setTotal(total.longValue());

        return result;
    }

    @Override
    public void addAppMessageTemplate(AppMessage template) {
        Date closure = null;
        if (template.getDateClosure() != null) {
            closure = template.getDateClosure().toDate();
        }

        appMessageDaoService.addAppMessageTemplate(
                template.getContent(),
                closure,
                template.getLevel(),
                template.getDisplayTime(),
                template.getEnabled());
    }

    @Override
    public void updateAppMessageTemplate(AppMessage template) {
        Date closure = null;
        if (template.getDateClosure() != null) {
            closure = template.getDateClosure().toDate();
        }

        appMessageDaoService.updateAppMessageTemplate(
                template.getId(),
                template.getContent(),
                closure,
                template.getLevel(),
                template.getDisplayTime(),
                template.getEnabled());
    }

    @Override
    public void deleteAppMessageTemplate(Integer id) {
        appMessageDaoService.deleteAppMessageTemplate(id);
    }

    @Override
    public AppMessage getAppMessageTemplate(Integer id) {
        return convertToAppMessage(appMessageDaoService.getMessageTemplate(id));
    }

    @Override
    public DisplayConfiguration getDisplayOldMessage() {
        NodeRef circabcRef = getCircabcNode();
        Serializable prop =
                nodeService.getProperty(circabcRef, CircabcModel.PROP_DISPLAY_OLD_APP_MESSAGE);

        DisplayConfiguration result = new DisplayConfiguration();
        result.setDisplay((prop == null || Boolean.parseBoolean(prop.toString())));

        return result;
    }

    @Override
    public void setDisplayOldMessage(Boolean displayOld) {
        NodeRef circabcRef = getCircabcNode();
        nodeService.setProperty(circabcRef, CircabcModel.PROP_DISPLAY_OLD_APP_MESSAGE, displayOld);
    }

    @Override
    public EnableConfiguration getEnableOldMessage() {
        NodeRef oldMessageRef = getOldMessageRef();
        EnableConfiguration result = new EnableConfiguration();
        result.setEnable(false);

        if (oldMessageRef != null) {
            result.setEnable(
                    (Boolean)
                            nodeService.getProperty(
                                    oldMessageRef, SystemMessageModel.PROP_IS_SYSTEMMESSAGE_ENABLED));
        }

        return result;
    }

    @Override
    public void setEnableOldMessage(Boolean enableOld) {

        NodeRef oldMessageRef = getOldMessageRef();

        if (oldMessageRef != null) {
            nodeService.setProperty(
                    oldMessageRef, SystemMessageModel.PROP_IS_SYSTEMMESSAGE_ENABLED, enableOld);
        }
    }

    @Override
    public void udpateOldAppMessage(AppMessage template) {
        NodeRef oldMessageRef = getOldMessageRef();
        nodeService.setProperty(
                oldMessageRef, SystemMessageModel.PROP_SYSTEMMESSAGE_TEXT, template.getContent());
    }

    @Override
    public PagedEmails getAppDistributionEmails(int page, int limit, String query) {
        List<DistributionEmailDAO> list =
                appMessageDaoService.selectDistributionEmails(page, limit, query);
        Long total = appMessageDaoService.countDistributionEmails(query);
        PagedEmails result = new PagedEmails();
        result.setData(list);
        result.setTotal(total);
        return result;
    }

    @Override
    public void addAppDistributionPostEmails(List<DistributionEmailDAO> list) {
        for (DistributionEmailDAO distribEmail : list) {
            if (Boolean.FALSE.equals(isSubscribedDistributionEmail(distribEmail.getEmailAddress()))) {
                appMessageDaoService.insertEmail(distribEmail);
            }
        }
    }

    @Override
    public Boolean isSubscribedDistributionEmail(String query) {
        if (query != null) {
            return appMessageDaoService.hasDistributionEmail(query) > 0;
        }

        return false;
    }

    @Override
    public void removeAppDistributionPostEmails(Integer id) {
        if (id != null) {

            appMessageDaoService.deleteDistributionEmail(id);
        }
    }

    @Override
    public Workbook getdistributionListAsExcel() {
        Workbook workbook = new XSSFWorkbook();
        Sheet s1 = workbook.createSheet();
        Row r1 = s1.createRow(0);
        Cell header = r1.createCell(0);
        header.setCellValue("email");
        int i = 1;

        for (DistributionEmailDAO distrib : getAppDistributionEmails(0, -1, "").getData()) {

            if (i == SpreadsheetVersion.EXCEL2007.getMaxRows()) {
                s1 = workbook.createSheet();
                r1 = s1.createRow(0);
                header = r1.createCell(0);
                header.setCellValue("email");
                i = 1;
            }

            Row r = s1.createRow(i);
            Cell mail = r.createCell(0);
            mail.setCellValue(distrib.getEmailAddress());
            i++;
        }

        return workbook;
    }

    @Override
    public DistributionEmailDAO getSubscribedDistributionEmail(String userId) {
        if (!"".equals(userId)) {
            NodeRef personRef = personService.getPerson(userId);
            String email = String.valueOf(nodeService.getProperty(personRef, ContentModel.PROP_EMAIL));
            return appMessageDaoService.getDistributionEmail(email);
        }

        return null;
    }

    @Override
    public void notifyTemplate(AppMessage template) {
        int page = 0;
        int limit = 25;
        PagedEmails mails;
        List<String> mailAddress = new ArrayList<>();

        do {
            mails = getAppDistributionEmails(page, limit, "");
            for (DistributionEmailDAO distrib : mails.getData()) {
                mailAddress.add(distrib.getEmailAddress());
            }
            notificationService.notifySystemMessage(mailAddress, template);
            page++;
            mailAddress.clear();
        } while (page < mails.getTotal() / limit);
        // last page
        if (mails.getTotal() % limit > 0) {
            mails = getAppDistributionEmails(page, limit, "");
            for (DistributionEmailDAO distrib : mails.getData()) {
                mailAddress.add(distrib.getEmailAddress());
            }
            notificationService.notifySystemMessage(mailAddress, template);
        }
    }
}
