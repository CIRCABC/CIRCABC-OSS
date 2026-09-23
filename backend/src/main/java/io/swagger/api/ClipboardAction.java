package io.swagger.api;

/**
 * Enumerates the operations that can be performed when transferring content
 * items via the clipboard.
 *
 * <p>Each constant carries a stable integer code ({@link #getValue()}) intended
 * for serialization or interchange with clients, decoupling the wire
 * representation from the enum's ordinal.
 *
 * <ul>
 *   <li>{@link #COPY} — duplicate the source item at the target location.</li>
 *   <li>{@link #MOVE} — relocate the source item to the target location.</li>
 *   <li>{@link #LINK} — create a link/reference to the source item at the
 *       target location.</li>
 * </ul>
 */
public enum ClipboardAction {
  COPY(0),
  MOVE(1),
  LINK(2);

  /** Stable integer code associated with the action, used for interchange. */
  private final int value;

  /**
   * Creates a clipboard action with its associated integer code.
   *
   * @param value the stable integer code representing this action
   */
  ClipboardAction(int value) {
    this.value = value;
  }

  /**
   * Returns the stable integer code associated with this action.
   *
   * @return the integer code representing this clipboard action
   */
  public int getValue() {
    return value;
  }
}
