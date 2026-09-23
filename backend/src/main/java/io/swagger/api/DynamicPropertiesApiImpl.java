package io.swagger.api;

import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicProperty;
import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicPropertyImpl;
import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicPropertyService;
import eu.europa.ec.digit.circabc.rest.service.dynamic.property.DynamicPropertyType;
import io.swagger.model.DynamicPropertyDefinition;
import io.swagger.model.DynamicPropertyDefinitionUpdatedValues;
import io.swagger.util.Converter;
import java.util.*;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Implementation of the {@link DynamicPropertiesApi} contract that backs the dynamic-properties
 * REST endpoints.
 *
 * <p>Dynamic properties are user-defined metadata definitions attached to a group (interest group)
 * node. This class translates between the REST layer DTOs ({@link DynamicPropertyDefinition} and
 * {@link DynamicPropertyDefinitionUpdatedValues}) and the underlying {@link DynamicPropertyService},
 * which persists and manages the dynamic properties in the Alfresco repository. It supports listing,
 * retrieving, creating, updating and deleting dynamic property definitions, including the handling of
 * selection / multi-selection value lists.
 *
 * @author beaurpi
 */
public class DynamicPropertiesApiImpl implements DynamicPropertiesApi {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(DynamicPropertiesApiImpl.class);

  /**
   * Service that performs the actual persistence and business logic for dynamic properties in the
   * Alfresco repository. Injected by Spring using the {@code DynamicPropertyService} qualifier.
   */
  @Autowired
  @Qualifier("DynamicPropertyService") // NOSONAR
  private DynamicPropertyService dynamicPropertiesService;

  /**
   * Returns all dynamic property definitions attached to the given group node.
   *
   * @param id the identifier of the group node whose dynamic properties are requested
   * @return the list of {@link DynamicPropertyDefinition} instances defined on the node; an empty
   *     list if none are defined
   */
  @Override
  public List<DynamicPropertyDefinition> groupsIdDynpropsGet(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    List<DynamicProperty> lDynProps =
      dynamicPropertiesService.getDynamicProperties(nodeRef);
    List<DynamicPropertyDefinition> result = new ArrayList<>();

    for (DynamicProperty dynProp : lDynProps) {
      DynamicPropertyDefinition ddd = toDynamicPropertyDefinition(dynProp);
      result.add(ddd);
    }

    return result;
  }

  /**
   * Deletes the dynamic property definition identified by the given id.
   *
   * @param id the identifier of the dynamic property node to delete
   */
  @Override
  public void dynpropsIdDelete(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    DynamicProperty dp = dynamicPropertiesService.getDynamicPropertyByID(
      nodeRef
    );
    dynamicPropertiesService.deleteDynamicProperty(dp);
  }

  /**
   * Creates a new dynamic property definition on the given group node.
   *
   * <p>For {@code SELECTION} and {@code MULTI_SELECTION} property types, the list of possible values
   * carried by {@code body} is flattened into the separator-delimited representation expected by the
   * service.
   *
   * @param id the identifier of the group node the new dynamic property is attached to
   * @param body the definition of the dynamic property to create (title, property type and, for
   *     selection types, the possible values)
   * @return the persisted {@link DynamicPropertyDefinition}, including its generated id and index
   */
  @Override
  public DynamicPropertyDefinition groupsIdDynpropsPost(
    String id,
    DynamicPropertyDefinition body
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);

    String values;

    values = extractValues(body);

    DynamicPropertyImpl dynamicProperty = new DynamicPropertyImpl(
      Converter.toMLText(body.getTitle()),
      DynamicPropertyType.valueOf(body.getPropertyType()),
      values
    );
    DynamicProperty dpTmp = dynamicPropertiesService.addDynamicProperty(
      nodeRef,
      dynamicProperty
    );

