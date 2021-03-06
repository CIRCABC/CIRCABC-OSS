//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.01.17 at 12:50:48 PM GMT 
//


package eu.cec.digit.circabc.migration.entities.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element ref="{https://circabc.europa.eu/Import/ImportSchema/1.0}statistic"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "statistics"
})
@XmlRootElement(name = "statistics")
public class Statistics
    extends XMLElement
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "statistic")
    protected List<Statistic> statistics;

    /**
     * Default no-arg constructor
     * 
     */
    public Statistics() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public Statistics(final List<Statistic> statistics) {
        super();
        this.statistics = statistics;
    }

    /**
     * Gets the value of the statistics property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the statistics property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStatistics().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Statistic }
     * 
     * 
     */
    public List<Statistic> getStatistics() {
        if (statistics == null) {
            statistics = new ArrayList<Statistic>();
        }
        return this.statistics;
    }

    public Statistics withStatistics(Statistic... values) {
        if (values!= null) {
            for (Statistic value: values) {
                getStatistics().add(value);
            }
        }
        return this;
    }

    public Statistics withStatistics(Collection<Statistic> values) {
        if (values!= null) {
            getStatistics().addAll(values);
        }
        return this;
    }

}
