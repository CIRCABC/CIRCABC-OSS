/**
 *
 */
package io.swagger.model;

/** @author beaurpi */
public enum CircabcServiceName {
    INFORMATION("information"),
    LIBRARY("library"),
    NEWSGROUPS("newsgroups"),
    EVENTS("events");

    private String serviceName;

    CircabcServiceName(String serviceName) {
        this.setServiceName(serviceName);
    }

    /** @return the serviceName */
    public String getServiceName() {
        return this.serviceName;
    }

    /** @param serviceName the serviceName to set */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return this.serviceName;
    }
}
