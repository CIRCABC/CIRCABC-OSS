package io.swagger.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DynamicPropertyDefinition {

    private String id = null;

    private Long index = null;

    private String name = null;

    private I18nProperty title = null;

    private String propertyType = null;

    private List<String> possibleValues = new ArrayList<>();

    private List<DynamicPropertyDefinitionUpdatedValues> updatedValues = new ArrayList<>();

    public List<DynamicPropertyDefinitionUpdatedValues> getUpdatedValues() {
        return updatedValues;
    }

    public void setUpdatedValues(List<DynamicPropertyDefinitionUpdatedValues> updatedValues) {
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

    public void setPossibleyValues(List<String> possibleValues) {
        this.possibleValues = possibleValues;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DynamicPropertyDefinition dynamicPropertyDefinition = (DynamicPropertyDefinition) o;
        return Objects.equals(this.id, dynamicPropertyDefinition.id)
                && Objects.equals(this.name, dynamicPropertyDefinition.name)
                && Objects.equals(this.title, dynamicPropertyDefinition.title)
                && Objects.equals(this.propertyType, dynamicPropertyDefinition.propertyType)
                && Objects.equals(this.possibleValues, dynamicPropertyDefinition.possibleValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, title, propertyType, possibleValues);
    }

    @Override
    public String toString() {

        return "class DynamicPropertyDefinition {\n"
                + "    id: "
                + toIndentedString(id)
                + "\n"
                + "    index: "
                + toIndentedString(index)
                + "\n"
                + "    name: "
                + toIndentedString(name)
                + "\n"
                + "    title: "
                + toIndentedString(title)
                + "\n"
                + "    propertyType: "
                + toIndentedString(propertyType)
                + "\n"
                + "    possibleyValues: "
                + toIndentedString(possibleValues)
                + "\n"
                + "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
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
