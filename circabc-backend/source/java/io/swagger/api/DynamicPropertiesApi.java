package io.swagger.api;

import io.swagger.model.DynamicPropertyDefinition;

import java.util.List;

/**
 * @author beaurpi
 */
public interface DynamicPropertiesApi {

    /**
     * get the list of dynamic properties definition in an Interest group /groups/{id}/dynprops
     */
    List<DynamicPropertyDefinition> groupsIdDynpropsGet(String id);

    /**
     * remove one dynamic property from the Interest group /dynprops/{id}
     */
    void dynpropsIdDelete(String id);

    /**
     * create a new dynamic property inside the Interest group /groups/{id}/dynprops
     */
    DynamicPropertyDefinition groupsIdDynpropsPost(String id, DynamicPropertyDefinition body);

    /**
     * update a dynamic property inside the Interest group /dynprops/{id}
     */
    DynamicPropertyDefinition dynpropsIdPut(String id, DynamicPropertyDefinition body);

    /**
     * get a dynamic property inside the Interest group /dynprops/{id}
     */
    DynamicPropertyDefinition dynpropsIdGet(String id);
}
