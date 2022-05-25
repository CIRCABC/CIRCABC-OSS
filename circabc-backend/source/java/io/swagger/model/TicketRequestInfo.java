package io.swagger.model;

public class TicketRequestInfo {

    private String requestDate;
    private String httpVerb;
    private String path;

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHttpVerb() {
        return httpVerb;
    }

    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((httpVerb == null) ? 0 : httpVerb.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((requestDate == null) ? 0 : requestDate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TicketRequestInfo other = (TicketRequestInfo) obj;
        if (httpVerb == null) {
            if (other.httpVerb != null) return false;
        } else if (!httpVerb.equals(other.httpVerb)) return false;
        if (path == null) {
            if (other.path != null) return false;
        } else if (!path.equals(other.path)) return false;
        if (requestDate == null) {
            if (other.requestDate != null) return false;
        } else if (!requestDate.equals(other.requestDate)) return false;
        return true;
    }
}
