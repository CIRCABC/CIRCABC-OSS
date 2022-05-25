//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.01.17 at 12:50:48 PM GMT 
//


package eu.cec.digit.circabc.migration.entities.generated.nodes;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.alfresco.service.cmr.repository.NodeRef;

import eu.cec.digit.circabc.migration.entities.TypedProperty.AuthorProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.CreatedProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.CreatorProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.DescriptionProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.DynamicProperty1;
import eu.cec.digit.circabc.migration.entities.TypedProperty.DynamicProperty2;
import eu.cec.digit.circabc.migration.entities.TypedProperty.DynamicProperty3;
import eu.cec.digit.circabc.migration.entities.TypedProperty.DynamicProperty4;
import eu.cec.digit.circabc.migration.entities.TypedProperty.DynamicProperty5;
import eu.cec.digit.circabc.migration.entities.TypedProperty.ExpirationDateProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.IssueDateProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.ModifiedProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.ModifierProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.NameProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.OwnerProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.ReferenceProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.SecurityRankingProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.StatusProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.TitleProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.VersionLabelProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.VersionNoteProperty;
import eu.cec.digit.circabc.migration.entities.adapter.AuthorPropertyAdapter;
import eu.cec.digit.circabc.migration.entities.adapter.DynamicProperty1Adapter;
import eu.cec.digit.circabc.migration.entities.adapter.DynamicProperty2Adapter;
import eu.cec.digit.circabc.migration.entities.adapter.DynamicProperty3Adapter;
import eu.cec.digit.circabc.migration.entities.adapter.DynamicProperty4Adapter;
import eu.cec.digit.circabc.migration.entities.adapter.DynamicProperty5Adapter;
import eu.cec.digit.circabc.migration.entities.adapter.ExpirationDatePropertyAdapter;
import eu.cec.digit.circabc.migration.entities.adapter.IssueDatePropertyAdapter;
import eu.cec.digit.circabc.migration.entities.adapter.ReferencePropertyAdapter;
import eu.cec.digit.circabc.migration.entities.adapter.SecurityRankingPropertyAdapter;
import eu.cec.digit.circabc.migration.entities.adapter.StatusPropertyAdapter;
import eu.cec.digit.circabc.migration.entities.adapter.VersionNotePropertyAdapter;
import eu.cec.digit.circabc.migration.entities.generated.properties.ExtendedProperty;
import eu.cec.digit.circabc.migration.entities.generated.properties.I18NProperty;
import eu.cec.digit.circabc.migration.entities.generated.properties.KeywordReferences;


