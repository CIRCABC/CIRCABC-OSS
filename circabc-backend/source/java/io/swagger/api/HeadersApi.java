package io.swagger.api;

import io.swagger.model.Category;
import io.swagger.model.Header;

import java.util.List;

public interface HeadersApi {

    // DELETE /headers/{id}
    // Delete one header based on the id parameter To see if this is allowed and
    // what will happen from all the categories and Interest group inside

    void deleteHeader(String id);

    // For one header, list its belonging categories
    // GET /headers/{id}/categories
    List<Category> getCategoriesByHeaderId(String id, String language, Boolean guest);

    // Get one header
    // GET /headers/{id}",
    Header getHeader(String id);

    // Gets all the `Headers` of the application. This call does not require any
    // authentication token
    // GET/headers
    List<Header> getHeaders(String language, Boolean guest);

    // creates a new category
    // POST"/headers/{id}/categories",
    Category headersIdCategoriesPost(String id, Category body);

    // Creates a new header in the application
    // POST /headers",
    Header postHeader(Header body);

    // Updates a header in the application
    // /headers/{id}
    Header putHeader(String id, Header body);

    Header getHeaderByCategory(String id);
}
