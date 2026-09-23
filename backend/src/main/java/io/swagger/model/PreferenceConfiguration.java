/**
 *
 */
package io.swagger.model;

/**
 * Domain model holding a user's (or group's) preference configuration.
 *
 * <p>This DTO aggregates the different categories of preferences exposed by the CIRCABC REST API.
 * Currently it groups the library-related preferences via {@link LibraryPreferences}. It is
 * typically serialized to / deserialized from JSON when preferences are read or updated through the
 * REST layer.
 *
 * @author beaurpi
 */
public class PreferenceConfiguration {

  /** The library-related preferences; never {@code null}, initialized with defaults. */
  private LibraryPreferences library = new LibraryPreferences();

  /**
   * Returns the library preferences.
   *
   * @return the library preferences
   */
  public LibraryPreferences getLibrary() {
    return library;
  }

  /**
   * Sets the library preferences.
   *
   * @param library the library preferences to set
   */
  public void setLibrary(LibraryPreferences library) {
    this.library = library;
  }
}
