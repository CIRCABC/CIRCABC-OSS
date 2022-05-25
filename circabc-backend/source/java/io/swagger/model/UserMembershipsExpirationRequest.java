/**
 *
 */
package io.swagger.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** @author beaurpi */
public class UserMembershipsExpirationRequest {

    private String userId;
    private Date expirationDate;
    private List<InterestGroupProfile> memberships = new ArrayList<>();

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public List<InterestGroupProfile> getMemberships() {
        return memberships;
    }

    public void setMemberships(List<InterestGroupProfile> memberships) {
        this.memberships = memberships;
    }
}
