/**
 *
 */
package io.swagger.model;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Container DTO representing a batch of users to be imported in bulk.
 *
 * <p>This model wraps a list of {@link BulkImportUser} entries and is bound to the {@code <members>}
 * XML root element (each entry mapped to a {@code <member>} element). It is used as the payload for
 * bulk user import operations, allowing multiple user records to be submitted and processed in a
 * single request.
 *
 * @author schwerr
 */
@XmlRootElement(name = "members")
public class BulkImportUsers {

  /** The collection of individual user records to import; {@code null} when unset. */
  List<BulkImportUser> userData = null;

  /**
   * Returns the list of users to be imported.
   *
   * @return the collection of {@link BulkImportUser} records, or {@code null} if none has been set
   */
  public List<BulkImportUser> getUserData() {
    return userData;
  }

  /**
   * Sets the list of users to be imported.
   *
   * @param userData the collection of {@link BulkImportUser} records to import
   */
  @XmlElement(name = "member")
  public void setUserData(List<BulkImportUser> userData) {
    this.userData = userData;
  }
}
