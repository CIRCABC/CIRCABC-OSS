//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.01.17 at 12:50:48 PM GMT 
//


package eu.cec.digit.circabc.migration.entities.generated.permissions;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.cec.digit.circabc.migration.entities.TypedProperty.DescriptionProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.OwnerProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.TitleProperty;
import eu.cec.digit.circabc.migration.entities.generated.properties.I18NProperty;


/**
 * <p>Java class for guest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="guest">
 *   &lt;complexContent>
 *     &lt;extension base="{https://circabc.europa.eu/Import/PermissionsSchema/1.0}globalAccessProfile">
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "guest")
@XmlRootElement(name = "guest")
public class Guest
    extends GlobalAccessProfile
    implements Serializable
{

    private final static long serialVersionUID = 1L;

    /**
     * Default no-arg constructor
     * 
     */
    public Guest() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public Guest(final List<I18NProperty> i18NTitles, final TitleProperty title, final List<I18NProperty> i18NDescriptions, final DescriptionProperty description, final OwnerProperty owner, final SimpleInformationPermissions informationPermission, final SimpleLibraryPermissions libraryPermission, final SimpleDirectoryPermissions directoryPermission, final SimpleEventPermissions eventPermission, final SimpleNewsgroupPermissions newsgroupPermission, final boolean visibility) {
        super(i18NTitles, title, i18NDescriptions, description, owner, informationPermission, libraryPermission, directoryPermission, eventPermission, newsgroupPermission, visibility);
    }

    @Override
    public Guest withI18NTitles(I18NProperty... values) {
        if (values!= null) {
            for (I18NProperty value: values) {
                getI18NTitles().add(value);
            }
        }
        return this;
    }

    @Override
    public Guest withI18NTitles(Collection<I18NProperty> values) {
        if (values!= null) {
            getI18NTitles().addAll(values);
        }
        return this;
    }

    @Override
    public Guest withTitle(TitleProperty value) {
        setTitle(value);
        return this;
    }

    @Override
    public Guest withI18NDescriptions(I18NProperty... values) {
        if (values!= null) {
            for (I18NProperty value: values) {
                getI18NDescriptions().add(value);
            }
        }
        return this;
    }

    @Override
    public Guest withI18NDescriptions(Collection<I18NProperty> values) {
        if (values!= null) {
            getI18NDescriptions().addAll(values);
        }
        return this;
    }

    @Override
    public Guest withDescription(DescriptionProperty value) {
        setDescription(value);
        return this;
    }

    @Override
    public Guest withOwner(OwnerProperty value) {
        setOwner(value);
        return this;
    }

    @Override
    public Guest withInformationPermission(SimpleInformationPermissions value) {
        setInformationPermission(value);
        return this;
    }

    @Override
    public Guest withLibraryPermission(SimpleLibraryPermissions value) {
        setLibraryPermission(value);
        return this;
    }

    @Override
    public Guest withDirectoryPermission(SimpleDirectoryPermissions value) {
        setDirectoryPermission(value);
        return this;
    }

    @Override
    public Guest withEventPermission(SimpleEventPermissions value) {
        setEventPermission(value);
        return this;
    }

    @Override
    public Guest withNewsgroupPermission(SimpleNewsgroupPermissions value) {
        setNewsgroupPermission(value);
        return this;
    }

    @Override
    public Guest withVisibility(boolean value) {
        setVisibility(value);
        return this;
    }

}
