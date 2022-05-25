/**
 * ***************************************************************************** Copyright 2006
 * European Community
 *
 * <p>Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 *
 * <p>https://joinup.ec.europa.eu/software/page/eupl
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * ****************************************************************************
 */
package eu.cec.digit.circabc.repo.dynamic.property;

import eu.cec.digit.circabc.model.CircabcModel;
import eu.cec.digit.circabc.model.DocumentModel;
import eu.cec.digit.circabc.service.cmr.security.CircabcConstant;
import eu.cec.digit.circabc.service.dynamic.property.DynamicProperty;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyService;
import eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyType;
import io.swagger.util.ApiToolBox;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyMap;
import org.alfresco.web.bean.repository.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryParser.QueryParser;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

import static eu.cec.digit.circabc.model.DynamicPropertyModel.*;
import static eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyType.MULTI_SELECTION;
import static eu.cec.digit.circabc.service.dynamic.property.DynamicPropertyType.SELECTION;

/**
 * Implementation for dynamic property operations.
 *
 * @author Slobodan Filipovic
 * <p>Migration 3.1 -> 3.4.6 - 02/12/2011 TODO QueryParser is taken from Lucene?
 */
public class DynamicPropertyServiceImpl implements DynamicPropertyService {

    /**
     * A logger for the class
     */
    static final Log logger = LogFactory.getLog(DynamicPropertyServiceImpl.class);

    private static final QName ASSOC_IG_DYNAMICPROPERTIESCONTAINER =
            QName.createQName(CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI, "igDpContainer");

    /**
     * The node service reference
     */
    private NodeService nodeService;
    /**
     * The permission service reference
     */
    private PermissionService permissionService;
    /**
     * The namespace service reference
     */
    private NamespaceService namespaceService;
    /**
     * The search service reference
     */
    private SearchService searchService;

    private SimpleCache<Long, List<DynamicProperty>> dynamicPropertyCache;

    private ApiToolBox apiToolBox;

