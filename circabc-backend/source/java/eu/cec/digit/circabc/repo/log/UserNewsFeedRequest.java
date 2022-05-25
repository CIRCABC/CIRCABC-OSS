/**
 *
 */
package eu.cec.digit.circabc.repo.log;

import java.util.List;

/** @author beaurpi */
public class UserNewsFeedRequest {

    private List<Long> igIds;
    private List<Long> activityIds;
    private String when; // values should be today / week / previousWeek

    /** @return the igIds */
    public List<Long> getIgIds() {
        return igIds;
    }

    /** @param igIds the igIds to set */
    public void setIgIds(List<Long> igIds) {
        this.igIds = igIds;
    }

    /** @return the activityIds */
    public List<Long> getActivityIds() {
        return activityIds;
    }

    /** @param activityIds the activityIds to set */
    public void setActivityIds(List<Long> activityIds) {
        this.activityIds = activityIds;
    }

    /** @return the when */
    public String getWhen() {
        return when;
    }

    /** @param when the when to set */
    public void setWhen(String when) {
        this.when = when;
    }
}
