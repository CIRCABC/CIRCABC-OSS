import {
  HttpClient,
  HttpErrorResponse,
  HttpHeaders,
} from '@angular/common/http';
import { Injector, inject, Service } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { User, UserService } from 'app/core/generated/circabc';
import { ALF_BASE_PATH, CBC_BASE_PATH } from 'app/core/variables';
import { firstValueFrom } from 'rxjs';

/**
 * Shape of the JSON payload returned by the CIRCABC `/login` endpoint,
 * wrapping the authentication ticket issued by the backend.
 */
interface TicketResponse {
  data: {
    ticket: string;
  };
}

/**
 * Application-wide authentication service.
 *
 * Responsible for authenticating users against the CIRCABC/Alfresco backend,
 * persisting the resulting session ticket and user profile in `sessionStorage`,
 * validating existing tickets, and clearing the session on logout.
 *
 * Key collaborators:
 * - {@link UserService} (generated CIRCABC API client) to fetch the
 *   authenticated user's profile.
 * - {@link HttpClient} for direct calls to the Alfresco/CIRCABC login
 *   endpoints (resolved lazily via {@link Injector} to avoid circular
 *   dependencies with HTTP interceptors).
 * - {@link TranslocoService} to switch the active UI language according to the
 *   user's preference.
 *
 * Registered as a root-level singleton (`providedIn: 'root'`).
 */
@Service()
export class LoginService {
  private readonly injector = inject(Injector);
  private readonly userService = inject(UserService);

  /** Base URL of the Alfresco backend, used for ticket validation/logout. */
  private readonly alfrescoURL = inject(ALF_BASE_PATH);
  /** Base URL of the CIRCABC backend, used for the login endpoint. */
  private readonly circabcURL = inject(CBC_BASE_PATH);

  /**
   * Validates and sanitizes ticket data to prevent header injection attacks
   * @param ticket - The ticket string to validate
   * @returns Sanitized ticket or empty string if invalid
   */
  private validateAndSanitizeTicket(ticket: string): string {
    if (!ticket || typeof ticket !== 'string') {
      return '';
    }

    // Remove any CRLF characters that could be used for header injection
    // NOSONAR: Control characters intentionally matched for security sanitization
    // biome-ignore lint/suspicious/noControlCharactersInRegex: Security sanitization
    const sanitized = ticket.replaceAll(/[\x00-\x1f\x7f-\x9f]/g, ''); // eslint-disable-line no-control-regex -- NOSONAR

    // Basic validation: ticket should be alphanumeric with allowed characters
    const ticketPattern = /^[a-zA-Z0-9+/=_-]+$/;
    if (!ticketPattern.test(sanitized)) {
      // eslint-disable-next-line no-console
      console.warn('Invalid ticket format detected');
      return '';
    }

    return sanitized;
  }

  /**
   * Coerces a possibly `null`/`undefined` value into a string.
   *
   * @param value - The value to convert.
   * @param defaultValue - Value returned when `value` is not a string. Defaults to an empty string.
   * @returns The original string, or `defaultValue` when `value` is not a string.
   */
  private static convert2String(
    value: string | null | undefined,
    defaultValue = ''
  ): string {
    if (typeof value === 'string') {
      return value;
    }
    return defaultValue;
  }

  /**
   * Persists the authentication ticket and the user's profile fields into
   * `sessionStorage`.
   *
   * @param ticket - The authentication ticket to store.
   * @param user - The user profile whose fields are written to session storage.
   */
  private static setlocalStorage(ticket: string, user: User): void {
    sessionStorage.setItem('ticket', ticket);
    sessionStorage.setItem(
      'user.userId',
      LoginService.convert2String(user.userId)
    );
    sessionStorage.setItem(
      'user.email',
      LoginService.convert2String(user.email)
    );
    sessionStorage.setItem(
      'user.avatar',
      LoginService.convert2String(user.avatar)
    );
    sessionStorage.setItem(
      'user.firstname',
      LoginService.convert2String(user.firstname)
    );
    sessionStorage.setItem(
      'user.lastname',
      LoginService.convert2String(user.lastname)
    );
    sessionStorage.setItem(
      'user.phone',
      LoginService.convert2String(user.phone)
    );
    sessionStorage.setItem(
      'user.contentFilterLang',
      LoginService.convert2String(user.contentFilterLang)
    );
    sessionStorage.setItem(
      'user.uiLang',
      LoginService.convert2String(user.uiLang)
    );
    if (user.visibility) {
      sessionStorage.setItem('user.visibility', user.visibility.toString());
    }
    sessionStorage.setItem('user.properties', JSON.stringify(user.properties));
  }

