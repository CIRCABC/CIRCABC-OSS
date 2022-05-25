//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.01.17 at 12:50:48 PM GMT 
//


package eu.cec.digit.circabc.migration.entities.generated.permissions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.cec.digit.circabc.migration.entities.XMLElement;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="permission" type="{https://circabc.europa.eu/Import/PermissionsSchema/1.0}informationPermissionItem"/>
 *       &lt;/sequence>
 *       &lt;attribute name="inherit" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "permissions"
})
@XmlRootElement(name = "informationUserRights")
public class InformationUserRights
    extends XMLElement
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "permission")
    protected List<InformationPermissionItem> permissions;
    @XmlAttribute
    protected Boolean inherit;

    /**
     * Default no-arg constructor
     * 
     */
    public InformationUserRights() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public InformationUserRights(final List<InformationPermissionItem> permissions, final Boolean inherit) {
        super();
        this.permissions = permissions;
        this.inherit = inherit;
    }

    /**
     * Gets the value of the permissions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the permissions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPermissions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link InformationPermissionItem }
     * 
     * 
     */
    public List<InformationPermissionItem> getPermissions() {
        if (permissions == null) {
            permissions = new ArrayList<InformationPermissionItem>();
        }
        return this.permissions;
    }

    /**
     * Gets the value of the inherit property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isInherit() {
        if (inherit == null) {
            return true;
        } else {
            return inherit;
        }
    }

    /**
     * Sets the value of the inherit property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInherit(Boolean value) {
        this.inherit = value;
    }

    public InformationUserRights withPermissions(InformationPermissionItem... values) {
        if (values!= null) {
            for (InformationPermissionItem value: values) {
                getPermissions().add(value);
            }
        }
        return this;
    }

    public InformationUserRights withPermissions(Collection<InformationPermissionItem> values) {
        if (values!= null) {
            getPermissions().addAll(values);
        }
        return this;
    }

    public InformationUserRights withInherit(Boolean value) {
        setInherit(value);
        return this;
    }

}