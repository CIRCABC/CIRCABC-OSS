package eu.europa.ec.digit.circabc.rest.service.user;

import eu.europa.ec.digit.circabc.rest.aspect.ContentNotifyAspect;
import eu.europa.ec.digit.circabc.rest.service.customization.NodePreferencesService;
import eu.europa.ec.digit.circabc.rest.service.helper.ContentManager;
import eu.europa.ec.digit.circabc.rest.service.helper.MetadataManager;
import io.swagger.api.CircabcApi;
import io.swagger.model.Util;
import io.swagger.model.alfresco.UserModel;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.configuration.ConfigurableService;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.metadata.MetadataExtracter;
import org.alfresco.repo.content.metadata.MetadataExtracterRegistry;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.EqualsHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link UserDetailsService}.
 *
 * <p>This service handles read and write operations on CIRCABC user (Alfresco
 * {@code cm:person}) nodes. Its responsibilities include:
 *
 * <ul>
 *   <li>Reading user profile details (name, e-mail, phone, avatar) into a
 *       {@link UserDetails} POJO.</li>
 *   <li>Managing a user's avatar: uploading a new image, linking an existing
 *       repository image, removing a custom avatar and resolving the default
 *       avatar configured in the CIRCABC dictionary folder.</li>
 *   <li>Updating editable user properties and preferences (interface language,
 *       content filter language, signature, etc.).</li>
 * </ul>
 *
 * <p>Avatars are stored as content children linked through the
 * {@code cm:preferenceImage} child association and exposed through the
 * {@code cm:avatar} target association (backward compatible with the legacy JSF
 * web client). User preferences are persisted under the person's configurable
 * {@code app:preferences} node.
 *
 * <p>Collaborating Alfresco and CIRCABC services are injected by Spring through
 * {@code @Autowired} field injection.
 */
public class UserDetailsServiceImpl implements UserDetailsService {

  /**
   * Lazily resolved and cached reference to the default avatar node, shared
   * across all users that have not set a custom avatar.
   */
  private NodeRef defaultAvatar;

  @Autowired
  private CircabcApi circabcApi;

  @Autowired
  private NodePreferencesService nodePreferencesService;

  private final Log logger = LogFactory.getLog(UserDetailsServiceImpl.class);

  @Autowired
  private NodeService nodeService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private ContentService contentService;

  @Autowired
  private OwnableService ownableService;

  @Autowired
  private MetadataExtracterRegistry metadataExtracterRegistry;

  @Autowired
  private ConfigurableService configurableService;

  @Autowired
  private SearchService searchService;

  @Autowired
  private NamespaceService namespaceService;

  @Autowired
  private PersonService personService;

  @Autowired
  private MetadataManager metadataManager;

  @Autowired
  private ContentManager contentManager;

  /** Name of the root folder (under the CIRCABC dictionary) holding user configuration files. */
  private static final String AVATAR_CONFIG_ROOT = "users";

  /** Name of the configuration sub-folder / element type used for user preferences. */
  private static final String AVATAR_CONFIG_TYPE = "preferences";

  /** Name of the configuration file that holds the default avatar image. */
  private static final String AVATAR_CONFIG_ELEMENT = "avatar";

  /** XPath used to locate the {@code app:preferences} node under a user's configurations folder. */
  private static final String PREFERENCES_XPATH =
    NamespaceService.APP_MODEL_PREFIX + ":" + AVATAR_CONFIG_TYPE;

