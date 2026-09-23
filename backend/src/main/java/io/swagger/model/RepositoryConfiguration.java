package io.swagger.model;

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

import java.util.Date;

/**
 * Domain model holding configuration data about a repository.
 *
 * <p>Instances carry the repository's identifying {@code name} together with the {@code
 * registrationDate} on which the repository was registered. Equality and hashing are based solely
 * on the {@code name}, so two configurations with the same name are treated as equal regardless of
 * their registration date.
 *
 * @author schwerr
 */
public class RepositoryConfiguration {

  /** The unique name identifying the repository; used as the basis for equality and hashing. */
  private String name = null;

  /** The date on which the repository was registered; defaults to the creation time. */
  private Date registrationDate = new Date();

  /**
   * Compares this configuration with another object for equality. Two {@code
   * RepositoryConfiguration} instances are considered equal when their {@link #name} values are
   * equal.
   *
   * @param obj the object to compare with this configuration.
   * @return {@code true} if the given object is a {@code RepositoryConfiguration} with an equal
   *     name; {@code false} otherwise.
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof RepositoryConfiguration other) {
      return this.name.equals(other.getName());
    }
    return false;
  }

  /**
   * Returns a hash code for this configuration, derived from its {@link #name}, consistent with
   * {@link #equals(Object)}.
   *
   * @return the hash code of the repository name.
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return name.hashCode();
  }

  /**
   * Gets the value of the name
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the value of the name
   *
   * @param name the name to set.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the value of the registrationDate
   *
   * @return the registrationDate
   */
  public Date getRegistrationDate() {
    return registrationDate;
  }

  /**
   * Sets the value of the registrationDate
   *
   * @param registrationDate the registrationDate to set.
   */
  public void setRegistrationDate(Date registrationDate) {
    this.registrationDate = registrationDate;
  }
}
