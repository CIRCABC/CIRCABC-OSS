package io.swagger.api;

import eu.cec.digit.circabc.service.newsgroup.AbuseReport;
import io.swagger.model.Node;

import java.util.List;

/**
 * @author beaurpi
 */
public interface ForumsApi {

    /**
     * get the content of a forums returns a list of forums/topics or any kind of child of this node
     * mapped URL /forums/{id}/content
     */
    List<Node> getForumById(String id);

    /**
     * create a new forum within one forum mapped url /forums/{id}/subforums
     */
    Node forumsIdSubforumsPost(String id, Node body);

    /**
     * get a list of sub forums
     */
    List<Node> forumsIdSubforumsGet(String id);

    /**
     * get a list of sub forums
     */
    List<Node> forumsIdSubforumsGet(String id, String sorting);

    /**
     * create a new empty topic within one forum this does not create any post inside mapped url
     * /forums/{id}/content
     */
    Node forumsIdContentPost(String id, Node body);

    /**
     * delete one forum with all its topics and posts
     */
    void forumsIdDelete(String id);

    /**
     * update a forum
     */
    void updateForum(String id, Node forumNode);

    /**
     * toggle the moderation of a forum. acceptAll is only considered if moderation is being disabled
     * and it concerns accepting of refusing all posts to be still checked
     */
    void toggleModeration(String id, boolean enable, boolean acceptAll);

    /**
     * verify a post by a moderator and accept or reject it (with an optional reason)
     */
    void verifyPost(String id, boolean approve, String rejectReason) throws Exception;

    /**
     * get signaled abuses
     */
    List<AbuseReport> getSignaledAbuses(String id);

    /**
     * signal an abuse
     */
    void signalAbuse(String id, String abuseText) throws Exception;

    /**
     * remove signaled abuses
     */
    void removeAbuses(String id);
}
