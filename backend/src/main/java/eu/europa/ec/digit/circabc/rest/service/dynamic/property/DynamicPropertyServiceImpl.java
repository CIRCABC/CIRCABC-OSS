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
package eu.europa.ec.digit.circabc.rest.service.dynamic.property;

import eu.europa.ec.digit.circabc.rest.exception.DynamicPropertyException;
import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.DocumentModel;
import io.swagger.model.alfresco.DynamicPropertyModel;
import io.swagger.util.ApiToolBox;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.*;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link DynamicPropertyService}.
 *
 * <p>Dynamic properties are user-defined metadata fields that can be attached to the documents of a
 * CIRCABC Interest Group (IG). This service manages their full life cycle: creating, reading,
 * updating and deleting the {@code dynamicProperty} nodes that live inside a dedicated container
 * node ({@code igDpContainer}) hanging off the IG root.
 *
 * <p>Each dynamic property is identified within its IG by a positional {@code index} (1-based, up to
 * {@link DynamicPropertyService#MAX_PROPERTY_BY_IG}). This index maps the logical property onto one
 * of the physical, reusable Alfresco content-model properties declared in {@link
 * io.swagger.model.alfresco.DocumentModel#ALL_DYN_PROPS}, on which the value is actually stored for
 * each document.
 *
 * <p>Results are cached per Interest Group (keyed by the IG node's database id) to avoid repeatedly
 * walking the repository; mutating operations invalidate the relevant cache entry.
 *
 * @author Slobodan Filipovic
 */
public class DynamicPropertyServiceImpl implements DynamicPropertyService {

  /**
   * A logger for the class
   */
  static final Log logger = LogFactory.getLog(DynamicPropertyServiceImpl.class);

  /**
   * QName of the parent/child association linking an Interest Group root node to its single dynamic
   * property container ({@code igDpContainer}).
   */
  private static final QName ASSOC_IG_DYNAMICPROPERTIESCONTAINER =
    QName.createQName(
      CircabcModel.CIRCABC_CONTENT_MODEL_1_0_URI,
      "igDpContainer"
    );

  /**
   * The node service reference
   */
  @Autowired
  private NodeService nodeService;

  /**
   * The permission service reference
   */
  @Autowired
  private PermissionService permissionService;

  /**
   * The search service reference
   */
  @Autowired
  private SearchService searchService;

  /**
   * Cache of the dynamic properties of an Interest Group, keyed by the IG node's database id
   * ({@code cm:node-dbid}). Populated on read and invalidated on any mutation.
   */
  private SimpleCache<Long, List<DynamicProperty>> dynamicPropertyCache;

  /**
   * Toolbox helper used, among other things, to resolve the repository path of a node when building
   * search queries.
   */
  @Autowired
  private ApiToolBox apiToolBox;

  /**
   * Adds a new dynamic property to an Interest Group.
   *
   * <p>Creates the dynamic property container on the IG if it does not yet exist, assigns the
   * property an index (using the caller-supplied index if free, otherwise the first available one)
   * and persists its label, type and — for selection types — its valid values.
   *
   * @param ig the Interest Group root node reference the property belongs to
   * @param dynamicProperty the dynamic property to add
   * @return the newly created dynamic property, reloaded from the repository
   * @throws NullPointerException if {@code dynamicProperty} is {@code null}
   * @throws IllegalArgumentException if the requested index is already in use
   * @throws IllegalStateException if the IG already holds the maximum number of dynamic properties
   */
  public DynamicProperty addDynamicProperty(
    final NodeRef ig,
    final DynamicProperty dynamicProperty
  ) {
    if (dynamicProperty == null) {
      throw new NullPointerException(
        "dynamicProperty is a mandatory parameter"
      );
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
          "The index " +
            index +
            " is already in use for an other dynamic property."
        );
      }
    } else {
      index = computeValidIndex(container);
    }

    final Serializable valueObject = dynamicProperty.getLabel();
    properties.put(
      DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_LABEL,
      valueObject
    );
    properties.put(DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_INDEX, index);
    properties.put(
      DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_TYPE,
      dynamicProperty.getType().getModelDataDefinition()
    );

    if (
      DynamicPropertyType.SELECTION.equals(dynamicProperty.getType()) ||
      DynamicPropertyType.MULTI_SELECTION.equals(dynamicProperty.getType())
    ) {
      properties.put(
        DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_VALID_VALUES,
        dynamicProperty.getValidValues()
      );
    }

    // Create the property
    final ChildAssociationRef assocRef = nodeService.createNode(
      container,
      DynamicPropertyModel.ASSOC_DYNAMIC_PROPERTY,
      DynamicPropertyModel.TYPE_DYNAMIC_PROPERTY,
      DynamicPropertyModel.TYPE_DYNAMIC_PROPERTY,
      properties
    );

    final NodeRef newProperty = assocRef.getChildRef();

    return getDynamicPropertyByID(newProperty);
  }

  private void invalidateCache(NodeRef ig) {
    Long key = (Long) nodeService.getProperty(ig, ContentModel.PROP_NODE_DBID);
    dynamicPropertyCache.remove(key);
  }

  private void putInCache(NodeRef ig, List<DynamicProperty> list) {
    Long key = (Long) nodeService.getProperty(ig, ContentModel.PROP_NODE_DBID);
    dynamicPropertyCache.put(key, list);
  }

  /**
   * Returns the dynamic properties defined for the Interest Group that the given node belongs to.
   *
   * <p>The result is served from the per-IG cache when available; otherwise it is loaded from the
   * repository and cached. An empty list is returned when the IG has no dynamic property container.
   *
   * @param dp a node reference belonging to an Interest Group, which may itself be the IG root
   * @return the sorted list of dynamic properties, or an empty list if none are defined
   * @throws IllegalArgumentException if the owning Interest Group cannot be resolved for {@code dp}
   */
  public List<DynamicProperty> getDynamicProperties(NodeRef dp) {
    NodeRef ig = resolveInterestGroup(dp);
    Long nodeDatabaseID = (Long) nodeService.getProperty(
      ig,
      ContentModel.PROP_NODE_DBID
    );

    List<DynamicProperty> cached = dynamicPropertyCache.get(nodeDatabaseID);
    if (cached != null) {
      return cached;
    }

    NodeRef container = getDynamicPropertyContainer(ig);
    if (container == null) {
      return Collections.emptyList();
    }

    List<DynamicProperty> dynamicProperties = loadDynamicProperties(
      container,
      ig
    );
    putInCache(ig, dynamicProperties);
    return dynamicProperties;
  }

  private NodeRef resolveInterestGroup(NodeRef dp) {
    if (nodeService.hasAspect(dp, CircabcModel.ASPECT_IGROOT)) {
      return dp;
    }
    NodeRef ig = getIgFromDynProp(dp);
    if (ig == null) {
      logger.warn(
        "The model seems corrupted, no IG found for the dynamic property " + dp
      );
      throw new IllegalArgumentException(
        "Impossible to get the interest group of the dynamic property " + dp
      );
    }
    return ig;
  }

  private List<DynamicProperty> loadDynamicProperties(
    NodeRef container,
    NodeRef ig
  ) {
    List<DynamicProperty> dynamicProperties = new ArrayList<>();
    List<ChildAssociationRef> dynamicPropertiesAssoc =
      nodeService.getChildAssocs(
        container,
        DynamicPropertyModel.ASSOC_DYNAMIC_PROPERTY,
        RegexQNamePattern.MATCH_ALL
      );

    boolean wasMLAware = MLPropertyInterceptor.isMLAware();
    try {
      MLPropertyInterceptor.setMLAware(true);
      for (ChildAssociationRef ref : dynamicPropertiesAssoc) {
        DynamicProperty prop = createDynamicPropertyFromNode(
          ref.getChildRef(),
          container,
          ig
        );
        if (prop != null) {
          dynamicProperties.add(prop);
        }
      }
      Collections.sort(dynamicProperties);
    } finally {
      MLPropertyInterceptor.setMLAware(wasMLAware);
    }
    return dynamicProperties;
  }

  private DynamicProperty createDynamicPropertyFromNode(
    NodeRef noderef,
    NodeRef container,
    NodeRef ig
  ) {
    if (
      !nodeService
        .getType(noderef)
        .equals(DynamicPropertyModel.TYPE_DYNAMIC_PROPERTY)
    ) {
      if (logger.isWarnEnabled()) {
        logger.warn(
          "A non dynamic property element founds under a dynamic property container: \n" +
            "   Interest group :  " +
            ig +
            "\n" +
            "   Container      :  " +
            container +
            "\n" +
            "   type           :  " +
            nodeService.getType(noderef) +
            "\n" +
            "   Node           : " +
            noderef
        );
      }
      return null;
    }

    MLText label = (MLText) nodeService.getProperty(
      noderef,
      DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_LABEL
    );
    Long index = (Long) nodeService.getProperty(
      noderef,
      DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_INDEX
    );
    String typeAsString = (String) nodeService.getProperty(
      noderef,
      DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_TYPE
    );
    DynamicPropertyType type = DynamicPropertyType.valueOf(typeAsString);

    String validValues = null;
    if (
      DynamicPropertyType.SELECTION.equals(type) ||
      DynamicPropertyType.MULTI_SELECTION.equals(type)
    ) {
      validValues = (String) nodeService.getProperty(
        noderef,
        DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_VALID_VALUES
      );
    }
    return new DynamicPropertyImpl(index, noderef, label, type, validValues);
  }

  /**
   * Deletes a dynamic property and clears its value from every document that referenced it.
   *
   * <p>Before removing the dynamic property node, the underlying content-model property is nulled
   * out on all documents of the Interest Group that carried a value for it.
   *
   * @param dp the dynamic property to delete
   * @throws IllegalArgumentException if {@code dp} is {@code null}, does not point to a valid
   *     dynamic property node, or its owning Interest Group cannot be resolved
   */
  public void deleteDynamicProperty(DynamicProperty dp) {
    if (
      dp == null ||
      !DynamicPropertyModel.TYPE_DYNAMIC_PROPERTY.isMatch(
        nodeService.getType(dp.getId())
      )
    ) {
      throw new IllegalArgumentException(
        "Only a valid dynamic property noderef is required here"
      );
    }

    final NodeRef ig = getIgFromDynProp(dp.getId());

    if (ig == null) {
      logger.warn(
        "The model seems corrupted, no IG found for the dynamic property " + dp
      );

      throw new IllegalArgumentException(
        "Impossible to get the interest group of the dynamic property " + dp
      );
    }
    invalidateCache(ig);

    final List<NodeRef> referencedDocument = getNodesForProperty(
      ig,
      dp,
      "*",
      false
    );
    final QName propertyQname = getPropertyQname(dp);

    for (final NodeRef ref : referencedDocument) {
      nodeService.setProperty(ref, propertyQname, null);
    }

    nodeService.deleteNode(dp.getId());
  }

  /**
   * Loads a single dynamic property from its node reference.
   *
   * <p>Reading is performed in ML-aware mode so that the multilingual label is returned as an {@link
   * MLText} rather than a single localized string.
   *
   * @param nodeRef the node reference of the dynamic property
   * @return the dynamic property built from the node's properties
   * @throws IllegalArgumentException if {@code nodeRef} does not point to a dynamic property node
   */
  public DynamicProperty getDynamicPropertyByID(NodeRef nodeRef) {
    if (
      !nodeService
        .getType(nodeRef)
        .equals(DynamicPropertyModel.TYPE_DYNAMIC_PROPERTY)
    ) {
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
      index = (Long) nodeService.getProperty(
        nodeRef,
        DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_INDEX
      );
      label = (MLText) nodeService.getProperty(
        nodeRef,
        DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_LABEL
      );
      typeAsString = (String) nodeService.getProperty(
        nodeRef,
        DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_TYPE
      );
      type = DynamicPropertyType.valueOf(typeAsString);

      if (
        DynamicPropertyType.SELECTION.equals(type) ||
        DynamicPropertyType.MULTI_SELECTION.equals(type)
      ) {
        validValues = (String) nodeService.getProperty(
          nodeRef,
          DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_VALID_VALUES
        );
      }
    } finally {
      MLPropertyInterceptor.setMLAware(wasMLAware);
    }

    return new DynamicPropertyImpl(index, nodeRef, label, type, validValues);
  }

  /**
   * Updates the multilingual label of a dynamic property and invalidates the IG cache.
   *
   * @param dp the dynamic property to update
   * @param label the new label as {@link MLText}
   * @throws NullPointerException if {@code dp} is {@code null}
   */
  public void updateDynamicPropertyLabel(DynamicProperty dp, MLText label) {
    if (dp == null) {
      throw new NullPointerException(
        "dynamicProperty is a mandatory parameter"
      );
    }

    final NodeRef igRootNodeRef = getIgFromDynProp(dp.getId());
    invalidateCache(igRootNodeRef);
    // Get the container

    final NodeRef newProperty = dp.getId();
    Serializable valueObject = label;
    nodeService.setProperty(
      newProperty,
      DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_LABEL,
      valueObject
    );
  }

  /**
   * Resolves the physical content-model property on which a dynamic property's value is stored.
   *
   * <p>The mapping is positional: the property's 1-based index selects an entry from {@link
   * io.swagger.model.alfresco.DocumentModel#ALL_DYN_PROPS}.
   *
   * @param dp the dynamic property
   * @return the QName of the backing document property
   * @throws IllegalArgumentException if {@code dp} or its index is {@code null}
   */
  public QName getPropertyQname(final DynamicProperty dp) {
    ParameterCheck.mandatory("The dynamic property", dp);
    ParameterCheck.mandatory("The dynamic property index", dp.getIndex());

    return DocumentModel.ALL_DYN_PROPS.get(dp.getIndex().intValue() - 1);
  }

  // HELPERS

  private List<NodeRef> getNodesForProperty(
    final NodeRef parent,
    final DynamicProperty property,
    String value,
    boolean like
  ) {
    if (property == null) {
      throw new IllegalArgumentException(
        "Please to define ne dynamic property for the search"
      );
    }

    final QName propName = getPropertyQname(property);

    List<NodeRef> documents;

    final StringBuilder query = new StringBuilder();

    // search only in the interest group
    query
      .append(" PATH:\"")
      .append(apiToolBox.getPathFromSpaceRef(parent, true))
      .append("\" ");
    // search only Circabc document having each keyword in the Keyword
    // property
    query
      .append(" AND  +@")
      .append(QueryParserBase.escape(propName.toString()))
      .append(":");
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
      sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

      sp.setLimitBy(LimitBy.UNLIMITED);

      results = searchService.query(sp);

      documents = new ArrayList<>(results.length());

      if (results.length() != 0) {
        NodeRef nodeRef;
        for (final ResultSetRow row : results) {
          nodeRef = row.getNodeRef();

          if (
            nodeService.exists(nodeRef) &&
            permissionService
              .hasPermission(nodeRef, PermissionService.READ)
              .equals(AccessStatus.ALLOWED)
          ) {
            documents.add(nodeRef);
          }
        }
      }
    } finally {
      if (results != null) {
        results.close();
      }
    }

    return documents;
  }

  /**
   * Walks up the primary parent chain from a node until it finds the enclosing Interest Group root
   * (the node carrying the {@code igRoot} aspect).
   *
   * @param property the node to start from, typically a dynamic property node
   * @return the root interest group node reference, or {@code null} if none is found
   */
  protected NodeRef getIgFromDynProp(final NodeRef property) {
    NodeRef tempRef = property;
    while (
      tempRef != null &&
      !nodeService.hasAspect(tempRef, CircabcModel.ASPECT_IGROOT)
    ) {
      tempRef = nodeService.getPrimaryParent(tempRef).getParentRef();
    }
    return tempRef;
  }

  private NodeRef getDynamicPropertyContainer(NodeRef ig) {
    if (ig == null) {
      throw new IllegalArgumentException("An interest group must is supplied");
    }

    if (!nodeService.hasAspect(ig, CircabcModel.ASPECT_IGROOT)) {
      throw new IllegalArgumentException(
        "Node must have aspect " + CircabcModel.ASPECT_IGROOT + " applied"
      );
    }

    NodeRef dynamicPropertiesContainer = null;

    final List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(
      ig,
      ASSOC_IG_DYNAMICPROPERTIESCONTAINER,
      RegexQNamePattern.MATCH_ALL
    );

    if (childAssocRefs.size() == 1) {
      final ChildAssociationRef toKeepAssocRef = childAssocRefs.get(0);
      dynamicPropertiesContainer = toKeepAssocRef.getChildRef();
    } else if (childAssocRefs.size() > 1) {
      // This is a problem - destroy all but the first
      if (logger.isWarnEnabled()) {
        logger.warn(
          "Cleaning up multiple properties containers on the interest group: " +
            ig
        );
      }
      throw new DynamicPropertyException(
        "Too many dynamic properties containers"
      );
    }

    return dynamicPropertiesContainer;
  }

  private NodeRef createDynamicPropertyContainer(NodeRef nodeRef) {
    final ChildAssociationRef assocRef = nodeService.createNode(
      nodeRef,
      ASSOC_IG_DYNAMICPROPERTIESCONTAINER,
      DynamicPropertyModel.TYPE_DYNAMIC_PROPERTY_CONTAINER,
      DynamicPropertyModel.TYPE_DYNAMIC_PROPERTY_CONTAINER,
      new PropertyMap()
    );

    final NodeRef dpContainerNodeRef = assocRef.getChildRef();

    permissionService.setPermission(
      dpContainerNodeRef,
      PermissionService.ALL_AUTHORITIES,
      PermissionService.ALL_PERMISSIONS,
      true
    );

    permissionService.setPermission(
      dpContainerNodeRef,
      "guest",
      PermissionService.ALL_PERMISSIONS,
      true
    );

    // Done
    return dpContainerNodeRef;
  }

  private Long computeValidIndex(NodeRef container) {
    // Get all the children
    final List<ChildAssociationRef> dynamicPropertiesAssoc =
      nodeService.getChildAssocs(
        container,
        DynamicPropertyModel.ASSOC_DYNAMIC_PROPERTY,
        RegexQNamePattern.MATCH_ALL
      );

    final List<Long> foundIndexes = new ArrayList<>(
      dynamicPropertiesAssoc.size()
    );

    NodeRef child = null;
    Serializable indexAsSer;
    Long index;
    for (ChildAssociationRef assoc : dynamicPropertiesAssoc) {
      child = assoc.getChildRef();
      indexAsSer = nodeService.getProperty(
        child,
        DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_INDEX
      );

      if (indexAsSer != null) {
        index = (Long) indexAsSer;

        if (foundIndexes.contains(index)) {
          logger.warn(
            "The model is corrupted, several dynamic properties were found without index " +
              child
          );
        } else {
          foundIndexes.add(index);
        }
      } else {
        logger.warn(
          "The model is corrupted, a dynamic property were found without index " +
            child
        );
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

    if (validIndex == null || validIndex.intValue() > MAX_PROPERTY_BY_IG) {
      throw new IllegalStateException(
        "Impossible to have more than " +
          MAX_PROPERTY_BY_IG +
          " dynamic property for an Interest Group"
      );
    }

    return validIndex;
  }

  private boolean isIndexFree(final NodeRef container, final Long index) {
    // Get all the children
    final List<ChildAssociationRef> dynamicPropertiesAssoc =
      nodeService.getChildAssocs(
        container,
        DynamicPropertyModel.ASSOC_DYNAMIC_PROPERTY,
        RegexQNamePattern.MATCH_ALL
      );

    boolean free = true;

    NodeRef child = null;
    Serializable indexAsSer;

    for (ChildAssociationRef assoc : dynamicPropertiesAssoc) {
      child = assoc.getChildRef();
      indexAsSer = nodeService.getProperty(
        child,
        DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_INDEX
      );

      if (index.equals(indexAsSer)) {
        free = false;
        break;
      }
    }

    return free;
  }

  /**
   * Updates the set of valid values of a selection-type dynamic property and, optionally,
   * reconciles the values already stored on existing documents.
   *
   * <p>When {@code updateExistingProperties} is {@code true}, documents holding a deleted value have
   * it removed, and documents holding a renamed value have it replaced according to {@code
   * updatedValues}. The IG cache is invalidated in all cases.
   *
   * @param dp the dynamic property to update; must be of type {@code SELECTION} or {@code
   *     MULTI_SELECTION}
   * @param validValues the new serialized list of valid values
   * @param updateExistingProperties whether values stored on existing documents should be updated
   * @param deletedValuse the values that were removed and must be cleared from existing documents
   * @param updatedValues a mapping of old value to new value to apply on existing documents
   * @throws IllegalArgumentException if {@code dp} is {@code null}, is not a valid dynamic property
   *     node, or is not of a selection type
   */
  public void updateDynamicPropertyValidValues(
    DynamicProperty dp,
    String validValues,
    boolean updateExistingProperties,
    Set<String> deletedValuse,
    Map<String, String> updatedValues
  ) {
    if (
      dp == null ||
      !DynamicPropertyModel.TYPE_DYNAMIC_PROPERTY.isMatch(
        nodeService.getType(dp.getId())
      )
    ) {
      throw new IllegalArgumentException(
        "Only a valid dynamic property noderef is required here"
      );
    }

    if (
      !(dp.getType().equals(DynamicPropertyType.SELECTION) ||
        dp.getType().equals(DynamicPropertyType.MULTI_SELECTION))
    ) {
      throw new IllegalArgumentException("Invalid dynamic property type ");
    }

    final NodeRef newProperty = dp.getId();

    nodeService.setProperty(
      newProperty,
      DynamicPropertyModel.PROP_DYNAMIC_PROPERTY_VALID_VALUES,
      validValues
    );
    final NodeRef igRootNodeRef = getIgFromDynProp(dp.getId());
    invalidateCache(igRootNodeRef);

    final QName propertyQName = getPropertyQname(dp);
    if (updateExistingProperties) {
      for (String item : deletedValuse) {
        deleteDynamicPropertyValues(igRootNodeRef, propertyQName, dp, item);
      }

      for (Entry<String, String> item : updatedValues.entrySet()) {
        updateDynamicPropertyValues(
          igRootNodeRef,
          propertyQName,
          dp,
          item.getKey(),
          item.getValue()
        );
      }
    }
  }

  private void updateDynamicPropertyValues(
    NodeRef igRootNodeRef,
    QName propertyQName,
    DynamicProperty dp,
    String first,
    String second
  ) {
    if (dp.getType() == DynamicPropertyType.SELECTION) {
      final List<NodeRef> nodesForProperty = getNodesForProperty(
        igRootNodeRef,
        dp,
        first,
        false
      );
      for (NodeRef nodeRef : nodesForProperty) {
        nodeService.setProperty(nodeRef, propertyQName, second);
      }
    } else if (dp.getType() == DynamicPropertyType.MULTI_SELECTION) {
      final List<NodeRef> nodesForProperty = getNodesForProperty(
        igRootNodeRef,
        dp,
        first,
        true
      );
      for (NodeRef nodeRef : nodesForProperty) {
        String oldProperty = (String) nodeService.getProperty(
          nodeRef,
          propertyQName
        );
        String newProperty = oldProperty.replace(first, second);
        nodeService.setProperty(nodeRef, propertyQName, newProperty);
      }
    }
  }

  private void deleteDynamicPropertyValues(
    NodeRef igRootNodeRef,
    QName propertyQName,
    DynamicProperty dp,
    String value
  ) {
    if (dp.getType() == DynamicPropertyType.SELECTION) {
      final List<NodeRef> nodesForProperty = getNodesForProperty(
        igRootNodeRef,
        dp,
        value,
        false
      );
      for (NodeRef nodeRef : nodesForProperty) {
        nodeService.removeProperty(nodeRef, propertyQName);
      }
    } else if (dp.getType() == DynamicPropertyType.MULTI_SELECTION) {
      final List<NodeRef> nodesForProperty = getNodesForProperty(
        igRootNodeRef,
        dp,
        value,
        true
      );
      for (NodeRef nodeRef : nodesForProperty) {
        String oldProperty = (String) nodeService.getProperty(
          nodeRef,
          propertyQName
        );
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

  /**
   * Injects the cache used to store dynamic properties per Interest Group. Wired by the Spring
   * container.
   *
   * @param dynamicPropertyCache the cache instance to use
   */
  public void setDynamicPropertyCache(
    SimpleCache<Long, List<DynamicProperty>> dynamicPropertyCache
  ) {
    this.dynamicPropertyCache = dynamicPropertyCache;
  }
}
