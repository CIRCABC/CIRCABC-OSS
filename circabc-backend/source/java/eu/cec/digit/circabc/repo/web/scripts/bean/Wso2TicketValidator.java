package eu.cec.digit.circabc.repo.web.scripts.bean;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class Wso2TicketValidator implements TicketValidator {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(Wso2TicketValidator.class);

    private String url;

    public String validateTicket(String ticket) {

        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(url);
        String result = null;
        try {
            method.addRequestHeader("Authorization", "Bearer " + ticket);
            client.executeMethod(method);

            if (method.getStatusCode() == HttpStatus.SC_OK) {
                String response = method.getResponseBodyAsString();
                if (logger.isInfoEnabled()) {
                    logger.info("Response = " + response);
                }
                int subStart = response.indexOf("sub\":\"") + 6;
                int subEnd = response.indexOf('\"', subStart);
                result = response.substring(subStart, subEnd);
            }

        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error when validate ticket ", e);
            }
        } finally {
            method.releaseConnection();
        }

        return result;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }
}
