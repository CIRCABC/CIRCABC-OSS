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
package eu.europa.ec.digit.circabc.rest.service.notification;

/**
 * Enumeration representing the global notification status of an entity (for example a user or a
 * group), used to determine whether notifications are switched on or off.
 *
 * <p>Each constant exposes its equivalent boolean representation through {@link #toBoolean()},
 * allowing the status to be converted to the primitive flag typically stored or transferred by the
 * REST/notification layer.
 *
 * @author Yanick Pignot
 */
public enum GlobalNotificationStatus {
  /** Notifications are enabled; {@link #toBoolean()} returns {@code true}. */
  ENABLED {
    /**
     * {@inheritDoc}
     *
     * @return {@code true}, indicating that notifications are enabled
     */
    public boolean toBoolean() {
      return true;
    }
  },
  /** Notifications are disabled; {@link #toBoolean()} returns {@code false}. */
  DISABLED {
    /**
     * {@inheritDoc}
     *
     * @return {@code false}, indicating that notifications are disabled
     */
    public boolean toBoolean() {
      return false;
    }
  };

  /**
   * Returns the boolean representation of this notification status.
   *
   * @return {@code true} if notifications are enabled, {@code false} otherwise
   */
  public abstract boolean toBoolean();
}
