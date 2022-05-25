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
package eu.cec.digit.circabc.web.ui.repo.component;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.component.UISelectMany;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

public class UICircabcSelectMany extends UISelectMany {

    public static final String COMPONENT_TYPE = "eu.cec.digit.circabc.faces.CircabcSelectMany";
    public static final String COMPONENT_FAMILY = "javax.faces.SelectMany";
    private final static Log logger = LogFactory.getLog(UICircabcSelectMany.class);

    @Override
    protected Object getConvertedValue(FacesContext context,
                                       Object submittedValue) {
        return submittedValue;
    }

    @Override
    public Object getValue() {
        if (isLocalValueSet()) {
            return super.getLocalValue();
        }
        {
            ValueBinding vb = getValueBinding("value");
            if (vb != null) {
                Object value = vb.getValue(getFacesContext());
                if (value instanceof String) {
                    return ((String) value).split(",");
                } else {
                    return value;
                }
            } else {
                return null;
            }

        }

    }

    @Override
    public void updateModel(FacesContext context) {
        if (!isValid()) {
            return;
        }
        if (!isLocalValueSet()) {
            return;
        }
        ValueBinding vb = getValueBinding("value");
        if (vb == null) {
            return;
        }
        try {
            Object localValue = getLocalValue();
            if (localValue instanceof String[]) {

                String[] items = (String[]) localValue;
                String valueAsString = StringUtils.join(items, ',');
                vb.setValue(context, valueAsString);
                setValue(null);
                setLocalValueSet(false);
            }


        } catch (RuntimeException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Runtime exception when update model", e);
            }

            throw e;
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Runtime exception when update model", e);
            }
        }
    }

}
