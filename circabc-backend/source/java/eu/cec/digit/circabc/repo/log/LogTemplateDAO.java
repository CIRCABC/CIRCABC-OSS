package eu.cec.digit.circabc.repo.log;

public class LogTemplateDAO {

    private Integer id;
    private String method;
    private String template;

    public LogTemplateDAO() {
    }

    public LogTemplateDAO(Integer id, String method, String template) {
        this.id = id;
        this.method = method;
        this.template = template;
    }

    public LogTemplateDAO(String method, String template) {
        this.method = method;
        this.template = template;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
