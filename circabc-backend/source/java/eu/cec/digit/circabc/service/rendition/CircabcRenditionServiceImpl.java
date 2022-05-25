package eu.cec.digit.circabc.service.rendition;

import eu.cec.digit.circabc.config.CircabcConfiguration;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * Implementation of the service to process renditions.
 *
 * @author schwerr
 */
public class CircabcRenditionServiceImpl implements CircabcRenditionService {

    private static final Log logger = LogFactory.getLog(CircabcRenditionServiceImpl.class);

    private NodeService nodeService = null;

    private RenditionDaoService renditionDaoService = null;

    /**
     * @see eu.cec.digit.circabc.service.rendition.CircabcRenditionService#addRequest(NodeRef)
     */
    @Override
    public void addRequest(NodeRef nodeRef) {

        if (!nodeService.exists(nodeRef)) {
            return;
        }

        String hostName =
                CircabcConfiguration.getProperty(CircabcConfiguration.DOC_PREVIEW_RENDER_HOST);

        if (hostName == null) {

            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                logger.warn("Error when retrieving machine name. Setting name to null.", e);
            }
        }

        String nodeRefId = nodeRef.getStoreRef() + "/" + nodeRef.getId();

        Request request = new Request(GUID.generate(), hostName, nodeRefId, new Date());

        try {
            renditionDaoService.addRequest(request);
        } catch (Exception e) {
            logger.error("Error when adding a rendition request for processing.", e);
        }
    }

    /**
     * Sets the value of the nodeService
     *
     * @param nodeService the nodeService to set.
     */
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * Sets the value of the renditionDaoService
     *
     * @param renditionDaoService the renditionDaoService to set.
     */
    public void setRenditionDaoService(RenditionDaoService renditionDaoService) {
        this.renditionDaoService = renditionDaoService;
    }
}