    return toDynamicPropertyDefinition(dpTmp);
  }

  /**
   * Maps an internal {@link DynamicProperty} to the REST layer {@link DynamicPropertyDefinition}
   * DTO.
   *
   * @param dpTmp the internal dynamic property to convert
   * @return the corresponding {@link DynamicPropertyDefinition} populated from {@code dpTmp}
   */
  private DynamicPropertyDefinition toDynamicPropertyDefinition(
    DynamicProperty dpTmp
  ) {
    DynamicPropertyDefinition ddd = new DynamicPropertyDefinition();
    ddd.setId(dpTmp.getId().getId());
    ddd.setIndex(dpTmp.getIndex());
    ddd.setName(dpTmp.getName());
    ddd.setTitle(Converter.toI18NProperty(dpTmp.getLabel()));
    ddd.setPossibleyValues(dpTmp.getListOfValidValues());
    ddd.setPropertyType(dpTmp.getType().getModelDataDefinition());
    return ddd;
  }

  /**
   * Builds the separator-delimited string of valid values for a selection-type dynamic property.
   *
   * <p>Only applies to {@code SELECTION} and {@code MULTI_SELECTION} property types; for any other
   * type an empty string is returned. Blank tokens are skipped and remaining tokens are trimmed.
   *
   * @param body the dynamic property definition carrying the possible values
   * @return the trimmed, separator-delimited concatenation of the possible values, or an empty
   *     string when the property is not a selection type or has no values
   */
  private String extractValues(DynamicPropertyDefinition body) {
    String values;
    final StringBuilder formatedValues = new StringBuilder();
    if (
      body.getPropertyType().equals(DynamicPropertyType.SELECTION.name()) ||
      body.getPropertyType().equals(DynamicPropertyType.MULTI_SELECTION.name())
    ) {
      for (String token : body.getPossibleValues()) {
        if (!token.isEmpty()) {
          formatedValues
            .append(token.trim())
            .append(DynamicPropertyService.MULTI_VALUES_SEPARATOR);
        }
      }
    }
    values = formatedValues.toString();
    return values;
  }

  /**
   * Updates an existing dynamic property definition.
   *
   * <p>The property is resolved from the id carried by {@code body}. The label (title) is always
   * updated. For selection-type properties the incoming {@code updatedValues} are partitioned by
   * their status into untouched/new values (kept), edited values (old-to-new mapping) and deleted
   * values, and the service is asked to reconcile the valid value list accordingly.
   *
   * @param id the identifier of the dynamic property (path parameter); the effective target is
   *     resolved from {@code body.getId()}
   * @param body the updated dynamic property definition, including title, property type and the
   *     per-value update instructions
   * @return the refreshed {@link DynamicPropertyDefinition} after the update has been applied
   */
  @Override
  public DynamicPropertyDefinition dynpropsIdPut(
    String id,
    DynamicPropertyDefinition body
  ) {
    NodeRef nodeRef = Converter.createNodeRefFromId(body.getId());
    DynamicProperty dp = dynamicPropertiesService.getDynamicPropertyByID(
      nodeRef
    );
    dynamicPropertiesService.updateDynamicPropertyLabel(
      dp,
      Converter.toMLText(body.getTitle())
    );

    if (body.getPropertyType().contains("SELECTION")) {
      List<DynamicPropertyDefinitionUpdatedValues> untouchedBodyValues =
        new ArrayList<>();
      Set<String> deletedValues = new HashSet<>();
      Map<String, String> editedBodyValues = new HashMap<>();

      for (DynamicPropertyDefinitionUpdatedValues value : body.getUpdatedValues()) {
        if ("".equals(value.getStatus()) || "new".equals(value.getStatus())) {
          untouchedBodyValues.add(value);
        } else if ("edited".equals(value.getStatus())) {
          editedBodyValues.put(value.getOld(), value.getNewValue());
          untouchedBodyValues.add(value);
        } else if ("deleted".equals(value.getStatus())) {
          deletedValues.add(value.getOld());
        }
      }

      String validValues = convertValues(untouchedBodyValues);

      dynamicPropertiesService.updateDynamicPropertyValidValues(
        dp,
        validValues,
        true,
        deletedValues,
        editedBodyValues
      );
    }

    return toDynamicPropertyDefinition(
      dynamicPropertiesService.getDynamicPropertyByID(nodeRef)
    );
  }

  /**
   * Builds the separator-delimited string of valid values from a list of value update instructions,
   * using each entry's new value.
   *
   * @param body the list of value update instructions to convert
   * @return the trimmed, separator-delimited concatenation of the non-empty new values
   */
  private String convertValues(
    List<DynamicPropertyDefinitionUpdatedValues> body
  ) {
    String values;
    final StringBuilder formatedValues = new StringBuilder();
    for (DynamicPropertyDefinitionUpdatedValues token : body) {
      if (!token.getNewValue().isEmpty()) {
        formatedValues
          .append(token.getNewValue().trim())
          .append(DynamicPropertyService.MULTI_VALUES_SEPARATOR);
      }
    }
    values = formatedValues.toString();
    return values;
  }

  /**
   * Retrieves a single dynamic property definition by its id.
   *
   * @param id the identifier of the dynamic property node to retrieve
   * @return the matching {@link DynamicPropertyDefinition}
   */
  @Override
  public DynamicPropertyDefinition dynpropsIdGet(String id) {
    NodeRef nodeRef = Converter.createNodeRefFromId(id);
    DynamicProperty dp = dynamicPropertiesService.getDynamicPropertyByID(
      nodeRef
    );
    return toDynamicPropertyDefinition(dp);
  }
}
