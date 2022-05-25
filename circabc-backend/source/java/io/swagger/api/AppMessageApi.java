package io.swagger.api;

import eu.cec.digit.circabc.service.app.message.DistributionEmailDAO;
import io.swagger.model.*;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

/**
 * @author beaurpi
 */
public interface AppMessageApi {

    /**
     * * get the list of enabled app messages
     *
     * @return
     */
    List<AppMessage> getAppMessages();

    /**
     * * get the list of templates defined in the application whether the message is enabled or not
     *
     * @param limit
     * @param page
     * @return
     */
    PagedAppMessages getAppMessageTemplates(int page, int limit);

    /**
     * * insert a new template in the system it does not mean it is save in the old system message
     *
     * @param template
     */
    void addAppMessageTemplate(AppMessage template);

    /**
     * * updates an existing template
     *
     * @param template
     */
    void updateAppMessageTemplate(AppMessage template);

    /**
     * * delete a template
     *
     * @param id
     */
    void deleteAppMessageTemplate(Integer id);

    /**
     * * get one template
     *
     * @param id
     * @return
     */
    AppMessage getAppMessageTemplate(Integer id);

    DisplayConfiguration getDisplayOldMessage();

    void setDisplayOldMessage(Boolean displayOld);

    EnableConfiguration getEnableOldMessage();

    void setEnableOldMessage(Boolean enableOld);

    void udpateOldAppMessage(AppMessage template);

    PagedEmails getAppDistributionEmails(int page, int limit, String query);

    void addAppDistributionPostEmails(List<DistributionEmailDAO> list);

    Boolean isSubscribedDistributionEmail(String query);

    void removeAppDistributionPostEmails(Integer id);

    Workbook getdistributionListAsExcel();

    DistributionEmailDAO getSubscribedDistributionEmail(String userId);

    void notifyTemplate(AppMessage template);
}
