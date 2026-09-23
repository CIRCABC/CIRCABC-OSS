import {
  ChangeDetectionStrategy,
  Component,
  inject,
  resource,
} from '@angular/core';

import { RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { EventItemDefinition, UserService } from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { UiMessageService } from 'app/core/message/ui-message.service';
import { getFullDate } from 'app/core/util';
import { DatePipe } from 'app/shared/pipes/date.pipe';
import { TimePipe } from 'app/shared/pipes/time.pipe';

/**
 * Standalone component that renders the list of upcoming events for the
 * currently authenticated user in their personal ("me") area.
 *
 * On initialization it retrieves the user's future events (from the current
 * date onwards) via {@link UserService} and exposes them through the
 * {@link UserEventsComponent.events} field, which the template iterates over
 * to display each event's date and time (formatted with the `DatePipe` and
 * `TimePipe`) and a router link to the related content.
 *
 * Key collaborators:
 * - {@link UserService}: fetches the user's events for a given period.
 * - {@link LoginService}: resolves the current user's identifier.
 * - {@link UiMessageService}: surfaces error messages to the user when
 *   loading events fails.
 */
@Component({
  selector: 'cbc-user-events',
  templateUrl: './user-events.component.html',
  styleUrl: './user-events.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe, TimePipe, TranslocoModule],
})
export class UserEventsComponent {
  /** API client used to retrieve the current user's events. */
  private readonly userService = inject(UserService);
  /** Service used to display error notifications in the UI. */
  private readonly uiMessageService = inject(UiMessageService);
  /** Service providing the currently authenticated user's identity. */
  private readonly loginService = inject(LoginService);

  /**
   * The current user's upcoming events (from the current date onwards), loaded
   * reactively. `undefined` until the request resolves. Signal-backed so the
   * OnPush view re-renders when the async load completes.
   *
   * Any error raised while loading is caught: its JSON body is parsed and,
   * when a `message` field is present, it is forwarded to the
   * {@link UiMessageService} as an error notification.
   */
  public readonly events = resource<EventItemDefinition[] | undefined, unknown>(
    {
      loader: async () => {
        const exactDate: Date = new Date();
        try {
          return await this.userService.getUserEventsPeriodAsync({
            userId: this.getUserId(),
            exactDate: getFullDate(exactDate),
            period: 'Future',
          });
        } catch (error) {
          const jsonError = JSON.parse(
            (error as { _body: string })._body
          ) as Record<string, string>;
          if (jsonError && 'message' in jsonError) {
            this.uiMessageService.addErrorMessage(jsonError.message);
          }
          return undefined;
        }
      },
    }
  ).value;

  /**
   * Resolves the identifier of the currently authenticated user.
   *
   * @returns The current user's username as provided by the
   * {@link LoginService}.
   */
  private getUserId(): string {
    return this.loginService.getCurrentUsername();
  }
}
