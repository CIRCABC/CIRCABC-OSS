package io.swagger.util;

import eu.cec.digit.circabc.model.DocumentModel;
import io.swagger.model.Node;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;

/**
 * Utility class to manage Nodes.
 * @author Alain Morlet
 */
public class NodeUtil {

  /**
   * Utility method that checks if the given node is of type CONTENT.
   */
  public static boolean isContent(Node n) {
    return n.getType().equals(ContentModel.TYPE_CONTENT.toString());
  }

  /**
   * Utility method used to check if a Node of type content is expired.
   */
  public static boolean isExpired(Node n) {
    boolean isExpired = false;
    if (isContent(n)) {
      String expirationDate = n
        .getProperties()
        .get(DocumentModel.PROP_EXPIRATION_DATE.getLocalName());
      if (expirationDate != null && !"".equals(expirationDate)) {
        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        try {
          Date parsedDate = Converter.convertStringToDate(expirationDate);
          Timestamp expireDate = new Timestamp(parsedDate.getTime());
          Timestamp currentTimestamp = new Timestamp(
            System.currentTimeMillis()
          );

          if (expireDate.before(currentTimestamp)) {
            isExpired = true;
          }
        } catch (ParseException e) {
          throw new AlfrescoRuntimeException("ParseException.", e);
        }
      }
    }
    return isExpired;
  }
}
