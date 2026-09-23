import { UiMessageLevel } from 'app/core/message/ui-message-level';

/**
 * Represents a single user-facing notification/message to be displayed in the
 * CIRCABC UI (e.g. success, info, warning or error banners).
 *
 * An instance carries the severity level, the textual content and the display
 * behaviour (whether it should close automatically and for how long it should
 * remain visible). It is typically produced and managed by the UI message
 * service and rendered by the message notification components.
 */
export class UiMessage {
  /** Severity/type of the message, driving its visual styling. */
  public level: UiMessageLevel;
  /** Textual content of the message shown to the user. */
  public body: string;
  /** Whether the message is currently active (visible). */
  public active: boolean;
  /** Whether the message should dismiss itself automatically after {@link displayTime}. Defaults to `false`. */
  public autoclose = false;
  /** Duration, in seconds, the message stays visible when {@link autoclose} is enabled. Defaults to `5`. */
  public displayTime = 5;

  /**
   * Creates a new UI message.
   *
   * @param level The severity level of the message.
   * @param content The text to display to the user (assigned to {@link body}).
   * @param autoclose Optional flag enabling automatic dismissal; when truthy it
   * sets {@link autoclose}. Defaults to `false`.
   * @param displayTime Optional visibility duration in seconds used when
   * auto-closing; when provided it overrides the default of `5`.
   */
  public constructor(
    level: UiMessageLevel,
    content: string,
    autoclose?: boolean,
    displayTime?: number
  ) {
    this.level = level;
    this.body = content;
    this.active = true;
    if (autoclose) {
      this.autoclose = autoclose;
    }
    if (displayTime) {
      this.displayTime = displayTime;
    }
  }
}
