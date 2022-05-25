package io.swagger.model;

import java.util.Objects;

public class Subscribed {

    private Boolean subscribed = null;

    public Boolean getSubscribed() {
        return subscribed;
    }

    public void setSubscribed(Boolean subscribed) {
        this.subscribed = subscribed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Subscribed that = (Subscribed) o;
        return Objects.equals(subscribed, that.subscribed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscribed);
    }

    @Override
    public String toString() {
        return "Subscribed{" + "subscribed=" + subscribed + '}';
    }
}
