package io.swagger.api;

import io.swagger.model.DynamicPropertyDefinition;
import java.util.List;

/**
 * Business operations for managing the dynamic property definitions of an Interest Group.
 *
 * <p>Dynamic properties allow an Interest Group to define custom metadata fields (beyond the
 * standard content model) that can be attached to its content. This interface exposes the CRUD
 * operations behind the {@code /groups/{id}/dynprops} and {@code /dynprops/{id}} REST resources;
 * implementations contain the actual logic while the corresponding webscript endpoints delegate
 * to them.
 *
 * @author beaurpi
 */
public interface DynamicPropertiesApi {
  /**
   * Retrieves the list of dynamic property definitions declared for an Interest Group.
   *
   * <p>Backs the {@code GET /groups/{id}/dynprops} resource.
   *
   * @param id the identifier of the Interest Group whose dynamic properties are requested
   * @return the dynamic property definitions declared for the Interest Group; may be empty when
   *     none are defined
   */
  List<DynamicPropertyDefinition> groupsIdDynpropsGet(String id);

  /**
   * Removes a single dynamic property definition from its Interest Group.
   *
   * <p>Backs the {@code DELETE /dynprops/{id}} resource.
   *
   * @param id the identifier of the dynamic property definition to remove
   */
  void dynpropsIdDelete(String id);

  /**
   * Creates a new dynamic property definition inside an Interest Group.
   *
   * <p>Backs the {@code POST /groups/{id}/dynprops} resource.
   *
   * @param id the identifier of the Interest Group the new dynamic property is added to
   * @param body the definition of the dynamic property to create
   * @return the newly created dynamic property definition
   */
  DynamicPropertyDefinition groupsIdDynpropsPost(
    String id,
    DynamicPropertyDefinition body
  );

  /**
   * Updates an existing dynamic property definition of an Interest Group.
   *
   * <p>Backs the {@code PUT /dynprops/{id}} resource.
   *
   * @param id the identifier of the dynamic property definition to update
   * @param body the new state of the dynamic property definition
   * @return the updated dynamic property definition
   */
  DynamicPropertyDefinition dynpropsIdPut(
    String id,
    DynamicPropertyDefinition body
  );

  /**
   * Retrieves a single dynamic property definition of an Interest Group.
   *
   * <p>Backs the {@code GET /dynprops/{id}} resource.
   *
   * @param id the identifier of the dynamic property definition to retrieve
   * @return the requested dynamic property definition
   */
  DynamicPropertyDefinition dynpropsIdGet(String id);
}
