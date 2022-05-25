package io.swagger.api;

import io.swagger.model.*;

import java.io.InputStream;
import java.util.List;

/**
 * @author beaurpi
 */
public interface CategoriesApi {

    /**
     * Get all the Categories belonging to all Headers in the application /categories
     *
     * @return List of Categories in CIRCABC
     */
    List<Category> getCategories();

    /**
     * get the list of Interest groups belonging to one category /categories/{id}/groups
     *
     * @param id Category ID
     * @return List of all interest groups in one Category
     */
    List<InterestGroup> getInterestGroupByCategoryId(String id);

    List<Profile> categoriesIdExportedProfilesGet(String id, String ignoreIgId);

    /**
     * To create a new group inside the category
     *
     * @return the Interest Group object
     */
    InterestGroup categoriesIdGroupsPost(String id, InterestGroupPostModel ig);

    PagedStatisticsContents getIGStatisticsContents(String id, int startItem, int amount);

    void calculateIGStatistics(String id);

    void categoriesIdGroupRequestPost(String categoryId, GroupCreationRequest body);

    List<User> categoriesIdAdminsGet(String categoryId);

    /**
     * get the list of logos uploaded in the category
     */
    List<Node> getCategoryLogoByCategoryId(String categoryId);

    /**
     * upload a new logo in the category logos folder
     */
    void postCategoryLogoByCategoryId(String categoryId, InputStream inputStream, String fileName);

    /**
     * mark one logo id as selected
     */
    void selectCategoryLogoByLogoId(String categoryId, String logoId);

    List<Node> deleteCategoryLogoByLogoId(String categoryId, String logoId);

    /**
     * get the details of a category
     */
    Category categoriesIdGet(String categoryId);

    /**
     * update the details of a category
     */
    Category categoriesIdPut(String categoryId, Category category);

    /**
     * add new users as category administrator
     */
    List<String> categoriesIdAdminsPost(String categoryId, List<String> userIds);

    /**
     * remove an admin from a category
     */
    void categoriesIdAdminsDelete(String categoryId, String userId);

    /**
     * create a new category in the header
     */
    Category headersIdCategoryPost(String headerId, Category categoryBody);

    void categoriesIdAdminContactPost(String categoryId, AdminContactRequest body);

    PagedGroupCreationRequests categoriesIdGroupRequestsGet(
            String categoryRef, Integer limit, Integer page, String filter);

    void categoriesIdGroupRequestApprovalPost(
            String categoryId, GroupCreationRequestApproval body, String username);

    void categoriesGroupRequestPut(String requestId, GroupCreationRequest body);
}
