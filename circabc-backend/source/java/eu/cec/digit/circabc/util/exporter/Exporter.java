package eu.cec.digit.circabc.util.exporter;

import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.view.ExporterContext;
import org.alfresco.service.namespace.QName;

import java.io.InputStream;
import java.util.Locale;

/**
 * @author schwerr
 */
public interface Exporter {

    /**
     * Start of Export
     */
    void start(ExporterContext context);

    /**
     * Start export of namespace
     *
     * @param prefix namespace prefix
     * @param uri    namespace uri
     */
    void startNamespace(String prefix, String uri);

    /**
     * End export of namespace
     *
     * @param prefix namespace prefix
     */
    void endNamespace(String prefix);

    /**
     * Start export of node
     *
     * @param nodeRef the node reference
     */
    void startNode(NodeRef nodeRef);

    /**
     * End export of node
     *
     * @param nodeRef the node reference
     */
    void endNode(NodeRef nodeRef);

    /**
     * Start export of node reference
     *
     * @param nodeRef the node reference
     */
    void startReference(NodeRef nodeRef, QName childName);

    /**
     * End export of node reference
     *
     * @param nodeRef the node reference
     */
    void endReference(NodeRef nodeRef);

    /**
     * Start export of aspects
     */
    void startAspects(NodeRef nodeRef);

    /**
     * Start export of aspect
     *
     * @param nodeRef the node reference
     * @param aspect  the aspect
     */
    void startAspect(NodeRef nodeRef, QName aspect);

    /**
     * End export of aspect
     *
     * @param nodeRef the node reference
     * @param aspect  the aspect
     */
    void endAspect(NodeRef nodeRef, QName aspect);

    /**
     * End export of aspects
     */
    void endAspects(NodeRef nodeRef);

    /**
     * Start export of ACL
     *
     * @param nodeRef for node reference
     */
    void startACL(NodeRef nodeRef);

    /**
     * Export permission
     *
     * @param nodeRef    for node reference
     * @param permission the permission
     */
    void permission(NodeRef nodeRef, AccessPermission permission);

    /**
     * End export of ACL
     *
     * @param nodeRef for node reference
     */
    void endACL(NodeRef nodeRef);

    /**
     * Start export of properties
     *
     * @param nodeRef the node reference
     */
    void startProperties(NodeRef nodeRef);

    /**
     * Start export of property
     *
     * @param nodeRef  the node reference
     * @param property the property name
     */
    void startProperty(NodeRef nodeRef, QName property);

    /**
     * End export of property
     *
     * @param nodeRef  the node reference
     * @param property the property name
     */
    void endProperty(NodeRef nodeRef, QName property);

    /**
     * End export of properties
     *
     * @param nodeRef the node reference
     */
    void endProperties(NodeRef nodeRef);

    /**
     * Export start of value collection
     *
     * @param nodeRef  the node reference
     * @param property the property name
     */
    void startValueCollection(NodeRef nodeRef, QName property);

    /**
     * Start export MLText
     *
     * @param nodeRef the node reference
     */
    void startValueMLText(NodeRef nodeRef, Locale locale);

    /**
     * End export MLText
     */
    void endValueMLText(NodeRef nodeRef);

    /**
     * Export property value
     *
     * @param nodeRef  the node reference
     * @param property the property name
     * @param value    the value
     * @param index    value index (or -1, if not part of multi-valued collection)
     */
    void value(NodeRef nodeRef, QName property, Object value, int index);

    /**
     * Export content stream property value
     *
     * @param nodeRef     the node reference
     * @param property    the property name
     * @param content     the content stream
     * @param contentData content descriptor
     * @param index       value index (or -1, if not part of multi-valued collection)
     */
    void content(NodeRef nodeRef, QName property, InputStream content, ContentData contentData,
                 int index);

    /**
     * Export end of value collection
     *
     * @param nodeRef  the node reference
     * @param property the property name
     */
    void endValueCollection(NodeRef nodeRef, QName property);

    /**
     * Start export of associations
     */
    void startAssocs(NodeRef nodeRef);

    /**
     * Start export of association
     *
     * @param nodeRef the node reference
     * @param assoc   the association name
     */
    void startAssoc(NodeRef nodeRef, QName assoc);

    /**
     * End export of association
     *
     * @param nodeRef the node reference
     * @param assoc   the association name
     */
    void endAssoc(NodeRef nodeRef, QName assoc);

    /**
     * End export of associations
     */
    void endAssocs(NodeRef nodeRef);

    /**
     * Export warning
     *
     * @param warning the warning message
     */
    void warning(String warning);

    /**
     * End export
     */
    void end();

    /**
     * Start export of versions
     */
    void startVersions(NodeRef nodeRef);

    /**
     * End export of versions
     */
    void endVersions(NodeRef nodeRef);

    /**
     * Start export of MLTranslations
     */
    void startMLTranslations(NodeRef nodeRef);

    /**
     * End export of MLTranslations
     */
    void endMLTranslations(NodeRef nodeRef);
}
