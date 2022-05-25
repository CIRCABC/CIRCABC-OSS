/**
 *
 */
package eu.cec.digit.circabc.service.app.message;

import java.util.Date;
import java.util.List;

/** @author beaurpi */
public interface AppMessageDaoService {

    /**
     * * get the list of templates
     *
     * @param limit
     * @param page
     */
    List<AppMessageDAO> selectAppMessageTemplates(int page, int limit);

    /**
     * * insert a new template
     *
     * @param content
     * @param dateClosure
     * @param level
     * @param displayTime
     * @param enabled
     */
    void addAppMessageTemplate(
            String content, Date dateClosure, String level, Integer displayTime, Boolean enabled);

    /**
     * * update an existing template
     *
     * @param id
     * @param content
     * @param dateClosure
     * @param level
     * @param displayTime
     * @param enabled
     */
    void updateAppMessageTemplate(
            Integer id,
            String content,
            Date dateClosure,
            String level,
            Integer displayTime,
            Boolean enabled);

    /**
     * * delete a template
     *
     * @param id
     */
    void deleteAppMessageTemplate(Integer id);

    /**
     * * get the definition of one template only
     *
     * @param id
     * @return
     */
    AppMessageDAO getMessageTemplate(Integer id);

    /**
     * * get the amout of templates, whether they are enabled or disabled
     *
     * @return
     */
    Integer countAppMessageTemplates();

    /**
     * * get the list of templates
     *
     * @param limit
     * @param page
     */
    List<DistributionEmailDAO> selectDistributionEmails(int page, int limit, String search);

    Long countDistributionEmails(String query);

    void insertEmail(DistributionEmailDAO distribEmail);

    int hasDistributionEmail(String query);

    void deleteDistributionEmail(Integer id);

    DistributionEmailDAO getDistributionEmail(String email);
}
