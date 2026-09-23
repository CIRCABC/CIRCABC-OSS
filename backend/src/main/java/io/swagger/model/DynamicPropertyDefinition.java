package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data transfer object describing the definition of a dynamic (configurable) property.
 *
 * <p>A dynamic property definition captures the metadata used to declare a custom,
 * configurable property within CIRCABC: its identifier, ordering index, machine name,
 * localized title, value type and the set of allowed values. It also carries any
 * requested value updates via {@link DynamicPropertyDefinitionUpdatedValues}.</p>
 *
 * <p>This class is a plain model/DTO used to serialize and deserialize property
 * definitions exchanged through the REST API; it contains no business logic.</p>
 */
public class DynamicPropertyDefinition {

  /** Unique identifier of the property definition. */
  private String id = null;

  /** Ordering index used to position this property relative to others. */
  private Long index = null;

  /** Machine (technical) name of the property. */
  private String name = null;

  /** Localized, human-readable title of the property. */
  private I18nProperty title = null;

  /** Type of the property value (e.g. text, date, list). */
  private String propertyType = null;

  /** Allowed values the property may take. */
  private List<String> possibleValues = new ArrayList<>();

  /** Requested changes to the property's values. */
  private List<DynamicPropertyDefinitionUpdatedValues> updatedValues =
    new ArrayList<>();

  /**
   * Returns the requested changes to the property's values.
   *
   * @return the list of value updates
   */
  public List<DynamicPropertyDefinitionUpdatedValues> getUpdatedValues() {
    return updatedValues;
  }

  /**
   * Sets the requested changes to the property's values.
   *
   * @param updatedValues the list of value updates to set
   */
  public void setUpdatedValues(
    List<DynamicPropertyDefinitionUpdatedValues> updatedValues
  ) {
    this.updatedValues = updatedValues;
  }

  /**
   * Get id
   *
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the unique identifier of the property definition.
   *
   * @param id the identifier to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get name
   *
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the machine (technical) name of the property.
   *
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get title
   *
   * @return title
   */
  public I18nProperty getTitle() {
    return title;
  }

  /**
   * Sets the localized, human-readable title of the property.
   *
   * @param title the title to set
   */
  public void setTitle(I18nProperty title) {
    this.title = title;
  }

  /**
   * Get propertyType
   *
   * @return propertyType
   */
  public String getPropertyType() {
    return propertyType;
  }

  /**
   * Sets the type of the property value.
   *
   * @param propertyType the property type to set
   */
  public void setPropertyType(String propertyType) {
    this.propertyType = propertyType;
  }

  /**
   * Get possibleyValues
   *
   * @return possibleyValues
   */
  public List<String> getPossibleValues() {
    return possibleValues;
  }

  /**
   * Sets the allowed values the property may take.
   *
   * @param possibleValues the possible values to set
   */
  public void setPossibleyValues(List<String> possibleValues) {
    this.possibleValues = possibleValues;
  }

  /**
   * Compares this property definition to another object for equality.
   *
   * <p>Two definitions are considered equal when their {@code id}, {@code name},
   * {@code title}, {@code propertyType} and {@code possibleValues} are all equal.</p>
   *
   * @param o the object to compare with
   * @return {@code true} if the given object is an equal {@code DynamicPropertyDefinition}
   */
  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DynamicPropertyDefinition dynamicPropertyDefinition =
      (DynamicPropertyDefinition) o;
    return (
      Objects.equals(this.id, dynamicPropertyDefinition.id) &&
      Objects.equals(this.name, dynamicPropertyDefinition.name) &&
      Objects.equals(this.title, dynamicPropertyDefinition.title) &&
      Objects.equals(
        this.propertyType,
        dynamicPropertyDefinition.propertyType
      ) &&
      Objects.equals(
        this.possibleValues,
        dynamicPropertyDefinition.possibleValues
      )
    );
  }

  /**
   * Returns a hash code consistent with {@link #equals(java.lang.Object)}.
   *
   * @return the hash code for this property definition
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, name, title, propertyType, possibleValues);
  }

  /**
   * Returns a human-readable string representation of this property definition.
   *
   * @return a string describing all fields of this object
   */
  @Override
  public String toString() {
    return (
      "class DynamicPropertyDefinition {\n" +
      "    id: " +
      toIndentedString(id) +
      "\n" +
      "    index: " +
      toIndentedString(index) +
      "\n" +
      "    name: " +
      toIndentedString(name) +
      "\n" +
      "    title: " +
      toIndentedString(title) +
      "\n" +
      "    propertyType: " +
      toIndentedString(propertyType) +
      "\n" +
      "    possibleyValues: " +
      toIndentedString(possibleValues) +
      "\n" +
      "}"
    );
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to convert
   * @return the indented string representation of {@code o}
   */
  private String toIndentedString(java.lang.Object o) {
    return Util.toIndentedString(o);
  }

  /**
   * @return the index
   */
  public Long getIndex() {
    return index;
  }

  /**
   * @param index the index to set
   */
  public void setIndex(Long index) {
    this.index = index;
  }
}
