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
package eu.cec.digit.circabc.business.api.props;

import java.io.Serializable;


/**
 * Wrap a node property with the property itself (name and value), the property configuration
 * (readonly, ...) and allow edition.
 *
 * @author Yanick Pignot
 */
public interface PropertyItem {

    /**
     * The unique identifier of a property
     *
     * @return the name
     */
    String getName();

    /**
     * The current value of the property. It means that the method returns the updated value if client
     * changet it.
     *
     * @return the value
     */
    Serializable getValue();

    /**
     * The original value of the property (can be null)
     *
     * @return the value
     */
    Serializable getOriginalValue();

    /**
     * The updated value of the property (can be null)
     *
     * @return the value
     */
    Serializable getUpdatedValue();

    /**
     * Update the value of the property (null allowed).
     *
     * <b>It is the responsability of the services to check and validate the property value and RW
     * mode.</b>
     *
     * @return the value
     */
    void setUpdatedValue(final Serializable newValue);

    /**
     * Return if the property has been updated
     *
     * @return true if the value is updated
     */
    boolean isValueUpdated();

    /**
     * The dispaly label of the property
     *
     * @return the label
     */
    String getLabel();

    /**
     * Is the property in read only mode (the property will be displayed in its edition process)
     *
     * @return the readOnly
     */
    boolean isReadOnly();

    /**
     * Is the property must be displayed even if it is not set or not part this kind of node.
     *
     * @return the ignoreIfMissing
     */
    boolean isIgnoreIfMissing();

    /**
     * Is the property must be displayed in editing mode?
     *
     * @return the viewInEditMode
     */
    boolean isViewInEditMode();

    /**
     * Is the property must be displayed in view mode?
     *
     * @return the viewInViewMode
     */
    boolean isViewInViewMode();

    /**
     * The conveter configuration identifier used by the target client to display the property.
     *
     * <b>It's the responsability of the client to know and interpret this value according its
     * needs</b>
     *
     * @return the readConverter
     */
    String getReadConverter();

    /**
     * The conveter configuration identifier used by the target client to edit the property.
     *
     * <b>It's the responsability of the client to know and interpret this value according its
     * needs</b>
     *
     * @return the writeConverter
     */
    String getWriteConverter();

}
