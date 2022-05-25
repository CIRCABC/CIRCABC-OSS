package eu.cec.digit.circabc.service.app.message;

/**
 * AppMessage
 */
public class DistributionEmailDAO {

    private Integer id = null;

    private String emailAddress = null;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the email
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * @param email the email to set
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
