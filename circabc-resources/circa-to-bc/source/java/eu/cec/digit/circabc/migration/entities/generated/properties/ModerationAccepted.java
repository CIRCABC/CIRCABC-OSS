//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.01.17 at 12:50:48 PM GMT 
//


package eu.cec.digit.circabc.migration.entities.generated.properties;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.cec.digit.circabc.migration.entities.XMLElement;
import eu.cec.digit.circabc.migration.entities.adapter.DateAdapter;


/**
 * <p>Java class for moderationAccepted complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="moderationAccepted">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="moderator" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="date" type="{http://www.w3.org/2001/XMLSchema}date"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "moderationAccepted", propOrder = {
    "moderator",
    "date"
})
public class ModerationAccepted
    extends XMLElement
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(required = true)
    protected String moderator;
    @XmlElement(required = true, type = String.class)
    @XmlJavaTypeAdapter(DateAdapter.class)
    @XmlSchemaType(name = "date")
    protected Date date;

    /**
     * Default no-arg constructor
     * 
     */
    public ModerationAccepted() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public ModerationAccepted(final String moderator, final Date date) {
        super();
        this.moderator = moderator;
        this.date = date;
    }

    /**
     * Gets the value of the moderator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getModerator() {
        return moderator;
    }

    /**
     * Sets the value of the moderator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModerator(String value) {
        this.moderator = value;
    }

    /**
     * Gets the value of the date property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getDate() {
        return date;
    }

    /**
     * Sets the value of the date property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDate(Date value) {
        this.date = value;
    }

    public ModerationAccepted withModerator(String value) {
        setModerator(value);
        return this;
    }

    public ModerationAccepted withDate(Date value) {
        setDate(value);
        return this;
    }

}