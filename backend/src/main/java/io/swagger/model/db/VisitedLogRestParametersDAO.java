package io.swagger.model.db;

/**
 * Data access parameter holder for "visited log" database operations.
 *
 * <p>This simple data-carrier bundles the identifying parameters required to
 * query or persist visited-log records: the name of the user who performed the
 * visit and the numeric identifier of the associated log entry (or the node/item
 * that was visited). It carries no behaviour beyond storing and exposing these
 * two values.
 */
public class VisitedLogRestParametersDAO {

  /** Name of the user associated with the visited-log record. */
  private String username;

  /** Numeric identifier of the visited-log record; defaults to {@code 0}. */
  private int id = 0;

  /**
   * Creates a new parameter holder with the supplied username and identifier.
   *
   * @param username the name of the user associated with the visited-log record
   * @param id the numeric identifier of the visited-log record
   */
  public VisitedLogRestParametersDAO(String username, int id) {
    super();
    this.username = username;
    this.id = id;
  }

  /**
   * Returns the name of the user associated with the visited-log record.
   *
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sets the name of the user associated with the visited-log record.
   *
   * @param username the username to set
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Returns the numeric identifier of the visited-log record.
   *
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * Sets the numeric identifier of the visited-log record.
   *
   * @param id the id to set
   */
  public void setId(int id) {
    this.id = id;
  }
}
