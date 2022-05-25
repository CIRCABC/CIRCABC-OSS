package io.swagger.api;

import io.swagger.exception.EmptyQueryStringException;
import io.swagger.model.PagedSearchNodes;

/**
 * @author beaurpi
 */
public interface SearchApi {

    PagedSearchNodes searchGet(String q, String node, Integer limit, Integer page)
            throws EmptyQueryStringException;
}
