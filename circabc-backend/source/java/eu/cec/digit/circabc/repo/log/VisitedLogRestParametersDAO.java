package eu.cec.digit.circabc.repo.log;

public class VisitedLogRestParametersDAO {

    private String username;
    private int id = 0;

    public VisitedLogRestParametersDAO(String username, int id) {
        super();
        this.username = username;
        this.id = id;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }
}
