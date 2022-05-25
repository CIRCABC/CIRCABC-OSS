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
package eu.cec.digit.circabc.business.impl.props;

import eu.cec.digit.circabc.business.api.props.PropertyItem;
import org.alfresco.web.config.PropertySheetConfigElement.ItemConfig;
import org.springframework.extensions.surf.util.I18NUtil;

import java.io.Serializable;

/**
 * Represent an property item implementation
 *
 * @author Yanick Pignot
 */
public class ConfigurableProperty implements Serializable, PropertyItem {

    /**
     *
     */
    private static final long serialVersionUID = 6493260574966173525L;

    private final String name;
    private final Serializable originalValue;
    private String label;
    private boolean readOnly;
    private boolean viewInViewMode;
    private boolean viewInEditMode;
    private boolean ignoreIfMissing;
    private String readConverter;
    private String writeConverter;

    private Serializable updatedValue;

    /**
     * Construct a property with the mandatory values.
     */
    public ConfigurableProperty(final String name, final Serializable originalValue) {
        this(name, originalValue, name, true, false, false, true, null, null);
    }

    /**
     * Construct a property with the mandatory values.
     */
    /*package*/ ConfigurableProperty(final String name, final Serializable originalValue,
                                     final ItemConfig item) {
        this(name, originalValue, item.getDisplayLabel(), item.isReadOnly(), item.isShownInViewMode(),
                item.isShownInEditMode(), item.getIgnoreIfMissing(), item.getConverter(),
                item.getComponentGenerator());

        // use the I18N label message id if provided.
        final String labelId = item.getDisplayLabelId();
        if (labelId != null && labelId.length() > 0) {
            this.label = I18NUtil.getMessage(labelId);
        }

    }

    /**
     * Construct a property with all available values
     */
    public ConfigurableProperty(final String name, final Serializable originalValue, String label,
                                boolean readOnly, boolean viewInViewMode, boolean viewInEditMode, boolean ignoreIfMissing,
                                String readConverter, String writeConverter) {
        super();
        this.name = name;
        this.originalValue = originalValue;
        this.label = label == null ? name : label;
        this.readOnly = readOnly;
        this.viewInViewMode = viewInViewMode;
        this.viewInEditMode = viewInEditMode;
        this.ignoreIfMissing = ignoreIfMissing;
        this.readConverter = readConverter;
        this.writeConverter = writeConverter;
    }


    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.props.PropertyItem#getName()
     */
    public final String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.props.PropertyItem#getValue()
     */
    public final Serializable getValue() {
        return updatedValue == null ? originalValue : updatedValue;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.props.PropertyItem#getOriginalValue()
     */
    public final Serializable getOriginalValue() {
        return originalValue;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.props.PropertyItem#getUpdatedValue()
     */
    public final Serializable getUpdatedValue() {
        return updatedValue;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.props.PropertyItem#setUpdatedValue(java.io.Serializable)
     */
    public final void setUpdatedValue(final Serializable newValue) {
        updatedValue = newValue;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.props.PropertyItem#isValueUpdated()
     */
    public final boolean isValueUpdated() {
        return updatedValue != null;
    }


    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.props.PropertyItem#getLabel()
     */
    public final String getLabel() {
        return label;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.props.PropertyItem#isReadOnly()
     */
    public final boolean isReadOnly() {
        return readOnly;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.props.PropertyItem#isIgnoreIfMissing()
     */
    public final boolean isIgnoreIfMissing() {
        return ignoreIfMissing;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.props.PropertyItem#isViewInEditMode()
     */
    public final boolean isViewInEditMode() {
        return viewInEditMode;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.props.PropertyItem#isViewInViewMode()
     */
    public final boolean isViewInViewMode() {
        return viewInViewMode;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.props.PropertyItem#getReadConverter()
     */
    public final String getReadConverter() {
        return readConverter;
    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.business.api.props.PropertyItem#getWriteConverter()
     */
    public final String getWriteConverter() {
        return writeConverter;
    }
}
