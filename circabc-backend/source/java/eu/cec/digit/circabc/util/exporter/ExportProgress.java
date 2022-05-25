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
public class ExportProgress implements Exporter {

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#start(org.alfresco.service.cmr.view.ExporterContext)
     */
    @Override
    public void start(ExporterContext context) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startNamespace(java.lang.String, java.lang.String)
     */
    @Override
    public void startNamespace(String prefix, String uri) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endNamespace(java.lang.String)
     */
    @Override
    public void endNamespace(String prefix) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void startNode(NodeRef nodeRef) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void endNode(NodeRef nodeRef) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startReference(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    public void startReference(NodeRef nodeRef, QName childName) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endReference(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void endReference(NodeRef nodeRef) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startAspects(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void startAspects(NodeRef nodeRef) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    public void startAspect(NodeRef nodeRef, QName aspect) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    public void endAspect(NodeRef nodeRef, QName aspect) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endAspects(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void endAspects(NodeRef nodeRef) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startACL(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void startACL(NodeRef nodeRef) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#permission(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.security.AccessPermission)
     */
    @Override
    public void permission(NodeRef nodeRef, AccessPermission permission) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endACL(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void endACL(NodeRef nodeRef) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startProperties(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void startProperties(NodeRef nodeRef) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startProperty(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    public void startProperty(NodeRef nodeRef, QName property) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endProperty(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    public void endProperty(NodeRef nodeRef, QName property) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endProperties(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void endProperties(NodeRef nodeRef) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startValueCollection(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    public void startValueCollection(NodeRef nodeRef, QName property) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startValueMLText(org.alfresco.service.cmr.repository.NodeRef, java.util.Locale)
     */
    @Override
    public void startValueMLText(NodeRef nodeRef, Locale locale) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endValueMLText(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void endValueMLText(NodeRef nodeRef) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#value(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.lang.Object, int)
     */
    @Override
    public void value(NodeRef nodeRef, QName property, Object value, int index) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#content(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.io.InputStream, org.alfresco.service.cmr.repository.ContentData, int)
     */
    @Override
    public void content(NodeRef nodeRef, QName property, InputStream content,
                        ContentData contentData, int index) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endValueCollection(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    public void endValueCollection(NodeRef nodeRef, QName property) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startAssocs(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void startAssocs(NodeRef nodeRef) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#startAssoc(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    public void startAssoc(NodeRef nodeRef, QName assoc) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endAssoc(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    public void endAssoc(NodeRef nodeRef, QName assoc) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#endAssocs(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void endAssocs(NodeRef nodeRef) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#warning(java.lang.String)
     */
    @Override
    public void warning(String warning) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.view.Exporter#end()
     */
    @Override
    public void end() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.module.circabc.Exporter#startVersions(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void startVersions(NodeRef nodeRef) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.module.circabc.Exporter#endVersions(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void endVersions(NodeRef nodeRef) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.module.circabc.Exporter#startMLTranslations(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void startMLTranslations(NodeRef nodeRef) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see eu.cec.digit.circabc.module.circabc.Exporter#endMLTranslations(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void endMLTranslations(NodeRef nodeRef) {
        // TODO Auto-generated method stub

    }

}
