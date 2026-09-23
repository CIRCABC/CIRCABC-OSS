import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
} from '@angular/core';
import { LoginService } from 'app/core/login.service';
import { interval } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

/**
 * Invisible, standalone-style utility component (`cbc-ticket-validator`) that
 * keeps the current user's authentication ticket alive.
 *
 * The component renders whatever is defined in its template
 * (`ticket-validator.component.html`) and has no user-facing inputs or outputs.
 * Its sole responsibility is to periodically ask the {@link LoginService} to
 * validate the active session ticket so that expired or invalid sessions can be
 * detected while the application is running.
 *
 * Uses {@link ChangeDetectionStrategy.OnPush} because it renders no
 * template-bound state and does not rely on change detection.
 *
 * Key collaborator: {@link LoginService} (performs the actual ticket
 * validation against the backend).
 */
@Component({
  selector: 'cbc-ticket-validator',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './ticket-validator.component.html',
})
export class TicketValidatorComponent implements OnInit {
  /**
   * Service used to validate the current authentication ticket with the
   * backend. Injected via Angular's `inject()` API.
   */
  private readonly loginService = inject(LoginService);

  /**
   * Angular lifecycle hook invoked once after the component is initialized.
   *
   * Starts a recurring timer that fires every 10 minutes and, on each tick,
   * asks {@link LoginService.validateTicket} to check the current session
   * ticket. The outcome is logged to the console ("Ticket is valid" /
   * "Ticket is invalid").
   *
   * @returns {void}
   */
  ngOnInit() {
    interval(10 * 60 * 1000)
      .pipe(mergeMap(async () => this.loginService.validateTicket()))
      .subscribe((data) =>
        // eslint-disable-next-line no-console
        console.log(data ? 'Ticket is valid' : 'Ticket is invalid')
      );
  }
}
