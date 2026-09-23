import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TranslocoModule } from '@jsverse/transloco';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';
import { ControlMessageComponent } from 'app/shared/control-message/control-message.component';
import { DataCyDirective } from 'app/shared/directives/data-cy.directive';
import { FocusDirective } from 'app/shared/directives/focus.directive';
import { SetTitlePipe } from 'app/shared/pipes/set-title.pipe';
import { SpinnerComponent } from 'app/shared/spinner/spinner.component';
import { environment } from 'environments/environment';

/**
 * Standalone Angular component (`cbc-login`) that renders the CIRCABC login page.
 *
 * It presents a reactive username/password form (with inline validation
 * messages, a submit spinner and the current CIRCABC release label) and drives
 * the authentication flow. On successful authentication the user is forwarded
 * to their intended destination; on failure the appropriate error/denied state
 * is surfaced to the template. If a non-guest (already authenticated) user
 * reaches this page they are immediately redirected to `/me`.
 *
 * Key collaborators:
 * - {@link LoginService} — performs authentication and reports guest status.
 * - {@link RedirectionService} — resolves the post-login redirect target.
 * - {@link Router} — used to redirect already-authenticated users.
 * - {@link FormBuilder} — builds the reactive login form.
 */
@Component({
  selector: 'cbc-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  preserveWhitespaces: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    DataCyDirective,
    FocusDirective,
    ControlMessageComponent,
    SpinnerComponent,
    RouterLink,
    SetTitlePipe,
    TranslocoModule,
  ],
})
export class LoginComponent implements OnInit {
  /** Service used to authenticate the user and query guest status. */
  private readonly loginService = inject(LoginService);
  /** Angular router used to navigate away when the user is already logged in. */
  private readonly router = inject(Router);
  /** Form builder used to construct the reactive login form. */
  private readonly fb = inject(FormBuilder);
  /** Service that resolves and performs the redirect after a successful login. */
  private readonly redirectionService = inject(RedirectionService);

  /** Current CIRCABC release identifier, displayed on the login page. */
  public circabcRelease = environment.circabcRelease;
  /** Signal set to `true` when an unexpected error occurs during login. */
  public loginError = signal(false);
  /** Signal set to `true` when credentials are valid in form but authentication is refused. */
  public loginDenied = signal(false);
  /** Signal set to `true` while an authentication request is in progress. */
  public loggingIn = signal(false);
  /** Signal set to `true` once the user has attempted to submit the form, used to trigger validation display. */
  public submitClicked = signal(false);

  /**
   * Reactive, non-nullable login form holding the `username` and `password`
   * controls, both of which are required.
   */
  public loginForm = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  /**
   * Redirects an already-authenticated (non-guest) user to their profile
   * page (`/me`) so the login form is only shown to guests.
   */

  ngOnInit() {
    if (!this.loginService.isGuest()) {
      void this.router.navigate(['/me']);
    }
  }

  /**
   * Handles the login form submission.
   *
   * Marks the form as submitted, and when it is valid, attempts to
   * authenticate via {@link LoginService}. On success it delegates to
   * {@link RedirectionService} to navigate to the intended destination; if
   * authentication is refused it sets {@link loginDenied}. Any thrown error is
   * caught, logged, flagged via {@link loginError} and the form is reset. The
   * {@link loggingIn} signal reflects the in-progress state throughout.
   *
   * @returns A promise that resolves once the login attempt (and any
   * subsequent redirection) has completed.
   */
  public async login() {
    this.submitClicked.set(true);
    if (this.loginForm.valid) {
      this.loggingIn.set(true);
      this.loginError.set(false);
      this.loginDenied.set(false);
      try {
        const response = await this.loginService.login(
          this.loginForm.getRawValue()
        );
        if (response) {
          await this.redirectionService.redirect();
        } else {
          this.loginDenied.set(true);
        }
      } catch (error) {
        this.loginError.set(true);
        console.error(error);
        this.loginForm.reset();
      }
      this.loggingIn.set(false);
    }
  }
}
