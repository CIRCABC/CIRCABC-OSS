package eu.cec.digit.circabc.web.ui.repo.converter;

import org.owasp.esapi.ESAPI;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

public class EncodeHtmlTextFieldConverter implements Converter {

    /**
     * <p>The standard converter id for this converter.</p>
     */
    public static final String CONVERTER_ID = "eu.cec.digit.circabc.faces.EncodeHtmlTextFieldConverter";

    public Object getAsObject(FacesContext context, UIComponent component, String value)
            throws ConverterException {
        return value;
    }

    @SuppressWarnings("unchecked")
    public String getAsString(FacesContext context, UIComponent component, Object value)
            throws ConverterException {
        if (value instanceof String) {
            return ESAPI.encoder().encodeForHTML((String) value);
        } else {
            return "";
        }
    }

}