  /**
   * Lazily-resolved {@link HttpClient} instance.
   *
   * Resolved through the {@link Injector} rather than injected directly to
   * break potential circular dependencies with HTTP interceptors that
   * themselves depend on this service.
   *
   * @returns The application {@link HttpClient}.
   */
  public get httpClient(): HttpClient {
    return this.injector.get<HttpClient>(HttpClient);
  }

  /**
   * Lazily-resolved {@link TranslocoService} instance.
   *
   * @returns The application {@link TranslocoService}.
   */
  public get translateService(): TranslocoService {
    return this.injector.get<TranslocoService>(TranslocoService);
  }

  /**
   * Stores the given ticket, loads the user's profile, persists it and applies
   * the user's preferred UI language.
   *
   * @param username - The user identifier used to fetch the profile.
   * @param ticket - The authentication ticket to associate with the session.
   * @returns A promise resolving to `true` when the profile was loaded and
   * stored successfully, or `false` if loading failed.
   */
  public async loadUser(username: string, ticket: string): Promise<boolean> {
    try {
      sessionStorage.setItem('ticket', ticket);
      const user: User = await this.userService.getUserAsync({
        userId: username,
      });
      LoginService.setlocalStorage(ticket, user);
      if (user.uiLang) {
        this.translateService.setActiveLang(user.uiLang);
      }
      return true;
    } catch (error) {
      console.error(error);
      return false;
    }
  }

  /**
   * Checks whether the ticket currently stored in `sessionStorage` is still
   * valid by querying the Alfresco ticket endpoint.
   *
   * @returns A promise resolving to `true` if a ticket exists and is accepted
   * by the backend, otherwise `false`.
   */
  public async validateTicket(): Promise<boolean> {
    const ticket = sessionStorage.getItem('ticket');
    let result = false;
    if (ticket) {
      try {
        await firstValueFrom(
          this.httpClient.get(`${this.alfrescoURL}/login/ticket/${ticket}`, {
            responseType: 'text',
          })
        );
        result = true;
      } catch (err) {
        result = false;
        console.error(err);
      }
    }

    return result;
  }

  /**
   * Authenticates a user with a username/password pair.
   *
   * Delegates to {@link tryLogin}; if the first attempt fails with an HTTP 403,
   * a single retry is performed to accommodate transient authentication states.
   *
   * @param userPassword - The credentials to authenticate with.
   * @param userPassword.username - The user's login name.
   * @param userPassword.password - The user's password.
   * @returns A promise resolving to `true` on successful login, otherwise `false`.
   */
  public async login(userPassword: {
    username: string;
    password: string;
  }): Promise<boolean> {
    const body = JSON.stringify(userPassword);
    try {
      await this.tryLogin(body, userPassword);
      return true;
    } catch (error) {
      if (error instanceof HttpErrorResponse) {
        if (error.status === 403) {
          try {
            await this.tryLogin(body, userPassword);
            return true;
          } catch (innerError) {
            console.error(innerError);
            return false;
          }
        }
      }
      return false;
    }
  }

  /**
   * Performs a single login request against the CIRCABC `/login` endpoint,
   * persists the returned ticket and the loaded user profile, and applies the
   * user's preferred UI language.
   *
   * @param body - The serialized JSON credentials sent as the request body.
   * @param userPassword - The credentials, used to fetch the user profile after login.
   * @param userPassword.username - The user's login name.
   * @param userPassword.password - The user's password.
   * @returns A promise that resolves once the ticket and profile are stored.
   * @throws HttpErrorResponse If the login request or profile fetch fails.
   */
  private async tryLogin(
    body: string,
    userPassword: { username: string; password: string }
  ) {
    const response = await firstValueFrom(
      this.httpClient.post<TicketResponse>(`${this.circabcURL}/login`, body)
    );
    const ticket: string = response.data.ticket;
    sessionStorage.setItem('ticket', ticket);
    const user: User = await this.userService.getUserAsync({
      userId: userPassword.username,
    });
    LoginService.setlocalStorage(ticket, user);
    if (user.uiLang) {
      this.translateService.setActiveLang(user.uiLang);
    }
  }