/**
 * <p>Java class for libraryContentVersion complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="libraryContentVersion">
 *   &lt;complexContent>
 *     &lt;extension base="{https://circabc.europa.eu/Import/NodesSchema/1.0}contentNode">
 *       &lt;sequence>
 *         &lt;group ref="{https://circabc.europa.eu/Import/PropertiesSchema/1.0}bProperties" minOccurs="0"/>
 *         &lt;group ref="{https://circabc.europa.eu/Import/PropertiesSchema/1.0}cProperties" minOccurs="0"/>
 *         &lt;element ref="{https://circabc.europa.eu/Import/PropertiesSchema/1.0}versionNote" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "libraryContentVersion", propOrder = {
    "securityRanking",
    "expirationDate",
    "author",
    "status",
    "issueDate",
    "reference",
    "keywords",
    "dynamicProperty1",
    "dynamicProperty2",
    "dynamicProperty3",
    "dynamicProperty4",
    "dynamicProperty5",
    "versionNote"
})
public class LibraryContentVersion
    extends ContentNode
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", type = String.class, defaultValue = "PUBLIC")
    @XmlJavaTypeAdapter(SecurityRankingPropertyAdapter.class)
    protected SecurityRankingProperty securityRanking;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", type = String.class)
    @XmlJavaTypeAdapter(ExpirationDatePropertyAdapter.class)
    @XmlSchemaType(name = "date")
    protected ExpirationDateProperty expirationDate;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", type = String.class)
    @XmlJavaTypeAdapter(AuthorPropertyAdapter.class)
    protected AuthorProperty author;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", type = String.class, defaultValue = "DRAFT")
    @XmlJavaTypeAdapter(StatusPropertyAdapter.class)
    protected StatusProperty status;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", type = String.class)
    @XmlJavaTypeAdapter(IssueDatePropertyAdapter.class)
    @XmlSchemaType(name = "date")
    protected IssueDateProperty issueDate;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", type = String.class)
    @XmlJavaTypeAdapter(ReferencePropertyAdapter.class)
    protected ReferenceProperty reference;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0")
    protected KeywordReferences keywords;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", type = String.class)
    @XmlJavaTypeAdapter(DynamicProperty1Adapter.class)
    protected DynamicProperty1 dynamicProperty1;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", type = String.class)
    @XmlJavaTypeAdapter(DynamicProperty2Adapter.class)
    protected DynamicProperty2 dynamicProperty2;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", type = String.class)
    @XmlJavaTypeAdapter(DynamicProperty3Adapter.class)
    protected DynamicProperty3 dynamicProperty3;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", type = String.class)
    @XmlJavaTypeAdapter(DynamicProperty4Adapter.class)
    protected DynamicProperty4 dynamicProperty4;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", type = String.class)
    @XmlJavaTypeAdapter(DynamicProperty5Adapter.class)
    protected DynamicProperty5 dynamicProperty5;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0", type = String.class)
    @XmlJavaTypeAdapter(VersionNotePropertyAdapter.class)
    protected VersionNoteProperty versionNote;

    /**
     * Default no-arg constructor
     * 
     */
    public LibraryContentVersion() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public LibraryContentVersion(final NodeRef nodeReference, final List<ExtendedProperty> extendedProperties, final CreatedProperty created, final CreatorProperty creator, final ModifiedProperty modified, final ModifierProperty modifier, final List<I18NProperty> i18NTitles, final TitleProperty title, final List<I18NProperty> i18NDescriptions, final DescriptionProperty description, final OwnerProperty owner, final NameProperty name, final String contentString, final VersionLabelProperty versionLabel, final String uri, final SecurityRankingProperty securityRanking, final ExpirationDateProperty expirationDate, final AuthorProperty author, final StatusProperty status, final IssueDateProperty issueDate, final ReferenceProperty reference, final KeywordReferences keywords, final DynamicProperty1 dynamicProperty1, final DynamicProperty2 dynamicProperty2, final DynamicProperty3 dynamicProperty3, final DynamicProperty4 dynamicProperty4, final DynamicProperty5 dynamicProperty5, final VersionNoteProperty versionNote) {
        super(nodeReference, extendedProperties, created, creator, modified, modifier, i18NTitles, title, i18NDescriptions, description, owner, name, contentString, versionLabel, uri);
        this.securityRanking = securityRanking;
        this.expirationDate = expirationDate;
        this.author = author;
        this.status = status;
        this.issueDate = issueDate;
        this.reference = reference;
        this.keywords = keywords;
        this.dynamicProperty1 = dynamicProperty1;
        this.dynamicProperty2 = dynamicProperty2;
        this.dynamicProperty3 = dynamicProperty3;
        this.dynamicProperty4 = dynamicProperty4;
        this.dynamicProperty5 = dynamicProperty5;
        this.versionNote = versionNote;
    }

    /**
     * Gets the value of the securityRanking property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public SecurityRankingProperty getSecurityRanking() {
        return securityRanking;
    }

    /**
     * Sets the value of the securityRanking property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSecurityRanking(SecurityRankingProperty value) {
        this.securityRanking = value;
    }

    /**
     * Gets the value of the expirationDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public ExpirationDateProperty getExpirationDate() {
        return expirationDate;
    }

    /**
     * Sets the value of the expirationDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExpirationDate(ExpirationDateProperty value) {
        this.expirationDate = value;
    }

    /**
     * Gets the value of the author property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public AuthorProperty getAuthor() {
        return author;
    }

    /**
     * Sets the value of the author property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthor(AuthorProperty value) {
        this.author = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StatusProperty getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatus(StatusProperty value) {
        this.status = value;
    }

    /**
     * Gets the value of the issueDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public IssueDateProperty getIssueDate() {
        return issueDate;
    }

    /**
     * Sets the value of the issueDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIssueDate(IssueDateProperty value) {
        this.issueDate = value;
    }

    /**
     * Gets the value of the reference property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public ReferenceProperty getReference() {
        return reference;
    }

    /**
     * Sets the value of the reference property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReference(ReferenceProperty value) {
        this.reference = value;
    }

    /**
     * Gets the value of the keywords property.
     * 
     * @return
     *     possible object is
     *     {@link KeywordReferences }
     *     
     */
    public KeywordReferences getKeywords() {
        return keywords;
    }

    /**
     * Sets the value of the keywords property.
     * 
     * @param value
     *     allowed object is
     *     {@link KeywordReferences }
     *     
     */
    public void setKeywords(KeywordReferences value) {
        this.keywords = value;
    }

    /**
     * Gets the value of the dynamicProperty1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public DynamicProperty1 getDynamicProperty1() {
        return dynamicProperty1;
    }

    /**
     * Sets the value of the dynamicProperty1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDynamicProperty1(DynamicProperty1 value) {
        this.dynamicProperty1 = value;
    }

    /**
     * Gets the value of the dynamicProperty2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public DynamicProperty2 getDynamicProperty2() {
        return dynamicProperty2;
    }

    /**
     * Sets the value of the dynamicProperty2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDynamicProperty2(DynamicProperty2 value) {
        this.dynamicProperty2 = value;
    }

    /**
     * Gets the value of the dynamicProperty3 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public DynamicProperty3 getDynamicProperty3() {
        return dynamicProperty3;
    }

    /**
     * Sets the value of the dynamicProperty3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDynamicProperty3(DynamicProperty3 value) {
        this.dynamicProperty3 = value;
    }

    /**
     * Gets the value of the dynamicProperty4 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public DynamicProperty4 getDynamicProperty4() {
        return dynamicProperty4;
    }

    /**
     * Sets the value of the dynamicProperty4 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDynamicProperty4(DynamicProperty4 value) {
        this.dynamicProperty4 = value;
    }

    /**
     * Gets the value of the dynamicProperty5 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public DynamicProperty5 getDynamicProperty5() {
        return dynamicProperty5;
    }

    /**
     * Sets the value of the dynamicProperty5 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDynamicProperty5(DynamicProperty5 value) {
        this.dynamicProperty5 = value;
    }

    /**
     * Gets the value of the versionNote property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public VersionNoteProperty getVersionNote() {
        return versionNote;
    }

    /**
     * Sets the value of the versionNote property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersionNote(VersionNoteProperty value) {
        this.versionNote = value;
    }

    public LibraryContentVersion withSecurityRanking(SecurityRankingProperty value) {
        setSecurityRanking(value);
        return this;
    }

    public LibraryContentVersion withExpirationDate(ExpirationDateProperty value) {
        setExpirationDate(value);
        return this;
    }

    public LibraryContentVersion withAuthor(AuthorProperty value) {
        setAuthor(value);
        return this;
    }

    public LibraryContentVersion withStatus(StatusProperty value) {
        setStatus(value);
        return this;
    }

    public LibraryContentVersion withIssueDate(IssueDateProperty value) {
        setIssueDate(value);
        return this;
    }

    public LibraryContentVersion withReference(ReferenceProperty value) {
        setReference(value);
        return this;
    }

    public LibraryContentVersion withKeywords(KeywordReferences value) {
        setKeywords(value);
        return this;
    }

    public LibraryContentVersion withDynamicProperty1(DynamicProperty1 value) {
        setDynamicProperty1(value);
        return this;
    }

    public LibraryContentVersion withDynamicProperty2(DynamicProperty2 value) {
        setDynamicProperty2(value);
        return this;
    }

    public LibraryContentVersion withDynamicProperty3(DynamicProperty3 value) {
        setDynamicProperty3(value);
        return this;
    }

    public LibraryContentVersion withDynamicProperty4(DynamicProperty4 value) {
        setDynamicProperty4(value);
        return this;
    }

    public LibraryContentVersion withDynamicProperty5(DynamicProperty5 value) {
        setDynamicProperty5(value);
        return this;
    }

    public LibraryContentVersion withVersionNote(VersionNoteProperty value) {
        setVersionNote(value);
        return this;
    }

    @Override
    public LibraryContentVersion withContentString(String value) {
        setContentString(value);
        return this;
    }

    @Override
    public LibraryContentVersion withVersionLabel(VersionLabelProperty value) {
        setVersionLabel(value);
        return this;
    }

    @Override
    public LibraryContentVersion withUri(String value) {
        setUri(value);
        return this;
    }

    @Override
    public LibraryContentVersion withName(NameProperty value) {
        setName(value);
        return this;
    }

    @Override
    public LibraryContentVersion withI18NTitles(I18NProperty... values) {
        if (values!= null) {
            for (I18NProperty value: values) {
                getI18NTitles().add(value);
            }
        }
        return this;
    }

    @Override
    public LibraryContentVersion withI18NTitles(Collection<I18NProperty> values) {
        if (values!= null) {
            getI18NTitles().addAll(values);
        }
        return this;
    }

    @Override
    public LibraryContentVersion withTitle(TitleProperty value) {
        setTitle(value);
        return this;
    }

    @Override
    public LibraryContentVersion withI18NDescriptions(I18NProperty... values) {
        if (values!= null) {
            for (I18NProperty value: values) {
                getI18NDescriptions().add(value);
            }
        }
        return this;
    }

    @Override
    public LibraryContentVersion withI18NDescriptions(Collection<I18NProperty> values) {
        if (values!= null) {
            getI18NDescriptions().addAll(values);
        }
        return this;
    }

    @Override
    public LibraryContentVersion withDescription(DescriptionProperty value) {
        setDescription(value);
        return this;
    }

    @Override
    public LibraryContentVersion withOwner(OwnerProperty value) {
        setOwner(value);
        return this;
    }

    @Override
    public LibraryContentVersion withNodeReference(NodeRef value) {
        setNodeReference(value);
        return this;
    }

    @Override
    public LibraryContentVersion withExtendedProperties(ExtendedProperty... values) {
        if (values!= null) {
            for (ExtendedProperty value: values) {
                getExtendedProperties().add(value);
            }
        }
        return this;
    }

    @Override
    public LibraryContentVersion withExtendedProperties(Collection<ExtendedProperty> values) {
        if (values!= null) {
            getExtendedProperties().addAll(values);
        }
        return this;
    }

    @Override
    public LibraryContentVersion withCreated(CreatedProperty value) {
        setCreated(value);
        return this;
    }

    @Override
    public LibraryContentVersion withCreator(CreatorProperty value) {
        setCreator(value);
        return this;
    }

    @Override
    public LibraryContentVersion withModified(ModifiedProperty value) {
        setModified(value);
        return this;
    }

    @Override
    public LibraryContentVersion withModifier(ModifierProperty value) {
        setModifier(value);
        return this;
    }

}