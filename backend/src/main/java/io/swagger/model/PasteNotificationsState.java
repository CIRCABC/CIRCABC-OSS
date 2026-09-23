package io.swagger.model;

/**
 * Immutable-style value object describing the notification "paste" capabilities
 * available for a given target (typically a node or folder) during a
 * copy/paste operation.
 *
 * <p>It carries two independent flags:
 * <ul>
 *   <li>{@code pasteEnabled} — whether pasting notification settings for the
 *       current item is allowed;</li>
 *   <li>{@code pasteAllEnabled} — whether pasting notification settings to all
 *       applicable items (a bulk/recursive paste) is allowed.</li>
 * </ul>
 *
 * <p>Instances are typically produced by the service layer and serialized into
 * the REST JSON response consumed by the frontend.
 */
public class PasteNotificationsState {

  /** Whether pasting notification settings for the current item is enabled. */
  boolean pasteEnabled;
  /** Whether pasting notification settings to all applicable items is enabled. */
  boolean pasteAllEnabled;

  /**
   * Creates a new state holder for the notification paste capabilities.
   *
   * @param pasteEnabled whether a single-item paste of notification settings is
   *     allowed
   * @param pasteAllEnabled whether a bulk/recursive paste of notification
   *     settings is allowed
   */
  public PasteNotificationsState(
    boolean pasteEnabled,
    boolean pasteAllEnabled
  ) {
    super();
    this.pasteEnabled = pasteEnabled;
    this.pasteAllEnabled = pasteAllEnabled;
  }

  /**
   * Indicates whether a single-item paste of notification settings is enabled.
   *
   * @return the pasteEnabled flag
   */
  public boolean isPasteEnabled() {
    return pasteEnabled;
  }

  /**
   * Indicates whether a bulk/recursive paste of notification settings is
   * enabled.
   *
   * @return the pasteAllEnabled flag
   */
  public boolean isPasteAllEnabled() {
    return pasteAllEnabled;
  }
}
