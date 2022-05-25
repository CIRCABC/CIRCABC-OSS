/*******************************************************************************
 * Copyright 2006 European Community
 *
 *  Licensed under the EUPL, Version 1.1 or - as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  You may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  https://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 ******************************************************************************/
package eu.cec.digit.circabc.web.wai.generator;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.generator.BaseComponentGenerator;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.common.converter.XMLDateConverter;
import org.alfresco.web.ui.repo.RepoConstants;
import org.alfresco.web.ui.repo.component.UIMultiValueEditor;
import org.alfresco.web.ui.repo.component.property.PropertySheetItem;
import org.alfresco.web.ui.repo.component.property.UIPropertySheet;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import java.util.Calendar;

/**
 * Generates a date picker component.
 *
 * @author gavinc
 */
public class DatePickerGenerator extends BaseComponentGenerator {

    public static final String CIRCABC_FACES_DATE_PICKER_RENDERER = "eu.cec.digit.circabc.faces.DatePickerGeneratorRenderer";
    private static final String MSG_DATE = "date_pattern";
    private boolean initialiseIfNull = false;
    private int yearCount = 30;
    private int thisYear = Calendar.getInstance().get(Calendar.YEAR);
    private int threeYearsAgo = thisYear - 3;
    private int startYear = thisYear - 2;
    private String noneLabel = null;

    public int getThreeYearsAgo() {
        return threeYearsAgo;
    }

    public int getThisYear() {
        return thisYear;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.web.wai.generator.IDateComponent#getStartYear()
     */
    public int getStartYear() {
        return startYear;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.web.wai.generator.IDateComponent#setStartYear(int)
     */
    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.web.wai.generator.IDateComponent#getYearCount()
     */
    public int getYearCount() {
        return yearCount;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.web.wai.generator.IDateComponent#setYearCount(int)
     */
    public void setYearCount(int yearCount) {
        this.yearCount = yearCount;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.web.wai.generator.IDateComponent#isInitialiseIfNull()
     */
    public boolean isInitialiseIfNull() {
        return initialiseIfNull;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.web.wai.generator.IDateComponent#setInitialiseIfNull(boolean)
     */
    public void setInitialiseIfNull(boolean initialiseIfNull) {
        this.initialiseIfNull = initialiseIfNull;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.web.wai.generator.IDateComponent#getNoneLabel()
     */
    public String getNoneLabel() {
        return this.noneLabel;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.web.wai.generator.IDateComponent#setNoneLabel(java.lang.String)
     */
    public void setNoneLabel(String noneLabel) {
        this.noneLabel = noneLabel;
    }

    @SuppressWarnings("unchecked")
    public UIComponent generate(FacesContext context, String id) {
        UIComponent component = context.getApplication().
                createComponent(ComponentConstants.JAVAX_FACES_INPUT);

        component.setRendererType(CIRCABC_FACES_DATE_PICKER_RENDERER);
        FacesHelper.setupComponentId(context, component, id);
        component.getAttributes().put("startYear", this.startYear);
        component.getAttributes().put("yearCount", this.yearCount);
        component.getAttributes().put("initialiseIfNull", this.initialiseIfNull);
        component.getAttributes().put("style", "margin-right: 7px;");
        if (this.noneLabel != null) {
            component.getAttributes().put("noneLabel", this.noneLabel);
        }

        return component;
    }

    @Override
    protected void setupConverter(FacesContext context,
                                  UIPropertySheet propertySheet, PropertySheetItem property,
                                  PropertyDefinition propertyDef, UIComponent component) {
        if (property.getConverter() != null) {
            // create and add the custom converter
            createAndSetConverter(context, property.getConverter(), component);
        } else {
            // use the default converter for the date component
            // we can cast this as we know it is an UIOutput type
            ((UIOutput) component).setConverter(getDefaultConverter(context));
        }
    }

    @Override
    protected void setupMandatoryValidation(FacesContext context,
                                            UIPropertySheet propertySheet, PropertySheetItem item,
                                            UIComponent component, boolean realTimeChecking, String idSuffix) {
        if (component instanceof UIMultiValueEditor) {
            // Override the setup of the mandatory validation
            // so we can send the _current_value id suffix.
            // We also enable real time so the page load
            // check disables the ok button if necessary, as the user
            // adds or removes items from the multi value list the
            // page will be refreshed and therefore re-check the status.

            super.setupMandatoryValidation(context, propertySheet, item,
                    component, true, "_current_value");
        } else {
            // setup the client validation rule with real time validation enabled
            // so that the initial page load checks the state of the date
            super.setupMandatoryValidation(context, propertySheet, item,
                    component, true, idSuffix);
        }

    }

    /**
     * Retrieves the default converter for the date component
     *
     * @param context FacesContext
     * @return XMLDateConverter
     */
    protected Converter getDefaultConverter(FacesContext context) {
        XMLDateConverter converter = (XMLDateConverter) context.getApplication().
                createConverter(RepoConstants.ALFRESCO_FACES_XMLDATE_CONVERTER);
        converter.setType("date");
        converter.setPattern(Application.getMessage(context, MSG_DATE));
        return converter;
    }

}