  /**
   * Logs the current user out by deleting the ticket on the Alfresco backend
   * and clearing local authentication state.
   *
   * The stored ticket is validated and sanitized first; if it is invalid the
   * local session is still cleared but the method reports failure.
   *
   * @returns A promise resolving to `true` when the ticket was successfully
   * revoked on the backend, or `false` if the ticket was invalid or the
   * revocation request failed.
   */
  public async logout(): Promise<boolean> {
    const rawTicket = this.getTicket();
    const ticket = this.validateAndSanitizeTicket(rawTicket);

    if (!ticket) {
      // eslint-disable-next-line no-console
      console.warn('Invalid ticket detected during logout');
      this.cleanAuthentication();
      return false;
    }

    const url = `${this.alfrescoURL}/login/ticket/${ticket}?format=json`;
    try {
      const httpHeaders = new HttpHeaders().set(
        'Authorization',
        `Basic ${btoa(ticket)}`
      );
      await firstValueFrom(
        this.httpClient.delete(url, { headers: httpHeaders })
      );
      this.cleanAuthentication();
      return true;
    } catch (error) {
      console.error(error);
      return false;
    }
  }

  /**
   * Determines whether the current session is anonymous (no ticket stored).
   *
   * @returns `true` if no authentication ticket is present, otherwise `false`.
   */
  public isGuest(): boolean {
    const ticket = LoginService.convert2String(
      sessionStorage.getItem('ticket')
    );
    return ticket === '';
  }

  /**
   * Returns the identifier of the currently authenticated user.
   *
   * @returns The stored user id, or `'guest'` when no user is authenticated.
   */
  public getCurrentUsername(): string {
    const currentUsername: string = LoginService.convert2String(
      sessionStorage.getItem('user.userId')
    );
    return currentUsername === '' ? 'guest' : currentUsername;
  }

  /**
   * Reconstructs the current {@link User} from the values persisted in
   * `sessionStorage`.
   *
   * @returns A {@link User} object populated from session storage. Fields that
   * were never stored default to empty strings.
   */
  public getUser(): User {
    const result = {
      userId: LoginService.convert2String(
        sessionStorage.getItem('user.userId')
      ),
      email: LoginService.convert2String(sessionStorage.getItem('user.email')),
      avatar: LoginService.convert2String(
        sessionStorage.getItem('user.avatar')
      ),
      firstname: LoginService.convert2String(
        sessionStorage.getItem('user.firstname')
      ),
      lastname: LoginService.convert2String(
        sessionStorage.getItem('user.lastname')
      ),
      phone: LoginService.convert2String(sessionStorage.getItem('user.phone')),
      contentFilterLang: LoginService.convert2String(
        sessionStorage.getItem('user.contentFilterLang')
      ),
      uiLang: LoginService.convert2String(
        sessionStorage.getItem('user.uiLang')
      ),
      properties: JSON.parse(
        sessionStorage.getItem('user.properties') as string
      ) as { [key: string]: string } | undefined,
    };

    let visibility = false;
    if (sessionStorage.getItem('user.phone') === 'true') {
      visibility = true;
    }

    return { ...result, visibility };
  }

  /**
   * Removes the ticket and all persisted user profile fields from
   * `sessionStorage`, resetting the session to an anonymous state.
   */
  public cleanAuthentication(): void {
    sessionStorage.removeItem('ticket');
    sessionStorage.removeItem('user.userId');
    sessionStorage.removeItem('user.email');
    sessionStorage.removeItem('user.avatar');
    sessionStorage.removeItem('user.firstname');
    sessionStorage.removeItem('user.lastname');
    sessionStorage.removeItem('user.phone');
    sessionStorage.removeItem('user.contentFilterLang');
    sessionStorage.removeItem('user.uiLang');
    sessionStorage.removeItem('user.visibility');
    sessionStorage.removeItem('user.properties');
  }

  /**
   * Retrieves the current authentication ticket from `sessionStorage`, after
   * validation and sanitization.
   *
   * @returns The sanitized ticket, or an empty string if no valid ticket is stored.
   */
  public getTicket(): string {
    const rawTicket = LoginService.convert2String(
      sessionStorage.getItem('ticket')
    );
    return this.validateAndSanitizeTicket(rawTicket);
  }
}
