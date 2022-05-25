package eu.cec.digit.circabc.repo.web.scripts.bean;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import eu.cec.digit.ecas.client.configuration.ChainableSpringConfiguration;
import eu.cec.digit.ecas.client.configuration.ConfigurationException;
import eu.cec.digit.ecas.client.configuration.SpringConfigurator;
import eu.cec.digit.ecas.client.validation.ClientUserValidator;
import eu.cec.digit.ecas.client.validation.EcasValidationConfigIntf;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class EcasTicketValidator implements TicketValidator {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(EcasTicketValidator.class);

    private final String webRootUrl =
            CircabcConfiguration.getProperty(CircabcConfiguration.WEB_ROOT_URL);
    private String service;
    private EcasValidationConfigIntf validationConfig;
    private SpringConfigurator sc;

    public String getService() {
        return this.service;
    }

    public void setService(String service) {
        this.service = this.webRootUrl + service;
    }

    public void setEcasConfiguration(ChainableSpringConfiguration ecasConfiguration) {
        sc = new SpringConfigurator();
        try {
            validationConfig = sc.getEcasValidationConfig(ecasConfiguration);
        } catch (ConfigurationException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Can not get  ecas validation configuration ", e);
            }
        }
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.repo.web.scripts.bean.TicektValidator#validateTicket(java.lang.String)
     */
    public String validateTicket(String ticket) {
        try {
            ClientUserValidator cuv = new ClientUserValidator(this.validationConfig);
            return cuv.validate(ticket, this.service).getUser();
        } catch (Exception e) {
            logger.error("Error checking validation ticket " + ticket, e);
            return null;
        }
    }
}
