package eu.cec.digit.circabc.service.rendition;

import java.util.List;

/**
 * Interface for the component that manages the database access to store and update rendition
 * requests to generate the documents for the PDF preview.
 *
 * @author schwerr
 */
public interface RenditionDaoService {

    /**
     * Adds a new request to be processed.
     */
    void addRequest(Request request);

    /**
     * Gets the list of requests to be processed (or being processed).
     */
    List<Request> getRequests(String hostName);

    /**
     * Gets the list of ten requests to be processed (or being processed) independently from the host
     * name.
     */
    List<Request> getTenRequests();

    /**
     * Gets the list of all requests for the provided document id.
     */
    List<Request> getRequestsForDocument(String documentId);

    /**
     * This method updates the request once it has been fetched and processed. It should be used in
     * the same transaction as getRequests until its fetch date is updated.
     */
    void updateRequest(Request request);

    /**
     * This method updates a request list once it has been got. It should be used in the same
     * transaction as getRequests until its fetch date is updated.
     */
    void updateFetchedRequests(List<Request> requests);
}
