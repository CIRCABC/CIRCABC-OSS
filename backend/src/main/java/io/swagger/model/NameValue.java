package io.swagger.model;

/**
 * Simple immutable-style key/value pair model.
 *
 * <p>Holds a {@code name} and its associated {@code value}, both represented as
 * strings. It is used as a lightweight container for passing named textual
 * values (for example configuration entries, form parameters or generic
 * attribute pairs) across the API layer.
 */
public class NameValue {

  /** The name (key) part of the pair. */
  private String name;

  /** The value associated with {@link #name}. */
  private String value;

  /**
   * Creates a new pair with the given name and value.
   *
   * @param name the name (key) of the pair
   * @param value the value associated with the name
   */
  public NameValue(String name, String value) {
    super();
    this.name = name;
    this.value = value;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }
}
