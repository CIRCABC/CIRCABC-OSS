/**
 *
 */
package io.swagger.model;

/** @author beaurpi */
public class PreferenceConfiguration {

    private LibraryPreferences library = new LibraryPreferences();

    /** @return the library */
    public LibraryPreferences getLibrary() {
        return library;
    }

    /** @param library the library to set */
    public void setLibrary(LibraryPreferences library) {
        this.library = library;
    }
}
