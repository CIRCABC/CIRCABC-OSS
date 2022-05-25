/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.repo.filefolder;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class FileInfoImpl implements FileInfo {

    /**
     *
     */
    private static final long serialVersionUID = -1025981884046770512L;

    private NodeRef nodeRef;
    private NodeRef linkNodeRef;
    private boolean isFolder;
    private boolean isLink;
    private Map<QName, Serializable> properties;

    /**
     * Package-level constructor
     */
    public FileInfoImpl(NodeRef nodeRef, boolean isFolder, Map<QName, Serializable> properties) {
        this.nodeRef = nodeRef;
        this.isFolder = isFolder;
        this.properties = properties;

        // Check if this is a link node
        if (properties.containsKey(ContentModel.PROP_LINK_DESTINATION)) {
            isLink = true;
            linkNodeRef = (NodeRef) properties.get(ContentModel.PROP_LINK_DESTINATION);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.model.FileInfo#isHidden()
     */

    public boolean isHidden() {
        return false;
    }

    /**
     * @see #getNodeRef()
     * @see NodeRef#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (this == obj) {
            return true;
        } else if (obj instanceof FileInfoImpl == false) {
            return false;
        }
        FileInfoImpl that = (FileInfoImpl) obj;
        return (this.getNodeRef().equals(that.getNodeRef()));
    }

    /**
     * @see #getNodeRef()
     * @see NodeRef#hashCode()
     */
    @Override
    public int hashCode() {
        return getNodeRef().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(80);
        sb.append("FileInfo")
                .append("[name=")
                .append(getName())
                .append(", isFolder=")
                .append(isFolder)
                .append(", nodeRef=")
                .append(nodeRef);

        if (isLink()) {
            sb.append(", linkref=");
            sb.append(linkNodeRef);
        }

        sb.append("]");
        return sb.toString();
    }

    public NodeRef getNodeRef() {
        return nodeRef;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public boolean isLink() {
        return isLink;
    }

    public NodeRef getLinkNodeRef() {
        return linkNodeRef;
    }

    public String getName() {
        return (String) properties.get(ContentModel.PROP_NAME);
    }

    public Date getCreatedDate() {
        return DefaultTypeConverter.INSTANCE.convert(
                Date.class, properties.get(ContentModel.PROP_CREATED));
    }

    public Date getModifiedDate() {
        return DefaultTypeConverter.INSTANCE.convert(
                Date.class, properties.get(ContentModel.PROP_MODIFIED));
    }

    public ContentData getContentData() {
        return DefaultTypeConverter.INSTANCE.convert(
                ContentData.class, properties.get(ContentModel.PROP_CONTENT));
    }

    public Map<QName, Serializable> getProperties() {
        return properties;
    }

    @Override
    public QName getType() {
        // TODO Auto-generated method stub
        return null;
    }
}
