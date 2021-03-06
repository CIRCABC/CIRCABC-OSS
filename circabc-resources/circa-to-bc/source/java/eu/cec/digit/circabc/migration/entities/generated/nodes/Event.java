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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.cec.digit.circabc.migration.entities.generated.properties.AudienceClosed;
import eu.cec.digit.circabc.migration.entities.generated.properties.AudienceOpen;
import eu.cec.digit.circabc.migration.entities.generated.properties.ContactInformation;
import eu.cec.digit.circabc.migration.entities.generated.properties.EventPriority;
import eu.cec.digit.circabc.migration.entities.generated.properties.EventType;
import eu.cec.digit.circabc.migration.entities.generated.properties.EveryTimesOccurence;
import eu.cec.digit.circabc.migration.entities.generated.properties.SingleDate;
import eu.cec.digit.circabc.migration.entities.generated.properties.TimeZone;
import eu.cec.digit.circabc.migration.entities.generated.properties.TimesOccurence;


/**
 * <p>Java class for event complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="event">
 *   &lt;complexContent>
 *     &lt;extension base="{https://circabc.europa.eu/Import/NodesSchema/1.0}appointment">
 *       &lt;sequence>
 *         &lt;group ref="{https://circabc.europa.eu/Import/PropertiesSchema/1.0}eventProperties"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "event", propOrder = {
    "type",
    "priority"
})
@XmlRootElement(name = "event")
public class Event
    extends Appointment
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", defaultValue = "Task")
    protected EventType type;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", defaultValue = "Low")
    protected EventPriority priority;

    /**
     * Default no-arg constructor
     * 
     */
    public Event() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public Event(final String appointmentTitle, final Locale language, final String _abstract, final Date date, final Date startDate, final Date startTime, final Date endTime, final EveryTimesOccurence everyTimesOccurence, final TimesOccurence timesOccurence, final SingleDate singleDate, final TimeZone timeZoneId, final String location, final AudienceClosed audienceClosed, final AudienceOpen audienceOpen, final ContactInformation contact, final EventType type, final EventPriority priority) {
        super(appointmentTitle, language, _abstract, date, startDate, startTime, endTime, everyTimesOccurence, timesOccurence, singleDate, timeZoneId, location, audienceClosed, audienceOpen, contact);
        this.type = type;
        this.priority = priority;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link EventType }
     *     
     */
    public EventType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link EventType }
     *     
     */
    public void setType(EventType value) {
        this.type = value;
    }

    /**
     * Gets the value of the priority property.
     * 
     * @return
     *     possible object is
     *     {@link EventPriority }
     *     
     */
    public EventPriority getPriority() {
        return priority;
    }

    /**
     * Sets the value of the priority property.
     * 
     * @param value
     *     allowed object is
     *     {@link EventPriority }
     *     
     */
    public void setPriority(EventPriority value) {
        this.priority = value;
    }

    public Event withType(EventType value) {
        setType(value);
        return this;
    }

    public Event withPriority(EventPriority value) {
        setPriority(value);
        return this;
    }

    @Override
    public Event withAppointmentTitle(String value) {
        setAppointmentTitle(value);
        return this;
    }

    @Override
    public Event withLanguage(Locale value) {
        setLanguage(value);
        return this;
    }

    @Override
    public Event withAbstract(String value) {
        setAbstract(value);
        return this;
    }

    @Override
    public Event withDate(Date value) {
        setDate(value);
        return this;
    }

    @Override
    public Event withStartDate(Date value) {
        setStartDate(value);
        return this;
    }

    @Override
    public Event withStartTime(Date value) {
        setStartTime(value);
        return this;
    }

    @Override
    public Event withEndTime(Date value) {
        setEndTime(value);
        return this;
    }

    @Override
    public Event withEveryTimesOccurence(EveryTimesOccurence value) {
        setEveryTimesOccurence(value);
        return this;
    }

    @Override
    public Event withTimesOccurence(TimesOccurence value) {
        setTimesOccurence(value);
        return this;
    }

    @Override
    public Event withSingleDate(SingleDate value) {
        setSingleDate(value);
        return this;
    }

    @Override
    public Event withTimeZoneId(TimeZone value) {
        setTimeZoneId(value);
        return this;
    }

    @Override
    public Event withLocation(String value) {
        setLocation(value);
        return this;
    }

    @Override
    public Event withAudienceClosed(AudienceClosed value) {
        setAudienceClosed(value);
        return this;
    }

    @Override
    public Event withAudienceOpen(AudienceOpen value) {
        setAudienceOpen(value);
        return this;
    }

    @Override
    public Event withContact(ContactInformation value) {
        setContact(value);
        return this;
    }

}
