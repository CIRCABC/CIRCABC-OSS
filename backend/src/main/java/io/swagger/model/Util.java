package io.swagger.model;

import io.swagger.model.alfresco.CircabcModel;
import io.swagger.model.alfresco.DocumentModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;

/**
 * Utility helpers for the Swagger-generated model classes.
 *
 * <p>This is a stateless, non-instantiable utility class that groups small helper
 * methods shared by the model layer. It provides support for rendering model
 * objects into indented string representations (used by generated {@code toString()}
 * implementations) and for resolving the appropriate Alfresco content property
 * {@link QName} for a given node type.
 */
public class Util {

  /**
   * Prevents instantiation of this utility class.
   *
   * @throws IllegalStateException always, since the class is not meant to be instantiated
   */
  private Util() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   *
   * @param o the object to render; may be {@code null}
   * @return the string {@code "null"} if {@code o} is {@code null}, otherwise the object's
   *     {@code toString()} value with every line break followed by four spaces of indentation
   */
  static String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

  /**
   * Resolves the Alfresco property {@link QName} that holds the binary content for a given
   * node type.
   *
   * <p>CIRCABC defines custom content types whose content is stored under type-specific
   * properties rather than the default {@link ContentModel#PROP_CONTENT}. This method maps
   * such types to their dedicated content property, falling back to the standard content
   * property for all other types.
   *
   * @param typeQName the qualified name of the node's content type
   * @return {@link CircabcModel#PROP_CONTENT} for customization content types,
   *     {@link DocumentModel#PROP_CONTENT} for hidden attachment content types, or
   *     {@link ContentModel#PROP_CONTENT} for any other type
   */
  public static QName getPropContent(QName typeQName) {
    if (CircabcModel.TYPE_CUSTOMIZATION_CONTENT.equals(typeQName)) {
      return CircabcModel.PROP_CONTENT;
    } else if (
      DocumentModel.TYPE_HIDDEN_ATTACHEMENT_CONTENT.equals(typeQName)
    ) {
      return DocumentModel.PROP_CONTENT;
    } else {
      return ContentModel.PROP_CONTENT;
    }
  }
}
