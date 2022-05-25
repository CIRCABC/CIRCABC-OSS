package eu.cec.digit.circabc.service.rendition;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Manages the posting of documents to render.
 *
 * @author schwerr
 */
public interface CircabcRenditionService {

    /**
     * Adds a new document nodeRef to render (asynchronous rendering)
     */
    void addRequest(NodeRef nodeRef);
}
