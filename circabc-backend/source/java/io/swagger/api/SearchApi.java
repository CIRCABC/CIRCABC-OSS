package io.swagger.api;

import io.swagger.exception.EmptyQueryStringException;
import io.swagger.model.PagedSearchNodes;
import java.util.Date;

/**
 * @author beaurpi
 */
public interface SearchApi {
  PagedSearchNodes searchGet(
    String q,
    String node,
    String language,
    Integer page,
    Integer limit,
    String searchFor,
    String searchIn,
    String creator,
    Date creationDateFrom,
    Date creationDateTo,
    Date modifiedDateFrom,
    Date modifiedDateTo,
    String keywords,
    String status,
    String securityRanking,
    String version,
    String[] dynamicProperties,
    String sort,
    boolean order
  ) throws EmptyQueryStringException;
}
