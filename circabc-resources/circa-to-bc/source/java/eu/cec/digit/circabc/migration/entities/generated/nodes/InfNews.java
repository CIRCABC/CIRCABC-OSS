package eu.cec.digit.circabc.migration.entities.generated.nodes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.alfresco.service.cmr.repository.NodeRef;

import eu.cec.digit.circabc.migration.entities.TypedProperty.CreatedProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.CreatorProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.DescriptionProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.ModifiedProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.ModifierProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.NameProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.OwnerProperty;
import eu.cec.digit.circabc.migration.entities.TypedProperty.TitleProperty;
import eu.cec.digit.circabc.migration.entities.generated.permissions.Notifications;
import eu.cec.digit.circabc.migration.entities.generated.properties.ExtendedProperty;
import eu.cec.digit.circabc.migration.entities.generated.properties.I18NProperty;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "informationNews", propOrder = {
    "notifications",
    "newsContent",
    "newsPattern",
    "newsLayout",
    "newsSize",
    "newsDate",
    "newsUrl",
    "infContents"
})
@XmlRootElement(name = "infNews")
public class InfNews
    extends NamedNode
    implements Serializable
{
    private final static long serialVersionUID = 1L;

    @XmlElement(namespace = "https://circabc.europa.eu/Import/PermissionsSchema/1.0")
    protected Notifications notifications;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0")
    protected String newsContent;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0")
    protected String newsPattern;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0")
    protected String newsLayout;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0")
    protected Integer newsSize;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0")
    protected Date newsDate;
    @XmlElement(namespace = "https://circabc.europa.eu/Import/PropertiesSchema/1.0")
    protected String newsUrl;
    @XmlElement(name = "infContent")
    protected List<InfContent> infContents;

    public InfNews() {
        super();
    }

    public Notifications getNotifications() { return notifications; }
    public void setNotifications(Notifications value) { this.notifications = value; }

    public String getNewsContent() { return newsContent; }
    public void setNewsContent(String value) { this.newsContent = value; }

    public String getNewsPattern() { return newsPattern; }
    public void setNewsPattern(String value) { this.newsPattern = value; }

    public String getNewsLayout() { return newsLayout; }
    public void setNewsLayout(String value) { this.newsLayout = value; }

    public Integer getNewsSize() { return newsSize; }
    public void setNewsSize(Integer value) { this.newsSize = value; }

    public Date getNewsDate() { return newsDate; }
    public void setNewsDate(Date value) { this.newsDate = value; }

    public String getNewsUrl() { return newsUrl; }
    public void setNewsUrl(String value) { this.newsUrl = value; }

    public List<InfContent> getInfContents() {
        if (infContents == null) {
            infContents = new ArrayList<InfContent>();
        }
        return this.infContents;
    }

    public InfNews withNotifications(Notifications value) { setNotifications(value); return this; }
    public InfNews withNewsContent(String value) { setNewsContent(value); return this; }
    public InfNews withNewsPattern(String value) { setNewsPattern(value); return this; }
    public InfNews withNewsLayout(String value) { setNewsLayout(value); return this; }
    public InfNews withNewsSize(Integer value) { setNewsSize(value); return this; }
    public InfNews withNewsDate(Date value) { setNewsDate(value); return this; }
    public InfNews withNewsUrl(String value) { setNewsUrl(value); return this; }

    public InfNews withInfContents(InfContent... values) {
        if (values != null) {
            for (InfContent value : values) {
                getInfContents().add(value);
            }
        }
        return this;
    }

    public InfNews withInfContents(Collection<InfContent> values) {
        if (values != null) {
            getInfContents().addAll(values);
        }
        return this;
    }

    @Override
    public InfNews withName(NameProperty value) { setName(value); return this; }

    @Override
    public InfNews withI18NTitles(I18NProperty... values) {
        if (values != null) { for (I18NProperty v : values) { getI18NTitles().add(v); } }
        return this;
    }

    @Override
    public InfNews withI18NTitles(Collection<I18NProperty> values) {
        if (values != null) { getI18NTitles().addAll(values); }
        return this;
    }

    @Override
    public InfNews withTitle(TitleProperty value) { setTitle(value); return this; }

    @Override
    public InfNews withI18NDescriptions(I18NProperty... values) {
        if (values != null) { for (I18NProperty v : values) { getI18NDescriptions().add(v); } }
        return this;
    }

    @Override
    public InfNews withI18NDescriptions(Collection<I18NProperty> values) {
        if (values != null) { getI18NDescriptions().addAll(values); }
        return this;
    }

    @Override
    public InfNews withDescription(DescriptionProperty value) { setDescription(value); return this; }
    @Override
    public InfNews withOwner(OwnerProperty value) { setOwner(value); return this; }
    @Override
    public InfNews withNodeReference(NodeRef value) { setNodeReference(value); return this; }

    @Override
    public InfNews withExtendedProperties(ExtendedProperty... values) {
        if (values != null) { for (ExtendedProperty v : values) { getExtendedProperties().add(v); } }
        return this;
    }

    @Override
    public InfNews withExtendedProperties(Collection<ExtendedProperty> values) {
        if (values != null) { getExtendedProperties().addAll(values); }
        return this;
    }

    @Override
    public InfNews withCreated(CreatedProperty value) { setCreated(value); return this; }
    @Override
    public InfNews withCreator(CreatorProperty value) { setCreator(value); return this; }
    @Override
    public InfNews withModified(ModifiedProperty value) { setModified(value); return this; }
    @Override
    public InfNews withModifier(ModifierProperty value) { setModifier(value); return this; }
}
