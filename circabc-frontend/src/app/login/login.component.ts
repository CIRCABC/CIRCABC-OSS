import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
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

@Component({
  selector: 'cbc-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  preserveWhitespaces: true,
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
  public circabcRelease = environment.circabcRelease;
  public loginError!: boolean;
  public loginDenied!: boolean;
  public loginForm!: FormGroup;
  public loggingIn = false;
  public submitClicked = false;

  public constructor(
    private loginService: LoginService,
    private router: Router,
    private fb: FormBuilder,
    private redirectionService: RedirectionService
  ) {}

  public ngOnInit(): void {
    if (!this.loginService.isGuest()) {
      // eslint-disable-next-line @typescript-eslint/no-floating-promises
      this.router.navigate(['/me']);
    }

    this.buildForm();
  }

  public async login() {
    this.submitClicked = true;
    if (this.loginForm.valid) {
      this.loggingIn = true;
      this.loginError = false;
      this.loginDenied = false;
      try {
        const response = await this.loginService.login(this.loginForm.value);

        if (response) {
          await this.redirectionService.redirect();
        } else {
          this.loginDenied = true;
        }
      } catch (error) {
        this.loginError = true;
        console.error(error);
        this.loginForm.reset();
      }

      this.loggingIn = false;
    }
  }

  get userNameControl(): AbstractControl {
    return this.loginForm.controls.username;
  }

  get passwordControl(): AbstractControl {
    return this.loginForm.controls.password;
  }

  private buildForm(): void {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]],
    });
  }
}