    public DynamicProperty addDynamicProperty(
            final NodeRef ig, final DynamicProperty dynamicProperty) {

        if (dynamicProperty == null) {
            throw new NullPointerException("dynamicProperty is a mandatory parameter");
        }
        invalidateCache(ig);

        // Get the container
        NodeRef container = getDynamicPropertyContainer(ig);
        // if container does not exists create it
        if (container == null) {
            container = createDynamicPropertyContainer(ig);
        }

        PropertyMap properties = new PropertyMap(4);

        Long index = null;

        if (dynamicProperty.getIndex() != null) {
            index = dynamicProperty.getIndex();

            if (!isIndexFree(container, index)) {
                throw new IllegalArgumentException(
                        "The index " + index + " is already in use for an other dynamic property.");
            }

        } else {
            index = computeValidIndex(container);
        }

        final Serializable valueObject = dynamicProperty.getLabel();
        properties.put(PROP_DYNAMIC_PROPERTY_LABEL, valueObject);
        properties.put(PROP_DYNAMIC_PROPERTY_INDEX, index);
        properties.put(PROP_DYNAMIC_PROPERTY_TYPE, dynamicProperty.getType().getModelDataDefinition());

        if (DynamicPropertyType.SELECTION.equals(dynamicProperty.getType())
                || DynamicPropertyType.MULTI_SELECTION.equals(dynamicProperty.getType())) {
            properties.put(PROP_DYNAMIC_PROPERTY_VALID_VALUES, dynamicProperty.getValidValues());
        }

        // Create the property
        final ChildAssociationRef assocRef =
                nodeService.createNode(
                        container,
                        ASSOC_DYNAMIC_PROPERTY,
                        TYPE_DYNAMIC_PROPERTY,
                        TYPE_DYNAMIC_PROPERTY,
                        properties);

        final NodeRef newProperty = assocRef.getChildRef();

        // done
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Added a keyword: \n"
                            + "   Interest group : "
                            + ig
                            + "\n"
                            + "   Property       : "
                            + dynamicProperty
                            + "\n"
                            + "   Index          : "
                            + index
                            + "\n"
                            + " 	Property Ref   : "
                            + newProperty);
        }
        DynamicProperty dynamicPropertyTmp = getDynamicPropertyByID(newProperty);
        return dynamicPropertyTmp;
    }

    private void invalidateCache(NodeRef ig) {
        Long key = (Long) nodeService.getProperty(ig, ContentModel.PROP_NODE_DBID);
        dynamicPropertyCache.remove(key);
    }

    private void putInCache(NodeRef ig, List<DynamicProperty> list) {
        Long key = (Long) nodeService.getProperty(ig, ContentModel.PROP_NODE_DBID);
        dynamicPropertyCache.put(key, list);
    }

    public List<DynamicProperty> getDynamicProperties(NodeRef dp) {
        List<DynamicProperty> dynamicProperties = new ArrayList<>();
        NodeRef ig;
        if (!nodeService.hasAspect(dp, CircabcModel.ASPECT_IGROOT)) {
            final NodeRef igFromDynProp = getIgFromDynProp(dp);
            if (igFromDynProp == null) {
                logger.warn("The model seems corrupted, no IG found for the dynamic property " + dp);

                throw new IllegalArgumentException(
                        "Impossible to get the interest group of the dynamic property " + dp);
            } else {
                ig = igFromDynProp;
            }
        } else {
            ig = dp;
        }

        Long nodeDatabaseID = (Long) nodeService.getProperty(ig, ContentModel.PROP_NODE_DBID);
        final List<DynamicProperty> list = dynamicPropertyCache.get(nodeDatabaseID);
        if (list != null) {
            return list;
        }

        NodeRef container = getDynamicPropertyContainer(ig);

        if (container == null) {
            // not created yet
            return Collections.emptyList();
        }

        // Get all the children
        final List<ChildAssociationRef> dynamicPropertiesAssoc =
                nodeService.getChildAssocs(container, ASSOC_DYNAMIC_PROPERTY, RegexQNamePattern.MATCH_ALL);

        final boolean wasMLAware = MLPropertyInterceptor.isMLAware();

        try {
            MLPropertyInterceptor.setMLAware(true);
            for (final ChildAssociationRef ref : dynamicPropertiesAssoc) {
                NodeRef noderef = ref.getChildRef();

                if (nodeService.getType(noderef).equals(TYPE_DYNAMIC_PROPERTY)) {
                    final MLText label =
                            (MLText) nodeService.getProperty(noderef, PROP_DYNAMIC_PROPERTY_LABEL);
                    final Long index = (Long) nodeService.getProperty(noderef, PROP_DYNAMIC_PROPERTY_INDEX);
                    final String typeAsString =
                            (String) nodeService.getProperty(noderef, PROP_DYNAMIC_PROPERTY_TYPE);
                    final DynamicPropertyType type = DynamicPropertyType.valueOf(typeAsString);
                    String validValues = null;
                    if (SELECTION.equals(type) || MULTI_SELECTION.equals(type)) {
                        validValues =
                                (String) nodeService.getProperty(noderef, PROP_DYNAMIC_PROPERTY_VALID_VALUES);
                    }
                    dynamicProperties.add(new DynamicPropertyImpl(index, noderef, label, type, validValues));

                } else {
                    if (logger.isWarnEnabled()) {
                        logger.warn(
                                "A non dynamic property element founds under a dynamic property container: \n"
                                        + "   Interest group :  "
                                        + ig
                                        + "\n"
                                        + "   Container      :  "
                                        + container
                                        + "\n"
                                        + "   type           :  "
                                        + nodeService.getType(noderef)
                                        + "\n"
                                        + "   Node           : "
                                        + noderef);
                    }
                }
            }
            Collections.sort(dynamicProperties);

            putInCache(ig, dynamicProperties);

        } finally {
            MLPropertyInterceptor.setMLAware(wasMLAware);
        }

        return dynamicProperties;
    }

    public void deleteDynamicProperty(DynamicProperty dp) {
        if (dp == null || !TYPE_DYNAMIC_PROPERTY.isMatch(nodeService.getType(dp.getId()))) {
            throw new IllegalArgumentException("Only a valid dynamic property noderef is required here");
        }

        final NodeRef ig = getIgFromDynProp(dp.getId());

        if (ig == null) {
            logger.warn("The model seems corrupted, no IG found for the dynamic property " + dp);

            throw new IllegalArgumentException(
                    "Impossible to get the interest group of the dynamic property " + dp);
        }
        invalidateCache(ig);

        final List<NodeRef> referencedDocument = getNodesForProperty(ig, dp, "*", false);
        final QName propertyQname = getPropertyQname(dp);

        for (final NodeRef ref : referencedDocument) {
            nodeService.setProperty(ref, propertyQname, null);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Dynamic property successfully dropped from the referenced documents: "
                            + "\n  Property: "
                            + dp
                            + "\n  Documents: "
                            + referencedDocument);
        }

        nodeService.deleteNode(dp.getId());

        if (logger.isDebugEnabled()) {
            logger.debug("Dynamic property successfully dropped from the ig: " + "\n  Property: " + dp);
        }
    }

    public DynamicProperty getDynamicPropertyByID(NodeRef nodeRef) {
        if (!nodeService.getType(nodeRef).equals(TYPE_DYNAMIC_PROPERTY)) {
            throw new IllegalArgumentException("Node ref is had wrong type");
        }

        Long index = null;
        String validValues = null;
        MLText label = null;
        String typeAsString = null;
        DynamicPropertyType type = null;

        final boolean wasMLAware = MLPropertyInterceptor.isMLAware();
        try {
            MLPropertyInterceptor.setMLAware(true);
            index = (Long) nodeService.getProperty(nodeRef, PROP_DYNAMIC_PROPERTY_INDEX);
            label = (MLText) nodeService.getProperty(nodeRef, PROP_DYNAMIC_PROPERTY_LABEL);
            typeAsString = (String) nodeService.getProperty(nodeRef, PROP_DYNAMIC_PROPERTY_TYPE);
            type = DynamicPropertyType.valueOf(typeAsString);

            if (SELECTION.equals(type) || MULTI_SELECTION.equals(type)) {
                validValues = (String) nodeService.getProperty(nodeRef, PROP_DYNAMIC_PROPERTY_VALID_VALUES);
            }
        } finally {
            MLPropertyInterceptor.setMLAware(wasMLAware);
        }

        return new DynamicPropertyImpl(index, nodeRef, label, type, validValues);
    }

    public void updateDynamicPropertyLabel(DynamicProperty dp, MLText label) {
        if (dp == null) {
            throw new NullPointerException("dynamicProperty is a mandatory parameter");
        }

        final NodeRef igRootNodeRef = getIgFromDynProp(dp.getId());
        invalidateCache(igRootNodeRef);
        // Get the container

        final NodeRef newProperty = dp.getId();
        Serializable valueObject = label;
        nodeService.setProperty(newProperty, PROP_DYNAMIC_PROPERTY_LABEL, valueObject);
    }

    public QName getPropertyQname(final DynamicProperty dp) {
        ParameterCheck.mandatory("The dynamic property", dp);
        ParameterCheck.mandatory("The dynamic property index", dp.getIndex());

        return DocumentModel.ALL_DYN_PROPS.get(dp.getIndex().intValue() - 1);
    }

    /////////////////
    /// HELPERS

    private List<NodeRef> getNodesForProperty(
            final NodeRef parent, final DynamicProperty property, String value, boolean like) {
        long startTime = 0;
        if (logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }

        if (property == null) {
            throw new IllegalArgumentException("Please to define ne dynamic property for the search");
        }

        final QName propName = getPropertyQname(property);

        List<NodeRef> documents;

        final StringBuilder query = new StringBuilder();

        // search only in the interest group
        query.append(" PATH:\"").append(apiToolBox.getPathFromSpaceRef(parent, true)).append("\" ");
        // search only Circabc document having each keyword in the Keyword
        // property
        query.append(" AND  +@").append(QueryParser.escape(propName.toString())).append(":");
        if (like) {
            query.append("*");
        }
        query.append(value);
        if (like) {
            query.append("*");
        }

        // perform the search against the repo
        ResultSet results = null;
        try {
            // Limit search to the first 100 matches
            final SearchParameters sp = new SearchParameters();
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.setQuery(query.toString());
            sp.addStore(Repository.getStoreRef());

            sp.setLimitBy(LimitBy.UNLIMITED);

            results = searchService.query(sp);
            if (logger.isDebugEnabled()) {
                logger.debug("Search for: " + query);
                logger.debug("Search results returned: " + results.length());
            }

            documents = new ArrayList<>(results.length());

            if (results.length() != 0) {
                NodeRef nodeRef;
                for (final ResultSetRow row : results) {
                    nodeRef = row.getNodeRef();

                    if (nodeService.exists(nodeRef)
                            && permissionService
                            .hasPermission(nodeRef, PermissionService.READ)
                            .equals(AccessStatus.ALLOWED)) {
                        documents.add(nodeRef);
                    }
                }
            }
        } finally {
            if (results != null) {
                results.close();
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Keywords search successfull"
                            + "\n  From: "
                            + parent
                            + "\n  With property: "
                            + property
                            + "\n  Result: "
                            + documents);

            final long endTime = System.currentTimeMillis();
            logger.debug(
                    "Time to query and build the list of document referenced by the keyword: "
                            + (endTime - startTime)
                            + "ms");
        }

        return documents;
    }

    /**
     * @return root interest group node reference
     */
    protected NodeRef getIgFromDynProp(final NodeRef property) {
        NodeRef tempRef = property;
        for (; ; ) {
            if (tempRef == null) {
                // we are above Company home. No ig found
                break;
            } else if (nodeService.hasAspect(tempRef, CircabcModel.ASPECT_IGROOT)) {
                // return the ig
                break;
            } else {
                tempRef = nodeService.getPrimaryParent(tempRef).getParentRef();
            }
        }
        return tempRef;
    }

    private NodeRef getDynamicPropertyContainer(NodeRef ig) {
        if (ig == null) {
            throw new IllegalArgumentException("An interest group must is supplied");
        }

        if (!getNodeService().hasAspect(ig, CircabcModel.ASPECT_IGROOT)) {
            throw new IllegalArgumentException(
                    "Node must have aspect " + CircabcModel.ASPECT_IGROOT + " applied");
        }

        NodeRef dynamicPropertiesContainer = null;

        final List<ChildAssociationRef> childAssocRefs =
                nodeService.getChildAssocs(
                        ig, ASSOC_IG_DYNAMICPROPERTIESCONTAINER, RegexQNamePattern.MATCH_ALL);

        if (childAssocRefs.size() == 0) {

            dynamicPropertiesContainer = null;
        } else if (childAssocRefs.size() == 1) {
            final ChildAssociationRef toKeepAssocRef = childAssocRefs.get(0);
            dynamicPropertiesContainer = toKeepAssocRef.getChildRef();

        } else if (childAssocRefs.size() > 1) {
            // This is a problem - destroy all but the first
            if (logger.isWarnEnabled()) {
                logger.warn("Cleaning up multiple properties containers on the interest group: " + ig);
            }
            throw new RuntimeException("Too many dynamic properties containers");
        }

        return dynamicPropertiesContainer;
    }

    private NodeRef createDynamicPropertyContainer(NodeRef nodeRef) {

        final ChildAssociationRef assocRef =
                nodeService.createNode(
                        nodeRef,
                        ASSOC_IG_DYNAMICPROPERTIESCONTAINER,
                        TYPE_DYNAMIC_PROPERTY_CONTAINER,
                        TYPE_DYNAMIC_PROPERTY_CONTAINER,
                        new PropertyMap());

        final NodeRef dpContainerNodeRef = assocRef.getChildRef();

        permissionService.setPermission(
                dpContainerNodeRef,
                PermissionService.ALL_AUTHORITIES,
                PermissionService.ALL_PERMISSIONS,
                true);

        permissionService.setPermission(
                dpContainerNodeRef,
                CircabcConstant.GUEST_AUTHORITY,
                PermissionService.ALL_PERMISSIONS,
                true);

        // Done
        return dpContainerNodeRef;
    }

    private Long computeValidIndex(NodeRef container) {
        // Get all the children
        final List<ChildAssociationRef> dynamicPropertiesAssoc =
                nodeService.getChildAssocs(container, ASSOC_DYNAMIC_PROPERTY, RegexQNamePattern.MATCH_ALL);

        final List<Long> foundIndexes = new ArrayList<>(dynamicPropertiesAssoc.size());

        NodeRef child = null;
        Serializable indexAsSer;
        Long index;
        for (ChildAssociationRef assoc : dynamicPropertiesAssoc) {
            child = assoc.getChildRef();
            indexAsSer = nodeService.getProperty(child, PROP_DYNAMIC_PROPERTY_INDEX);

            if (indexAsSer != null) {
                index = (Long) indexAsSer;

                if (foundIndexes.contains(index)) {
                    logger.warn(
                            "The model is corrupted, several dynamic properties were found without index "
                                    + child);
                } else {
                    foundIndexes.add(index);
                }
            } else {
                logger.warn("The model is corrupted, a dynamic property were found without index " + child);
            }
        }

        Long validIndex = null;
        Long currentIndex = null;
        for (int x = 1; ; x++) {
            currentIndex = (long) x;
            if (!foundIndexes.contains(currentIndex)) {
                validIndex = currentIndex;
                break;
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("A valid index is found (" + validIndex + ") for the container " + container);
        }

        if (validIndex.intValue() > MAX_PROPERTY_BY_IG) {
            throw new IllegalStateException(
                    "Impossible to have more than "
                            + MAX_PROPERTY_BY_IG
                            + " dynamic property for an Interest Group");
        }

        return validIndex;
    }

    private boolean isIndexFree(final NodeRef container, final Long index) {
        // Get all the children
        final List<ChildAssociationRef> dynamicPropertiesAssoc =
                nodeService.getChildAssocs(container, ASSOC_DYNAMIC_PROPERTY, RegexQNamePattern.MATCH_ALL);

        boolean free = true;

        NodeRef child = null;
        Serializable indexAsSer;

        for (ChildAssociationRef assoc : dynamicPropertiesAssoc) {
            child = assoc.getChildRef();
            indexAsSer = nodeService.getProperty(child, PROP_DYNAMIC_PROPERTY_INDEX);

            if (index.equals(indexAsSer)) {
                free = false;
                break;
            }
        }

        return free;
    }

    public void updateDynamicPropertyValidValues(
            DynamicProperty dp,
            String validValues,
            boolean updateExistingProperties,
            Set<String> deletedValuse,
            Map<String, String> updatedValues) {
        if (dp == null || !TYPE_DYNAMIC_PROPERTY.isMatch(nodeService.getType(dp.getId()))) {
            throw new IllegalArgumentException("Only a valid dynamic property noderef is required here");
        }

        if (!(dp.getType().equals(DynamicPropertyType.SELECTION)
                || dp.getType().equals(DynamicPropertyType.MULTI_SELECTION))) {
            throw new IllegalArgumentException("Invalid dynamic property type ");
        }

        final NodeRef newProperty = dp.getId();

        nodeService.setProperty(newProperty, PROP_DYNAMIC_PROPERTY_VALID_VALUES, validValues);
        final NodeRef igRootNodeRef = getIgFromDynProp(dp.getId());
        invalidateCache(igRootNodeRef);

        final QName propertyQName = getPropertyQname(dp);
        if (updateExistingProperties) {

            for (String item : deletedValuse) {
                deleteDynamicPropertyValues(igRootNodeRef, propertyQName, dp, item);
            }

            for (Entry<String, String> item : updatedValues.entrySet()) {
                updateDynamicPropertyValues(
                        igRootNodeRef, propertyQName, dp, item.getKey(), item.getValue());
            }
        }
    }

    private void updateDynamicPropertyValues(
            NodeRef igRootNodeRef, QName propertyQName, DynamicProperty dp, String first, String second) {
        if (dp.getType() == SELECTION) {
            final List<NodeRef> nodesForProperty = getNodesForProperty(igRootNodeRef, dp, first, false);
            for (NodeRef nodeRef : nodesForProperty) {
                nodeService.setProperty(nodeRef, propertyQName, second);
            }
        } else if (dp.getType() == MULTI_SELECTION) {
            final List<NodeRef> nodesForProperty = getNodesForProperty(igRootNodeRef, dp, first, true);
            for (NodeRef nodeRef : nodesForProperty) {
                String oldProperty = (String) nodeService.getProperty(nodeRef, propertyQName);
                String newProperty = oldProperty.replace(first, second);
                nodeService.setProperty(nodeRef, propertyQName, newProperty);
            }
        }
    }

    private void deleteDynamicPropertyValues(
            NodeRef igRootNodeRef, QName propertyQName, DynamicProperty dp, String value) {
        if (dp.getType() == SELECTION) {
            final List<NodeRef> nodesForProperty = getNodesForProperty(igRootNodeRef, dp, value, false);
            for (NodeRef nodeRef : nodesForProperty) {
                nodeService.removeProperty(nodeRef, propertyQName);
            }
        } else if (dp.getType() == MULTI_SELECTION) {
            final List<NodeRef> nodesForProperty = getNodesForProperty(igRootNodeRef, dp, value, true);
            for (NodeRef nodeRef : nodesForProperty) {
                String oldProperty = (String) nodeService.getProperty(nodeRef, propertyQName);
                String newProperty = oldProperty.replace(value + ",", "");
                if (newProperty.equals(oldProperty)) {
                    newProperty = newProperty.replace("," + value, "");
                    if (newProperty.equals(oldProperty)) {
                        newProperty = newProperty.replace(value, "");
                    }
                }
                nodeService.setProperty(nodeRef, propertyQName, newProperty);
            }
        }
    }

    //////////
    /// IOC

    /**
     * @return the nodeService
     */
    protected final NodeService getNodeService() {
        return nodeService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public final void setNodeService(final NodeService nodeService) {
        this.nodeService = nodeService;
    }

    /**
     * @return the permissionService
     */
    protected final PermissionService getPermissionService() {
        return permissionService;
    }

    /**
     * @param permissionService the permissionService to set
     */
    public final void setPermissionService(final PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * @return the namespaceService
     */
    protected final NamespaceService getNamespaceService() {
        return namespaceService;
    }

    /**
     * @param namespaceService the namespaceService to set
     */
    public final void setNamespaceService(final NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    /**
     * @return the search service
     */
    protected final SearchService getSearchService() {
        return searchService;
    }

    /**
     * @param searchService the searchService to set
     */
    public final void setSearchService(final SearchService searchService) {
        this.searchService = searchService;
    }

    public SimpleCache<Long, List<DynamicProperty>> getDynamicPropertyCache() {
        return dynamicPropertyCache;
    }

    public void setDynamicPropertyCache(
            SimpleCache<Long, List<DynamicProperty>> dynamicPropertyCache) {
        this.dynamicPropertyCache = dynamicPropertyCache;
    }

    public ApiToolBox getApiToolBox() {
        return apiToolBox;
    }

    public void setApiToolBox(ApiToolBox apiToolBox) {
        this.apiToolBox = apiToolBox;
    }
}
