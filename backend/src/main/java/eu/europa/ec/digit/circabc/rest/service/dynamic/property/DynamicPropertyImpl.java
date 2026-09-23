/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.europa.ec.digit.circabc.rest.service.dynamic.property;

import java.io.Serializable;
import java.util.*;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Default implementation of {@link DynamicProperty}.
 *
 * <p>A dynamic property is a user-configurable metadata field (for example a text field, text area,
 * date field or a single/multi selection list) that can be attached to CIRCABC nodes. This class is
 * a plain, serializable value object holding the property's ordering index, its multilingual label,
 * its {@link DynamicPropertyType type} and, for selection types, the set of valid values.
 *
 * <p>The valid values of a selection property are stored as a single string in which the individual
 * choices are separated by {@link DynamicPropertyService#MULTI_VALUES_SEPARATOR}.
 *
 * <p>Instances are ordered by their {@link #getIndex() index} through the {@link Comparable}
 * contract inherited from {@link DynamicProperty}.
 */
public class DynamicPropertyImpl implements DynamicProperty, Serializable {

  /**
   * Serialization version identifier for this value object.
   */
  private static final long serialVersionUID = -4539369354206620896L;

  /** Ordering position of the property within its owning collection. */
  private Long index;

  /**
   * For selection types, the allowed values encoded as a single string whose entries are separated
   * by {@link DynamicPropertyService#MULTI_VALUES_SEPARATOR}.
   */
  private String validValues;

  /** The multilingual display label of the property. */
  private MLText label;

  /** The kind of property (e.g. text field, date field, single or multi selection). */
  private DynamicPropertyType type;

  /**
   * The node reference identifying where the dynamic property is stored in the repository.
   */
  private NodeRef id;

  /**
   * Creates a dynamic property without an index or repository id.
   *
   * @param label the multilingual label of the property
   * @param type the type of the property
   * @param validValues the encoded valid values (relevant for selection types), or {@code null}
   */
  public DynamicPropertyImpl(
    final MLText label,
    final DynamicPropertyType type,
    final String validValues
  ) {
    this(null, label, type, validValues);
  }

  /**
   * Creates a dynamic property with an ordering index but without a repository id.
   *
   * @param index the ordering position of the property
   * @param label the multilingual label of the property
   * @param type the type of the property
   * @param validValues the encoded valid values (relevant for selection types), or {@code null}
   */
  public DynamicPropertyImpl(
    final Long index,
    final MLText label,
    final DynamicPropertyType type,
    final String validValues
  ) {
    this(index, null, label, type, validValues);
  }

  /**
   * Creates a fully-specified dynamic property. Package-private constructor used internally when the
   * repository node reference is known.
   *
   * @param index the ordering position of the property
   * @param id the node reference where the property is stored
   * @param label the multilingual label of the property
   * @param type the type of the property
   * @param validValues the encoded valid values (relevant for selection types), or {@code null}
   */
  /*package */ DynamicPropertyImpl(
    final Long index,
    final NodeRef id,
    final MLText label,
    final DynamicPropertyType type,
    final String validValues
  ) {
    this.id = id;
    this.label = label;
    this.type = type;
    this.validValues = validValues;
    this.index = index;
  }

  /**
   * @return the validValues
   */
  public String getValidValues() {
    return validValues;
  }

  /**
   * @param validValues the validValues to set
   */
  public void setValidValues(String validValues) {
    this.validValues = validValues;
  }

  /**
   * Returns the valid values formatted for display, replacing the internal
   * {@link DynamicPropertyService#MULTI_VALUES_SEPARATOR} with a human-readable "; " separator.
   *
   * @return the display-friendly valid values, or an empty string if none are set
   */
  public String getDisplayValidValues() {
    return getValidValues() == null
      ? ""
      : getValidValues().replace(
          String.valueOf(DynamicPropertyService.MULTI_VALUES_SEPARATOR),
          "; "
        );
  }

  /**
   * @return the label
   */
  public MLText getLabel() {
    return label;
  }

  /**
   * @return the index
   */
  public Long getIndex() {
    return index;
  }

  /**
   * @return the type
   */
  public DynamicPropertyType getType() {
    return type;
  }

  public NodeRef getId() {
    return id;
  }

  /**
   * Returns the locales for which the label defines a value.
   *
   * @return a string representation of the label's locales, or {@code "[]"} if no label is set
   */
  public String getLanguages() {
    String result = "[]";
    if (this.label != null) {
      result = Arrays.toString(this.label.getLocales().toArray());
    }
    return result;
  }

  /**
   * Returns the name of the property, taken from the default value of its multilingual label.
   *
   * @return the label's default value, or an empty string if no label is set
   */
  public String getName() {
    return this.label == null ? "" : this.label.getDefaultValue();
  }

  /**
   * Returns a human-readable textual representation of this dynamic property, including its id,
   * index, label, type and (for selection types) its valid values.
   *
   * @return a string describing this property
   */
  @Override
  public String toString() {
    return (
      "Dynamic property " +
      ((id != null) ? (" - id: " + id) : "") +
      ((index != null) ? (" - index: " + index) : "") +
      " - label: " +
      label +
      " - type: " +
      type +
      ((DynamicPropertyType.SELECTION.equals(type))
        ? (" - validValues: " + validValues)
        : "")
    );
  }

  /**
   * Computes a hash code based on the id, index, label, type and valid values.
   *
   * @return the hash code of this property
   */
  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + ((id == null) ? 0 : id.hashCode());
    result = PRIME * result + ((index == null) ? 0 : index.hashCode());
    result = PRIME * result + ((label == null) ? 0 : label.hashCode());
    result = PRIME * result + ((type == null) ? 0 : type.hashCode());
    result =
      PRIME * result + ((validValues == null) ? 0 : validValues.hashCode());
    return result;
  }

  /**
   * Compares this property with another for equality. Two properties are equal when their id,
   * index, label, type and valid values are all equal.
   *
   * @param obj the object to compare with
   * @return {@code true} if the given object is an equal {@code DynamicPropertyImpl}, otherwise
   *     {@code false}
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DynamicPropertyImpl other = (DynamicPropertyImpl) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (index == null) {
      if (other.index != null) {
        return false;
      }
    } else if (!index.equals(other.index)) {
      return false;
    }
    if (label == null) {
      if (other.label != null) {
        return false;
      }
    } else if (!label.equals(other.label)) {
      return false;
    }
    if (type == null) {
      if (other.type != null) {
        return false;
      }
    } else if (!type.equals(other.type)) {
      return false;
    }
    if (validValues == null) {
      if (other.validValues != null) {
        return false;
      }
    } else if (!validValues.equals(other.validValues)) {
      return false;
    }
    return true;
  }

  /**
   * Parses the encoded {@link #getValidValues() valid values} string into a list of individual
   * values, splitting on {@link DynamicPropertyService#MULTI_VALUES_SEPARATOR}.
   *
   * @return the list of valid values, or an empty list if none are defined
   */
  public List<String> getListOfValidValues() {
    final String values = this.getValidValues();

    List<String> items = null;

    if (values != null && !values.isEmpty()) {
      final StringTokenizer tokens = new StringTokenizer(
        values,
        String.valueOf(DynamicPropertyService.MULTI_VALUES_SEPARATOR),
        false
      );

      items = new ArrayList<>(tokens.countTokens());

      while (tokens.hasMoreTokens()) {
        items.add(tokens.nextToken());
      }
    } else {
      items = Collections.emptyList();
    }

    return items;
  }

  /**
   * Indicates whether this property is a selection-based type (single or multi selection).
   *
   * @return {@code true} if the type is {@link DynamicPropertyType#SELECTION} or
   *     {@link DynamicPropertyType#MULTI_SELECTION}, otherwise {@code false}
   */
  public boolean isSelectionType() {
    return (
      this.type.equals(DynamicPropertyType.SELECTION) ||
      this.type.equals(DynamicPropertyType.MULTI_SELECTION)
    );
  }

  /**
   * Compares this property to another by their ordering index.
   *
   * @param o the property to compare with
   * @return a negative integer, zero, or a positive integer as this property's index is less than,
   *     equal to, or greater than the other property's index
   */
  public int compareTo(DynamicProperty o) {
    return (int) (this.getIndex() - o.getIndex());
  }
}
