package io.swagger.api;

import eu.cec.digit.circabc.repo.dynamic.property.DynamicPropertyImpl;
import eu.cec.digit.circabc.service.dynamic.property.DynamicProperty;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyService;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyType;
import io.swagger.model.DynamicPropertyDefinition;
import io.swagger.model.DynamicPropertyDefinitionUpdatedValues;
import io.swagger.util.Converter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * @author beaurpi
 */
public class DynamicPropertiesApiImpl implements DynamicPropertiesApi {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(DynamicPropertiesApiImpl.class);

    private DynamicPropertyService dynamicPropertiesService;

    @Override
    public List<DynamicPropertyDefinition> groupsIdDynpropsGet(String id) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        List<DynamicProperty> lDynProps = dynamicPropertiesService.getDynamicProperties(nodeRef);
        List<DynamicPropertyDefinition> result = new ArrayList<>();

        for (DynamicProperty dynProp : lDynProps) {
            DynamicPropertyDefinition ddd = toDynamicPropertyDefinition(dynProp);
            result.add(ddd);
        }

        return result;
    }

    /**
     * @return the dynamicPropertiesService
     */
    public DynamicPropertyService getDynamicPropertiesService() {
        return dynamicPropertiesService;
    }

    /**
     * @param dynamicPropertiesService the dynamicPropertiesService to set
     */
    public void setDynamicPropertiesService(DynamicPropertyService dynamicPropertiesService) {
        this.dynamicPropertiesService = dynamicPropertiesService;
    }

    @Override
    public void dynpropsIdDelete(String id) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        DynamicProperty dp = dynamicPropertiesService.getDynamicPropertyByID(nodeRef);
        dynamicPropertiesService.deleteDynamicProperty(dp);
    }

    @Override
    public DynamicPropertyDefinition groupsIdDynpropsPost(String id, DynamicPropertyDefinition body) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);

        String values;

        values = extractValues(body);

        DynamicPropertyImpl dynamicProperty =
                new DynamicPropertyImpl(
                        Converter.toMLText(body.getTitle()),
                        DynamicPropertyType.valueOf(body.getPropertyType()),
                        values);
        DynamicProperty dpTmp = dynamicPropertiesService.addDynamicProperty(nodeRef, dynamicProperty);

        return toDynamicPropertyDefinition(dpTmp);
    }

    /**
     * @param dpTmp
     * @return
     */
    private DynamicPropertyDefinition toDynamicPropertyDefinition(DynamicProperty dpTmp) {
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
     * @param body
     * @return
     */
    private String extractValues(DynamicPropertyDefinition body) {
        String values;
        final StringBuilder formatedValues = new StringBuilder();
        if (body.getPropertyType().equals(DynamicPropertyType.SELECTION.name())
                || body.getPropertyType().equals(DynamicPropertyType.MULTI_SELECTION.name())) {
            for (String token : body.getPossibleValues()) {
                if (token.length() > 0) {
                    formatedValues.append(token.trim()).append(DynamicPropertyService.MULTI_VALUES_SEPARATOR);
                }
            }
        }
        values = formatedValues.toString();
        return values;
    }

    @Override
    public DynamicPropertyDefinition dynpropsIdPut(String id, DynamicPropertyDefinition body) {
        NodeRef nodeRef = Converter.createNodeRefFromId(body.getId());
        DynamicProperty dp = dynamicPropertiesService.getDynamicPropertyByID(nodeRef);
        dynamicPropertiesService.updateDynamicPropertyLabel(dp, Converter.toMLText(body.getTitle()));

        if (body.getPropertyType().contains("SELECTION")) {
            List<DynamicPropertyDefinitionUpdatedValues> untouchedBodyValues = new ArrayList<>();
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
                    dp, validValues, true, deletedValues, editedBodyValues);
        }

        return toDynamicPropertyDefinition(dynamicPropertiesService.getDynamicPropertyByID(nodeRef));
    }

    private String convertValues(List<DynamicPropertyDefinitionUpdatedValues> body) {
        String values;
        final StringBuilder formatedValues = new StringBuilder();
        for (DynamicPropertyDefinitionUpdatedValues token : body) {
            if (token.getNewValue().length() > 0) {
                formatedValues
                        .append(token.getNewValue().trim())
                        .append(DynamicPropertyService.MULTI_VALUES_SEPARATOR);
            }
        }
        values = formatedValues.toString();
        return values;
    }

    @Override
    public DynamicPropertyDefinition dynpropsIdGet(String id) {
        NodeRef nodeRef = Converter.createNodeRefFromId(id);
        DynamicProperty dp = dynamicPropertiesService.getDynamicPropertyByID(nodeRef);
        return toDynamicPropertyDefinition(dp);
    }
}
