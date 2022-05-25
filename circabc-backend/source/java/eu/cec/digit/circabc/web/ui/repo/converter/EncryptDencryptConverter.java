package eu.cec.digit.circabc.web.ui.repo.converter;

import eu.cec.digit.circabc.model.DocumentModel;
import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.servlet.ServletContext;
import java.io.Serializable;

/**
 * Decrypts or encrypts a property to display it in the UI or store it in the repository.
 *
 * @author schwerr
 */
public class EncryptDencryptConverter implements Converter {

    /**
     * <p>The standard converter id for this converter.</p>
     */
    public static final String CONVERTER_ID = "eu.cec.digit.circabc.faces.EncryptDencryptConverter";
    private static MetadataEncryptor metadataEncryptor = null;

    /**
     * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext,
     * javax.faces.component.UIComponent, java.lang.String)
     */
    @Override
    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) throws ConverterException {

        initMetadataEncryptor();

        // No matter what name you put, only the type is compared which is
        // always d:encrypted for encrypted properties
        Object encryptedPropertyValue = metadataEncryptor.encrypt(
                DocumentModel.PROP_ENCRYPTEDTEXT1, value);

        return encryptedPropertyValue;
    }

    /**
     * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext,
     * javax.faces.component.UIComponent, java.lang.Object)
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component,
                              Object value) throws ConverterException {

        initMetadataEncryptor();

        // No matter what name you put, only the type is compared which is
        // always d:encrypted for encrypted properties
        String decryptedPropertyValue = (String) metadataEncryptor.decrypt(
                DocumentModel.PROP_ENCRYPTEDTEXT1, (Serializable) value);

        return decryptedPropertyValue;
    }

    private void initMetadataEncryptor() {

        if (metadataEncryptor == null) {
            WebApplicationContext context = WebApplicationContextUtils.
                    getWebApplicationContext((ServletContext)
                            FacesContext.getCurrentInstance().
                                    getExternalContext().getContext());

            metadataEncryptor = (MetadataEncryptor)
                    context.getBean("metadataEncryptor");
        }
    }
}
