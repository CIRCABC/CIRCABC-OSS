//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.01.17 at 12:50:48 PM GMT 
//


package eu.cec.digit.circabc.migration.entities.generated.nodes;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.cec.digit.circabc.migration.entities.XMLElement;
import eu.cec.digit.circabc.migration.entities.adapter.DateAdapter;
import eu.cec.digit.circabc.migration.entities.adapter.LocaleAdapter;
import eu.cec.digit.circabc.migration.entities.adapter.TimeAdapter;
import eu.cec.digit.circabc.migration.entities.generated.properties.AudienceClosed;
import eu.cec.digit.circabc.migration.entities.generated.properties.AudienceOpen;
import eu.cec.digit.circabc.migration.entities.generated.properties.ContactInformation;
import eu.cec.digit.circabc.migration.entities.generated.properties.EveryTimesOccurence;
import eu.cec.digit.circabc.migration.entities.generated.properties.SingleDate;
import eu.cec.digit.circabc.migration.entities.generated.properties.TimeZone;
import eu.cec.digit.circabc.migration.entities.generated.properties.TimesOccurence;


/**
 * <p>Java class for appointment complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="appointment">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{https://circabc.europa.eu/Import/PropertiesSchema/1.0}appointmentProperties"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "appointment", propOrder = {
    "appointmentTitle",
    "language",
    "_abstract",
    "date",
    "startDate",
    "startTime",
    "endTime",
    "everyTimesOccurence",
    "timesOccurence",
    "singleDate",
    "timeZoneId",
    "location",
    "audienceClosed",
    "audienceOpen",
    "contact"
})
@XmlSeeAlso({
    Event.class,
    Meeting.class
})
public abstract class Appointment
    extends XMLElement
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", required = true)
    protected String appointmentTitle;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", type = String.class, defaultValue = "en")
    @XmlJavaTypeAdapter(LocaleAdapter.class)
    protected Locale language;
    @XmlElement(name = "abstract", namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0")
    protected String _abstract;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", type = String.class)
    @XmlJavaTypeAdapter(DateAdapter.class)
    @XmlSchemaType(name = "date")
    protected Date date;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", required = true, type = String.class)
    @XmlJavaTypeAdapter(DateAdapter.class)
    @XmlSchemaType(name = "date")
    protected Date startDate;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", required = true, type = String.class)
    @XmlJavaTypeAdapter(TimeAdapter.class)
    @XmlSchemaType(name = "time")
    protected Date startTime;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", required = true, type = String.class)
    @XmlJavaTypeAdapter(TimeAdapter.class)
    @XmlSchemaType(name = "time")
    protected Date endTime;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0")
    protected EveryTimesOccurence everyTimesOccurence;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0")
    protected TimesOccurence timesOccurence;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0")
    protected SingleDate singleDate;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", defaultValue = "GMT+1")
    protected TimeZone timeZoneId;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0")
    protected String location;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0")
    protected AudienceClosed audienceClosed;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0")
    protected AudienceOpen audienceOpen;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", required = true)
    protected ContactInformation contact;

    /**
     * Default no-arg constructor
     * 
     */
    public Appointment() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public Appointment(final String appointmentTitle, final Locale language, final String _abstract, final Date date, final Date startDate, final Date startTime, final Date endTime, final EveryTimesOccurence everyTimesOccurence, final TimesOccurence timesOccurence, final SingleDate singleDate, final TimeZone timeZoneId, final String location, final AudienceClosed audienceClosed, final AudienceOpen audienceOpen, final ContactInformation contact) {
        super();
        this.appointmentTitle = appointmentTitle;
        this.language = language;
        this._abstract = _abstract;
        this.date = date;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.everyTimesOccurence = everyTimesOccurence;
        this.timesOccurence = timesOccurence;
        this.singleDate = singleDate;
        this.timeZoneId = timeZoneId;
        this.location = location;
        this.audienceClosed = audienceClosed;
        this.audienceOpen = audienceOpen;
        this.contact = contact;
    }

    /**
     * Gets the value of the appointmentTitle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAppointmentTitle() {
        return appointmentTitle;
    }

    /**
     * Sets the value of the appointmentTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAppointmentTitle(String value) {
        this.appointmentTitle = value;
    }

    /**
     * Gets the value of the language property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Locale getLanguage() {
        return language;
    }

    /**
     * Sets the value of the language property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLanguage(Locale value) {
        this.language = value;
    }

    /**
     * Gets the value of the abstract property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAbstract() {
        return _abstract;
    }

    /**
     * Sets the value of the abstract property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAbstract(String value) {
        this._abstract = value;
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

    /**
     * Gets the value of the startDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Sets the value of the startDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartDate(Date value) {
        this.startDate = value;
    }

    /**
     * Gets the value of the startTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Sets the value of the startTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartTime(Date value) {
        this.startTime = value;
    }

    /**
     * Gets the value of the endTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Sets the value of the endTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndTime(Date value) {
        this.endTime = value;
    }

    /**
     * Gets the value of the everyTimesOccurence property.
     * 
     * @return
     *     possible object is
     *     {@link EveryTimesOccurence }
     *     
     */
    public EveryTimesOccurence getEveryTimesOccurence() {
        return everyTimesOccurence;
    }

    /**
     * Sets the value of the everyTimesOccurence property.
     * 
     * @param value
     *     allowed object is
     *     {@link EveryTimesOccurence }
     *     
     */
    public void setEveryTimesOccurence(EveryTimesOccurence value) {
        this.everyTimesOccurence = value;
    }

    /**
     * Gets the value of the timesOccurence property.
     * 
     * @return
     *     possible object is
     *     {@link TimesOccurence }
     *     
     */
    public TimesOccurence getTimesOccurence() {
        return timesOccurence;
    }

    /**
     * Sets the value of the timesOccurence property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimesOccurence }
     *     
     */
    public void setTimesOccurence(TimesOccurence value) {
        this.timesOccurence = value;
    }

    /**
     * Gets the value of the singleDate property.
     * 
     * @return
     *     possible object is
     *     {@link SingleDate }
     *     
     */
    public SingleDate getSingleDate() {
        return singleDate;
    }

    /**
     * Sets the value of the singleDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link SingleDate }
     *     
     */
    public void setSingleDate(SingleDate value) {
        this.singleDate = value;
    }

    /**
     * Gets the value of the timeZoneId property.
     * 
     * @return
     *     possible object is
     *     {@link TimeZone }
     *     
     */
    public TimeZone getTimeZoneId() {
        return timeZoneId;
    }

    /**
     * Sets the value of the timeZoneId property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimeZone }
     *     
     */
    public void setTimeZoneId(TimeZone value) {
        this.timeZoneId = value;
    }

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocation(String value) {
        this.location = value;
    }

    /**
     * Gets the value of the audienceClosed property.
     * 
     * @return
     *     possible object is
     *     {@link AudienceClosed }
     *     
     */
    public AudienceClosed getAudienceClosed() {
        return audienceClosed;
    }

    /**
     * Sets the value of the audienceClosed property.
     * 
     * @param value
     *     allowed object is
     *     {@link AudienceClosed }
     *     
     */
    public void setAudienceClosed(AudienceClosed value) {
        this.audienceClosed = value;
    }

    /**
     * Gets the value of the audienceOpen property.
     * 
     * @return
     *     possible object is
     *     {@link AudienceOpen }
     *     
     */
    public AudienceOpen getAudienceOpen() {
        return audienceOpen;
    }

    /**
     * Sets the value of the audienceOpen property.
     * 
     * @param value
     *     allowed object is
     *     {@link AudienceOpen }
     *     
     */
    public void setAudienceOpen(AudienceOpen value) {
        this.audienceOpen = value;
    }

    /**
     * Gets the value of the contact property.
     * 
     * @return
     *     possible object is
     *     {@link ContactInformation }
     *     
     */
    public ContactInformation getContact() {
        return contact;
    }

    /**
     * Sets the value of the contact property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContactInformation }
     *     
     */
    public void setContact(ContactInformation value) {
        this.contact = value;
    }

    public Appointment withAppointmentTitle(String value) {
        setAppointmentTitle(value);
        return this;
    }

    public Appointment withLanguage(Locale value) {
        setLanguage(value);
        return this;
    }

    public Appointment withAbstract(String value) {
        setAbstract(value);
        return this;
    }

    public Appointment withDate(Date value) {
        setDate(value);
        return this;
    }

    public Appointment withStartDate(Date value) {
        setStartDate(value);
        return this;
    }

    public Appointment withStartTime(Date value) {
        setStartTime(value);
        return this;
    }

    public Appointment withEndTime(Date value) {
        setEndTime(value);
        return this;
    }

    public Appointment withEveryTimesOccurence(EveryTimesOccurence value) {
        setEveryTimesOccurence(value);
        return this;
    }

    public Appointment withTimesOccurence(TimesOccurence value) {
        setTimesOccurence(value);
        return this;
    }

    public Appointment withSingleDate(SingleDate value) {
        setSingleDate(value);
        return this;
    }

    public Appointment withTimeZoneId(TimeZone value) {
        setTimeZoneId(value);
        return this;
    }

    public Appointment withLocation(String value) {
        setLocation(value);
        return this;
    }

    public Appointment withAudienceClosed(AudienceClosed value) {
        setAudienceClosed(value);
        return this;
    }

    public Appointment withAudienceOpen(AudienceOpen value) {
        setAudienceOpen(value);
        return this;
    }

    public Appointment withContact(ContactInformation value) {
        setContact(value);
        return this;
    }

}
