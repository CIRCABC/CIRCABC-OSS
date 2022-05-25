import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { LoginService } from 'app/core/login.service';
import { RedirectionService } from 'app/core/redirection.service';

@Component({
  selector: 'cbc-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  preserveWhitespaces: true,
})
export class LoginComponent implements OnInit {
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
