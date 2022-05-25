/**
 *
 */
package eu.cec.digit.circabc.service.app.message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.spring.SqlSessionTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author beaurpi */
public class AppMessageDaoServiceImpl implements AppMessageDaoService {

    private static final Log logger = LogFactory.getLog(AppMessageDaoServiceImpl.class);

    private SqlSessionTemplate sqlSessionTemplate = null;

    /*
     * (non-Javadoc)
     *
     * @see eu.cec.digit.circabc.service.app.message.AppMessageDaoService#
     * selectAppMessageTemplate()
     */
    @Override
    public List<AppMessageDAO> selectAppMessageTemplates(int page, int limit) {

        Map<String, Object> props = new HashMap<>();

        // 0 based system
        if (page > 0) {
            props.put("page", page - 1);
        }

        if (limit > 0) {
            props.put("limit", limit);
        }

        // for mysql
        props.put("limitMin", (page - 1) * limit);

        List<AppMessageDAO> result =
                (List<AppMessageDAO>) sqlSessionTemplate.selectList("AppMessage.select_app_messages", props);
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see eu.cec.digit.circabc.service.app.message.AppMessageDaoService#
     * addAppMessageTemplate(java.lang.String, java.util.Date, java.lang.String,
     * java.lang.Integer, java.lang.Boolean)
     */
    @Override
    public void addAppMessageTemplate(
            String content, Date dateClosure, String level, Integer displayTime, Boolean enabled) {
        Map<String, Object> props = new HashMap<>();
        props.put("dateClosure", dateClosure);
        props.put("displayTime", displayTime);
        props.put("content", content);
        props.put("level", level);
        props.put("enabled", enabled);

        sqlSessionTemplate.insert("AppMessage.insert_app_message", props);
    }

    /*
     * (non-Javadoc)
     *
     * @see eu.cec.digit.circabc.service.app.message.AppMessageDaoService#
     * updateAppMessageTemplate(java.lang.Integer, java.lang.String, java.util.Date,
     * java.lang.String, java.lang.Integer, java.lang.Boolean)
     */
    @Override
    public void updateAppMessageTemplate(
            Integer id,
            String content,
            Date dateClosure,
            String level,
            Integer displayTime,
            Boolean enabled) {
        Map<String, Object> props = new HashMap<>();
        props.put("dateClosure", dateClosure);
        props.put("displayTime", displayTime);
        props.put("content", content);
        props.put("level", level);
        props.put("enabled", enabled);
        props.put("id", id);

        sqlSessionTemplate.update("AppMessage.update_app_message", props);
    }

    /*
     * (non-Javadoc)
     *
     * @see eu.cec.digit.circabc.service.app.message.AppMessageDaoService#
     * deleteAppMessageTemplate(java.lang.Integer)
     */
    @Override
    public void deleteAppMessageTemplate(Integer id) {
        Map<String, Object> props = new HashMap<>();
        props.put("id", id);

        sqlSessionTemplate.delete("AppMessage.delete_app_message", props);
    }

    /** @return the sqlSessionTemplate */
    public SqlSessionTemplate getSqlSessionTemplate() {
        return sqlSessionTemplate;
    }

    /** @param sqlSessionTemplate the sqlSessionTemplate to set */
    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    @Override
    public AppMessageDAO getMessageTemplate(Integer id) {
        Map<String, Object> props = new HashMap<>();
        props.put("id", id);
        AppMessageDAO template =
                (AppMessageDAO) sqlSessionTemplate.selectOne("AppMessage.select_app_message", props);
        return template;
    }

    @Override
    public Integer countAppMessageTemplates() {
        return (Integer) sqlSessionTemplate.selectOne("AppMessage.count_app_messages");
    }

    @Override
    public List<DistributionEmailDAO> selectDistributionEmails(int page, int limit, String search) {
        Map<String, Object> props = new HashMap<>();
        // 0 based system
        if (page > 0) {
            props.put("page", page - 1);
        }
        props.put("limit", limit);

        // for mysql
        props.put("limitMin", (page - 1) * limit);

        if (search != null && !"null".equals(search)) {
            props.put("search", search.toLowerCase());
        } else {
            props.put("search", "");
        }

        List<DistributionEmailDAO> result =
                (List<DistributionEmailDAO>)
                        sqlSessionTemplate.selectList("AppMessage.select_distribution_emails", props);
        return result;
    }

    @Override
    public Long countDistributionEmails(String query) {
        Map<String, Object> props = new HashMap<>();
        if (query != null && !"null".equals(query)) {
            props.put("search", query.toLowerCase());
        } else {
            props.put("search", "");
        }

        return Long.parseLong(
                sqlSessionTemplate.selectOne("AppMessage.count_distribution_emails", props).toString());
    }

    @Override
    public void insertEmail(DistributionEmailDAO distribEmail) {
        Map<String, Object> props = new HashMap<>();
        props.put("email", distribEmail.getEmailAddress());
        sqlSessionTemplate.insert("AppMessage.insert_distribution_email", props);
    }

    @Override
    public int hasDistributionEmail(String query) {
        Map<String, Object> props = new HashMap<>();
        props.put("email", query.toLowerCase());
        return (int) sqlSessionTemplate.selectOne("AppMessage.count_distribution_email", props);
    }

    @Override
    public void deleteDistributionEmail(Integer id) {
        Map<String, Object> props = new HashMap<>();
        props.put("id", id);
        sqlSessionTemplate.delete("AppMessage.delete_distribution_email", props);
    }

    @Override
    public DistributionEmailDAO getDistributionEmail(String email) {
        Map<String, Object> props = new HashMap<>();
        props.put("email", email.toLowerCase());
        return (DistributionEmailDAO)
                sqlSessionTemplate.selectOne("AppMessage.get_distribution_email", props);
    }
}
