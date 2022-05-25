/**
 *
 */
package io.swagger.api;

import eu.cec.digit.circabc.business.api.content.Attachement;
import io.swagger.model.Node;

import java.util.List;
import java.util.Objects;

/** @author schwerr */
public class PostNode extends Node {

    List<Attachement> attachments = null;

    /** @return the attachments */
    public List<Attachement> getAttachments() {
        return attachments;
    }

    /** @param attachments the attachments to set */
    public void setAttachments(List<Attachement> attachments) {
        this.attachments = attachments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PostNode postNode = (PostNode) o;
        return Objects.equals(attachments, postNode.attachments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), attachments);
    }
}
