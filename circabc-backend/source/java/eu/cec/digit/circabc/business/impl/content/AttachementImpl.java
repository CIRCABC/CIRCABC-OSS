/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.business.impl.content;

import eu.cec.digit.circabc.business.api.content.Attachement;
import eu.cec.digit.circabc.model.DocumentModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Yanick Pignot
 */
public class AttachementImpl implements Attachement, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 9009856942455481374L;
    private final NodeRef referer;
    private final NodeRef refered;

    private final AttachementType type;
    private final String name;
    private final String title;

    private long size = 0;
    private String mimetype = "";
    private String encoding = "";

    /**
     * @param referer
     * @param refered
     */
    public AttachementImpl(final NodeRef referer, final NodeRef refered,
                           final NodeService nodeService) {
        super();
        this.referer = referer;
        this.refered = refered;

        final Map<QName, Serializable> props = nodeService.getProperties(refered);
        name = (String) props.get(ContentModel.PROP_NAME);
        title = (String) props.get(ContentModel.PROP_TITLE);

        final QName nodeType = nodeService.getType(refered);

        if (DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT.equals(nodeType)) {
            type = AttachementType.HIDDEN_FILE;
        } else {
            type = AttachementType.REPO_LINK;
        }
    }

    public AttachementType geType() {
        return type;
    }

    public NodeRef getAttachedOn() {
        return referer;
    }

    public String getName() {
        return name;
    }

    public NodeRef getNodeRef() {
        return refered;
    }

    public String getTitle() {
        if (title != null && title.length() > 0) {
            return title;
        } else {
            return getName();
        }
    }

    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * @return the mimetype
     */
    public String getMimetype() {
        return mimetype;
    }

    /**
     * @param mimetype the mimetype to set
     */
    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    /**
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
