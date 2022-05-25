package eu.cec.digit.circabc.service.rendition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mybatis.spring.SqlSessionTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Implementation for the component that manages the database access to store and update rendition
 * requests to generate the documents for the PDF preview.
 *
 * @author schwerr
 */
public class RenditionDaoServiceImpl implements RenditionDaoService {

    private static final Log logger = LogFactory.getLog(RenditionDaoServiceImpl.class);

    private SqlSessionTemplate sqlSessionTemplate = null;

    /**
     * @see eu.cec.digit.circabc.service.rendition.RenditionDaoService#addRequest(Request)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void addRequest(Request request) {

        List<Request> existsRequests =
                (List<Request>)
                        sqlSessionTemplate.selectList(
                                "Rendition.select_request_exists", request.getNodeRefUUID());

        // Was this nodeRef already added to processing?
        if (existsRequests != null && !existsRequests.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Request already exists: " + request.getNodeRefUUID());
            }
            return;
        }

        request.setFetchDate(null);
        request.setStartProcessingDate(null);
        request.setEndProcessingDate(null);
        request.setSuccess(false);
        request.setErrorMessage(null);

        sqlSessionTemplate.insert("Rendition.insert_request", request);

        if (logger.isDebugEnabled()) {
            logger.debug("Request added: " + request.getNodeRefUUID());
        }
    }

    /**
     * @see eu.cec.digit.circabc.service.rendition.RenditionDaoService#getRequests()
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Request> getRequests(String hostName) {
        return (List<Request>) sqlSessionTemplate.selectList("Rendition.get_requests", hostName);
    }

    /**
     * @see eu.cec.digit.circabc.service.rendition.RenditionDaoService#getRequests()
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Request> getTenRequests() {
        return (List<Request>) sqlSessionTemplate.selectList("Rendition.get_ten_requests");
    }

    /**
     * @see eu.cec.digit.circabc.service.rendition.RenditionDaoService#getRequestsForDocument(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Request> getRequestsForDocument(String documentId) {
        return (List<Request>)
                sqlSessionTemplate.selectList(
                        "Rendition.get_requests_for_document", "workspace://SpacesStore/" + documentId);
    }

    /**
     * @see eu.cec.digit.circabc.service.rendition.RenditionDaoService#updateRequest(Request)
     */
    @Override
    public void updateRequest(Request request) {
        sqlSessionTemplate.update("Rendition.update_request", request);
    }

    /**
     * @see eu.cec.digit.circabc.service.rendition.RenditionDaoService#updateFetchedRequests(java.util.List)
     */
    @Override
    public void updateFetchedRequests(List<Request> requests) {

        if (requests == null || requests.size() == 0) {
            return;
        }

        List<String> requestIds = new ArrayList<>();

        for (Request request : requests) {
            request.setFetchDate(new Date());
            requestIds.add(request.getRequestId());
        }

        sqlSessionTemplate.update("Rendition.update_fetched_requests", requestIds);
    }

    /**
     * @param sqlSessionTemplate the sqlSessionTemplate to set
     */
    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }
}