  /**
   * {@inheritDoc}
   *
   * <p>Resolves the default avatar lazily from the CIRCABC dictionary folder and
   * caches it. If the configuration file cannot be found or an error occurs, a
   * warning is logged and {@code null} may be returned.
   *
   * @return the default avatar node reference, or {@code null} if it could not be resolved
   */
  @Override
  public NodeRef getDefaultAvatar() {
    if (defaultAvatar == null) {
      final NodeRef rootRef = circabcApi.getCircabcNodeRef();
      try {
        defaultAvatar = nodePreferencesService.getDefaultConfigurationFile(
          rootRef,
          AVATAR_CONFIG_ROOT,
          AVATAR_CONFIG_TYPE,
          AVATAR_CONFIG_ELEMENT
        );
      } catch (Exception e) {
        if (logger.isErrorEnabled()) {
          logger.warn("Error getting the default avatar: " + e.getMessage(), e);
        }
      }

      if (defaultAvatar == null && logger.isWarnEnabled()) {
        logger.warn(
          "Impossible to found the default avatar. Check the presence of a file in the location: $circabc dictionary folder$/" +
            AVATAR_CONFIG_ROOT +
            "/" +
            AVATAR_CONFIG_TYPE +
            "/" +
            AVATAR_CONFIG_ELEMENT
        );
      }
    }

    return defaultAvatar;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns the person's custom avatar if one is linked through the
   * {@code cm:avatar} association, otherwise falls back to the default avatar.
   * Any error is logged and results in {@code null}.
   *
   * @param person the person node to query
   * @return the custom avatar, the default avatar, or {@code null} on error
   */
  @Override
  public NodeRef getAvatar(NodeRef person) {
    try {
      final List<AssociationRef> avatarAssocs = getAvatarAssociations(person);

      if (avatarAssocs == null || avatarAssocs.isEmpty()) {
        return getDefaultAvatar();
      } else {
        return avatarAssocs.get(0).getTargetRef();
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return null;
    }
  }

  private List<AssociationRef> getAvatarAssociations(final NodeRef person) {
    final List<AssociationRef> targetAssoc = nodeService.getTargetAssocs(
      person,
      ContentModel.ASSOC_AVATAR
    );

    if (
      logger.isWarnEnabled() && targetAssoc != null && targetAssoc.size() > 1
    ) {
      logger.warn(
        "Business model inconsistency: To many avatars found (" +
          targetAssoc.size() +
          ") for user " +
          nodeService.getProperty(person, ContentModel.PROP_USERNAME)
      );
    }

    return targetAssoc;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Reads the user name, first name, last name, e-mail, phone and avatar of
   * the given person node into a {@link UserDetails} POJO.
   *
   * @param person the person node to query
   * @return a populated {@link UserDetails} instance
   */
  @Override
  public UserDetails getUserDetails(NodeRef person) {
    UserDetails user = new UserDetails();
    user.setUserName(
      (String) nodeService.getProperty(person, ContentModel.PROP_USERNAME)
    );
    user.setFirstName(
      (String) nodeService.getProperty(person, ContentModel.PROP_FIRSTNAME)
    );
    user.setLastName(
      (String) nodeService.getProperty(person, ContentModel.PROP_LASTNAME)
    );
    user.setEmail(
      (String) nodeService.getProperty(person, ContentModel.PROP_EMAIL)
    );

    user.setPhone(
      (String) nodeService.getProperty(person, UserModel.PROP_PHONE)
    );

    user.setAvatar(getAvatar(person));

    return user;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Resolves the person node from its user name and delegates to
   * {@link #getUserDetails(NodeRef)}.
   *
   * @param username the user name to query
   * @return a populated {@link UserDetails} instance
   * @throws InvalidArgumentException if no person exists for the given user name
   */
  @Override
  public UserDetails getUserDetails(String username) {
    if (personService.personExists(username)) {
      return getUserDetails(personService.getPerson(username));
    } else {
      throw new InvalidArgumentException("User " + username + " not found");
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Uploads the given file, creates a content node for it and links it as the
   * person's avatar.
   *
   * @param person         the person node to update
   * @param avatarFileName the name of the avatar file
   * @param avatarFile      the avatar image file to upload
   * @return the created avatar content node reference
   */
  @Override
  public NodeRef updateAvatar(
    NodeRef person,
    String avatarFileName,
    File avatarFile
  ) {
    return updateAvatar(person, avatarFileName, avatarFile, null);
  }

  /**
   * Set the avatar of a person, either from an uploaded file or from an existing
   * repository image.
   *
   * <p>Ensures the {@code cm:preferences} aspect is present, removes any previous
   * preference image child node and {@code cm:avatar} association, then either
   * creates a new content node from {@code avatarFile} (when {@code imageRef} is
   * {@code null}) or reuses {@code imageRef}. The resulting image is granted guest
   * read access and wired up through the {@code cm:avatar} association.
   *
   * @param person         the person node to update
   * @param avatarFileName the name of the avatar file (used when creating new content)
   * @param avatarFile      the avatar image file to upload, or {@code null} when reusing {@code imageRef}
   * @param imageRef       an existing repository image to use as avatar, or {@code null} to create one
   * @return the avatar node reference that has been linked to the person
   */
  private NodeRef updateAvatar(
    final NodeRef person,
    final String avatarFileName,
    final File avatarFile,
    final NodeRef imageRef
  ) {
    // ensure cm:person has 'cm:preferences' aspect applied - as we want to add the avatar as
    // the child node of the 'cm:preferenceImage' association
    if (!nodeService.hasAspect(person, ContentModel.ASPECT_PREFERENCES)) {
      nodeService.addAspect(person, ContentModel.ASPECT_PREFERENCES, null);
    }

    // remove old image child node if we already have one
    final List<ChildAssociationRef> prefAssocs = getImagePrefAssociations(
      person
    );
    removeChildAssociations(prefAssocs);

    // wire up 'cm:avatar' target association - backward compatible with JSF web-client avatar
    // and allow to set an avatar from the library or user home.
    final List<AssociationRef> avatarAssocs = getAvatarAssociations(person);
    removeAssociations(person, avatarAssocs);

    final NodeRef imageToLink;
    if (imageRef == null) {
      imageToLink = createContent(
        person,
        avatarFileName,
        ContentModel.ASSOC_PREFERENCE_IMAGE,
        ContentModel.TYPE_CONTENT,
        avatarFile,
        false
      );
    } else {
      imageToLink = imageRef;
    }

    permissionService.setPermission(
      imageToLink,
      "guest",
      PermissionService.READ,
      true
    );

    nodeService.createAssociation(
      person,
      imageToLink,
      ContentModel.ASSOC_AVATAR
    );

    return imageToLink;
  }

  private void removeChildAssociations(final List<ChildAssociationRef> assocs) {
    if (assocs != null) {
      for (final ChildAssociationRef assoc : assocs) {
        nodeService.deleteNode(assoc.getChildRef());
      }
    }
  }

  private void removeAssociations(
    final NodeRef person,
    final List<AssociationRef> assocs
  ) {
    if (assocs != null) {
      for (final AssociationRef assoc : assocs) {
        nodeService.removeAssociation(
          person,
          assoc.getTargetRef(),
          assoc.getTypeQName()
        );
      }
    }
  }

  private NodeRef createContent(
    final NodeRef parent,
    final String name,
    final QName associationQname,
    final QName childTypeQName,
    final File file,
    final boolean applyNotification
  ) {
    return createContent(
      parent,
      name,
      associationQname,
      childTypeQName,
      file,
      null,
      applyNotification
    );
  }

  private NodeRef createContent(
    final NodeRef parent,
    final String name,
    final QName associationQname,
    final QName childTypeQName,
    final File file,
    final InputStream inputStream,
    final boolean applyNotification
  ) {
    final QName validAssocQName =
      associationQname == null ? ContentModel.ASSOC_CONTAINS : associationQname;
    final QName validTypeQName =
      childTypeQName == null ? ContentModel.TYPE_CONTENT : childTypeQName;

    // generate a valid and unique name
    final String validName = metadataManager.getValidUniqueName(
      parent,
      validAssocQName,
      name
    );

    // comput mimetype and encoding
    final String mimetype = contentManager.computeMimeType(validName);
    String encoding = "UTF-8";
    if (file != null) {
      encoding = contentManager.computeEncoding(file, mimetype);
    }

    // create a content
    final NodeRef contentRef = createContent(
      parent,
      validAssocQName,
      validTypeQName,
      validName,
      mimetype
    );

    // update node content
    updateContent(contentRef, file, inputStream, null, mimetype, encoding);

    if (applyNotification) {
      // Mandatory workaround to ensure that the notification trigger will be not called in another transaction
      nodeService.addAspect(
        contentRef,
        ContentNotifyAspect.ASPECT_CONTENT_NOTIFY,
        null
      );
    }

    // extract and set common properties
    final Map<QName, Serializable> extractedProps = extractedProperties(
      file,
      mimetype,
      encoding
    );
    //https://webgate.ec.europa.eu/CITnet/jira/browse/DIGIT-CIRCABC-2614
    extractedProps.remove(ContentModel.PROP_AUTHOR);
    extractedProps.remove(ContentModel.PROP_DESCRIPTION);
    extractedProps.remove(ContentModel.PROP_TITLE);
    updateProperties(contentRef, mimetype, extractedProps);

    return contentRef;
  }

  private void updateProperties(
    final NodeRef fileNodeRef,
    final String mimetype,
    final Map<QName, Serializable> extractedProps
  ) {
    final Serializable author = extractedProps.get(ContentModel.PROP_AUTHOR);
    final Serializable title = extractedProps.get(ContentModel.PROP_TITLE);
    final Serializable description = extractedProps.get(
      ContentModel.PROP_DESCRIPTION
    );

    // set the author aspect
    final Map<QName, Serializable> authorProps = Collections.singletonMap(
      ContentModel.PROP_AUTHOR,
      author
    );
    nodeService.addAspect(fileNodeRef, ContentModel.ASPECT_AUTHOR, authorProps);

    // take ownership (added for Improvement DIGIT-CIRCABC-1841)
    ownableService.takeOwnership(fileNodeRef);

    // apply the titled aspect - title and description
    final Map<QName, Serializable> titledProps = new HashMap<>(3, 1.0f);
    titledProps.put(ContentModel.PROP_TITLE, title);
    titledProps.put(ContentModel.PROP_DESCRIPTION, description);
    nodeService.addAspect(fileNodeRef, ContentModel.ASPECT_TITLED, titledProps);

    final boolean inlineEdit = metadataManager.isInlineEditable(mimetype);

    // apply the inlineeditable aspect
    if (inlineEdit) {
      final Map<QName, Serializable> editProps = Collections.singletonMap(
        ApplicationModel.PROP_EDITINLINE,
        (Serializable) Boolean.TRUE
      );
      nodeService.addAspect(
        fileNodeRef,
        ApplicationModel.ASPECT_INLINEEDITABLE,
        editProps
      );
    }
  }

  private void updateContent(
    final NodeRef contentRef,
    final File fileContent,
    final InputStream inputStream,
    final String stringContent,
    final String mimetype,
    final String encoding
  ) {
    // get a writer for the content and put the file
    QName propContent = Util.getPropContent(nodeService.getType(contentRef));

    final ContentWriter writer = contentService.getWriter(
      contentRef,
      propContent,
      true
    );

    // set the mimetype and encoding
    writer.setMimetype(mimetype);
    writer.setEncoding(encoding);
    if (fileContent != null) {
      writer.putContent(fileContent);
    } else if (inputStream != null) {
      writer.putContent(inputStream);
    } else {
      writer.putContent(stringContent == null ? "" : stringContent);
    }
  }

  private Map<QName, Serializable> extractedProperties(
    final File file,
    final String mimeType,
    final String encoding
  ) {
    Map<QName, Serializable> props = null;

    if (file == null) {
      return Collections.emptyMap();
    }

    try {
      //	Try and extract metadata from the file
      final ContentReader cr = new FileContentReader(file);
      cr.setMimetype(mimeType);
      cr.setEncoding(encoding);

      props = extractContentMetadata(cr);
    } catch (Exception ignore) {
      logger.error(
        "Error extracting metadata for file '" + file.getPath() + "'.",
        ignore
      );

      props = null;
    }

    if (props == null) {
      return Collections.emptyMap();
    } else {
      return props;
    }
  }

  /**
   * Extract metadata properties from the content of the given reader.
   *
   * <p>Looks up a {@link MetadataExtracter} registered for the reader's mimetype
   * and runs it. If no extracter is available, an empty map is returned. Any
   * extraction failure is logged and also results in the properties gathered so
   * far being returned.
   *
   * @param contentReader the content reader to extract metadata from
   * @return the extracted properties, or an empty map when no extracter matches the mimetype
   */
  public Map<QName, Serializable> extractContentMetadata(
    final ContentReader contentReader
  ) {
    final String mimetype = contentReader.getMimetype();

    // look for a transformer
    final MetadataExtracter extracter = metadataExtracterRegistry.getExtracter(
      mimetype
    );
    if (extracter == null) {
      return Collections.emptyMap();
    } else {
      final Map<QName, Serializable> properties = HashMap.newHashMap(10);

      try {
        // we have a transformer, so do it
        extracter.extract(contentReader, properties);
      } catch (Exception silentFailure) {
        // it failed
        logger.warn(
          "Metadata extraction failed: \n" +
            "   mimetype: " +
            mimetype +
            "\n" +
            "   extracter: " +
            extracter.getClass()
        );
      }

      return properties;
    }
  }

  private NodeRef createContent(
    final NodeRef parent,
    final QName associationQname,
    final QName typeQname,
    final String name,
    String mimetype
  ) {
    //	 guess a mimetype based on the filename
    QName propContent = Util.getPropContent(typeQname);

    final ContentData contentData = new ContentData(
      null,
      mimetype,
      0L,
      "UTF-8"
    );
    final Map<QName, Serializable> properties = HashMap.newHashMap(2);
    properties.put(ContentModel.PROP_NAME, name);
    properties.put(propContent, contentData);

    QName associationNameQName = QName.createQName(
      ContentModel.PROP_NAME.getNamespaceURI(),
      name
    );

    final ChildAssociationRef assocRef = nodeService.createNode(
      parent,
      associationQname,
      associationNameQName,
      typeQname,
      properties
    );

    return assocRef.getChildRef();
  }

  /**
   * {@inheritDoc}
   *
   * <p>Links the existing repository node {@code imageRef} as the person's avatar,
   * using the image's {@code cm:name} as the avatar file name.
   *
   * @param person   the person node to update
   * @param imageRef the existing repository image to use as avatar
   */
  @Override
  public void updateAvatar(NodeRef person, NodeRef imageRef) {
    updateAvatar(
      person,
      (String) nodeService.getProperty(imageRef, ContentModel.PROP_NAME),
      null,
      imageRef
    );
  }

  /**
   * {@inheritDoc}
   *
   * <p>Removes any custom avatar by deleting the preference image child node and
   * the {@code cm:avatar} association, so that the person falls back to the
   * default avatar.
   *
   * @param person the person node to update
   */
  @Override
  public void removeAvatar(NodeRef person) {
    // remove old image child node if we already have one
    final List<ChildAssociationRef> prefAssocs = getImagePrefAssociations(
      person
    );
    removeChildAssociations(prefAssocs);

    // remove avatar associations
    final List<AssociationRef> avatarAssocs = getAvatarAssociations(person);
    removeAssociations(person, avatarAssocs);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Computes the changed properties and preferences for the person and applies
   * only the modified ones. This method does not handle avatar changes; use
   * {@link #updateAvatar(NodeRef, String, File)}, {@link #updateAvatar(NodeRef, NodeRef)}
   * or {@link #removeAvatar(NodeRef)} for that.
   *
   * @param personRef   the person node reference (used for the security check; must equal
   *                    {@code userDetails.getNodeRef()})
   * @param userDetails the details to apply to the person
   * @throws IllegalArgumentException if {@code userDetails.getNodeRef()} does not equal {@code personRef}
   */
  @Override
  public void updateUserDetails(NodeRef personRef, UserDetails userDetails) {
    if (!userDetails.getNodeRef().equals(personRef)) {
      throw new IllegalArgumentException(
        "User details node reference (" +
          userDetails.getNodeRef() +
          ") must be equals to the person reference (" +
          personRef +
          ")"
      );
    }

    final Map<QName, Serializable> propertiesToUpdate = getUpdatedProperties(
      personRef,
      userDetails
    );
    final Map<QName, Serializable> preferencesToUpdate = getUpdatedPreferences(
      personRef,
      userDetails
    );

    if (propertiesToUpdate.size() > 0) {
      nodeService.addProperties(userDetails.getNodeRef(), propertiesToUpdate);
      final NodeRef prefRef = createUserPreferencesRef(
        userDetails.getNodeRef()
      );

      nodeService.addProperties(prefRef, preferencesToUpdate);
    }
  }

  private Map<QName, Serializable> getUpdatedPreferences(
    NodeRef personRef,
    UserDetails userDetails
  ) {
    Set<QName> preferences = Set.of(
      UserService.PREF_INTERFACE_LANGUAGE,
      UserService.PREF_CONTENT_FILTER_LANGUAGE,
      UserService.PREF_SIGNATURE
    );

    Map<QName, Serializable> updatedPreferences = new HashMap<>();
    Map<QName, Serializable> userPreferences = getUserPreferences(personRef);

    for (QName qname : preferences) {
      final Serializable originalValue = userPreferences.get(qname);
      if (qname.equals(UserService.PREF_INTERFACE_LANGUAGE)) {
        update(
          originalValue,
          userDetails.getUserInterfaceLanguage(),
          qname,
          updatedPreferences
        );
      }

      if (qname.equals(UserService.PREF_CONTENT_FILTER_LANGUAGE)) {
        update(
          originalValue,
          userDetails.getContentFilterLanguage(),
          qname,
          updatedPreferences
        );
      }

      if (qname.equals(UserService.PREF_SIGNATURE)) {
        update(
          originalValue,
          userDetails.getSignature(),
          qname,
          updatedPreferences
        );
      }
    }

    return updatedPreferences;
  }

  private void update(
    Serializable originalValue,
    Serializable newValue,
    QName qname,
    Map<QName, Serializable> map
  ) {
    if (!EqualsHelper.nullSafeEquals(originalValue, newValue)) {
      map.put(qname, newValue);
    } else {
      map.remove(qname);
    }
  }

  private Map<QName, Serializable> getUpdatedProperties(
    NodeRef personRef,
    UserDetails userDetails
  ) {
    Map<QName, Serializable> updatedProperties = new HashMap<>();
    Map<QName, Serializable> userProperties = getUserProperties(personRef);

    updateProperty(
      updatedProperties,
      userProperties,
      ContentModel.PROP_EMAIL,
      userDetails.getEmail()
    );
    updateProperty(
      updatedProperties,
      userProperties,
      ContentModel.PROP_FIRSTNAME,
      userDetails.getFirstName()
    );
    updateProperty(
      updatedProperties,
      userProperties,
      ContentModel.PROP_LASTNAME,
      userDetails.getLastName()
    );
    updateProperty(
      updatedProperties,
      userProperties,
      UserModel.PROP_DESCRIPTION,
      userDetails.getDescription()
    );
    updateProperty(
      updatedProperties,
      userProperties,
      UserModel.PROP_FAX,
      userDetails.getFax()
    );
    updateProperty(
      updatedProperties,
      userProperties,
      UserModel.PROP_ORGDEPNUMBER,
      userDetails.getOrganisation()
    );
    updateProperty(
      updatedProperties,
      userProperties,
      UserModel.PROP_PHONE,
      userDetails.getPhone()
    );
    updateProperty(
      updatedProperties,
      userProperties,
      UserModel.PROP_POSTAL_ADDRESS,
      userDetails.getPostalAddress()
    );
    updateProperty(
      updatedProperties,
      userProperties,
      UserModel.PROP_TITLE,
      userDetails.getTitle()
    );
    updateProperty(
      updatedProperties,
      userProperties,
      UserModel.PROP_URL,
      userDetails.getUrl()
    );
    updateProperty(
      updatedProperties,
      userProperties,
      UserModel.PROP_GLOBAL_NOTIFICATION,
      userDetails.getGlobalNotification()
    );
    updateProperty(
      updatedProperties,
      userProperties,
      UserModel.PROP_VISISBILITY,
      userDetails.getVisibility()
    );

    return updatedProperties;
  }

  private void updateProperty(
    Map<QName, Serializable> updated,
    Map<QName, Serializable> original,
    QName qname,
    Serializable newValue
  ) {
    update(original.get(qname), newValue, qname, updated);
  }

  private Map<QName, Serializable> getUserProperties(NodeRef personRef) {
    return nodeService.getProperties(personRef);
  }

  private Map<QName, Serializable> getUserPreferences(final NodeRef person) {
    final NodeRef preferenceRef = getOrCreateUserPreferencesRef(person, false);

    if (preferenceRef == null) {
      return new HashMap<>();
    } else {
      return nodeService.getProperties(preferenceRef);
    }
  }

  private List<ChildAssociationRef> getImagePrefAssociations(
    final NodeRef person
  ) {
    final List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(
      person,
      ContentModel.ASSOC_PREFERENCE_IMAGE,
      RegexQNamePattern.MATCH_ALL
    );

    if (
      logger.isWarnEnabled() && childAssocs != null && childAssocs.size() > 1
    ) {
      logger.warn(
        "Business model inconsistency: To many prefered images found (" +
          childAssocs.size() +
          ") for user " +
          nodeService.getProperty(person, ContentModel.PROP_USERNAME)
      );
    }
    return childAssocs;
  }

  private NodeRef createUserPreferencesRef(final NodeRef person) {
    return getOrCreateUserPreferencesRef(person, true);
  }

  private NodeRef getOrCreateUserPreferencesRef(
    final NodeRef person,
    final boolean createIfMissing
  ) {
    NodeRef prefRef = null;

    if (!nodeService.hasAspect(person, ApplicationModel.ASPECT_CONFIGURABLE)) {
      if (createIfMissing) {
        // create the configuration folder for this Person node
        configurableService.makeConfigurable(person);
      }
    } else {
      // target of the assoc is the configurations folder ref
      final NodeRef configRef = configurableService.getConfigurationFolder(
        person
      );
      if (configRef == null) {
        logger.error(
          "Unable to find associated 'configurations' folder for node: " +
            person
        );
      } else {
        final List<NodeRef> nodes = searchService.selectNodes(
          configRef,
          PREFERENCES_XPATH,
          null,
          namespaceService,
          false
        );

        if (nodes.size() == 1) {
          prefRef = nodes.get(0);
        } else {
          //	create the preferences Node for this user
          final ChildAssociationRef childRef = nodeService.createNode(
            configRef,
            ContentModel.ASSOC_CONTAINS,
            QName.createQName(
              NamespaceService.APP_MODEL_1_0_URI,
              AVATAR_CONFIG_TYPE
            ),
            ContentModel.TYPE_CMOBJECT
          );

          prefRef = childRef.getChildRef();
        }
      }
    }

    return prefRef;
  }
}
