package io.swagger.api;

import io.swagger.model.InformationPage;
import io.swagger.model.News;
import io.swagger.model.PagedNews;

/**
 * @author beaurpi
 */
public interface InformationApi {

    InformationPage groupsIdInformationGet(String id);

    PagedNews groupsIdInformationNewsGet(String id, Integer limit, Integer page);

    News groupsIdInformationNewsPost(String id, News news);

    void newsIdDelete(String id);

    News newsIdGet(String id);

    News newsIdPut(String id, News news);

    void groupsIdInformationPut(String id, InformationPage body);
}
